package com.cavin.salary_slip.scheduler;

import com.cavin.salary_slip.model.Employee;
import com.cavin.salary_slip.service.ExcelReaderService;
import com.cavin.salary_slip.service.PdfService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class SalarySlipScheduler {

    private final ExcelReaderService excelReaderService;
    private final PdfService pdfService;

    public SalarySlipScheduler(ExcelReaderService excelReaderService, PdfService pdfService) {
        this.excelReaderService = excelReaderService;
        this.pdfService = pdfService;
    }

    /**
     * Runs every day at 10 AM
     * Cron format: second minute hour day month weekday
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void generateSalarySlips() {
        try {
            String excelPath = "C:\\workspace\\JulySalaryEPF1.xlsx";
            String outputDir = "C:\\workspace\\slips\\";

            // Make sure output folder exists
            File dir = new File(outputDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Get current month name for sheet selection
            String currentMonthSheet = LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
            //String currentMonthSheet = null;

                    // Try to read from current month's sheet, fall back to first sheet if not found
            List<Employee> employees;
            try {
                employees = excelReaderService.readEmployeesFromExcel(excelPath, currentMonthSheet);
                System.out.println("Reading from sheet: " + currentMonthSheet);
            } catch (IllegalArgumentException e) {
                employees = excelReaderService.readEmployeesFromExcel(excelPath);
                System.out.println("Sheet " + currentMonthSheet + " not found, using default sheet");
            }

            // Generate PDF for each employee
            for (Employee emp : employees) {
                String pdfPath = outputDir + emp.getEmployeeName() + "_SalarySlip.pdf";
                pdfService.generateSalarySlip(emp, pdfPath);
                System.out.println("Generated slip for: " + emp.getEmployeeName());
            }

        } catch (Exception e) {
            System.err.println("Error generating salary slips: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @PostConstruct
    public void onStartup() {
        System.out.println("Running salary slip generation on startup...");
        generateSalarySlips();
    }
}
