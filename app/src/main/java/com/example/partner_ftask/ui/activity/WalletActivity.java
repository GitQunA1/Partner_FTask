package com.example.partner_ftask.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.partner_ftask.R;
import com.example.partner_ftask.data.api.ApiClient;
import com.example.partner_ftask.data.api.ApiService;
import com.example.partner_ftask.data.model.ApiResponse;
import com.example.partner_ftask.data.model.PageResponse;
import com.example.partner_ftask.data.model.Transaction;
import com.example.partner_ftask.data.model.Wallet;
import com.example.partner_ftask.data.repository.AuthRepository;
import com.example.partner_ftask.ui.adapter.TransactionsAdapter;
import com.example.partner_ftask.utils.DateTimeUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletActivity extends AppCompatActivity {

    private TextView tvBalance;
    private TextView tvTotalEarned;
    private TextView tvTotalWithdrawn;
    private TextView tvEmpty;
    private RecyclerView recyclerViewTransactions;
    private TransactionsAdapter transactionsAdapter;
    private ProgressBar progressBar;
    private ProgressBar progressBarTransactions;
    private MaterialButton btnLoadMore;

    private AuthRepository authRepository;
    private ApiService apiService;

    private int currentPage = 1;
    private final int pageSize = 10;
    private boolean isLoading = false;
    private boolean hasMorePages = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        // Initialize repository
        authRepository = new AuthRepository(this);
        apiService = ApiClient.getApiService();

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
        tvEmpty = findViewById(R.id.tv_empty);
        recyclerViewTransactions = findViewById(R.id.recycler_view_transactions);
        progressBar = findViewById(R.id.progress_bar);
        progressBarTransactions = findViewById(R.id.progress_bar_transactions);
        btnLoadMore = findViewById(R.id.btn_load_more);

        // Setup RecyclerView
        transactionsAdapter = new TransactionsAdapter();
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(transactionsAdapter);

        // Setup load more button
        btnLoadMore.setOnClickListener(v -> loadTransactions(false));

        // Load wallet info
        loadWalletInfo();

        // Load transactions
        loadTransactions(true);
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
        progressBar.setVisibility(View.VISIBLE);

        authRepository.getWallet(new AuthRepository.WalletCallback() {
            @Override
            public void onSuccess(Wallet wallet) {
                progressBar.setVisibility(View.GONE);
                displayWalletInfo(wallet);
            }

            @Override
            public void onError(String errorMessage) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(WalletActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayWalletInfo(Wallet wallet) {
        if (wallet == null) {
            return;
        }

        tvBalance.setText(DateTimeUtils.formatCurrency(wallet.getBalance()));
        tvTotalEarned.setText(DateTimeUtils.formatCurrency(wallet.getTotalEarned()));
        tvTotalWithdrawn.setText(DateTimeUtils.formatCurrency(wallet.getTotalWithdrawn()));
    }

    private void loadTransactions(boolean isFirstLoad) {
        if (isLoading || (!hasMorePages && !isFirstLoad)) {
            return;
        }

        isLoading = true;

        if (isFirstLoad) {
            currentPage = 1;
            progressBarTransactions.setVisibility(View.VISIBLE);
            tvEmpty.setVisibility(View.GONE);
        } else {
            btnLoadMore.setEnabled(false);
        }

        apiService.getUserTransactions(currentPage, pageSize)
                .enqueue(new Callback<ApiResponse<PageResponse<Transaction>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PageResponse<Transaction>>> call,
                                          Response<ApiResponse<PageResponse<Transaction>>> response) {
                        isLoading = false;
                        progressBarTransactions.setVisibility(View.GONE);
                        btnLoadMore.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<PageResponse<Transaction>> apiResponse = response.body();
                            if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                                PageResponse<Transaction> pageResponse = apiResponse.getResult();
                                List<Transaction> transactions = pageResponse.getContent();

                                if (isFirstLoad) {
                                    if (transactions.isEmpty()) {
                                        recyclerViewTransactions.setVisibility(View.GONE);
                                        tvEmpty.setVisibility(View.VISIBLE);
                                        btnLoadMore.setVisibility(View.GONE);
                                    } else {
                                        recyclerViewTransactions.setVisibility(View.VISIBLE);
                                        tvEmpty.setVisibility(View.GONE);
                                        transactionsAdapter.setTransactions(transactions);

                                        // Check if has more pages
                                        hasMorePages = currentPage < pageResponse.getTotalPages();
                                        btnLoadMore.setVisibility(hasMorePages ? View.VISIBLE : View.GONE);
                                    }
                                } else {
                                    transactionsAdapter.addTransactions(transactions);
                                    hasMorePages = currentPage < pageResponse.getTotalPages();
                                    btnLoadMore.setVisibility(hasMorePages ? View.VISIBLE : View.GONE);
                                }

                                currentPage++;
                            } else {
                                Toast.makeText(WalletActivity.this, apiResponse.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(WalletActivity.this, "Lỗi khi tải giao dịch",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PageResponse<Transaction>>> call, Throwable t) {
                        isLoading = false;
                        progressBarTransactions.setVisibility(View.GONE);
                        btnLoadMore.setEnabled(true);
                        Toast.makeText(WalletActivity.this, "Lỗi kết nối: " + t.getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

