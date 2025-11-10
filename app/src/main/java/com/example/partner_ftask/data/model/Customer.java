package com.example.partner_ftask.data.model;

import java.util.List;

public class Customer {
    private int id;
    private List<Address> addresses;
    private User user;

    // Legacy fields for backward compatibility
    private String fullName;
    private String phoneNumber;
    private String email;
    private String avatar;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Address> getAddresses() {
        return addresses;
    }

    public void setAddresses(List<Address> addresses) {
        this.addresses = addresses;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    // Legacy getters for backward compatibility
    public String getFullName() {
        if (user != null) {
            return user.getFullName();
        }
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhoneNumber() {
        if (user != null) {
            return user.getPhoneNumber();
        }
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        if (user != null) {
            return user.getEmail();
        }
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatar() {
        if (user != null) {
            return user.getAvatar();
        }
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }
}

