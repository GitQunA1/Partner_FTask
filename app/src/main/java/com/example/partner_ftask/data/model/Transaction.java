package com.example.partner_ftask.data.model;

import com.google.gson.annotations.SerializedName;

public class Transaction {
    @SerializedName("id")
    private int id;

    @SerializedName("type")
    private String type; // EARNING, WITHDRAWAL, etc.

    @SerializedName("amount")
    private double amount;

    @SerializedName("balanceBefore")
    private double balanceBefore;

    @SerializedName("balanceAfter")
    private double balanceAfter;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status; // COMPLETED, PENDING, etc.

    @SerializedName("createdAt")
    private String createdAt;

    @SerializedName("user")
    private User user;

    // Getters
    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public double getAmount() {
        return amount;
    }

    public double getBalanceBefore() {
        return balanceBefore;
    }

    public double getBalanceAfter() {
        return balanceAfter;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public User getUser() {
        return user;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setBalanceBefore(double balanceBefore) {
        this.balanceBefore = balanceBefore;
    }

    public void setBalanceAfter(double balanceAfter) {
        this.balanceAfter = balanceAfter;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

