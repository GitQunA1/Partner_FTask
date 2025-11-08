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
import com.example.partner_ftask.data.model.Wallet;
import com.example.partner_ftask.data.repository.AuthRepository;
import com.example.partner_ftask.ui.activity.OtpLoginActivity;
import com.example.partner_ftask.ui.activity.WalletActivity;
import com.example.partner_ftask.utils.DateTimeUtils;
import com.example.partner_ftask.utils.PreferenceManager;

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
    private ProgressBar progressBar;

    private PreferenceManager preferenceManager;
    private AuthRepository authRepository;

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
        progressBar = view.findViewById(R.id.progress_bar);

        // Initialize managers
        preferenceManager = new PreferenceManager(requireContext());
        authRepository = new AuthRepository(requireContext());

        // Load user info
        loadUserInfo();

        // Load wallet info
        loadWalletInfo();

        // Setup buttons
        btnLogout.setOnClickListener(v -> showLogoutDialog());
        btnRefreshWallet.setOnClickListener(v -> loadWalletInfo());
        btnViewReviews.setOnClickListener(v -> openReviewsActivity());
        btnViewWallet.setOnClickListener(v -> openWalletActivity());
    }

    private void loadUserInfo() {
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
        // Clear preferences
        preferenceManager.clearAll();

        // Go to login activity
        Intent intent = new Intent(requireContext(), OtpLoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}

