package com.example.partner_ftask;

import android.app.Application;

import com.example.partner_ftask.data.api.ApiClient;

public class PartnerFTaskApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize API client
        ApiClient.init(this);
    }
}

