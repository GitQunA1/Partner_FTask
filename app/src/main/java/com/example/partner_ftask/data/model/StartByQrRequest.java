package com.example.partner_ftask.data.model;

import com.google.gson.annotations.SerializedName;

public class StartByQrRequest {
    @SerializedName("qrToken")
    private String qrToken;

    public StartByQrRequest() {
    }

    public StartByQrRequest(String qrToken) {
        this.qrToken = qrToken;
    }

    public String getQrToken() {
        return qrToken;
    }

    public void setQrToken(String qrToken) {
        this.qrToken = qrToken;
    }
}

