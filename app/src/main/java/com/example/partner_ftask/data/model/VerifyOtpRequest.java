package com.example.partner_ftask.data.model;

public class VerifyOtpRequest {
    private String phone;
    private String otp;
    private String role;

    public VerifyOtpRequest(String phone, String otp, String role) {
        this.phone = phone;
        this.otp = otp;
        this.role = role;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

