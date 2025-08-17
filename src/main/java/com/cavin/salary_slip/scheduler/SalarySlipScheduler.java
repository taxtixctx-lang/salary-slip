package com.cavin.salary_slip.scheduler;

import com.cavin.salary_slip.constants.AppConstants;
import com.cavin.salary_slip.model.Employee;
import com.cavin.salary_slip.service.ExcelReaderService;
import com.cavin.salary_slip.service.PdfService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SalarySlipScheduler {
    private static final Logger logger = LoggerFactory.getLogger(SalarySlipScheduler.class);
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 5000; // 5 seconds

    private final ExcelReaderService excelReaderService;
    private final PdfService pdfService;
    private final Map<String, BatchStatus> batchStatusMap = new ConcurrentHashMap<>();

    @Value("${salary.slip.excel.path}")
    private String excelPath;

    @Value("${salary.slip.output.dir}")
    private String baseOutputDir;

    @Value("${salary.slip.scheduler.cron}")
    private String schedulerCron;

    @Value("${salary.slip.generate.on.startup:false}")
    private boolean generateOnStartup;

    @Value("${salary.slip.notification.email:}")
    private String notificationEmail;

    public SalarySlipScheduler(ExcelReaderService excelReaderService, PdfService pdfService) {
        this.excelReaderService = excelReaderService;
        this.pdfService = pdfService;
    }

    /**
     * Runs every day at 10 AM
     * Cron format: second minute hour day month weekday
     */
    @Scheduled(cron = "${salary.slip.scheduler.cron}")
    public void generateSalarySlips() {
        String batchId = UUID.randomUUID().toString();
        BatchStatus status = new BatchStatus(batchId);
        batchStatusMap.put(batchId, status);

        try {
            // Validate input file exists
            if (!validateInputFile()) {
                status.setError("Input Excel file not found: " + excelPath);
                sendNotification("Salary Slip Generation Failed", status.getError());
                return;
            }

            // Create unique output directory with timestamp
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.format(AppConstants.TIMESTAMP_FORMATTER);
            String uniqueOutputDir = baseOutputDir + AppConstants.BATCH_PREFIX + timestamp + "/";
            status.setOutputDirectory(uniqueOutputDir);

            createOutputDirectory(uniqueOutputDir, status);
            if (status.hasError()) {
                return;
            }

            // Try to read from current month's sheet with retry logic
            String currentMonthSheet = now.format(AppConstants.MONTH_YEAR_FORMATTER);
            status.setCurrentSheet(currentMonthSheet);

            processWithRetry(status, uniqueOutputDir, currentMonthSheet);

            if (!status.hasError()) {
                String successMessage = String.format(AppConstants.SUCCESS_MESSAGE_FORMAT,
                        status.getProcessedCount(), uniqueOutputDir);
                logger.info(successMessage);
                status.setComplete(true);

                // Cleanup old batches if needed
                cleanupOldBatches();
            }

        } catch (Exception e) {
            String errorMessage = String.format(AppConstants.GENERATE_ERROR_FORMAT, e.getMessage());
            logger.error(errorMessage, e);
            status.setError(errorMessage);
            sendNotification("Salary Slip Generation Failed", errorMessage);
        }
    }

    private void processWithRetry(BatchStatus status, String outputDir, String sheetName) {
        int retryCount = 0;
        Exception lastException = null;

        while (retryCount < MAX_RETRIES) {
            try {
                processBatch(status, outputDir, sheetName);
                return; // Success, exit retry loop
            } catch (Exception e) {
                lastException = e;
                retryCount++;
                logger.warn("Attempt {} failed. Retrying in {} ms...", retryCount, RETRY_DELAY_MS);
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // All retries failed
        if (lastException != null) {
            String errorMessage = String.format("Failed after %d attempts: %s", MAX_RETRIES, lastException.getMessage());
            status.setError(errorMessage);
            sendNotification("Salary Slip Generation Failed", errorMessage);
        }
    }

    private void processBatch(BatchStatus status, String outputDir, String sheetName) {
        final AtomicInteger processedCount = new AtomicInteger(0);
        final List<String> failedEmployees = new ArrayList<>();

        excelReaderService.processEmployeesInBatches(
            excelPath,
            sheetName,
            batch -> {
                try {
                    Future<List<String>> batchResult = pdfService.generateSalarySlipsInBatch(batch, outputDir);
                    int batchSize = batch.size();
                    processedCount.addAndGet(batchSize);
                    status.incrementProcessedCount(batchSize);
                    logger.info("Processed batch of {} employees. Total processed: {}",
                        batchSize, processedCount.get());
                } catch (Exception e) {
                    batch.forEach(emp -> failedEmployees.add(emp.getEmpId()));
                    logger.error("Error processing batch: {}", e.getMessage(), e);
                }
            }
        );

        if (!failedEmployees.isEmpty()) {
            String errorMessage = String.format("Failed to process employees: %s", String.join(", ", failedEmployees));
            status.setError(errorMessage);
            sendNotification("Partial Batch Processing Failed", errorMessage);
        }
    }

    private boolean validateInputFile() {
        return Files.exists(Paths.get(excelPath));
    }

    private void createOutputDirectory(String outputDir, BatchStatus status) {
        try {
            Files.createDirectories(Paths.get(outputDir));
        } catch (Exception e) {
            String error = AppConstants.DIR_CREATE_ERROR + ": " + outputDir;
            logger.error(error, e);
            status.setError(error);
            sendNotification("Directory Creation Failed", error);
        }
    }

    private void cleanupOldBatches() {
        try {
            Path outputPath = Paths.get(baseOutputDir);
            if (!Files.exists(outputPath)) {
                return;
            }

            // Keep only last 5 batch directories
            Files.list(outputPath)
                .filter(path -> path.getFileName().toString().startsWith(AppConstants.BATCH_PREFIX))
                .sorted((p1, p2) -> -p1.getFileName().compareTo(p2.getFileName()))
                .skip(5)
                .forEach(path -> {
                    try {
                        Files.walk(path)
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete);
                    } catch (Exception e) {
                        logger.warn("Failed to cleanup old batch: {}", path, e);
                    }
                });
        } catch (Exception e) {
            logger.warn("Failed to perform cleanup of old batches", e);
        }
    }

    private void sendNotification(String subject, String message) {
        if (StringUtils.hasText(notificationEmail)) {
            // TODO: Implement email notification
            logger.info("Would send notification to {}: {} - {}", notificationEmail, subject, message);
        }
    }

    @PostConstruct
    public void onStartup() {
        if (generateOnStartup) {
            logger.info("Running salary slip generation on startup...");
            generateSalarySlips();
        } else {
            logger.info("Startup generation disabled. Will run according to schedule: {}", schedulerCron);
        }
    }

    public BatchStatus getBatchStatus(String batchId) {
        return batchStatusMap.get(batchId);
    }

    public List<BatchStatus> getRecentBatches() {
        return batchStatusMap.values().stream()
            .sorted(Comparator.comparing(BatchStatus::getStartTime).reversed())
            .limit(10)
            .toList();
    }

    // Inner class to track batch processing status
    public static class BatchStatus {
        private final String batchId;
        private final LocalDateTime startTime;
        private String outputDirectory;
        private String currentSheet;
        private final AtomicInteger processedCount = new AtomicInteger(0);
        private String error;
        private boolean complete;

        public BatchStatus(String batchId) {
            this.batchId = batchId;
            this.startTime = LocalDateTime.now();
        }

        // Getters
        public String getBatchId() { return batchId; }
        public LocalDateTime getStartTime() { return startTime; }
        public String getOutputDirectory() { return outputDirectory; }
        public String getCurrentSheet() { return currentSheet; }
        public int getProcessedCount() { return processedCount.get(); }
        public String getError() { return error; }
        public boolean isComplete() { return complete; }
        public boolean hasError() { return error != null; }

        // Setters
        public void setOutputDirectory(String outputDirectory) { this.outputDirectory = outputDirectory; }
        public void setCurrentSheet(String currentSheet) { this.currentSheet = currentSheet; }
        public void setError(String error) { this.error = error; }
        public void setComplete(boolean complete) { this.complete = complete; }
        public void incrementProcessedCount(int delta) { processedCount.addAndGet(delta); }
    }
}
