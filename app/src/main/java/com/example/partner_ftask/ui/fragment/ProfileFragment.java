package com.example.partner_ftask.ui.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.partner_ftask.R;
import com.example.partner_ftask.data.api.ApiClient;
import com.example.partner_ftask.data.api.ApiService;
import com.example.partner_ftask.data.model.ApiResponse;
import com.example.partner_ftask.data.model.District;
import com.example.partner_ftask.data.model.UpdateDistrictsRequest;
import com.example.partner_ftask.data.model.UserInfoResponse;
import com.example.partner_ftask.data.model.Wallet;
import com.example.partner_ftask.data.repository.AuthRepository;
import com.example.partner_ftask.ui.activity.PhoneLoginActivity;
import com.example.partner_ftask.ui.activity.WalletActivity;
import com.example.partner_ftask.ui.adapter.DistrictSelectionAdapter;
import com.example.partner_ftask.utils.DateTimeUtils;
import com.example.partner_ftask.utils.PreferenceManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private TextView tvFullName;
    private TextView tvPhoneNumber;
    private TextView tvBalance;
    private TextView tvTotalEarned;
    private TextView tvTotalWithdrawn;
    private Button btnLogout;
    private Button btnRefreshWallet;
    private Button btnViewWallet;
    private Button btnViewReviews;
    private Button btnDistrictSettings;
    private ProgressBar progressBar;

    private PreferenceManager preferenceManager;
    private AuthRepository authRepository;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tvFullName = view.findViewById(R.id.tv_full_name);
        tvPhoneNumber = view.findViewById(R.id.tv_phone_number);
        tvBalance = view.findViewById(R.id.tv_balance);
        tvTotalEarned = view.findViewById(R.id.tv_total_earned);
        tvTotalWithdrawn = view.findViewById(R.id.tv_total_withdrawn);
        btnLogout = view.findViewById(R.id.btn_logout);
        btnRefreshWallet = view.findViewById(R.id.btn_refresh_wallet);
        btnViewReviews = view.findViewById(R.id.btn_view_reviews);
        btnViewWallet = view.findViewById(R.id.btn_view_wallet);
        btnDistrictSettings = view.findViewById(R.id.btn_district_settings);
        progressBar = view.findViewById(R.id.progress_bar);

        // Initialize managers
        preferenceManager = new PreferenceManager(requireContext());
        authRepository = new AuthRepository(requireContext());
        ApiClient.init(requireContext());
        apiService = ApiClient.getApiService();

        // Load user info
        loadUserInfo();

        // Load wallet info
        loadWalletInfo();

        // Setup buttons
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        btnRefreshWallet.setOnClickListener(v -> loadWalletInfo());
        btnViewReviews.setOnClickListener(v -> openReviewsActivity());
        btnViewWallet.setOnClickListener(v -> openWalletActivity());
        btnDistrictSettings.setOnClickListener(v -> openDistrictSettings());
    }

    private void loadUserInfo() {
        apiService.getUserInfo().enqueue(new Callback<ApiResponse<UserInfoResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserInfoResponse>> call, Response<ApiResponse<UserInfoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UserInfoResponse> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        UserInfoResponse userInfo = apiResponse.getResult();
                        preferenceManager.saveUserInfo(userInfo);
                        displayUserInfo(userInfo);
                    } else {
                        displayUserInfoFromPreferences();
                    }
                } else {
                    displayUserInfoFromPreferences();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserInfoResponse>> call, Throwable t) {
                displayUserInfoFromPreferences();
            }
        });
    }

    private void displayUserInfo(UserInfoResponse userInfo) {
        if (userInfo == null) {
            displayUserInfoFromPreferences();
            return;
        }

        String fullName = userInfo.getFullName();
        String phone = userInfo.getPhone();

        if (fullName != null && !fullName.isEmpty()) {
            tvFullName.setText(fullName);
        } else {
            tvFullName.setText("Partner");
        }

        if (phone != null && !phone.isEmpty()) {
            tvPhoneNumber.setText(phone);
        } else {
            tvPhoneNumber.setText("Chưa có thông tin");
        }
    }

    private void displayUserInfoFromPreferences() {
        String fullName = preferenceManager.getFullName();
        String phoneNumber = preferenceManager.getPhoneNumber();

        if (fullName != null && !fullName.isEmpty()) {
            tvFullName.setText(fullName);
        } else {
            tvFullName.setText("Partner");
        }

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            tvPhoneNumber.setText(phoneNumber);
        } else {
            tvPhoneNumber.setText("Chưa có thông tin");
        }
    }

    public void refreshUserInfo() {
        loadUserInfo();
    }

    private void loadWalletInfo() {
        setLoading(true);

        authRepository.getWallet(new AuthRepository.WalletCallback() {
            @Override
            public void onSuccess(Wallet wallet) {
                setLoading(false);
                displayWalletInfo(wallet);
            }

            @Override
            public void onError(String errorMessage) {
                setLoading(false);
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                // Display default values
                displayDefaultWalletInfo();
            }
        });
    }

    private void displayWalletInfo(Wallet wallet) {
        if (wallet == null) {
            displayDefaultWalletInfo();
            return;
        }

        tvBalance.setText(DateTimeUtils.formatCurrency(wallet.getBalance()));
        tvTotalEarned.setText(DateTimeUtils.formatCurrency(wallet.getTotalEarned()));
        tvTotalWithdrawn.setText(DateTimeUtils.formatCurrency(wallet.getTotalWithdrawn()));

        // Update user info from wallet if available
        if (wallet.getUser() != null) {
            if (wallet.getUser().getFullName() != null && !wallet.getUser().getFullName().isEmpty()) {
                tvFullName.setText(wallet.getUser().getFullName());
                preferenceManager.saveFullName(wallet.getUser().getFullName());
            }

            if (wallet.getUser().getPhoneNumber() != null && !wallet.getUser().getPhoneNumber().isEmpty()) {
                tvPhoneNumber.setText(wallet.getUser().getPhoneNumber());
                preferenceManager.savePhoneNumber(wallet.getUser().getPhoneNumber());
            }
        }
    }

    private void displayDefaultWalletInfo() {
        tvBalance.setText(DateTimeUtils.formatCurrency(0));
        tvTotalEarned.setText(DateTimeUtils.formatCurrency(0));
        tvTotalWithdrawn.setText(DateTimeUtils.formatCurrency(0));
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRefreshWallet.setEnabled(!isLoading);
    }

    private void openWalletActivity() {
        Intent intent = new Intent(requireContext(), WalletActivity.class);
        startActivity(intent);
    }

    private void openReviewsActivity() {
        Intent intent = new Intent(requireContext(), com.example.partner_ftask.ui.activity.ReviewsActivity.class);
        startActivity(intent);
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đăng xuất")
                .setMessage("Bạn có chắc chắn muốn đăng xuất?")
                .setPositiveButton("Đăng xuất", (dialog, which) -> logout())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void logout() {
        preferenceManager.clearAll();

        Intent intent = new Intent(requireContext(), PhoneLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    private void openDistrictSettings() {
        apiService.getPartnerDistricts().enqueue(new Callback<ApiResponse<List<District>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<District>>> call, Response<ApiResponse<List<District>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<District>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        List<District> registeredDistricts = apiResponse.getResult();
                        loadAllDistricts(registeredDistricts);
                    } else {
                        loadAllDistricts(new java.util.ArrayList<>());
                    }
                } else {
                    loadAllDistricts(new java.util.ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<District>>> call, Throwable t) {
                loadAllDistricts(new java.util.ArrayList<>());
            }
        });
    }

    private void loadAllDistricts(List<District> registeredDistricts) {
        apiService.getAllDistricts().enqueue(new Callback<ApiResponse<List<District>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<District>>> call, Response<ApiResponse<List<District>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<District>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        List<District> allDistricts = apiResponse.getResult();
                        showDistrictDialog(allDistricts, registeredDistricts);
                    } else {
                        Toast.makeText(requireContext(), "Không thể tải danh sách quận", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Lỗi khi tải danh sách quận", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<District>>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDistrictDialog(List<District> districts, List<District> registeredDistricts) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setTitle("Chọn các quận bạn muốn làm việc");

        android.view.View dialogView = getLayoutInflater().inflate(R.layout.dialog_district_selection, null);
        androidx.recyclerview.widget.RecyclerView recyclerView = dialogView.findViewById(R.id.recycler_view_districts);
        
        recyclerView.setLayoutManager(new androidx.recyclerview.widget.LinearLayoutManager(requireContext()));
        DistrictSelectionAdapter adapter = new DistrictSelectionAdapter(districts);
        
        java.util.Set<Long> registeredIds = new java.util.HashSet<>();
        for (District d : registeredDistricts) {
            registeredIds.add(d.getId());
        }
        adapter.setSelectedDistricts(registeredIds);
        
        recyclerView.setAdapter(adapter);

        builder.setView(dialogView);
        builder.setPositiveButton("Xác nhận", null);
        builder.setCancelable(true);

        android.app.AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            android.widget.Button positiveButton = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                List<Long> selectedIds = adapter.getSelectedDistrictIds();
                if (selectedIds.isEmpty()) {
                    Toast.makeText(requireContext(), "Vui lòng chọn ít nhất một quận", Toast.LENGTH_SHORT).show();
                } else {
                    dialog.dismiss();
                    updatePartnerDistricts(selectedIds);
                }
            });
        });

        dialog.show();
    }


    private void updatePartnerDistricts(List<Long> districtIds) {
        UpdateDistrictsRequest request = new UpdateDistrictsRequest(districtIds);
        apiService.updatePartnerDistricts(request).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Void> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        Toast.makeText(requireContext(), "Đã cập nhật quận làm việc thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(requireContext(), "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Lỗi cập nhật quận làm việc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

