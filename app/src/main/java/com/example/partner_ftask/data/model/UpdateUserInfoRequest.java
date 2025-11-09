package com.example.partner_ftask.data.model;

import com.google.gson.annotations.SerializedName;

public class UpdateUserInfoRequest {
    @SerializedName("fcmToken")
    private String fcmToken;

    @SerializedName("fullName")
    private String fullName;

    @SerializedName("gender")
    private String gender;

    public UpdateUserInfoRequest() {
    }

    public UpdateUserInfoRequest(String fcmToken, String fullName, String gender) {
        this.fcmToken = fcmToken;
        this.fullName = fullName;
        this.gender = gender;
    }

    public UpdateUserInfoRequest(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}

