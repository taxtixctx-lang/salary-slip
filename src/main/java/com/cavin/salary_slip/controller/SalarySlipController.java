package com.cavin.salary_slip.controller;

import com.cavin.salary_slip.constants.AppConstants;
import com.cavin.salary_slip.model.Employee;
import com.cavin.salary_slip.service.ExcelReaderService;
import com.cavin.salary_slip.service.PdfService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/salary-slip")
@Tag(name = "Salary Slip Generator", description = "API endpoints for salary slip generation and management")
public class SalarySlipController {

    private static final Logger logger = LoggerFactory.getLogger(SalarySlipController.class);

    private final ExcelReaderService excelReaderService;
    private final PdfService pdfService;

    @Value("${salary.slip.output.dir}")
    private String baseOutputDir;

    public SalarySlipController(ExcelReaderService excelReaderService, PdfService pdfService) {
        this.excelReaderService = excelReaderService;
        this.pdfService = pdfService;
    }

    @Operation(summary = "Generate salary slips from Excel file",
            description = "Upload an Excel file containing employee salary data and generate PDF salary slips")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully generated salary slips",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or processing error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Response.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Response.class)))
    })
    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> generateSalarySlips(
            @Parameter(description = "Excel file containing employee salary data", required = true)
            @RequestParam("file") MultipartFile excelFile,
            @Parameter(description = "Name of the sheet to process (defaults to current month if not specified)")
            @RequestParam(value = "sheetName", required = false) String sheetName) {
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
                    return ResponseEntity.internalServerError()
                            .body(new Response(false, AppConstants.DIR_CREATE_ERROR, 0));
                }
            }

            // Save the uploaded file temporarily
            Path tempPath = Files.createTempFile(AppConstants.TEMP_FILE_PREFIX, AppConstants.TEMP_FILE_SUFFIX);
            excelFile.transferTo(tempPath.toFile());

            // Get current month name if no sheet specified
            if (sheetName == null || sheetName.isEmpty()) {
                sheetName = now.format(AppConstants.MONTH_YEAR_FORMATTER);
                logger.info("No sheet specified, defaulting to current month: {}", sheetName);
            }

            // Try to read from specified sheet, fall back to first sheet if not found
            List<Employee> employees;
            try {
                employees = excelReaderService.readEmployeesFromExcel(tempPath.toString(), sheetName);
                logger.info("Reading from sheet: {}", sheetName);
            } catch (IllegalArgumentException e) {
                employees = excelReaderService.readEmployeesFromExcel(tempPath.toString());
                logger.warn("Sheet {} not found, using default sheet", sheetName);
            }

            // Generate PDF for each employee
            for (Employee emp : employees) {
                String pdfPath = uniqueOutputDir + emp.getEmployeeName() + AppConstants.PDF_FILE_SUFFIX;
                pdfService.generateSalarySlip(emp, pdfPath);
                logger.info("Generated slip for: {} in directory: {}", emp.getEmployeeName(), uniqueOutputDir);
            }

            // Clean up the temporary file
            Files.deleteIfExists(tempPath);

            String successMessage = String.format(AppConstants.SUCCESS_MESSAGE_FORMAT, employees.size(), uniqueOutputDir);
            logger.info(successMessage);

            return ResponseEntity.ok()
                    .body(new Response(true, successMessage, employees.size()));

        } catch (Exception e) {
            String errorMessage = String.format(AppConstants.GENERATE_ERROR_FORMAT, e.getMessage());
            logger.error(errorMessage, e);
            return ResponseEntity.badRequest()
                    .body(new Response(false, errorMessage, 0));
        }
    }

    @Operation(summary = "Get available sheet names from Excel file",
            description = "Upload an Excel file and retrieve the list of available sheet names")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved sheet names",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or processing error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Response.class)))
    })
    @GetMapping(value = "/sheets", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> getSheetNames(
            @Parameter(description = "Excel file to read sheet names from", required = true)
            @RequestParam("file") MultipartFile excelFile) {
        try {
            // Save the uploaded file temporarily
            Path tempPath = Files.createTempFile("upload_", ".xlsx");
            excelFile.transferTo(tempPath.toFile());

            // Get sheet names
            List<String> sheetNames = excelReaderService.getSheetNames(tempPath.toString());
            logger.info("Found {} sheets in uploaded file", sheetNames.size());

            // Clean up the temporary file
            Files.deleteIfExists(tempPath);

            return ResponseEntity.ok(sheetNames);
        } catch (Exception e) {
            String errorMessage = "Error reading sheet names: " + e.getMessage();
            logger.error(errorMessage, e);
            return ResponseEntity.badRequest()
                    .body(new Response(false, errorMessage, 0));
        }
    }

    @Schema(description = "API Response Object")
        private record Response(@Schema(description = "Indicates if the operation was successful") boolean success,
                                @Schema(description = "Response message with details about the operation") String message,
                                @Schema(description = "Number of salary slips processed") int count) {
            private Response(boolean success, String message, int count) {
                this.success = success;
                this.message = message;
                this.count = count;
            }

            @Override
            public boolean success() {
                return success;
            }

            @Override
            public String message() {
                return message;
            }

            @Override
            public int count() {
                return count;
            }
        }
}
