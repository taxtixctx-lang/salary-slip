package com.cavin.salary_slip.service;

import com.cavin.salary_slip.model.Employee;
import com.cavin.salary_slip.model.SalaryDetails;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
public class ExcelReaderService {
    private static final Logger logger = LoggerFactory.getLogger(ExcelReaderService.class);
    private static final int BATCH_SIZE = 1000;
    private static final int MAX_CACHE_SIZE = 10;

    private final Map<String, CachedWorkbook> workbookCache = new ConcurrentHashMap<>();

    @Value("${salary.slip.excel.sheet.name}")
    private String defaultSheetName;

    public void processEmployeesInBatches(String filePath, String sheetName, Consumer<List<Employee>> batchProcessor) {
        validateInputs(filePath, sheetName);

        CachedWorkbook cachedWorkbook = getOrLoadWorkbook(filePath);
        Sheet sheet = getTargetSheet(cachedWorkbook.workbook, sheetName);

        try {
            processBatchesFromSheet(sheet, batchProcessor);
        } catch (Exception e) {
            logger.error("Error processing Excel file: {}", e.getMessage(), e);
            throw new RuntimeException("Error processing Excel file: " + e.getMessage(), e);
        } finally {
            cleanupCache();
        }
    }

    private void processBatchesFromSheet(Sheet sheet, Consumer<List<Employee>> batchProcessor) {
        Iterator<Row> rows = sheet.iterator();
        List<Employee> currentBatch = new ArrayList<>(BATCH_SIZE);

        // Skip header row
        if (rows.hasNext()) {
            validateHeaderRow(rows.next());
        }

        while (rows.hasNext()) {
            Row row = rows.next();
            if (isRowEmpty(row)) continue;

            try {
                Employee employee = convertRowToEmployee(row);
                if (validateEmployee(employee)) {
                    currentBatch.add(employee);
                }

                if (currentBatch.size() >= BATCH_SIZE) {
                    batchProcessor.accept(new ArrayList<>(currentBatch));
                    currentBatch.clear();
                }
            } catch (Exception e) {
                logger.error("Error processing row {}: {}", row.getRowNum(), e.getMessage());
            }
        }

        // Process remaining employees
        if (!currentBatch.isEmpty()) {
            batchProcessor.accept(new ArrayList<>(currentBatch));
        }
    }

    private boolean validateEmployee(Employee employee) {
        if (!StringUtils.hasText(employee.getEmpId())) {
            logger.warn("Invalid employee record: Missing employee ID");
            return false;
        }
        if (!StringUtils.hasText(employee.getEmployeeName())) {
            logger.warn("Invalid employee record for ID {}: Missing name", employee.getEmpId());
            return false;
        }
        if (employee.getSalaryDetails() == null) {
            logger.warn("Invalid employee record for ID {}: Missing salary details", employee.getEmpId());
            return false;
        }
        return true;
    }

    private void validateHeaderRow(Row headerRow) {
        // Add header validation if needed
        String[] expectedHeaders = {
                "Employee ID", "Name", "Designation", "Bank Account", "IFSC", "UAN",
                "Payable Days", "Salary Date", "PAN", "Aadhar",
                "Basic", "HRA", "DA", "Special Allowance", "Travel Allowance",
                "Income Tax", "EPF", "Leave Deduction"
        };

        // Implement header validation logic if needed
    }

    private Employee convertRowToEmployee(Row row) {
        Employee employee = new Employee();
        SalaryDetails salary = new SalaryDetails();

        try {
            // Use array to make the column mapping more maintainable
            String[] stringFields = {
                    getStringValue(row.getCell(0)), // EmpId
                    getStringValue(row.getCell(1)), // Name
                    getStringValue(row.getCell(2)), // Designation
                    getStringValue(row.getCell(3)), // Bank Account
                    getStringValue(row.getCell(4)), // IFSC
                    getStringValue(row.getCell(5)), // UAN
                    getStringValue(row.getCell(8)), // PAN
                    getStringValue(row.getCell(9))  // Aadhar
            };

            double[] numericFields = {
                    getNumericValue(row.getCell(10)), // Basic
                    getNumericValue(row.getCell(11)), // HRA
                    getNumericValue(row.getCell(12)), // DA
                    getNumericValue(row.getCell(13)), // Special Allowance
                    getNumericValue(row.getCell(14)), // Travel Allowance
                    getNumericValue(row.getCell(15)), // Income Tax
                    getNumericValue(row.getCell(16)), // EPF
                    getNumericValue(row.getCell(17))  // Leave Deduction
            };

            // Set string fields
            employee.setEmpId(stringFields[0]);
            employee.setEmployeeName(stringFields[1]);
            employee.setDesignation(stringFields[2]);
            employee.setBankAccountNo(stringFields[3]);
            employee.setIfscCode(stringFields[4]);
            employee.setUanNo(stringFields[5]);
            employee.setPanNo(stringFields[6]);
            employee.setAadharNo(stringFields[7]);

            // Set numeric fields
            employee.setPayableDays((int) getNumericValue(row.getCell(6)));
            employee.setSalaryDate(getDateValue(row.getCell(7)));

            // Set salary details
            salary.setBasic(numericFields[0]);
            salary.setHra(numericFields[1]);
            salary.setDa(numericFields[2]);
            salary.setSpecialAllowance(numericFields[3]);
            salary.setTravellingAllowance(numericFields[4]);
            salary.setIncomeTax(numericFields[5]);
            salary.setEpf(numericFields[6]);
            salary.setLeaveDeduction(numericFields[7]);

            employee.setSalaryDetails(salary);
        } catch (Exception e) {
            logger.error("Error converting row to employee at row {}: {}", row.getRowNum(), e.getMessage());
            throw new RuntimeException("Error converting row to employee", e);
        }

        return employee;
    }

    @Cacheable("sheetNames")
    public List<String> getSheetNames(String filePath) {
        try {
            CachedWorkbook cachedWorkbook = getOrLoadWorkbook(filePath);
            int sheetCount = cachedWorkbook.workbook.getNumberOfSheets();
            List<String> sheetNames = new ArrayList<>(sheetCount);

            for (int i = 0; i < sheetCount; i++) {
                sheetNames.add(cachedWorkbook.workbook.getSheetName(i));
            }

            return sheetNames;
        } catch (Exception e) {
            logger.error("Error reading sheet names from Excel file: {}", e.getMessage(), e);
            throw new RuntimeException("Error reading Excel file: " + e.getMessage(), e);
        }
    }

    private CachedWorkbook getOrLoadWorkbook(String filePath) {
        return workbookCache.computeIfAbsent(filePath, path -> {
            try {
                Path file = Path.of(path);
                if (!Files.exists(file)) {
                    throw new RuntimeException("Excel file not found: " + path);
                }

                byte[] content = Files.readAllBytes(file);
                long lastModified = Files.getLastModifiedTime(file).toMillis();

                try (InputStream is = Files.newInputStream(file)) {
                    return new CachedWorkbook(new XSSFWorkbook(is), lastModified, content);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error loading Excel file: " + e.getMessage(), e);
            }
        });
    }

    private void validateInputs(String filePath, String sheetName) {
        if (!StringUtils.hasText(filePath)) {
            throw new IllegalArgumentException("File path cannot be empty");
        }
        if (!Files.exists(Path.of(filePath))) {
            throw new IllegalArgumentException("Excel file not found: " + filePath);
        }
    }

    private Sheet getTargetSheet(Workbook workbook, String sheetName) {
        if (StringUtils.hasText(sheetName)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet != null) {
                return sheet;
            }
            logger.warn("Sheet {} not found, falling back to first sheet", sheetName);
        }
        return workbook.getSheetAt(0);
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getDateCellValue().toString();
                }
                yield String.valueOf((long) cell.getNumericCellValue()).trim();
            }
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            default -> "";
        };
    }

    private double getNumericValue(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
        return switch (cell.getCellType()) {
            case NUMERIC -> cell.getNumericCellValue();
            case STRING -> {
                try {
                    String value = cell.getStringCellValue().trim().replace(",", "");
                    yield Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    yield 0.0;
                }
            }
            default -> 0.0;
        };
    }

    private LocalDate getDateValue(Cell cell) {
        if (cell == null) {
            return LocalDate.now();
        }
        try {
            switch (cell.getCellType()) {
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                    }
                    break;
                case STRING:
                    String dateStr = cell.getStringCellValue().trim();
                    try {
                        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception e1) {
                        try {
                            return LocalDate.parse(dateStr,
                                    DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH));
                        } catch (Exception e2) {
                            try {
                                return LocalDate.parse("01 " + dateStr,
                                        DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.ENGLISH));
                            } catch (Exception e3) {
                                logger.warn("Could not parse date '{}', using current date", dateStr);
                                return LocalDate.now();
                            }
                        }
                    }
            }
        } catch (Exception e) {
            logger.error("Error parsing date from cell: {}", e.getMessage());
        }
        return LocalDate.now();
    }

    private void cleanupCache() {
        if (workbookCache.size() > MAX_CACHE_SIZE) {
            // Remove oldest entries when cache gets too large
            List<Map.Entry<String, CachedWorkbook>> entries = workbookCache.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, CachedWorkbook>comparingByValue()
                            .reversed())
                    .collect(Collectors.toList());

            entries.subList(MAX_CACHE_SIZE, entries.size())
                    .forEach(entry -> {
                        try {
                            entry.getValue().workbook.close();
                            workbookCache.remove(entry.getKey());
                        } catch (Exception e) {
                            logger.warn("Error closing workbook: {}", e.getMessage());
                        }
                    });
        }
    }

    // Inner class for caching workbooks with their metadata
    private static class CachedWorkbook implements Comparable<CachedWorkbook> {
        private final Workbook workbook;
        private final long lastModified;
        private final byte[] content;

        public CachedWorkbook(Workbook workbook, long lastModified, byte[] content) {
            this.workbook = workbook;
            this.lastModified = lastModified;
            this.content = content;
        }

        @Override
        public int compareTo(CachedWorkbook other) {
            return Long.compare(this.lastModified, other.lastModified);
        }
    }
}
