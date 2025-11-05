package com.example.partner_ftask.utils;

import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

public class JwtUtils {

    private static final String TAG = "JwtUtils";

    /**
     * Decode JWT token và lấy Partner ID từ payload
     * @param token JWT token string (format: Bearer xxxxx hoặc xxxxx)
     * @return partnerId, hoặc -1 nếu không tìm thấy
     */
    public static int getPartnerIdFromToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                Log.w(TAG, "Token is null or empty");
                return -1;
            }

            // Remove "Bearer " prefix if exists
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            // JWT format: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                Log.e(TAG, "Invalid JWT token format. Expected 3 parts, got: " + parts.length);
                return -1;
            }

            // Decode payload (second part)
            String payload = parts[1];

            // JWT uses Base64 URL encoding without padding
            // Android's Base64 needs padding, so we add it
            int padding = (4 - (payload.length() % 4)) % 4;
            for (int i = 0; i < padding; i++) {
                payload += "=";
            }

            // Decode Base64
            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE | Base64.NO_WRAP);
            String decodedPayload = new String(decodedBytes);

            Log.d(TAG, "Decoded JWT Payload: " + decodedPayload);

            // Parse JSON
            JSONObject json = new JSONObject(decodedPayload);

            // Try different possible field names for partner ID
            int partnerId = -1;

            // Try "partnerId" field
            if (json.has("partnerId")) {
                partnerId = json.getInt("partnerId");
                Log.d(TAG, "Found partnerId: " + partnerId);
                return partnerId;
            }

            // Try "partner_id" field
            if (json.has("partner_id")) {
                partnerId = json.getInt("partner_id");
                Log.d(TAG, "Found partner_id: " + partnerId);
                return partnerId;
            }

            // Try "sub" (subject) field - might be partner ID
            if (json.has("sub")) {
                String sub = json.getString("sub");
                try {
                    partnerId = Integer.parseInt(sub);
                    Log.d(TAG, "Found partnerId in 'sub': " + partnerId);
                    return partnerId;
                } catch (NumberFormatException e) {
                    Log.w(TAG, "'sub' field is not a number: " + sub);
                }
            }

            // Try "id" field
            if (json.has("id")) {
                partnerId = json.getInt("id");
                Log.d(TAG, "Found id: " + partnerId);
                return partnerId;
            }

            // Try "userId" and check if there's a nested partner object
            if (json.has("userId")) {
                int userId = json.getInt("userId");
                Log.d(TAG, "Found userId: " + userId + " (might be partnerId)");
                // In some systems, userId = partnerId for partner role
                return userId;
            }

            Log.w(TAG, "No partnerId found in token payload");
            Log.w(TAG, "Available fields: " + json.keys().toString());

        } catch (Exception e) {
            Log.e(TAG, "Error decoding JWT token", e);
        }

        return -1;
    }

    /**
     * Get user role from token
     */
    public static String getRoleFromToken(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return null;
            }

            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return null;
            }

            String payload = parts[1];
            int padding = (4 - (payload.length() % 4)) % 4;
            for (int i = 0; i < padding; i++) {
                payload += "=";
            }

            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE | Base64.NO_WRAP);
            String decodedPayload = new String(decodedBytes);

            JSONObject json = new JSONObject(decodedPayload);

            if (json.has("role")) {
                return json.getString("role");
            }

            if (json.has("roles")) {
                return json.getString("roles");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error getting role from token", e);
        }

        return null;
    }

    /**
     * Check if token is expired
     */
    public static boolean isTokenExpired(String token) {
        try {
            if (token == null || token.isEmpty()) {
                return true;
            }

            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return true;
            }

            String payload = parts[1];
            int padding = (4 - (payload.length() % 4)) % 4;
            for (int i = 0; i < padding; i++) {
                payload += "=";
            }

            byte[] decodedBytes = Base64.decode(payload, Base64.URL_SAFE | Base64.NO_WRAP);
            String decodedPayload = new String(decodedBytes);

            JSONObject json = new JSONObject(decodedPayload);

            if (json.has("exp")) {
                long exp = json.getLong("exp");
                long currentTime = System.currentTimeMillis() / 1000; // Convert to seconds

                boolean expired = currentTime > exp;
                Log.d(TAG, "Token expired: " + expired + " (exp: " + exp + ", current: " + currentTime + ")");

                return expired;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error checking token expiration", e);
        }

        return true; // If can't verify, assume expired
    }
}

