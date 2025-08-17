package com.cavin.salary_slip.service;

import com.cavin.salary_slip.model.Employee;
import com.cavin.salary_slip.model.SalaryDetails;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
public class PdfService {
    private static final Logger logger = LoggerFactory.getLogger(PdfService.class);

    @Value("${salary.slip.logo.path:static/img.png}")
    private String logoPath;

    private Image getLogoImage() {
        try {
            // Try to load from classpath resources first
            ClassPathResource resource = new ClassPathResource(logoPath);
            if (resource.exists()) {
                return Image.getInstance(resource.getInputStream().readAllBytes());
            }
            // Fallback to file system if resource doesn't exist
            return Image.getInstance(logoPath);
        } catch (Exception e) {
            logger.warn("Could not load logo image from path: {}. Using text header instead.", logoPath, e);
            return null;
        }
    }

    public void generateSalarySlip(Employee emp, String pdfPath) throws Exception {
        Document document = new Document(PageSize.A4, 20, 20, 20, 20);
        PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
        document.open();

        // Create header table with 2 columns
        PdfPTable headerTable = getHeaderTable();

        // Add header table to document
        document.add(headerTable);

        // Add spacing after header
        document.add(new Paragraph("\n"));

        // CIN + Level
        Paragraph cin = new Paragraph("CIN NO. - U80903BR2022PTC055945",
                new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD));
        document.add(cin);

        Paragraph level = new Paragraph("Level - 1 (5200 - 20200)\n\n",
                new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD));
        document.add(level);

        // Employee Info Table
        PdfPTable empTable = getEmpTable(emp);

        document.add(empTable);

        // Salary Table
        SalaryDetails salaryDetails = emp.getSalaryDetails();
        PdfPTable salaryTable = getSalaryTable(salaryDetails);

        document.add(salaryTable);

        // Add some space before signatures
        document.add(new Paragraph("\n\n"));

        // Create signature table
        PdfPTable signatureTable = getSignatureTable();

        document.add(signatureTable);

        document.close();
    }

    private PdfPTable getSalaryTable(SalaryDetails salaryDetails) {
        PdfPTable salaryTable = new PdfPTable(4);
        salaryTable.setWidthPercentage(100);
        salaryTable.setSpacingBefore(10f);

        salaryTable.addCell(getHeaderCell("Earning (Rs.)"));
        salaryTable.addCell(getHeaderCell(""));
        salaryTable.addCell(getHeaderCell("Deductions (Rs.)"));
        salaryTable.addCell(getHeaderCell(""));

        // Earnings
        salaryTable.addCell(getCell("Basic", PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getBasic()), PdfPCell.ALIGN_RIGHT, false));

        salaryTable.addCell(getCell("Income Tax (TDS)", PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getIncomeTax()), PdfPCell.ALIGN_RIGHT, false));

        salaryTable.addCell(getCell("House Rent Allowance", PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getHra()), PdfPCell.ALIGN_RIGHT, false));

        salaryTable.addCell(getCell("EPF Deduction", PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getEpf()), PdfPCell.ALIGN_RIGHT, false));

        salaryTable.addCell(getCell("Dearness Allowance", PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getDa()), PdfPCell.ALIGN_RIGHT, false));

        salaryTable.addCell(getCell("Leave Deduction", PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getLeaveDeduction()), PdfPCell.ALIGN_RIGHT, false));

        salaryTable.addCell(getCell("Special Allowance", PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getSpecialAllowance()), PdfPCell.ALIGN_RIGHT, false));

        salaryTable.addCell(getCell("", PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell("", PdfPCell.ALIGN_RIGHT, false));

        salaryTable.addCell(getCell("Travelling Allowance", PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getTravellingAllowance()), PdfPCell.ALIGN_RIGHT, false));

        salaryTable.addCell(getCell("", PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell("", PdfPCell.ALIGN_RIGHT, false));

        // Totals
        salaryTable.addCell(getCell("Total Earning", PdfPCell.ALIGN_LEFT, true));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getTotalEarnings()), PdfPCell.ALIGN_RIGHT, true));

        salaryTable.addCell(getCell("Total Deduction", PdfPCell.ALIGN_LEFT, true));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getTotalDeductions()), PdfPCell.ALIGN_RIGHT, true));

        salaryTable.addCell(getCell("Net Salary", PdfPCell.ALIGN_LEFT, true));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getNetSalary()), PdfPCell.ALIGN_RIGHT, true));
        salaryTable.addCell(getCell("", PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell("", PdfPCell.ALIGN_LEFT, false));
        return salaryTable;
    }

    private PdfPTable getEmpTable(Employee emp) {
        PdfPTable empTable = new PdfPTable(4);
        empTable.setWidthPercentage(100);
        empTable.setSpacingBefore(5f);
        empTable.setSpacingAfter(5f);

        empTable.addCell(getCell("Emp. - Id", PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getEmpId(), PdfPCell.ALIGN_LEFT, false));
        empTable.addCell(getCell("Payable Days", PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(String.valueOf(emp.getPayableDays()), PdfPCell.ALIGN_LEFT, false));

        empTable.addCell(getCell("Name", PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getEmployeeName(), PdfPCell.ALIGN_LEFT, false));
        empTable.addCell(getCell("Month/Year", PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(formatSalaryMonth(emp.getSalaryDate()), PdfPCell.ALIGN_LEFT, false));

        empTable.addCell(getCell("Designation", PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getDesignation(), PdfPCell.ALIGN_LEFT, false));
        empTable.addCell(getCell("Pan No.", PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getPanNo(), PdfPCell.ALIGN_LEFT, false));

        empTable.addCell(getCell("Bank Account No", PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getBankAccountNo(), PdfPCell.ALIGN_LEFT, false));
        empTable.addCell(getCell("Aadhar No", PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getAadharNo(), PdfPCell.ALIGN_LEFT, false));

        empTable.addCell(getCell("IFSC Code", PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getIfscCode(), PdfPCell.ALIGN_LEFT, false));
        empTable.addCell(getCell("", PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell("", PdfPCell.ALIGN_LEFT, false));

        empTable.addCell(getCell("UAN No.", PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getUanNo(), PdfPCell.ALIGN_LEFT, false));
        empTable.addCell(getCell("", PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell("", PdfPCell.ALIGN_LEFT, false));
        return empTable;
    }

    private static PdfPTable getSignatureTable() {
        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(100);
        signatureTable.setSpacingBefore(30f); // Space for actual signatures

        // Create signature cells
        PdfPCell leftSignature = new PdfPCell();
        leftSignature.setBorder(Rectangle.TOP);
        leftSignature.setPaddingTop(30f); // Space for manual signature
        Paragraph srManager = new Paragraph("SR. Manager Finance & Accounting",
            new Font(Font.FontFamily.HELVETICA, 10));
        srManager.setAlignment(Element.ALIGN_CENTER);
        leftSignature.addElement(srManager);

        PdfPCell rightSignature = new PdfPCell();
        rightSignature.setBorder(Rectangle.TOP);
        rightSignature.setPaddingTop(30f); // Space for manual signature
        Paragraph director = new Paragraph("Director Finance",
            new Font(Font.FontFamily.HELVETICA, 10));
        director.setAlignment(Element.ALIGN_CENTER);
        rightSignature.addElement(director);

        signatureTable.addCell(leftSignature);
        signatureTable.addCell(rightSignature);
        return signatureTable;
    }

    private PdfPTable getHeaderTable() throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{80, 20}); // 80% for text, 20% for logo

        // Left cell for company details
        PdfPCell leftCell = getParagraphCell();

        // Right cell for logo
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        // Add logo to right cell
        Image logo = getLogoImage();
        if (logo != null) {
            logo.scaleToFit(100, 80); // Adjusted height to match 4 lines
            logo.setAlignment(Element.ALIGN_CENTER);
            rightCell.addElement(logo);
        }

        // Add cells to header table
        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        return headerTable;
    }

    private static PdfPCell getParagraphCell() {
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);

        // Add title and company details to left cell
        Paragraph title = new Paragraph("Pay Slip", new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
        title.setAlignment(Element.ALIGN_CENTER);
        leftCell.addElement(title);

        Paragraph company = new Paragraph("NK STOCK TALK PVT LTD",
                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
        company.setAlignment(Element.ALIGN_CENTER);
        leftCell.addElement(company);

        Paragraph address1 = new Paragraph("Rupaspur Ara Garden, Manglam Vihar Colony",
                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
        address1.setAlignment(Element.ALIGN_CENTER);
        leftCell.addElement(address1);

        Paragraph address2 = new Paragraph("B.V College, Patna - 800014",
                new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD));
        address2.setAlignment(Element.ALIGN_CENTER);
        leftCell.addElement(address2);
        return leftCell;
    }

    private PdfPCell getCell(String text, int alignment, boolean bold) {
        Font font = bold ? new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD) :
                new Font(Font.FontFamily.HELVETICA, 9);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5f);
        return cell;
    }

    private PdfPCell getHeaderCell(String text) {
        Font font = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cell.setPadding(6f);
        return cell;
    }

    private String formatSalaryMonth(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate lastDay = date.withDayOfMonth(date.lengthOfMonth());
        return lastDay.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
