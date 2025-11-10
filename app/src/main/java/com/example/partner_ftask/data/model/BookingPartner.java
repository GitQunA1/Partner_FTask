package com.example.partner_ftask.data.model;

public class BookingPartner {
    private int id;
    private String status;
    private double partnerEarnings;
    private String cancelReason;
    private Partner partner;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public double getPartnerEarnings() {
        return partnerEarnings;
    }

    public void setPartnerEarnings(double partnerEarnings) {
        this.partnerEarnings = partnerEarnings;
    }


    public String getCancelReason() {
        return cancelReason;
    }

    public void setCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    public Partner getPartner() {
        return partner;
    }

    public void setPartner(Partner partner) {
        this.partner = partner;
    }
}

