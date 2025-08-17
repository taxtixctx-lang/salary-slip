package com.cavin.salary_slip.service;

import com.cavin.salary_slip.model.Employee;
import com.cavin.salary_slip.model.SalaryDetails;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
public class ExcelReaderService {

    @Value("${salary.slip.excel.sheet.name}")
    private String currentMonthSheet;

    // Read from default sheet (using configured sheet name)
    public List<Employee> readEmployeesFromExcel(String filePath) {
        return readEmployeesFromExcel(filePath, currentMonthSheet);
    }

    // Read from specific sheet by index
    public List<Employee> readEmployeesFromExcel(String filePath, int sheetIndex) {
        return readEmployeesFromExcel(filePath, sheetIndex, null);
    }

    // Read from specific sheet by name
    public List<Employee> readEmployeesFromExcel(String filePath, String sheetName) {
        return readEmployeesFromExcel(filePath, -1, sheetName);
    }

    // Main method that handles both sheet index and name
    private List<Employee> readEmployeesFromExcel(String filePath, int sheetIndex, String sheetName) {
        List<Employee> employees = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Get the sheet either by name or index
            Sheet sheet;
            if (sheetName != null && !sheetName.isEmpty()) {
                sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    throw new IllegalArgumentException("Sheet not found: " + sheetName);
                }
            } else {
                if (sheetIndex >= workbook.getNumberOfSheets()) {
                    throw new IllegalArgumentException("Invalid sheet index: " + sheetIndex);
                }
                sheet = workbook.getSheetAt(sheetIndex);
            }

            Iterator<Row> rows = sheet.iterator();

            // skip header row
            if (rows.hasNext()) {
                rows.next();
            }

            while (rows.hasNext()) {
                Row row = rows.next();

                // Skip empty rows
                if (isRowEmpty(row)) {
                    continue;
                }

                Employee employee = new Employee();
                SalaryDetails salary = new SalaryDetails();

                employee.setEmpId(getStringValue(row.getCell(0)));
                employee.setEmployeeName(getStringValue(row.getCell(1)));
                employee.setDesignation(getStringValue(row.getCell(2)));
                employee.setBankAccountNo(getStringValue(row.getCell(3)));
                employee.setIfscCode(getStringValue(row.getCell(4)));
                employee.setUanNo(getStringValue(row.getCell(5)));
                employee.setPayableDays((int) getNumericValue(row.getCell(6)));

                // Month/Year as Date
                Cell dateCell = row.getCell(7);
                employee.setSalaryDate(getDateValue(dateCell));

                employee.setPanNo(getStringValue(row.getCell(8)));
                employee.setAadharNo(getStringValue(row.getCell(9)));

                // Salary details
                salary.setBasic(getNumericValue(row.getCell(10)));
                salary.setHra(getNumericValue(row.getCell(11)));
                salary.setDa(getNumericValue(row.getCell(12)));
                salary.setSpecialAllowance(getNumericValue(row.getCell(13)));
                salary.setTravellingAllowance(getNumericValue(row.getCell(14)));
                salary.setIncomeTax(getNumericValue(row.getCell(15)));
                salary.setEpf(getNumericValue(row.getCell(16)));
                salary.setLeaveDeduction(getNumericValue(row.getCell(17)));

                employee.setSalaryDetails(salary);

                employees.add(employee);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading Excel file: " + e.getMessage(), e);
        }
        return employees;
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
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();
                    }
                    break;
                case STRING:
                    String dateStr = cell.getStringCellValue().trim();
                    // Try different date formats
                    try {
                        // Try dd/MM/yyyy format
                        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                    } catch (Exception e1) {
                        try {
                            // Try full datetime format (e.g., "Mon Jun 30 00:00:00 IST 2025")
                            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", java.util.Locale.ENGLISH));
                        } catch (Exception e2) {
                            try {
                                // Try MMM yyyy format
                                return LocalDate.parse("01 " + dateStr, DateTimeFormatter.ofPattern("dd MMM yyyy", java.util.Locale.ENGLISH));
                            } catch (Exception e3) {
                                // If all parsing attempts fail, return current date
                                return LocalDate.now();
                            }
                        }
                    }
            }
        } catch (Exception e) {
            // If any error occurs, return current date
            System.err.println("Error parsing date from cell: " + e.getMessage());
        }
        return LocalDate.now();
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

    // Helper method to get available sheet names
    public List<String> getSheetNames(String filePath) {
        List<String> sheetNames = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetNames.add(workbook.getSheetName(i));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading Excel file: " + e.getMessage(), e);
        }
        return sheetNames;
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


    private double getNumericValueFromFormattedString(Cell cell) {
        if (cell == null) {
            return 0.0;
        }
        try {
            String value;
            if (cell.getCellType() == CellType.NUMERIC) {
                value = String.valueOf(cell.getNumericCellValue());
            } else {
                value = cell.getStringCellValue();
            }
            // Remove commas and spaces, then parse
            value = value.trim().replace(",", "").replace(" ", "");
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }
}
