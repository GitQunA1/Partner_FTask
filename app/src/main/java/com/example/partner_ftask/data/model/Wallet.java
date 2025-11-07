package com.example.partner_ftask.data.model;

import com.google.gson.annotations.SerializedName;

public class Wallet {
    private int id;
    private double balance;
    private double totalEarned;
    private double totalWithdrawn;
    private User user;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getTotalEarned() {
        return totalEarned;
    }

    public void setTotalEarned(double totalEarned) {
        this.totalEarned = totalEarned;
    }

    public double getTotalWithdrawn() {
        return totalWithdrawn;
    }

    public void setTotalWithdrawn(double totalWithdrawn) {
        this.totalWithdrawn = totalWithdrawn;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}

