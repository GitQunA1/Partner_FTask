package com.example.partner_ftask.ui.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.partner_ftask.R;
import com.example.partner_ftask.ui.activity.LoginActivity;
import com.example.partner_ftask.utils.PreferenceManager;

public class ProfileFragment extends Fragment {

    private TextView tvFullName;
    private TextView tvPhoneNumber;
    private Button btnLogout;
    private PreferenceManager preferenceManager;

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
        btnLogout = view.findViewById(R.id.btn_logout);

        // Initialize preference manager
        preferenceManager = new PreferenceManager(requireContext());

        // Load user info
        loadUserInfo();

        // Setup logout button
        btnLogout.setOnClickListener(v -> showLogoutDialog());
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
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}

