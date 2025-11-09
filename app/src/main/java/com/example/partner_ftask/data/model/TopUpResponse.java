package com.example.partner_ftask.data.model;

import com.google.gson.annotations.SerializedName;

public class TopUpResponse {
    @SerializedName("code")
    private String code;

    @SerializedName("paymentUrl")
    private String paymentUrl;

    @SerializedName("message")
    private String message;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

