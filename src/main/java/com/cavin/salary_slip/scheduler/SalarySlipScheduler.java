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

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SalarySlipScheduler {

    private static final Logger logger = LoggerFactory.getLogger(SalarySlipScheduler.class);

    private final ExcelReaderService excelReaderService;
    private final PdfService pdfService;

    @Value("${salary.slip.excel.path}")
    private String excelPath;

    @Value("${salary.slip.output.dir}")
    private String baseOutputDir;

    @Value("${salary.slip.scheduler.cron}")
    private String schedulerCron;

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
        try {
            // Create unique output directory with timestamp
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.format(AppConstants.TIMESTAMP_FORMATTER);
            String uniqueOutputDir = baseOutputDir + AppConstants.BATCH_PREFIX + timestamp + "/";

            File dir = new File(uniqueOutputDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                if (!created) {
                    logger.error(AppConstants.DIR_CREATE_ERROR + ": {}", uniqueOutputDir);
                    return;
                }
            }

            // Try to read from current month's sheet, fall back to first sheet if not found
            String currentMonthSheet = now.format(AppConstants.MONTH_YEAR_FORMATTER);
            List<Employee> employees;
            try {
                employees = excelReaderService.readEmployeesFromExcel(excelPath, currentMonthSheet);
                logger.info("Reading from sheet: {}", currentMonthSheet);
            } catch (IllegalArgumentException e) {
                employees = excelReaderService.readEmployeesFromExcel(excelPath);
                logger.warn("Sheet {} not found, using default sheet", currentMonthSheet);
            }

            // Generate PDF for each employee
            for (Employee emp : employees) {
                String pdfPath = uniqueOutputDir + emp.getEmployeeName() + AppConstants.PDF_FILE_SUFFIX;
                pdfService.generateSalarySlip(emp, pdfPath);
                logger.info("Generated slip for: {} in directory: {}", emp.getEmployeeName(), uniqueOutputDir);
            }

            String successMessage = String.format(AppConstants.SUCCESS_MESSAGE_FORMAT, employees.size(), uniqueOutputDir);
            logger.info(successMessage);

        } catch (Exception e) {
            String errorMessage = String.format(AppConstants.GENERATE_ERROR_FORMAT, e.getMessage());
            logger.error(errorMessage, e);
        }
    }

    @PostConstruct
    public void onStartup() {
        logger.info("Running salary slip generation on startup...");
        generateSalarySlips();
    }
}
