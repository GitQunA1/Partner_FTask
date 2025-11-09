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

public class OtpVerificationActivity extends AppCompatActivity {

    private TextView tvPhoneNumber;
    private EditText etOtp;
    private Button btnVerify;
    private Button btnResendOtp;
    private ProgressBar progressBar;
    private PreferenceManager preferenceManager;
    private AuthRepository authRepository;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        phoneNumber = getIntent().getStringExtra("PHONE_NUMBER");
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Toast.makeText(this, "Lỗi: Không có số điện thoại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        preferenceManager = new PreferenceManager(this);
        authRepository = new AuthRepository(this);

        tvPhoneNumber = findViewById(R.id.tv_phone_number);
        etOtp = findViewById(R.id.et_otp);
        btnVerify = findViewById(R.id.btn_verify);
        btnResendOtp = findViewById(R.id.btn_resend_otp);
        progressBar = findViewById(R.id.progress_bar);

        tvPhoneNumber.setText(phoneNumber);
        etOtp.setText("123456");

        btnVerify.setOnClickListener(v -> verifyOtp());
        btnResendOtp.setOnClickListener(v -> resendOtp());
    }

    private void verifyOtp() {
        String otp = etOtp.getText().toString().trim();

        if (TextUtils.isEmpty(otp)) {
            etOtp.setError("Vui lòng nhập mã OTP");
            etOtp.requestFocus();
            return;
        }

        if (otp.length() != 6) {
            etOtp.setError("Mã OTP phải có 6 số");
            etOtp.requestFocus();
            return;
        }

        setLoading(true);

        authRepository.verifyOtp(phoneNumber, otp, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse authResponse) {
                setLoading(false);
                handleLoginSuccess(authResponse);
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                Toast.makeText(OtpVerificationActivity.this, errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void resendOtp() {
        Toast.makeText(this, "Đang gửi lại mã OTP...", Toast.LENGTH_SHORT).show();
        etOtp.setText("123456");
    }

    private void handleLoginSuccess(AuthResponse authResponse) {
        String accessToken = authResponse.getAccessToken();
        if (accessToken != null) {
            preferenceManager.saveAccessToken(accessToken);

            int partnerIdFromToken = com.example.partner_ftask.utils.JwtUtils.getPartnerIdFromToken(accessToken);
            if (partnerIdFromToken > 0) {
                preferenceManager.savePartnerId(partnerIdFromToken);
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

            if (authResponse.getUser().getPartner() != null) {
                int partnerIdFromApi = authResponse.getUser().getPartner().getId();
                preferenceManager.savePartnerId(partnerIdFromApi);
            }
        }

        preferenceManager.setLoggedIn(true);

        String message = authResponse.isNewUser()
            ? "Đăng ký thành công! Chào mừng bạn đến với Partner FTask!"
            : "Đăng nhập thành công!";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        goToMainActivity();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnVerify.setEnabled(!isLoading);
        btnResendOtp.setEnabled(!isLoading);
        etOtp.setEnabled(!isLoading);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

