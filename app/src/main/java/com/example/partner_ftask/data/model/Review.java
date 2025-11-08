package com.example.partner_ftask.data.model;

import com.google.gson.annotations.SerializedName;

public class Review {
    @SerializedName("id")
    private int id;

    @SerializedName("bookingId")
    private int bookingId;

    @SerializedName("customerId")
    private int customerId;

    @SerializedName("customerName")
    private String customerName;

    @SerializedName("partnerId")
    private int partnerId;

    @SerializedName("partnerName")
    private String partnerName;

    @SerializedName("rating")
    private int rating;

    @SerializedName("description")
    private String description;

    @SerializedName("createdAt")
    private String createdAt;

    // Getters
    public int getId() {
        return id;
    }

    public int getBookingId() {
        return bookingId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public int getPartnerId() {
        return partnerId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public int getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public void setPartnerId(int partnerId) {
        this.partnerId = partnerId;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}

