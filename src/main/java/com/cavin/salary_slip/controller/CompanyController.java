package com.cavin.salary_slip.controller;

import com.cavin.salary_slip.model.CompanyDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/company")
@Tag(name = "Company Management", description = "APIs for managing company details")
public class CompanyController {

    private final CompanyDetails companyDetails;

    public CompanyController(CompanyDetails companyDetails) {
        this.companyDetails = companyDetails;
    }

    @Operation(summary = "Get company details",
            description = "Retrieve current company details including name and address")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved company details",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CompanyDetails.class)))
    })
    @GetMapping
    public ResponseEntity<CompanyDetails> getCompanyDetails() {
        return ResponseEntity.ok(companyDetails);
    }

    @Operation(summary = "Update company details",
            description = "Update company name, address and other details")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated company details",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CompanyDetails.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping
    public ResponseEntity<CompanyDetails> updateCompanyDetails(@RequestBody CompanyDetails updatedDetails) {
        companyDetails.setName(updatedDetails.getName());
        companyDetails.setAddressLine1(updatedDetails.getAddressLine1());
        companyDetails.setAddressLine2(updatedDetails.getAddressLine2());
        companyDetails.setCin(updatedDetails.getCin());
        companyDetails.setLevel(updatedDetails.getLevel());
        return ResponseEntity.ok(companyDetails);
    }

    @Operation(summary = "Update company address",
            description = "Update only the company address lines")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated company address",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CompanyDetails.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PatchMapping("/address")
    public ResponseEntity<CompanyDetails> updateAddress(
            @RequestParam(required = false) String addressLine1,
            @RequestParam(required = false) String addressLine2) {

        if (addressLine1 != null) {
            companyDetails.setAddressLine1(addressLine1);
        }
        if (addressLine2 != null) {
            companyDetails.setAddressLine2(addressLine2);
        }
        return ResponseEntity.ok(companyDetails);
    }

    @Operation(summary = "Update company name",
            description = "Update only the company name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully updated company name",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CompanyDetails.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PatchMapping("/name")
    public ResponseEntity<CompanyDetails> updateCompanyName(@RequestParam String name) {
        companyDetails.setName(name);
        return ResponseEntity.ok(companyDetails);
    }
}
