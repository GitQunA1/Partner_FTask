package com.example.partner_ftask.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.partner_ftask.R;
import com.example.partner_ftask.data.api.ApiClient;
import com.example.partner_ftask.data.api.ApiService;
import com.example.partner_ftask.data.model.ApiResponse;
import com.example.partner_ftask.data.model.Booking;
import com.example.partner_ftask.data.model.StartByQrRequest;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QrScannerActivity extends AppCompatActivity {
    private static final String TAG = "QrScannerActivity";

    private DecoratedBarcodeView barcodeView;
    private CaptureManager captureManager;
    private ProgressBar progressBar;
    private MaterialButton btnFlashlight;
    private ApiService apiService;
    private boolean isFlashlightOn = false;
    private boolean isProcessing = false;

    private ActivityResultLauncher<String> requestPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "==========================================");
        Log.d(TAG, "🚀 QrScannerActivity onCreate");
        Log.d(TAG, "==========================================");

        setContentView(R.layout.activity_qr_scanner);

        apiService = ApiClient.getApiService();
        Log.d(TAG, "✅ ApiService initialized");

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Quét mã QR");
        }
        toolbar.setNavigationOnClickListener(v -> finish());
        Log.d(TAG, "✅ Toolbar setup complete");

        // Initialize views
        barcodeView = findViewById(R.id.barcode_scanner);
        progressBar = findViewById(R.id.progress_bar);
        btnFlashlight = findViewById(R.id.btn_flashlight);

        Log.d(TAG, "✅ Views initialized:");
        Log.d(TAG, "  - barcodeView: " + (barcodeView != null));
        Log.d(TAG, "  - progressBar: " + (progressBar != null));
        Log.d(TAG, "  - btnFlashlight: " + (btnFlashlight != null));

        // Setup permission launcher
        setupPermissionLauncher();
        Log.d(TAG, "✅ Permission launcher setup");

        // Setup flashlight button
        btnFlashlight.setOnClickListener(v -> toggleFlashlight());
        Log.d(TAG, "✅ Flashlight button listener set");


        // Check camera permission
        boolean hasPermission = hasCameraPermission();
        Log.d(TAG, "📷 Camera permission: " + hasPermission);

        if (hasPermission) {
            Log.d(TAG, "✅ Has camera permission, initializing scanner...");
            initializeScanner(savedInstanceState);
        } else {
            Log.d(TAG, "⚠️ No camera permission, requesting...");
            requestCameraPermission();
        }

        Log.d(TAG, "==========================================");
    }

    private void setupPermissionLauncher() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "✅ Camera permission granted");
                        initializeScanner(null);
                    } else {
                        Log.w(TAG, "⚠️ Camera permission denied");
                        showPermissionDeniedDialog();
                    }
                });
    }

    private boolean hasCameraPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestCameraPermission() {
        requestPermissionLauncher.launch(Manifest.permission.CAMERA);
    }

    private void showPermissionDeniedDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Cần quyền truy cập Camera")
                .setMessage("Ứng dụng cần quyền truy cập camera để quét mã QR. Vui lòng cấp quyền trong cài đặt.")
                .setPositiveButton("Đóng", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void initializeScanner(Bundle savedInstanceState) {
        Log.d(TAG, "==========================================");
        Log.d(TAG, "Initializing QR Scanner...");
        Log.d(TAG, "==========================================");

        try {
            captureManager = new CaptureManager(this, barcodeView);
            captureManager.initializeFromIntent(getIntent(), savedInstanceState);
            Log.d(TAG, "✅ CaptureManager created and initialized");

            // Set callback for scan result using anonymous inner class instead of lambda
            barcodeView.decodeContinuous(new com.journeyapps.barcodescanner.BarcodeCallback() {
                @Override
                public void barcodeResult(com.journeyapps.barcodescanner.BarcodeResult result) {
                    // IMMEDIATE FEEDBACK
                    runOnUiThread(() -> {
                        Toast.makeText(QrScannerActivity.this, "📷 CALLBACK TRIGGERED!", Toast.LENGTH_SHORT).show();
                    });

                    Log.d(TAG, "==========================================");
                    Log.d(TAG, "📷 barcodeResult callback triggered!");
                    Log.d(TAG, "Result: " + result);
                    Log.d(TAG, "Result null? " + (result == null));

                    if (result != null) {
                        Log.d(TAG, "Result text: " + result.getText());
                        Log.d(TAG, "Result text null? " + (result.getText() == null));
                    }

                    Log.d(TAG, "isProcessing: " + isProcessing);

                    try {
                        if (result != null && result.getText() != null && !isProcessing) {
                            isProcessing = true;
                            String qrToken = result.getText();

                            Log.d(TAG, "✅ All conditions met! Processing QR...");
                            Log.d(TAG, "📝 QR Token Length: " + qrToken.length());
                            Log.d(TAG, "📝 QR Token: " + qrToken);

                            // Pause scanning while processing
                            barcodeView.pause();

                            // Process QR token
                            processQrToken(qrToken);
                        } else {
                            Log.w(TAG, "⚠️ Conditions not met:");
                            Log.w(TAG, "  - result null? " + (result == null));
                            if (result != null) {
                                Log.w(TAG, "  - text null? " + (result.getText() == null));
                            }
                            Log.w(TAG, "  - isProcessing? " + isProcessing);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Exception in barcodeResult callback!", e);
                        isProcessing = false;
                        barcodeView.resume();
                    }

                    Log.d(TAG, "==========================================");
                }

                @Override
                public void possibleResultPoints(java.util.List<com.google.zxing.ResultPoint> resultPoints) {
                    // Not needed for our use case
                }
            });

            Log.d(TAG, "✅ Callback registered successfully");

            captureManager.decode();
            Log.d(TAG, "✅ QR Scanner initialized successfully");
            Log.d(TAG, "==========================================");

        } catch (Exception e) {
            Log.e(TAG, "==========================================");
            Log.e(TAG, "❌ Failed to initialize scanner!", e);
            Log.e(TAG, "==========================================");
            Toast.makeText(this, "Lỗi khởi tạo camera: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void processQrToken(String qrToken) {
        Log.d(TAG, "========== START BOOKING BY QR ==========");
        Log.d(TAG, "📝 QR Token: " + qrToken);
        Log.d(TAG, "📝 Token Length: " + qrToken.length());
        Log.d(TAG, "📝 Token starts with: " + (qrToken.length() > 20 ? qrToken.substring(0, 20) : qrToken));

        progressBar.setVisibility(View.VISIBLE);
        btnFlashlight.setEnabled(false);

        StartByQrRequest request = new StartByQrRequest(qrToken);
        Log.d(TAG, "🚀 Calling API: POST /partners/bookings/start-by-qr");
        Log.d(TAG, "🚀 Request body token: " + request.getQrToken());

        apiService.startBookingByQr(request).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call,
                                 Response<ApiResponse<Booking>> response) {
                progressBar.setVisibility(View.GONE);

                Log.d(TAG, "========== API RESPONSE ==========");
                Log.d(TAG, "📥 Response Code: " + response.code());
                Log.d(TAG, "📥 Response Message: " + response.message());
                Log.d(TAG, "📥 Response isSuccessful: " + response.isSuccessful());
                Log.d(TAG, "📥 Response body null: " + (response.body() == null));

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    Log.d(TAG, "✅ API Response Code: " + apiResponse.getCode());
                    Log.d(TAG, "✅ API Response Message: " + apiResponse.getMessage());
                    Log.d(TAG, "✅ API Response Result null: " + (apiResponse.getResult() == null));

                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        Booking booking = apiResponse.getResult();
                        Log.d(TAG, "✅ Booking ID: " + booking.getId());
                        Log.d(TAG, "✅ Booking Status: " + booking.getStatus());
                        handleSuccess(booking);
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ?
                                        apiResponse.getMessage() : "Unknown error";
                        Log.e(TAG, "❌ API Error: " + errorMsg);
                        Log.e(TAG, "❌ API Response Code was: " + apiResponse.getCode());
                        handleError(errorMsg);
                    }
                } else {
                    // Log error body
                    String errorBody = "";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            Log.e(TAG, "❌ Error Body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to read error body", e);
                    }

                    String errorMessage = "Không thể bắt đầu công việc (HTTP " + response.code() + ")";
                    if (response.code() == 400) {
                        errorMessage = "Mã QR không hợp lệ hoặc đã hết hạn";
                    } else if (response.code() == 401) {
                        errorMessage = "Chưa đăng nhập hoặc token hết hạn";
                    } else if (response.code() == 403) {
                        errorMessage = "Bạn không có quyền thực hiện thao tác này";
                    } else if (response.code() == 404) {
                        errorMessage = "Không tìm thấy booking";
                    }

                    if (!errorBody.isEmpty()) {
                        errorMessage += "\n" + errorBody;
                    }

                    Log.e(TAG, "❌ Final Error: " + errorMessage);
                    handleError(errorMessage);
                }
                Log.d(TAG, "==========================================");
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);

                Log.e(TAG, "========== API CALL FAILED ==========");
                Log.e(TAG, "❌ Error Type: " + t.getClass().getSimpleName());
                Log.e(TAG, "❌ Error Message: " + t.getMessage());
                Log.e(TAG, "Stack Trace:", t);
                Log.e(TAG, "==========================================");

                String errorMessage = "Lỗi kết nối: ";
                if (t instanceof java.net.UnknownHostException) {
                    errorMessage += "Không thể kết nối đến server";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage += "Timeout khi gọi API";
                } else {
                    errorMessage += t.getMessage();
                }

                handleError(errorMessage);
            }
        });
    }

    private void handleSuccess(Booking booking) {
        Log.d(TAG, "========== SUCCESS ==========");
        Log.d(TAG, "✅ Booking started successfully!");
        Log.d(TAG, "✅ Booking ID: " + booking.getId());
        Log.d(TAG, "✅ Navigating to BookingDetailActivity...");

        // Show success message
        Toast.makeText(this, "✅ Đã bắt đầu công việc thành công!", Toast.LENGTH_LONG).show();

        // Navigate to booking detail
        Intent intent = new Intent(this, BookingDetailActivity.class);
        intent.putExtra("booking_id", booking.getId());
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        Log.d(TAG, "Starting BookingDetailActivity with booking_id=" + booking.getId());
        startActivity(intent);

        Log.d(TAG, "Finishing QrScannerActivity");
        finish();
        Log.d(TAG, "==========================================");
    }

    private void handleError(String message) {
        Log.e(TAG, "🔴 Showing error dialog: " + message);

        runOnUiThread(() -> {
            new AlertDialog.Builder(this)
                    .setTitle("⚠️ Quét mã QR thất bại")
                    .setMessage(message != null && !message.isEmpty() ?
                               message : "Có lỗi xảy ra. Vui lòng thử lại.")
                    .setPositiveButton("Quét lại", (dialog, which) -> {
                        Log.d(TAG, "User chose to scan again");
                        isProcessing = false;
                        btnFlashlight.setEnabled(true);
                        barcodeView.resume();
                    })
                    .setNegativeButton("Hủy", (dialog, which) -> {
                        Log.d(TAG, "User cancelled");
                        finish();
                    })
                    .setCancelable(false)
                    .show();
        });
    }

    private void toggleFlashlight() {
        if (barcodeView != null) {
            if (isFlashlightOn) {
                barcodeView.setTorchOff();
                btnFlashlight.setIconResource(R.drawable.ic_flashlight_off);
                isFlashlightOn = false;
            } else {
                barcodeView.setTorchOn();
                btnFlashlight.setIconResource(R.drawable.ic_flashlight_on);
                isFlashlightOn = true;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (captureManager != null) {
            captureManager.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (captureManager != null) {
            captureManager.onPause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (captureManager != null) {
            captureManager.onDestroy();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (captureManager != null) {
            captureManager.onSaveInstanceState(outState);
        }
    }
}

