package com.cavin.salary_slip.model;

public class SalaryDetails {
    // Earnings
    private double basic;
    private double hra;  // House Rent Allowance
    private double da;   // Dearness Allowance
    private double specialAllowance;
    private double travellingAllowance;

    // Deductions
    private double incomeTax;
    private double epf;
    private double leaveDeduction;

    // Getters & Setters
    public double getBasic() {
        return basic;
    }
    public void setBasic(double basic) {
        this.basic = basic;
    }
    public double getHra() {
        return hra;
    }
    public void setHra(double hra) {
        this.hra = hra;
    }
    public double getDa() {
        return da;
    }
    public void setDa(double da) {
        this.da = da;
    }
    public double getSpecialAllowance() {
        return specialAllowance;
    }
    public void setSpecialAllowance(double specialAllowance) {
        this.specialAllowance = specialAllowance;
    }
    public double getTravellingAllowance() {
        return travellingAllowance;
    }
    public void setTravellingAllowance(double travellingAllowance) {
        this.travellingAllowance = travellingAllowance;
    }
    public double getIncomeTax() {
        return incomeTax;
    }
    public void setIncomeTax(double incomeTax) {
        this.incomeTax = incomeTax;
    }
    public double getEpf() {
        return epf;
    }
    public void setEpf(double epf) {
        this.epf = epf;
    }
    public double getLeaveDeduction() {
        return leaveDeduction;
    }
    public void setLeaveDeduction(double leaveDeduction) {
        this.leaveDeduction = leaveDeduction;
    }

    // Computed values
    public double getTotalEarnings() {
        return basic + hra + da + specialAllowance + travellingAllowance;
    }

    public double getTotalDeductions() {
        return incomeTax + epf + leaveDeduction;
    }

    public double getNetSalary() {
        return getTotalEarnings() - getTotalDeductions();
    }
}
