package com.example.partner_ftask.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.partner_ftask.R;
import com.example.partner_ftask.data.model.Wallet;
import com.example.partner_ftask.data.repository.AuthRepository;
import com.example.partner_ftask.utils.DateTimeUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

public class WalletActivity extends AppCompatActivity {

    private static final String TAG = "WalletActivity";

    private TextView tvBalance;
    private TextView tvTotalEarned;
    private TextView tvTotalWithdrawn;
    private TextView tvUserName;
    private MaterialButton btnRefresh;
    private ProgressBar progressBar;

    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        // Initialize repository
        authRepository = new AuthRepository(this);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Ví của tôi");
        }

        // Initialize views
        tvBalance = findViewById(R.id.tv_balance);
        tvTotalEarned = findViewById(R.id.tv_total_earned);
        tvTotalWithdrawn = findViewById(R.id.tv_total_withdrawn);
        tvUserName = findViewById(R.id.tv_user_name);
        btnRefresh = findViewById(R.id.btn_refresh);
        progressBar = findViewById(R.id.progress_bar);

        // Setup refresh button
        btnRefresh.setOnClickListener(v -> loadWalletInfo());

        // Load wallet info
        loadWalletInfo();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
                Toast.makeText(WalletActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayWalletInfo(Wallet wallet) {
        if (wallet == null) {
            return;
        }

        // Display wallet amounts
        tvBalance.setText(DateTimeUtils.formatCurrency(wallet.getBalance()));
        tvTotalEarned.setText(DateTimeUtils.formatCurrency(wallet.getTotalEarned()));
        tvTotalWithdrawn.setText(DateTimeUtils.formatCurrency(wallet.getTotalWithdrawn()));

        // Display user name
        if (wallet.getUser() != null && wallet.getUser().getFullName() != null) {
            tvUserName.setText(wallet.getUser().getFullName());
        }

        android.util.Log.d(TAG, "Wallet info displayed - Balance: " + wallet.getBalance());
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRefresh.setEnabled(!isLoading);
    }
}

