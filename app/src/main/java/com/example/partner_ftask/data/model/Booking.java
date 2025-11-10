package com.example.partner_ftask.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Booking {
    private int id;
    private String startAt;
    private String completedAt;
    private double totalPrice;
    private String customerNote;
    private int requiredPartners;
    private int numberOfJoinedPartner;
    private String status;
    private boolean isCustomerAccepted;
    private String method;
    private Customer customer;
    private Variant variant;
    private Address address;
    private List<BookingPartner> partners;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStartAt() {
        return startAt;
    }

    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getCustomerNote() {
        return customerNote;
    }

    public void setCustomerNote(String customerNote) {
        this.customerNote = customerNote;
    }

    public int getRequiredPartners() {
        return requiredPartners;
    }

    public void setRequiredPartners(int requiredPartners) {
        this.requiredPartners = requiredPartners;
    }

    public int getNumberOfJoinedPartner() {
        return numberOfJoinedPartner;
    }

    public void setNumberOfJoinedPartner(int numberOfJoinedPartner) {
        this.numberOfJoinedPartner = numberOfJoinedPartner;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isCustomerAccepted() {
        return isCustomerAccepted;
    }

    public void setCustomerAccepted(boolean customerAccepted) {
        isCustomerAccepted = customerAccepted;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<BookingPartner> getPartners() {
        return partners;
    }

    public void setPartners(List<BookingPartner> partners) {
        this.partners = partners;
    }
}

