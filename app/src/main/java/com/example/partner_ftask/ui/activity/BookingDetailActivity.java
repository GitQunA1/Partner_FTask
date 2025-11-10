package com.example.partner_ftask.ui.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.partner_ftask.R;
import com.example.partner_ftask.data.api.ApiClient;
import com.example.partner_ftask.data.api.ApiService;
import com.example.partner_ftask.data.model.ApiResponse;
import com.example.partner_ftask.data.model.Booking;
import com.example.partner_ftask.data.model.BookingPartner;
import com.example.partner_ftask.utils.DateTimeUtils;
import com.google.android.material.appbar.MaterialToolbar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingDetailActivity extends AppCompatActivity {

    private TextView tvServiceName;
    private TextView tvStatus;
    private TextView tvServiceDescription;
    private TextView tvDuration;
    private TextView tvPrice;
    private TextView tvStartTime;
    private TextView tvAddress;
    private TextView tvCustomerNote;
    private TextView tvCustomerName;
    private TextView tvCustomerPhone;
    private Button btnPrimaryAction;
    private Button btnCancel;
    private ProgressBar progressBar;

    private ApiService apiService;
    private int bookingId;
    private Booking currentBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_detail);

        // Initialize views
        initViews();

        // Get booking ID from intent
        bookingId = getIntent().getIntExtra("booking_id", -1);
        if (bookingId == -1) {
            Toast.makeText(this, "Lỗi: Không tìm thấy thông tin booking", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize API service
        apiService = ApiClient.getApiService();

        // Load booking detail
        loadBookingDetail();
    }

    private void initViews() {
        // Setup back button instead of toolbar
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        tvServiceName = findViewById(R.id.tv_service_name);
        tvStatus = findViewById(R.id.tv_status);
        tvServiceDescription = findViewById(R.id.tv_service_description);
        tvDuration = findViewById(R.id.tv_duration);
        tvPrice = findViewById(R.id.tv_price);
        tvStartTime = findViewById(R.id.tv_start_time);
        tvAddress = findViewById(R.id.tv_address);
        tvCustomerNote = findViewById(R.id.tv_customer_note);
        tvCustomerName = findViewById(R.id.tv_customer_name);
        tvCustomerPhone = findViewById(R.id.tv_customer_phone);
        btnPrimaryAction = findViewById(R.id.btn_primary_action);
        btnCancel = findViewById(R.id.btn_cancel);
        progressBar = findViewById(R.id.progress_bar);
    }

    private void loadBookingDetail() {
        showLoading(true);

        apiService.getBookingDetail(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        currentBooking = apiResponse.getResult();
                        displayBookingInfo(currentBooking);
                    } else {
                        Toast.makeText(BookingDetailActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BookingDetailActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(BookingDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBookingInfo(Booking booking) {
        // Service info
        if (booking.getVariant() != null) {
            tvServiceName.setText(booking.getVariant().getName());
            tvServiceDescription.setText(booking.getVariant().getDescription());
            tvDuration.setText(DateTimeUtils.formatDuration(booking.getVariant().getDuration()));
        }

        // Status
        String partnerStatus = getPartnerStatus(booking);
        tvStatus.setText(getStatusText(partnerStatus));
        tvStatus.setBackgroundColor(getStatusColor(partnerStatus));

        // Price
        tvPrice.setText(DateTimeUtils.formatCurrency(booking.getTotalPrice()));

        // Booking info
        tvStartTime.setText(DateTimeUtils.formatDateTime(booking.getStartAt()));

        if (booking.getAddress() != null) {
            tvAddress.setText(booking.getAddress().getFullAddress());
        }

        if (booking.getCustomerNote() != null && !booking.getCustomerNote().isEmpty()) {
            tvCustomerNote.setText(booking.getCustomerNote());
        } else {
            tvCustomerNote.setText("Không có ghi chú");
        }

        // Customer info
        if (booking.getCustomer() != null) {
            tvCustomerName.setText(booking.getCustomer().getFullName());
            tvCustomerPhone.setText(booking.getCustomer().getPhoneNumber());
        }

        // Setup action buttons
        setupActionButtons(booking, partnerStatus);
    }

    private String getPartnerStatus(Booking booking) {
        if (booking.getPartners() != null && !booking.getPartners().isEmpty()) {
            BookingPartner bookingPartner = booking.getPartners().get(0);
            return bookingPartner.getStatus();
        }
        return booking.getStatus();
    }

    private String getStatusText(String status) {
        switch (status) {
            case "PENDING":
                return "Chờ nhận";
            case "PARTIALLY_ACCEPTED":
                return "Đang chờ";
            case "JOINED":
                return "Đã nhận";
            case "WORKING":
                return "Đang làm";
            case "COMPLETED":
                return "Hoàn thành";
            case "CANCELLED":
                return "Đã hủy";
            default:
                return status;
        }
    }

    private int getStatusColor(String status) {
        switch (status) {
            case "PENDING":
            case "PARTIALLY_ACCEPTED":
                return 0xFF4CAF50; // Green
            case "JOINED":
                return 0xFF2196F3; // Blue
            case "WORKING":
                return 0xFFFF9800; // Orange
            case "COMPLETED":
                return 0xFF9E9E9E; // Gray
            case "CANCELLED":
                return 0xFFF44336; // Red
            default:
                return 0xFF757575;
        }
    }

    private void setupActionButtons(Booking booking, String status) {
        btnPrimaryAction.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.GONE);

        switch (status) {
            case "PENDING":
            case "PARTIALLY_ACCEPTED":
                btnPrimaryAction.setText("Nhận việc");
                btnPrimaryAction.setOnClickListener(v -> claimBooking());
                break;
            case "JOINED":
                btnPrimaryAction.setText("Bắt đầu");
                btnPrimaryAction.setOnClickListener(v -> startBooking());
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(v -> showCancelDialog());
                break;
            case "WORKING":
                btnPrimaryAction.setText("Hoàn thành");
                btnPrimaryAction.setOnClickListener(v -> completeBooking());
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setOnClickListener(v -> showCancelDialog());
                break;
            case "COMPLETED":
            case "CANCELLED":
                btnPrimaryAction.setVisibility(View.GONE);
                break;
        }
    }

    private void claimBooking() {
        new AlertDialog.Builder(this)
                .setTitle("Nhận việc")
                .setMessage("Bạn có chắc chắn muốn nhận công việc này?")
                .setPositiveButton("Nhận việc", (dialog, which) -> {
                    performClaimBooking();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performClaimBooking() {
        showLoading(true);

        apiService.claimBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        Toast.makeText(BookingDetailActivity.this, "Nhận việc thành công!", Toast.LENGTH_SHORT).show();
                        loadBookingDetail();
                    } else {
                        Toast.makeText(BookingDetailActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BookingDetailActivity.this, "Lỗi khi nhận việc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(BookingDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startBooking() {
        new AlertDialog.Builder(this)
                .setTitle("Bắt đầu công việc")
                .setMessage("Bạn có muốn bắt đầu công việc này không?")
                .setPositiveButton("Bắt đầu", (dialog, which) -> {
                    performStartBooking();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performStartBooking() {
        showLoading(true);

        apiService.startBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        Toast.makeText(BookingDetailActivity.this, "Đã bắt đầu công việc!", Toast.LENGTH_SHORT).show();
                        loadBookingDetail();
                    } else {
                        Toast.makeText(BookingDetailActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BookingDetailActivity.this, "Lỗi khi bắt đầu công việc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(BookingDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void completeBooking() {
        new AlertDialog.Builder(this)
                .setTitle("Hoàn thành công việc")
                .setMessage("Bạn có chắc chắn đã hoàn thành công việc này?")
                .setPositiveButton("Hoàn thành", (dialog, which) -> {
                    performCompleteBooking();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performCompleteBooking() {
        showLoading(true);

        apiService.completeBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        Toast.makeText(BookingDetailActivity.this, "Đã hoàn thành công việc!", Toast.LENGTH_SHORT).show();
                        loadBookingDetail();
                    } else {
                        Toast.makeText(BookingDetailActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BookingDetailActivity.this, "Lỗi khi hoàn thành công việc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(BookingDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Hủy công việc")
                .setMessage("Bạn có chắc chắn muốn hủy công việc này?")
                .setPositiveButton("Hủy việc", (dialog, which) -> {
                    performCancelBooking();
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void performCancelBooking() {
        showLoading(true);

        apiService.cancelBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        Toast.makeText(BookingDetailActivity.this, "Đã hủy công việc!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(BookingDetailActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BookingDetailActivity.this, "Lỗi khi hủy công việc", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);
                Toast.makeText(BookingDetailActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}

