package com.example.partner_ftask.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.partner_ftask.MainActivity;
import com.example.partner_ftask.R;
import com.example.partner_ftask.data.model.AuthResponse;
import com.example.partner_ftask.data.repository.AuthRepository;
import com.example.partner_ftask.utils.PreferenceManager;

public class OtpLoginActivity extends AppCompatActivity {

    private static final String TAG = "OtpLoginActivity";

    private EditText etPhoneNumber;
    private EditText etOtp;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvInfo;

    private PreferenceManager preferenceManager;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_login);

        preferenceManager = new PreferenceManager(this);
        authRepository = new AuthRepository(this);

        // Check if already logged in
        if (preferenceManager.isLoggedIn()) {
            goToMainActivity();
            return;
        }

        // Initialize views
        etPhoneNumber = findViewById(R.id.et_phone_number);
        etOtp = findViewById(R.id.et_otp);
        btnLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        tvInfo = findViewById(R.id.tv_info);

        // Pre-fill OTP with default value for testing
        etOtp.setText("123456");

        // Setup login button
        btnLogin.setOnClickListener(v -> login());
    }

    private void login() {
        String phone = etPhoneNumber.getText().toString().trim();
        String otp = etOtp.getText().toString().trim();

        // Validation
        if (TextUtils.isEmpty(phone)) {
            etPhoneNumber.setError("Vui lòng nhập số điện thoại");
            etPhoneNumber.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(otp)) {
            etOtp.setError("Vui lòng nhập mã OTP");
            etOtp.requestFocus();
            return;
        }

        // Show loading
        setLoading(true);

        // Call API
        authRepository.verifyOtp(phone, otp, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse authResponse) {
                setLoading(false);
                handleLoginSuccess(authResponse);
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                Toast.makeText(OtpLoginActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleLoginSuccess(AuthResponse authResponse) {
        android.util.Log.d(TAG, "========== LOGIN SUCCESS ==========");
        android.util.Log.d(TAG, "Access Token: " + (authResponse.getAccessToken() != null ? "✓" : "✗"));
        android.util.Log.d(TAG, "Is New User: " + authResponse.isNewUser());

        if (authResponse.getUser() != null) {
            android.util.Log.d(TAG, "User ID: " + authResponse.getUser().getId());
            android.util.Log.d(TAG, "Full Name: " + authResponse.getUser().getFullName());
            android.util.Log.d(TAG, "Phone: " + authResponse.getUser().getPhoneNumber());
            android.util.Log.d(TAG, "Role: " + authResponse.getUser().getRole());

            if (authResponse.getUser().getPartner() != null) {
                android.util.Log.d(TAG, "Partner ID from API: " + authResponse.getUser().getPartner().getId());
            } else {
                android.util.Log.w(TAG, "⚠️ No Partner information from API");
            }
        }

        // Save authentication data
        String accessToken = authResponse.getAccessToken();
        if (accessToken != null) {
            preferenceManager.saveAccessToken(accessToken);

            // ✅ CRITICAL: Extract Partner ID from JWT token (like old LoginActivity)
            int partnerIdFromToken = com.example.partner_ftask.utils.JwtUtils.getPartnerIdFromToken(accessToken);
            android.util.Log.d(TAG, "Partner ID from JWT Token: " + partnerIdFromToken);

            if (partnerIdFromToken > 0) {
                preferenceManager.savePartnerId(partnerIdFromToken);
                android.util.Log.d(TAG, "✅ Saved Partner ID from token: " + partnerIdFromToken);
            } else {
                android.util.Log.w(TAG, "⚠️ Could not extract Partner ID from token");
            }
        }

        if (authResponse.getUser() != null) {
            preferenceManager.saveUserId(authResponse.getUser().getId());

            if (authResponse.getUser().getFullName() != null) {
                preferenceManager.saveFullName(authResponse.getUser().getFullName());
            }

            if (authResponse.getUser().getPhoneNumber() != null) {
                preferenceManager.savePhoneNumber(authResponse.getUser().getPhoneNumber());
            }

            // Also try to save partner ID from API response (as backup)
            if (authResponse.getUser().getPartner() != null) {
                int partnerIdFromApi = authResponse.getUser().getPartner().getId();
                preferenceManager.savePartnerId(partnerIdFromApi);
                android.util.Log.d(TAG, "✅ Also saved Partner ID from API: " + partnerIdFromApi);
            }
        }

        preferenceManager.setLoggedIn(true);
        android.util.Log.d(TAG, "===================================");

        // Show success message
        String message = authResponse.isNewUser()
            ? "Đăng ký thành công! Chào mừng bạn đến với Partner FTask!"
            : "Đăng nhập thành công!";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Navigate to main activity
        goToMainActivity();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        etPhoneNumber.setEnabled(!isLoading);
        etOtp.setEnabled(!isLoading);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

