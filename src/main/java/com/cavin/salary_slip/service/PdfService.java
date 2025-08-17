package com.cavin.salary_slip.service;

import com.cavin.salary_slip.constants.AppConstants;
import com.cavin.salary_slip.model.CompanyDetails;
import com.cavin.salary_slip.model.Employee;
import com.cavin.salary_slip.model.SalaryDetails;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.FileOutputStream;
import java.time.LocalDate;

import static com.cavin.salary_slip.constants.AppConstants.LEFT_SIGNATURE;
import static com.cavin.salary_slip.constants.AppConstants.RIGHT_SIGNATURE;

@Service
public class PdfService {
    private static final Logger logger = LoggerFactory.getLogger(PdfService.class);

    @Value("${salary.slip.logo.path:static/img.png}")
    private String logoPath;

    private final CompanyDetails companyDetails;

    public PdfService(CompanyDetails companyDetails) {
        this.companyDetails = companyDetails;
    }

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
        Document document = new Document(PageSize.A4, AppConstants.PAGE_MARGIN, AppConstants.PAGE_MARGIN,
                AppConstants.PAGE_MARGIN, AppConstants.PAGE_MARGIN);
        PdfWriter.getInstance(document, new FileOutputStream(pdfPath));
        document.open();

        // Create header table with 2 columns
        PdfPTable headerTable = getHeaderTable();
        document.add(headerTable);

        // Add spacing after header
        document.add(new Paragraph(AppConstants.NEW_LINE));

        // CIN + Level
        String companyCin = StringUtils.hasText(companyDetails.getCin()) ? companyDetails.getCin() : "";
        if (!companyCin.isEmpty()) {
            Paragraph cin = new Paragraph(companyCin,
                    new Font(AppConstants.DEFAULT_FONT_FAMILY, AppConstants.FONT_SIZE_NORMAL, Font.BOLD));
            document.add(cin);
        }

        // Add company level
        String companyLevel = StringUtils.hasText(companyDetails.getLevel()) ? companyDetails.getLevel() : "";
        if (!companyLevel.isEmpty()) {
            Paragraph level = new Paragraph(companyLevel + AppConstants.DOUBLE_NEW_LINE,
                    new Font(AppConstants.DEFAULT_FONT_FAMILY, AppConstants.FONT_SIZE_NORMAL, Font.BOLD));
            document.add(level);
        }


        // Employee Info Table
        PdfPTable empTable = getEmpTable(emp);
        document.add(empTable);

        // Salary Table
        PdfPTable salaryTable = getSalaryTable(emp.getSalaryDetails());
        document.add(salaryTable);

        // Add some space before signatures
        document.add(new Paragraph(AppConstants.DOUBLE_NEW_LINE));

        // Create signature table
        PdfPTable signatureTable = getSignatureTable();
        document.add(signatureTable);

        document.close();
    }

    private PdfPTable getSalaryTable(SalaryDetails salaryDetails) {
        PdfPTable salaryTable = new PdfPTable(AppConstants.SALARY_TABLE_COLUMNS);
        salaryTable.setWidthPercentage(AppConstants.TABLE_WIDTH_PERCENTAGE);
        salaryTable.setSpacingBefore(AppConstants.SPACING_AFTER_HEADER);

        salaryTable.addCell(getHeaderCell(AppConstants.EARNINGS_HEADER));
        salaryTable.addCell(getHeaderCell(AppConstants.EMPTY_HEADER));
        salaryTable.addCell(getHeaderCell(AppConstants.DEDUCTIONS_HEADER));
        salaryTable.addCell(getHeaderCell(AppConstants.EMPTY_HEADER));

        // Earnings
        salaryTable.addCell(getCell(AppConstants.BASIC_LABEL, PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getBasic()), PdfPCell.ALIGN_RIGHT, false));
        salaryTable.addCell(getCell(AppConstants.INCOME_TAX_LABEL, PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getIncomeTax()), PdfPCell.ALIGN_RIGHT, false));

        salaryTable.addCell(getCell(AppConstants.HRA_LABEL, PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getHra()), PdfPCell.ALIGN_RIGHT, false));
        salaryTable.addCell(getCell(AppConstants.EPF_LABEL, PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getEpf()), PdfPCell.ALIGN_RIGHT, false));

        salaryTable.addCell(getCell(AppConstants.DA_LABEL, PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getDa()), PdfPCell.ALIGN_RIGHT, false));
        salaryTable.addCell(getCell(AppConstants.LEAVE_DEDUCTION_LABEL, PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getLeaveDeduction()), PdfPCell.ALIGN_RIGHT, false));

        salaryTable.addCell(getCell(AppConstants.SPECIAL_ALLOWANCE_LABEL, PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getSpecialAllowance()), PdfPCell.ALIGN_RIGHT, false));
        salaryTable.addCell(getCell(AppConstants.EMPTY_HEADER, PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(AppConstants.EMPTY_HEADER, PdfPCell.ALIGN_RIGHT, false));

        salaryTable.addCell(getCell(AppConstants.TRAVEL_ALLOWANCE_LABEL, PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getTravellingAllowance()), PdfPCell.ALIGN_RIGHT, false));
        salaryTable.addCell(getCell(AppConstants.EMPTY_HEADER, PdfPCell.ALIGN_LEFT, false));
        salaryTable.addCell(getCell(AppConstants.EMPTY_HEADER, PdfPCell.ALIGN_RIGHT, false));

        // Totals
        salaryTable.addCell(getCell(AppConstants.TOTAL_EARNING_LABEL, AppConstants.DEFAULT_CELL_ALIGN_LEFT, true));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getTotalEarnings()), AppConstants.DEFAULT_CELL_ALIGN_RIGHT, true));

        salaryTable.addCell(getCell(AppConstants.TOTAL_DEDUCTION_LABEL, AppConstants.DEFAULT_CELL_ALIGN_LEFT, true));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getTotalDeductions()), AppConstants.DEFAULT_CELL_ALIGN_RIGHT, true));

        salaryTable.addCell(getCell(AppConstants.NET_SALARY_LABEL, AppConstants.DEFAULT_CELL_ALIGN_LEFT, true));
        salaryTable.addCell(getCell(String.valueOf(salaryDetails.getNetSalary()), AppConstants.DEFAULT_CELL_ALIGN_RIGHT, true));
        salaryTable.addCell(getCell(AppConstants.LABEL_EMPTY, AppConstants.DEFAULT_CELL_ALIGN_LEFT, false));
        salaryTable.addCell(getCell(AppConstants.LABEL_EMPTY, AppConstants.DEFAULT_CELL_ALIGN_LEFT, false));

        return salaryTable;
    }

    private PdfPTable getEmpTable(Employee emp) {
        PdfPTable empTable = new PdfPTable(AppConstants.EMP_TABLE_COLUMNS);
        empTable.setWidthPercentage(AppConstants.TABLE_WIDTH_PERCENTAGE);
        empTable.setSpacingBefore(AppConstants.TABLE_SPACING_BEFORE);
        empTable.setSpacingAfter(AppConstants.TABLE_SPACING_AFTER);

        empTable.addCell(getCell(AppConstants.LABEL_EMP_ID, PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getEmpId(), PdfPCell.ALIGN_LEFT, false));
        empTable.addCell(getCell(AppConstants.LABEL_PAYABLE_DAYS, PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(String.valueOf(emp.getPayableDays()), PdfPCell.ALIGN_LEFT, false));

        empTable.addCell(getCell(AppConstants.LABEL_NAME, PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getEmployeeName(), PdfPCell.ALIGN_LEFT, false));
        empTable.addCell(getCell(AppConstants.LABEL_MONTH_YEAR, PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(formatSalaryMonth(emp.getSalaryDate()), PdfPCell.ALIGN_LEFT, false));

        empTable.addCell(getCell(AppConstants.LABEL_DESIGNATION, PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getDesignation(), PdfPCell.ALIGN_LEFT, false));
        empTable.addCell(getCell(AppConstants.LABEL_PAN, PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getPanNo(), PdfPCell.ALIGN_LEFT, false));

        empTable.addCell(getCell(AppConstants.LABEL_BANK_ACCOUNT, PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getBankAccountNo(), PdfPCell.ALIGN_LEFT, false));
        empTable.addCell(getCell(AppConstants.LABEL_AADHAR, PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getAadharNo(), PdfPCell.ALIGN_LEFT, false));

        empTable.addCell(getCell(AppConstants.LABEL_IFSC, PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getIfscCode(), PdfPCell.ALIGN_LEFT, false));
        empTable.addCell(getCell(AppConstants.LABEL_EMPTY, PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(AppConstants.LABEL_EMPTY, PdfPCell.ALIGN_LEFT, false));

        empTable.addCell(getCell(AppConstants.LABEL_UAN, PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(emp.getUanNo(), PdfPCell.ALIGN_LEFT, false));
        empTable.addCell(getCell(AppConstants.LABEL_EMPTY, PdfPCell.ALIGN_LEFT, true));
        empTable.addCell(getCell(AppConstants.LABEL_EMPTY, PdfPCell.ALIGN_LEFT, false));

        return empTable;
    }

    private PdfPTable getSignatureTable() {
        PdfPTable signatureTable = new PdfPTable(2);
        signatureTable.setWidthPercentage(AppConstants.TABLE_WIDTH_PERCENTAGE);
        signatureTable.setSpacingBefore(30f);

        // Create signature cells
        PdfPCell leftSignature = new PdfPCell();
        leftSignature.setBorder(Rectangle.TOP);
        leftSignature.setPaddingTop(30f); // Space for manual signature
        Paragraph srManager = new Paragraph(LEFT_SIGNATURE, new Font(Font.FontFamily.HELVETICA, 10));
        srManager.setAlignment(Element.ALIGN_CENTER);
        leftSignature.addElement(srManager);

        PdfPCell rightSignature = new PdfPCell();
        rightSignature.setBorder(Rectangle.TOP);
        rightSignature.setPaddingTop(30f); // Space for manual signature
        Paragraph director = new Paragraph(RIGHT_SIGNATURE, new Font(Font.FontFamily.HELVETICA, 10));
        director.setAlignment(Element.ALIGN_CENTER);
        rightSignature.addElement(director);

        signatureTable.addCell(leftSignature);
        signatureTable.addCell(rightSignature);
        return signatureTable;
    }

    private PdfPTable getHeaderTable() throws DocumentException {
        PdfPTable headerTable = new PdfPTable(AppConstants.HEADER_TABLE_COLUMNS);
        headerTable.setWidthPercentage(AppConstants.TABLE_WIDTH_PERCENTAGE);
        headerTable.setWidths(AppConstants.HEADER_TABLE_COLUMN_WIDTHS);

        // Left cell for company details
        PdfPCell leftCell = getParagraphCell();

        // Right cell for logo
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(AppConstants.NO_BORDER);
        rightCell.setVerticalAlignment(AppConstants.DEFAULT_CELL_VERTICAL_ALIGN);

        // Add logo to right cell
        Image logo = getLogoImage();
        if (logo != null) {
            logo.scaleToFit(AppConstants.LOGO_MAX_WIDTH, AppConstants.LOGO_MAX_HEIGHT);
            logo.setAlignment(AppConstants.DEFAULT_CELL_ALIGN_CENTER);
            rightCell.addElement(logo);
        }

        // Add cells to header table
        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        return headerTable;
    }

    private PdfPCell getParagraphCell() {
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(AppConstants.NO_BORDER);

        // Add title and company details to left cell
        Paragraph title = new Paragraph(AppConstants.PDF_TITLE,
                new Font(AppConstants.DEFAULT_FONT_FAMILY, AppConstants.FONT_SIZE_HEADER, Font.BOLD));
        title.setAlignment(AppConstants.DEFAULT_CELL_ALIGN_CENTER);
        leftCell.addElement(title);

        // Add company name and address
        String companyName = StringUtils.hasText(companyDetails.getName()) ? companyDetails.getName() : AppConstants.COMPANY_NAME;

        Paragraph company = new Paragraph(companyName,
                new Font(AppConstants.DEFAULT_FONT_FAMILY, AppConstants.FONT_SIZE_HEADER, Font.BOLD));
        company.setAlignment(AppConstants.DEFAULT_CELL_ALIGN_CENTER);
        leftCell.addElement(company);

        String cAddressLine1 = StringUtils.hasText(companyDetails.getAddressLine1()) ? companyDetails.getAddressLine1() : AppConstants.COMPANY_ADDRESS_LINE1;

        Paragraph address1 = new Paragraph(cAddressLine1,
                new Font(AppConstants.DEFAULT_FONT_FAMILY, AppConstants.FONT_SIZE_HEADER, Font.BOLD));
        address1.setAlignment(AppConstants.DEFAULT_CELL_ALIGN_CENTER);
        leftCell.addElement(address1);

        String cAddressLine2 = StringUtils.hasText(companyDetails.getAddressLine2()) ? companyDetails.getAddressLine2() : AppConstants.COMPANY_ADDRESS_LINE2;

        Paragraph address2 = new Paragraph(cAddressLine2,
                new Font(AppConstants.DEFAULT_FONT_FAMILY, AppConstants.FONT_SIZE_HEADER, Font.BOLD));
        address2.setAlignment(AppConstants.DEFAULT_CELL_ALIGN_CENTER);
        leftCell.addElement(address2);

        return leftCell;
    }

    private PdfPCell getCell(String text, int alignment, boolean bold) {
        Font font = bold ? new Font(AppConstants.DEFAULT_FONT_FAMILY, AppConstants.FONT_SIZE_NORMAL, Font.BOLD) :
                new Font(AppConstants.DEFAULT_FONT_FAMILY, AppConstants.FONT_SIZE_NORMAL);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(AppConstants.CELL_PADDING);
        return cell;
    }

    private PdfPCell getHeaderCell(String text) {
        Font font = new Font(AppConstants.DEFAULT_FONT_FAMILY, AppConstants.FONT_SIZE_HEADER, Font.BOLD);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(AppConstants.DEFAULT_CELL_ALIGN_CENTER);
        cell.setBackgroundColor(AppConstants.HEADER_CELL_BG_COLOR);
        cell.setPadding(AppConstants.HEADER_CELL_PADDING);
        return cell;
    }

    private String formatSalaryMonth(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        LocalDate lastDay = date.withDayOfMonth(date.lengthOfMonth());
        return lastDay.format(AppConstants.SALARY_DATE_FORMATTER);
    }
}
