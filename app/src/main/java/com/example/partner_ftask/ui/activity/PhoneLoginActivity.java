package com.example.partner_ftask.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.partner_ftask.MainActivity;
import com.example.partner_ftask.R;
import com.example.partner_ftask.utils.PreferenceManager;

public class PhoneLoginActivity extends AppCompatActivity {

    private EditText etPhoneNumber;
    private Button btnSendOtp;
    private ProgressBar progressBar;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        preferenceManager = new PreferenceManager(this);

        if (preferenceManager.isLoggedIn()) {
            goToMainActivity();
            return;
        }

        etPhoneNumber = findViewById(R.id.et_phone_number);
        btnSendOtp = findViewById(R.id.btn_send_otp);
        progressBar = findViewById(R.id.progress_bar);

        btnSendOtp.setOnClickListener(v -> sendOtp());
    }

    private void sendOtp() {
        String phone = etPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(phone)) {
            etPhoneNumber.setError("Vui lòng nhập số điện thoại");
            etPhoneNumber.requestFocus();
            return;
        }

        if (phone.length() < 10) {
            etPhoneNumber.setError("Số điện thoại không hợp lệ");
            etPhoneNumber.requestFocus();
            return;
        }

        setLoading(true);

        Toast.makeText(this, "Đang gửi mã OTP...", Toast.LENGTH_SHORT).show();

        new android.os.Handler().postDelayed(() -> {
            setLoading(false);
            Intent intent = new Intent(PhoneLoginActivity.this, OtpVerificationActivity.class);
            intent.putExtra("PHONE_NUMBER", phone);
            startActivity(intent);
        }, 1000);
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSendOtp.setEnabled(!isLoading);
        etPhoneNumber.setEnabled(!isLoading);
    }

    private void goToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}

