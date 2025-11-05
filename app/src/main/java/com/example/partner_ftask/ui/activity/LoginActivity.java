package com.example.partner_ftask.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.partner_ftask.MainActivity;
import com.example.partner_ftask.R;
import com.example.partner_ftask.utils.PreferenceManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etToken;
    private Button btnLogin;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        preferenceManager = new PreferenceManager(this);

        // Check if already logged in
        if (preferenceManager.isLoggedIn()) {
            goToMainActivity();
            return;
        }

        // Initialize views
        etToken = findViewById(R.id.et_token);
        btnLogin = findViewById(R.id.btn_login);

        // Setup login button
        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String token = etToken.getText().toString().trim();

        if (token.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập access token", Toast.LENGTH_SHORT).show();
            return;
        }

        // Decode JWT token to get partner ID
        int partnerId = com.example.partner_ftask.utils.JwtUtils.getPartnerIdFromToken(token);
        String role = com.example.partner_ftask.utils.JwtUtils.getRoleFromToken(token);
        boolean isExpired = com.example.partner_ftask.utils.JwtUtils.isTokenExpired(token);

        android.util.Log.d("LoginActivity", "========== TOKEN INFO ==========");
        android.util.Log.d("LoginActivity", "Token length: " + token.length());
        android.util.Log.d("LoginActivity", "Partner ID: " + partnerId);
        android.util.Log.d("LoginActivity", "Role: " + role);
        android.util.Log.d("LoginActivity", "Is Expired: " + isExpired);
        android.util.Log.d("LoginActivity", "================================");

        // Validate token
        if (isExpired) {
            Toast.makeText(this, "Token đã hết hạn! Vui lòng lấy token mới.", Toast.LENGTH_LONG).show();
            return;
        }

        if (partnerId <= 0) {
            Toast.makeText(this, "⚠️ Không tìm thấy Partner ID trong token!\n" +
                    "Token có thể không hợp lệ hoặc không phải Partner token.",
                    Toast.LENGTH_LONG).show();
            // Continue anyway for debugging, but warn user
        }

        // Save token and partner info
        preferenceManager.saveAccessToken(token);
        preferenceManager.setLoggedIn(true);

        // Save partner ID from token
        if (partnerId > 0) {
            preferenceManager.savePartnerId(partnerId);
            android.util.Log.d("LoginActivity", "✅ Saved Partner ID: " + partnerId);
        } else {
            android.util.Log.w("LoginActivity", "⚠️ No valid Partner ID to save");
        }

        // For demo purposes, save some dummy partner info
        // In production, you should call GET /users/me API to get actual user info
        preferenceManager.saveFullName("Partner #" + (partnerId > 0 ? partnerId : "Unknown"));
        preferenceManager.savePhoneNumber("N/A");

        String message = partnerId > 0
            ? "Đăng nhập thành công!\nPartner ID: " + partnerId
            : "Đăng nhập thành công!\n⚠️ Không tìm thấy Partner ID";

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        goToMainActivity();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

