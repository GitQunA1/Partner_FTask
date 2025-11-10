package com.example.partner_ftask.data.model;

import com.google.gson.annotations.SerializedName;

public class UnreadCountResponse {
    @SerializedName("unreadCount")
    private int unreadCount;

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
}

