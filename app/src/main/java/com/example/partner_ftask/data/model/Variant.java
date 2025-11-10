package com.example.partner_ftask.data.model;

public class Variant {
    private int id;
    private String name;
    private String description;
    private int duration; // Legacy field
    private int durationHours;
    private double price; // Legacy field
    private double pricePerVariant;
    private boolean isMultiPartner;
    private int numberOfPartners;
    private int serviceCatalogId;
    private String image;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDuration() {
        return durationHours > 0 ? durationHours : duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDurationHours() {
        return durationHours;
    }

    public void setDurationHours(int durationHours) {
        this.durationHours = durationHours;
    }

    public double getPrice() {
        return pricePerVariant > 0 ? pricePerVariant : price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPricePerVariant() {
        return pricePerVariant;
    }

    public void setPricePerVariant(double pricePerVariant) {
        this.pricePerVariant = pricePerVariant;
    }

    public boolean isMultiPartner() {
        return isMultiPartner;
    }

    public void setMultiPartner(boolean multiPartner) {
        isMultiPartner = multiPartner;
    }

    public int getNumberOfPartners() {
        return numberOfPartners;
    }

    public void setNumberOfPartners(int numberOfPartners) {
        this.numberOfPartners = numberOfPartners;
    }

    public int getServiceCatalogId() {
        return serviceCatalogId;
    }

    public void setServiceCatalogId(int serviceCatalogId) {
        this.serviceCatalogId = serviceCatalogId;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}

