package com.example.partner_ftask.data.model;

public class UpdateUserInfoRequest {
    private String gender;
    private String fcmToken;
    private String fullName;

    public UpdateUserInfoRequest(String fullName) {
        this.fullName = fullName;
    }

    public UpdateUserInfoRequest(String gender, String fcmToken, String fullName) {
        this.gender = gender;
        this.fcmToken = fcmToken;
        this.fullName = fullName;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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
}

