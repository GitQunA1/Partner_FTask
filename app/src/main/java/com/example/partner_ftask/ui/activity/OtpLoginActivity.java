package com.example.partner_ftask.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.partner_ftask.MainActivity;
import com.example.partner_ftask.R;
import com.example.partner_ftask.data.api.ApiClient;
import com.example.partner_ftask.data.api.ApiService;
import com.example.partner_ftask.data.model.ApiResponse;
import com.example.partner_ftask.data.model.AuthResponse;
import com.example.partner_ftask.data.repository.AuthRepository;
import com.example.partner_ftask.utils.PreferenceManager;
import com.google.firebase.messaging.FirebaseMessaging;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OtpLoginActivity extends AppCompatActivity {

    private static final String TAG = "OtpLoginActivity";

    private EditText etPhoneNumber;
    private EditText etOtp;
    private Button btnLogin;
    private ProgressBar progressBar;
    private PreferenceManager preferenceManager;
    private AuthRepository authRepository;
    private ApiService apiService;
    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_login);

        preferenceManager = new PreferenceManager(this);
        authRepository = new AuthRepository(this);
        apiService = ApiClient.getApiService();

        // Setup notification permission launcher
        setupPermissionLauncher();

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
        // Pre-fill OTP with default value for testing
        etOtp.setText("123456");

        // Setup login button
        btnLogin.setOnClickListener(v -> login());

        // Request notification permission
        requestNotificationPermission();

        // Get FCM token
        getFCMToken();
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "✅ Notification permission granted");
                    } else {
                        Log.w(TAG, "⚠️ Notification permission denied");
                    }
                });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private void getFCMToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "❌ Get FCM token failed", task.getException());
                        return;
                    }
                    String token = task.getResult();
                    Log.i(TAG, "🔑 FCM Token: " + token);

                    // Save token locally
                    preferenceManager.saveFcmToken(token);

                    // Send to server if logged in
                    if (preferenceManager.isLoggedIn()) {
                        sendTokenToServer(token);
                    }
                });
    }

    private void sendTokenToServer(String token) {
        if (token == null || token.isEmpty()) {
            return;
        }

        // Create request with FCM token and keep existing user data
        String fullName = preferenceManager.getFullName();
        String phoneNumber = preferenceManager.getPhoneNumber();

        // Set gender to null to not update it (or get from somewhere if available)
        com.example.partner_ftask.data.model.UpdateUserInfoRequest request =
            new com.example.partner_ftask.data.model.UpdateUserInfoRequest(
                token,
                fullName != null && !fullName.isEmpty() ? fullName : null,
                null // Don't update gender, keep existing value in backend
            );

        apiService.updateUserInfo(request).enqueue(new Callback<ApiResponse<com.example.partner_ftask.data.model.User>>() {
            @Override
            public void onResponse(Call<ApiResponse<com.example.partner_ftask.data.model.User>> call,
                                 Response<ApiResponse<com.example.partner_ftask.data.model.User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ FCM Token sent to server");
                } else {
                    Log.e(TAG, "❌ Failed to send FCM token: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<com.example.partner_ftask.data.model.User>> call, Throwable t) {
                Log.e(TAG, "❌ Error sending FCM token: " + t.getMessage());
            }
        });
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
        // Save authentication data
        String accessToken = authResponse.getAccessToken();
        if (accessToken != null) {
            preferenceManager.saveAccessToken(accessToken);

            // Extract Partner ID from JWT token
            int partnerIdFromToken = com.example.partner_ftask.utils.JwtUtils.getPartnerIdFromToken(accessToken);
            if (partnerIdFromToken > 0) {
                preferenceManager.savePartnerId(partnerIdFromToken);
                android.util.Log.d(TAG, "Login OK - Partner ID: " + partnerIdFromToken);
            } else {
                android.util.Log.w(TAG, "Login OK - No Partner ID in token");
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
                android.util.Log.d(TAG, "Login OK - Partner ID from API: " + partnerIdFromApi);
            }
        }

        preferenceManager.setLoggedIn(true);

        // Send FCM token to server after login
        String fcmToken = preferenceManager.getFcmToken();
        if (fcmToken != null && !fcmToken.isEmpty()) {
            sendTokenToServer(fcmToken);
        }

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

