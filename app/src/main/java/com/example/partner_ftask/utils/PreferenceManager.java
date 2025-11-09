package com.example.partner_ftask.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.partner_ftask.data.model.UserInfoResponse;
import com.google.gson.Gson;

public class PreferenceManager {
    private static final String PREF_NAME = "FTaskPartnerPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_PARTNER_ID = "partner_id";
    private static final String KEY_FULL_NAME = "full_name";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_USER_INFO = "user_info";
    private static final String KEY_HAS_COMPLETED_SETUP = "has_completed_setup";

    private Gson gson;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new Gson();
    }

    public void saveAccessToken(String token) {
        editor.putString(KEY_ACCESS_TOKEN, token);
        editor.apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public void saveUserId(int userId) {
        editor.putInt(KEY_USER_ID, userId);
        editor.apply();
    }

    public int getUserId() {
        return sharedPreferences.getInt(KEY_USER_ID, -1);
    }

    public void savePartnerId(int partnerId) {
        editor.putInt(KEY_PARTNER_ID, partnerId);
        editor.apply();
    }

    public int getPartnerId() {
        return sharedPreferences.getInt(KEY_PARTNER_ID, -1);
    }

    public void saveFullName(String fullName) {
        editor.putString(KEY_FULL_NAME, fullName);
        editor.apply();
    }

    public String getFullName() {
        return sharedPreferences.getString(KEY_FULL_NAME, "");
    }

    public void savePhoneNumber(String phoneNumber) {
        editor.putString(KEY_PHONE_NUMBER, phoneNumber);
        editor.apply();
    }

    public String getPhoneNumber() {
        return sharedPreferences.getString(KEY_PHONE_NUMBER, "");
    }

    public void setLoggedIn(boolean isLoggedIn) {
        editor.putBoolean(KEY_IS_LOGGED_IN, isLoggedIn);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void clearAll() {
        editor.clear();
        editor.apply();
    }

    public void saveUserInfo(UserInfoResponse userInfo) {
        if (userInfo == null) {
            editor.remove(KEY_USER_INFO);
        } else {
            String json = gson.toJson(userInfo);
            editor.putString(KEY_USER_INFO, json);
        }
        editor.apply();
    }

    public UserInfoResponse getUserInfo() {
        String json = sharedPreferences.getString(KEY_USER_INFO, null);
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return gson.fromJson(json, UserInfoResponse.class);
        } catch (Exception e) {
            return null;
        }
    }

    public void setHasCompletedSetup(boolean hasCompleted) {
        editor.putBoolean(KEY_HAS_COMPLETED_SETUP, hasCompleted);
        editor.apply();
    }

    public boolean hasCompletedSetup() {
        return sharedPreferences.getBoolean(KEY_HAS_COMPLETED_SETUP, false);
    }
}

