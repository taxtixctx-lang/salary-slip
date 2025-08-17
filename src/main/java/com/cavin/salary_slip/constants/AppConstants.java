package com.cavin.salary_slip.constants;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Rectangle;

import java.time.format.DateTimeFormatter;

public final class AppConstants {
    private AppConstants() {
        // Private constructor to prevent instantiation
    }

    // File and Directory Constants
    public static final String TEMP_FILE_PREFIX = "upload_";
    public static final String TEMP_FILE_SUFFIX = ".xlsx";
    public static final String PDF_FILE_SUFFIX = "_SalarySlip.pdf";
    public static final String BATCH_PREFIX = "batch_";

    // Date Format Constants
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd_HHmmss";
    public static final String MONTH_YEAR_FORMAT = "MMMM yyyy";
    public static final String SALARY_DATE_FORMAT = "dd/MM/yyyy";
    public static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern(TIMESTAMP_FORMAT);
    public static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern(MONTH_YEAR_FORMAT);
    public static final DateTimeFormatter SALARY_DATE_FORMATTER = DateTimeFormatter.ofPattern(SALARY_DATE_FORMAT);

    // PDF Constants
    public static final float PAGE_MARGIN = 20f;
    public static final float SPACING_AFTER_HEADER = 10f;
    public static final float TABLE_SPACING_BEFORE = 5f;
    public static final float TABLE_SPACING_AFTER = 5f;
    public static final float CELL_PADDING = 5f;
    public static final float HEADER_CELL_PADDING = 6f;
    public static final int FONT_SIZE_NORMAL = 9;
    public static final int FONT_SIZE_HEADER = 12;

    // PDF Table Constants
    public static final float TABLE_WIDTH_PERCENTAGE = 100f;
    public static final float[] HEADER_TABLE_COLUMN_WIDTHS = {80f, 20f}; // 80% for text, 20% for logo
    public static final int HEADER_TABLE_COLUMNS = 2;
    public static final int SALARY_TABLE_COLUMNS = 4;
    public static final int EMP_TABLE_COLUMNS = 4;

    // PDF Logo Constants
    public static final float LOGO_MAX_WIDTH = 100f;
    public static final float LOGO_MAX_HEIGHT = 80f;

    // Company Details Constants
    public static final String COMPANY_NAME = "AVETA IVF";
    public static final String COMPANY_ADDRESS_LINE1 = "Rupaspur Ara Garden, Manglam Vihar Colony";
    public static final String COMPANY_ADDRESS_LINE2 = "B.V College, Patna - 800014";
    public static final String PDF_TITLE = "Pay Slip";

    // Employee Table Labels
    public static final String LABEL_EMP_ID = "Emp. - Id";
    public static final String LABEL_PAYABLE_DAYS = "Payable Days";
    public static final String LABEL_NAME = "Name";
    public static final String LABEL_MONTH_YEAR = "Month/Year";
    public static final String LABEL_DESIGNATION = "Designation";
    public static final String LABEL_PAN = "Pan No.";
    public static final String LABEL_BANK_ACCOUNT = "Bank Account No";
    public static final String LABEL_AADHAR = "Aadhar No";
    public static final String LABEL_IFSC = "IFSC Code";
    public static final String LABEL_UAN = "UAN No.";
    public static final String LABEL_EMPTY = "";

    // API Response Messages
    public static final String SUCCESS_MESSAGE_FORMAT = "Successfully generated %d salary slips in directory: %s";
    public static final String DIR_CREATE_ERROR = "Failed to create output directory";
    public static final String GENERATE_ERROR_FORMAT = "Error generating salary slips: %s";

    // Table Header Labels
    public static final String EARNINGS_HEADER = "Earning (Rs.)";
    public static final String DEDUCTIONS_HEADER = "Deductions (Rs.)";
    public static final String EMPTY_HEADER = "";

    // Salary Component Labels
    public static final String BASIC_LABEL = "Basic";
    public static final String HRA_LABEL = "House Rent Allowance";
    public static final String DA_LABEL = "Dearness Allowance";
    public static final String SPECIAL_ALLOWANCE_LABEL = "Special Allowance";
    public static final String TRAVEL_ALLOWANCE_LABEL = "Travelling Allowance";
    public static final String INCOME_TAX_LABEL = "Income Tax (TDS)";
    public static final String EPF_LABEL = "EPF";
    public static final String LEAVE_DEDUCTION_LABEL = "Leave Deduction";
    public static final String TOTAL_EARNING_LABEL = "Total Earning";
    public static final String TOTAL_DEDUCTION_LABEL = "Total Deduction";
    public static final String NET_SALARY_LABEL = "Net Salary";

    // Signature Section Labels
    public static final String LEFT_SIGNATURE = "SR. Manager Finance & Accounting";
    public static final String RIGHT_SIGNATURE = "Director Finance";

    // PDF Layout Constants
    public static final String NEW_LINE = "\n";
    public static final String DOUBLE_NEW_LINE = "\n\n";

    // PDF Font Constants
    public static final Font.FontFamily DEFAULT_FONT_FAMILY = Font.FontFamily.HELVETICA;

    // PDF Table Alignment Constants
    public static final int DEFAULT_CELL_ALIGN_LEFT = Element.ALIGN_LEFT;
    public static final int DEFAULT_CELL_ALIGN_RIGHT = Element.ALIGN_RIGHT;
    public static final int DEFAULT_CELL_ALIGN_CENTER = Element.ALIGN_CENTER;
    public static final int DEFAULT_CELL_VERTICAL_ALIGN = Element.ALIGN_MIDDLE;

    // PDF Cell Border Constants
    public static final int NO_BORDER = Rectangle.NO_BORDER;

    // PDF Cell Background Colors
    public static final BaseColor HEADER_CELL_BG_COLOR = BaseColor.LIGHT_GRAY;
}
