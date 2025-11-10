package com.example.partner_ftask.data.model;

public class StartBookingQrRequest {
    private String qrToken;

    public StartBookingQrRequest(String qrToken) {
        this.qrToken = qrToken;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }
}

