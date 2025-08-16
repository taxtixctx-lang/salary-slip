package com.cavin.salary_slip.model;

import java.time.LocalDate;

public class Employee {
    private String empId;
    private String employeeName;
    private String designation;
    private String bankAccountNo;
    private String ifscCode;
    private String uanNo;
    private int payableDays;
    private LocalDate salaryDate;  // e.g. 30/06/2025
    private String panNo;
    private String aadharNo;

    private SalaryDetails salaryDetails;

    // Getters & Setters
    public String getEmpId() {
        return empId;
    }
    public void setEmpId(String empId) {
        this.empId = empId;
    }
    public String getEmployeeName() {
        return employeeName;
    }
    public void setEmployeeName(String employeeName) {
        this.employeeName = employeeName;
    }
    public String getDesignation() {
        return designation;
    }
    public void setDesignation(String designation) {
        this.designation = designation;
    }
    public String getBankAccountNo() {
        return bankAccountNo;
    }
    public void setBankAccountNo(String bankAccountNo) {
        this.bankAccountNo = bankAccountNo;
    }
    public String getIfscCode() {
        return ifscCode;
    }
    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }
    public String getUanNo() {
        return uanNo;
    }
    public void setUanNo(String uanNo) {
        this.uanNo = uanNo;
    }
    public int getPayableDays() {
        return payableDays;
    }
    public void setPayableDays(int payableDays) {
        this.payableDays = payableDays;
    }
    public LocalDate getSalaryDate() {
        return salaryDate;
    }
    public void setSalaryDate(LocalDate salaryDate) {
        this.salaryDate = salaryDate;
    }
    public String getPanNo() {
        return panNo;
    }
    public void setPanNo(String panNo) {
        this.panNo = panNo;
    }
    public String getAadharNo() {
        return aadharNo;
    }
    public void setAadharNo(String aadharNo) {
        this.aadharNo = aadharNo;
    }
    public SalaryDetails getSalaryDetails() {
        return salaryDetails;
    }
    public void setSalaryDetails(SalaryDetails salaryDetails) {
        this.salaryDetails = salaryDetails;
    }
}
