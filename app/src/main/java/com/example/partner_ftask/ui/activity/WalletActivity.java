package com.example.partner_ftask.ui.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
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
import com.example.partner_ftask.data.model.TopUpResponse;
import com.example.partner_ftask.data.model.Transaction;
import com.example.partner_ftask.data.model.Wallet;
import com.example.partner_ftask.data.repository.AuthRepository;
import com.example.partner_ftask.ui.adapter.TransactionsAdapter;
import com.example.partner_ftask.utils.DateTimeUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

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
    private MaterialButton btnTopUp;
    private MaterialButton btnWithdraw;

    private AuthRepository authRepository;
    private ApiService apiService;
    private Wallet currentWallet;

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
        btnTopUp = findViewById(R.id.btn_top_up);
        btnWithdraw = findViewById(R.id.btn_withdraw);

        // Setup RecyclerView
        transactionsAdapter = new TransactionsAdapter();
        recyclerViewTransactions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewTransactions.setAdapter(transactionsAdapter);

        // Setup buttons
        btnLoadMore.setOnClickListener(v -> loadTransactions(false));
        btnTopUp.setOnClickListener(v -> showTopUpDialog());
        btnWithdraw.setOnClickListener(v -> showWithdrawalDialog());

        // Load wallet info
        loadWalletInfo();

        // Load transactions
        loadTransactions(true);

        // Handle payment return from deep link
        handlePaymentReturn(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handlePaymentReturn(intent);
    }

    private void handlePaymentReturn(Intent intent) {
        if (intent == null || intent.getData() == null) {
            return;
        }

        Uri data = intent.getData();
        if (data != null && "partnerftask".equals(data.getScheme())) {
            String responseCode = data.getQueryParameter("vnp_ResponseCode");
            String transactionStatus = data.getQueryParameter("vnp_TransactionStatus");
            String orderInfo = data.getQueryParameter("vnp_OrderInfo");

            if ("00".equals(responseCode) && "00".equals(transactionStatus)) {
                // Payment successful - Need to confirm with backend
                progressBar.setVisibility(View.VISIBLE);
                confirmPaymentWithBackend(orderInfo, responseCode, transactionStatus);
            } else {
                // Payment failed
                String message = getPaymentErrorMessage(responseCode);
                Toast.makeText(this, "Nạp tiền thất bại: " + message, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void confirmPaymentWithBackend(String orderInfo, String responseCode, String transactionStatus) {
        // Call backend to confirm payment and update wallet
        apiService.confirmPayment(orderInfo, responseCode, transactionStatus)
                .enqueue(new Callback<ApiResponse<String>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<String>> call,
                                          Response<ApiResponse<String>> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<String> apiResponse = response.body();
                            if (apiResponse.getCode() == 200) {
                                Toast.makeText(WalletActivity.this,
                                    "Nạp tiền thành công!", Toast.LENGTH_LONG).show();

                                // Reload wallet info after successful confirmation
                                loadWalletInfo();
                                loadTransactions(true);
                            } else {
                                Toast.makeText(WalletActivity.this,
                                    "Xác nhận thanh toán thất bại: " + apiResponse.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(WalletActivity.this,
                                "Không thể xác nhận thanh toán với server",
                                Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(WalletActivity.this,
                            "Lỗi kết nối khi xác nhận: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private String getPaymentErrorMessage(String responseCode) {
        if (responseCode == null) return "Không xác định";

        switch (responseCode) {
            case "07":
                return "Trừ tiền thành công. Giao dịch bị nghi ngờ";
            case "09":
                return "Giao dịch không thành công do: Thẻ/Tài khoản chưa đăng ký dịch vụ InternetBanking";
            case "10":
                return "Giao dịch không thành công do: Khách hàng xác thực thông tin thẻ/tài khoản không đúng quá 3 lần";
            case "11":
                return "Giao dịch không thành công do: Đã hết hạn chờ thanh toán";
            case "12":
                return "Giao dịch không thành công do: Thẻ/Tài khoản bị khóa";
            case "13":
                return "Giao dịch không thành công do Quý khách nhập sai mật khẩu xác thực giao dịch";
            case "24":
                return "Giao dịch không thành công do: Khách hàng hủy giao dịch";
            case "51":
                return "Giao dịch không thành công do: Tài khoản không đủ số dư";
            case "65":
                return "Giao dịch không thành công do: Tài khoản đã vượt quá giới hạn giao dịch trong ngày";
            case "75":
                return "Ngân hàng thanh toán đang bảo trì";
            case "79":
                return "Giao dịch không thành công do: KH nhập sai mật khẩu thanh toán quá số lần quy định";
            default:
                return "Lỗi không xác định (Mã: " + responseCode + ")";
        }
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
                currentWallet = wallet;
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
                                java.util.List<Transaction> transactions = pageResponse.getContent();

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

    private void showTopUpDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_top_up, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        TextView tvCurrentBalance = dialogView.findViewById(R.id.tvCurrentBalance);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        // Quick amount buttons
        MaterialButton btn50k = dialogView.findViewById(R.id.btn50k);
        MaterialButton btn100k = dialogView.findViewById(R.id.btn100k);
        MaterialButton btn200k = dialogView.findViewById(R.id.btn200k);
        MaterialButton btn500k = dialogView.findViewById(R.id.btn500k);
        MaterialButton btn1m = dialogView.findViewById(R.id.btn1m);
        MaterialButton btn2m = dialogView.findViewById(R.id.btn2m);

        if (currentWallet != null) {
            tvCurrentBalance.setText(DateTimeUtils.formatCurrency(currentWallet.getBalance()));
        }

        // Set quick amounts
        btn50k.setOnClickListener(v -> etAmount.setText("50000"));
        btn100k.setOnClickListener(v -> etAmount.setText("100000"));
        btn200k.setOnClickListener(v -> etAmount.setText("200000"));
        btn500k.setOnClickListener(v -> etAmount.setText("500000"));
        btn1m.setOnClickListener(v -> etAmount.setText("1000000"));
        btn2m.setOnClickListener(v -> etAmount.setText("2000000"));

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                processTopUp(amount);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void showWithdrawalDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_withdrawal, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        TextView tvCurrentBalance = dialogView.findViewById(R.id.tvCurrentBalance);
        EditText etAmount = dialogView.findViewById(R.id.etAmount);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btnCancel);
        MaterialButton btnConfirm = dialogView.findViewById(R.id.btnConfirm);

        // Quick amount buttons
        MaterialButton btn50k = dialogView.findViewById(R.id.btn50k);
        MaterialButton btn100k = dialogView.findViewById(R.id.btn100k);
        MaterialButton btn200k = dialogView.findViewById(R.id.btn200k);
        MaterialButton btn500k = dialogView.findViewById(R.id.btn500k);
        MaterialButton btnAll = dialogView.findViewById(R.id.btnAll);

        if (currentWallet != null) {
            tvCurrentBalance.setText(DateTimeUtils.formatCurrency(currentWallet.getBalance()));
        }

        // Set quick amounts
        btn50k.setOnClickListener(v -> etAmount.setText("50000"));
        btn100k.setOnClickListener(v -> etAmount.setText("100000"));
        btn200k.setOnClickListener(v -> etAmount.setText("200000"));
        btn500k.setOnClickListener(v -> etAmount.setText("500000"));
        btnAll.setOnClickListener(v -> {
            if (currentWallet != null) {
                etAmount.setText(String.valueOf((int) currentWallet.getBalance()));
            }
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnConfirm.setOnClickListener(v -> {
            String amountStr = etAmount.getText().toString().trim();
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số tiền", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (currentWallet != null && amount > currentWallet.getBalance()) {
                    Toast.makeText(this, "Số dư không đủ", Toast.LENGTH_SHORT).show();
                    return;
                }
                dialog.dismiss();
                processWithdrawal(amount);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void processTopUp(double amount) {
        progressBar.setVisibility(View.VISIBLE);

        // Create deep link callback URL for app
        String callbackUrl = "partnerftask://payment/vnpay-return";

        apiService.topUpWallet(amount, callbackUrl).enqueue(new Callback<ApiResponse<TopUpResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<TopUpResponse>> call,
                                   Response<ApiResponse<TopUpResponse>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<TopUpResponse> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        TopUpResponse topUpResponse = apiResponse.getResult();
                        String paymentUrl = topUpResponse.getPaymentUrl();

                        if (paymentUrl != null && !paymentUrl.isEmpty()) {
                            // Open payment URL in browser
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                            startActivity(browserIntent);

                            Toast.makeText(WalletActivity.this,
                                    "Đang chuyển đến trang thanh toán...", Toast.LENGTH_SHORT).show();
                        } else {
                            showError("Không thể tạo link thanh toán");
                        }
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể nạp tiền");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<TopUpResponse>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void processWithdrawal(double amount) {
        progressBar.setVisibility(View.VISIBLE);

        apiService.withdrawalWallet(amount).enqueue(new Callback<ApiResponse<Wallet>>() {
            @Override
            public void onResponse(Call<ApiResponse<Wallet>> call,
                                   Response<ApiResponse<Wallet>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Wallet> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        Toast.makeText(WalletActivity.this,
                                "Rút tiền thành công", Toast.LENGTH_SHORT).show();

                        // Reload wallet info to get updated balance
                        loadWalletInfo();
                        loadTransactions(true);
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể rút tiền");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Wallet>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload wallet when coming back from payment
        loadWalletInfo();
    }
}

