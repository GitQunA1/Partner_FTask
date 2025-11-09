package com.example.partner_ftask.data.repository;

import android.content.Context;
import android.util.Log;

import com.example.partner_ftask.data.api.ApiClient;
import com.example.partner_ftask.data.api.ApiService;
import com.example.partner_ftask.data.model.ApiResponse;
import com.example.partner_ftask.data.model.AuthResponse;
import com.example.partner_ftask.data.model.VerifyOtpRequest;
import com.example.partner_ftask.data.model.Wallet;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private final ApiService apiService;

    public AuthRepository(Context context) {
        ApiClient.init(context);
        this.apiService = ApiClient.getApiService();
    }

    public interface AuthCallback {
        void onSuccess(AuthResponse authResponse);
        void onError(String errorMessage);
    }

    public interface WalletCallback {
        void onSuccess(Wallet wallet);
        void onError(String errorMessage);
    }

    // Verify OTP and login/register
    public void verifyOtp(String phone, String otp, AuthCallback callback) {
        VerifyOtpRequest request = new VerifyOtpRequest(phone, otp, "PARTNER");

        Log.d(TAG, "Verifying OTP for phone: " + phone);

        apiService.verifyOtp(request).enqueue(new Callback<ApiResponse<AuthResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<AuthResponse>> call, Response<ApiResponse<AuthResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<AuthResponse> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        Log.d(TAG, "✅ OTP verification successful");
                        callback.onSuccess(apiResponse.getResult());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ?
                            apiResponse.getMessage() : "Xác thực OTP thất bại";
                        Log.e(TAG, "❌ OTP verification failed: " + errorMsg);
                        callback.onError(errorMsg);
                    }
                } else {
                    String errorMsg = "Mã OTP không đúng hoặc đã hết hạn";
                    Log.e(TAG, "❌ Response not successful: " + response.code());
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<AuthResponse>> call, Throwable t) {
                String errorMsg = "Lỗi kết nối: " + t.getMessage();
                Log.e(TAG, "❌ Network error: " + t.getMessage());
                callback.onError(errorMsg);
            }
        });
    }

    // Get wallet information
    public void getWallet(WalletCallback callback) {
        Log.d(TAG, "Fetching wallet information...");

        apiService.getUserWallet().enqueue(new Callback<ApiResponse<Wallet>>() {
            @Override
            public void onResponse(Call<ApiResponse<Wallet>> call, Response<ApiResponse<Wallet>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Wallet> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        Log.d(TAG, "✅ Wallet data retrieved successfully");
                        callback.onSuccess(apiResponse.getResult());
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ?
                            apiResponse.getMessage() : "Không thể lấy thông tin ví";
                        Log.e(TAG, "❌ Failed to get wallet: " + errorMsg);
                        callback.onError(errorMsg);
                    }
                } else {
                    String errorMsg = "Không thể lấy thông tin ví";
                    Log.e(TAG, "❌ Response not successful: " + response.code());
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Wallet>> call, Throwable t) {
                String errorMsg = "Lỗi kết nối: " + t.getMessage();
                Log.e(TAG, "❌ Network error: " + t.getMessage());
                callback.onError(errorMsg);
            }
        });
    }
}

