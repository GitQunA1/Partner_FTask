package com.example.partner_ftask.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.partner_ftask.R;
import com.example.partner_ftask.data.api.ApiClient;
import com.example.partner_ftask.data.api.ApiService;
import com.example.partner_ftask.data.model.ApiResponse;
import com.example.partner_ftask.data.model.Booking;
import com.example.partner_ftask.data.model.PageResponse;
import com.example.partner_ftask.ui.activity.BookingDetailActivity;
import com.example.partner_ftask.ui.adapter.BookingAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobsFragment extends Fragment implements BookingAdapter.OnBookingClickListener {

    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_jobs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view_jobs);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);

        // Setup RecyclerView
        adapter = new BookingAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Initialize API service
        apiService = ApiClient.getApiService();

        // Setup swipe refresh
        swipeRefresh.setOnRefreshListener(() -> loadAvailableJobs());

        // Check token before loading
        checkTokenAndLoadData();
    }

    private void checkTokenAndLoadData() {
        com.example.partner_ftask.utils.PreferenceManager prefManager =
            new com.example.partner_ftask.utils.PreferenceManager(requireContext());
        String token = prefManager.getAccessToken();

        android.util.Log.d("JobsFragment", "Token exists: " + (token != null && !token.isEmpty()));
        android.util.Log.d("JobsFragment", "Token length: " + (token != null ? token.length() : 0));

        if (token == null || token.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            tvEmpty.setText("Vui lòng đăng nhập để xem công việc");
            Toast.makeText(requireContext(),
                "Bạn chưa đăng nhập! Vui lòng vào tab Cá nhân để đăng nhập.",
                Toast.LENGTH_LONG).show();
        } else {
            // Load data
            loadAvailableJobs();
        }
    }

    private void loadAvailableJobs() {
        showLoading(true);
        tvEmpty.setVisibility(View.GONE);

        // Load ALL bookings, then filter for PENDING and PARTIALLY_ACCEPTED on client side
        // Note: API uses 1-based pagination, not 0-based
        // We pass null for status to get all bookings, then filter below
        apiService.getBookings(null, 1, 50, null, null, null, null, null)
                .enqueue(new Callback<ApiResponse<PageResponse<Booking>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PageResponse<Booking>>> call, Response<ApiResponse<PageResponse<Booking>>> response) {
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);

                        // Log response for debugging
                        android.util.Log.d("JobsFragment", "Response Code: " + response.code());
                        android.util.Log.d("JobsFragment", "Response Success: " + response.isSuccessful());

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<PageResponse<Booking>> apiResponse = response.body();
                            android.util.Log.d("JobsFragment", "API Code: " + apiResponse.getCode());
                            android.util.Log.d("JobsFragment", "API Message: " + apiResponse.getMessage());

                            if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                                List<Booking> allBookings = apiResponse.getResult().getContent();
                                android.util.Log.d("JobsFragment", "Total bookings from API: " + (allBookings != null ? allBookings.size() : 0));

                                // Filter: Chỉ hiển thị bookings có status PENDING hoặc PARTIALLY_ACCEPTED
                                List<Booking> availableBookings = filterAvailableBookings(allBookings);
                                android.util.Log.d("JobsFragment", "Available bookings (PENDING/PARTIALLY_ACCEPTED): " + availableBookings.size());

                                if (availableBookings != null && !availableBookings.isEmpty()) {
                                    // Có data → Hiện RecyclerView, ẩn empty text
                                    recyclerView.setVisibility(View.VISIBLE);
                                    tvEmpty.setVisibility(View.GONE);
                                    adapter.setBookings(availableBookings);
                                } else {
                                    // Không có data → Ẩn RecyclerView, hiện empty text
                                    recyclerView.setVisibility(View.GONE);
                                    tvEmpty.setVisibility(View.VISIBLE);
                                    adapter.setBookings(new ArrayList<>());  // Clear adapter
                                    Toast.makeText(requireContext(), "Không có công việc khả dụng", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Lỗi không xác định";
                                Toast.makeText(requireContext(), "Lỗi: " + errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            // Get error details
                            String errorBody = "Unknown error";
                            try {
                                if (response.errorBody() != null) {
                                    errorBody = response.errorBody().string();
                                    android.util.Log.e("JobsFragment", "Error Body: " + errorBody);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            String errorMessage = "Lỗi khi tải dữ liệu (Code: " + response.code() + ")";
                            if (response.code() == 401) {
                                errorMessage = "Lỗi xác thực! Vui lòng đăng nhập lại.";
                            } else if (response.code() == 404) {
                                errorMessage = "Không tìm thấy API endpoint!";
                            } else if (response.code() == 500) {
                                errorMessage = "Lỗi server! Vui lòng thử lại sau.";
                            }

                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PageResponse<Booking>>> call, Throwable t) {
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);

                        // Log error details
                        android.util.Log.e("JobsFragment", "API Call Failed", t);

                        String errorMessage = "Lỗi kết nối: ";
                        if (t instanceof java.net.UnknownHostException) {
                            errorMessage += "Không thể kết nối đến server. Kiểm tra internet!";
                        } else if (t instanceof java.net.SocketTimeoutException) {
                            errorMessage += "Timeout! Server không phản hồi.";
                        } else {
                            errorMessage += t.getMessage();
                        }

                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    /**
     * Filter bookings để chỉ hiển thị những booking còn available (PENDING hoặc PARTIALLY_ACCEPTED)
     * PENDING: Chưa có partner nào nhận
     * PARTIALLY_ACCEPTED: Đã có partner nhận nhưng chưa đủ số lượng yêu cầu
     */
    private List<Booking> filterAvailableBookings(List<Booking> bookings) {
        List<Booking> filtered = new ArrayList<>();

        if (bookings == null) {
            return filtered;
        }

        for (Booking booking : bookings) {
            String status = booking.getStatus();

            // Chỉ thêm vào list nếu status là PENDING hoặc PARTIALLY_ACCEPTED
            if ("PENDING".equals(status) || "PARTIALLY_ACCEPTED".equals(status)) {
                filtered.add(booking);
                android.util.Log.d("JobsFragment", "✅ Booking #" + booking.getId() +
                    " - Status: " + status +
                    " - Service: " + (booking.getVariant() != null ? booking.getVariant().getName() : "N/A") +
                    " - Added to available list");
            } else {
                android.util.Log.d("JobsFragment", "❌ Booking #" + booking.getId() +
                    " - Status: " + status + " - Skipped (not available)");
            }
        }

        return filtered;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBookingClick(Booking booking) {
        // Open detail activity
        Intent intent = new Intent(requireContext(), BookingDetailActivity.class);
        intent.putExtra("booking_id", booking.getId());
        startActivity(intent);
    }

    @Override
    public void onActionClick(Booking booking) {
        // Claim the booking
        claimBooking(booking.getId());
    }

    private void claimBooking(int bookingId) {
        showLoading(true);

        // Log request details
        android.util.Log.d("JobsFragment", "========== CLAIM BOOKING REQUEST ==========");
        android.util.Log.d("JobsFragment", "Booking ID: " + bookingId);
        android.util.Log.d("JobsFragment", "Endpoint: POST /partners/bookings/" + bookingId + "/claim");

        // Check token
        com.example.partner_ftask.utils.PreferenceManager prefManager =
            new com.example.partner_ftask.utils.PreferenceManager(requireContext());
        String token = prefManager.getAccessToken();
        android.util.Log.d("JobsFragment", "Token exists: " + (token != null && !token.isEmpty()));
        if (token != null && token.length() > 20) {
            android.util.Log.d("JobsFragment", "Token preview: " + token.substring(0, 20) + "...");
        }

        apiService.claimBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                showLoading(false);

                // Log response details
                android.util.Log.d("JobsFragment", "========== CLAIM BOOKING RESPONSE ==========");
                android.util.Log.d("JobsFragment", "HTTP Status Code: " + response.code());
                android.util.Log.d("JobsFragment", "Response Success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    android.util.Log.d("JobsFragment", "API Code: " + apiResponse.getCode());
                    android.util.Log.d("JobsFragment", "API Message: " + apiResponse.getMessage());

                    if (apiResponse.getResult() != null) {
                        Booking booking = apiResponse.getResult();
                        android.util.Log.d("JobsFragment", "Booking Status: " + booking.getStatus());
                        android.util.Log.d("JobsFragment", "Partners Count: " + (booking.getPartners() != null ? booking.getPartners().size() : 0));
                    }

                    if (apiResponse.getCode() == 200) {
                        android.util.Log.d("JobsFragment", "✅ CLAIM SUCCESS!");
                        Toast.makeText(requireContext(), "Nhận việc thành công!", Toast.LENGTH_SHORT).show();
                        loadAvailableJobs(); // Reload list
                    } else {
                        android.util.Log.e("JobsFragment", "❌ CLAIM FAILED - API Error Code: " + apiResponse.getCode());
                        Toast.makeText(requireContext(), "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Get error body
                    String errorBody = "No error body";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            android.util.Log.e("JobsFragment", "Error Body: " + errorBody);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("JobsFragment", "Failed to read error body", e);
                    }

                    android.util.Log.e("JobsFragment", "❌ CLAIM FAILED - HTTP " + response.code());

                    String errorMessage = "Lỗi khi nhận việc (Code: " + response.code() + ")";
                    if (response.code() == 401) {
                        errorMessage = "Chưa đăng nhập hoặc token hết hạn!";
                    } else if (response.code() == 403) {
                        errorMessage = "Bạn không có quyền nhận việc này!";
                    } else if (response.code() == 404) {
                        errorMessage = "Không tìm thấy booking này!";
                    } else if (response.code() == 409) {
                        errorMessage = "Booking đã được nhận hoặc không còn khả dụng!";
                    }

                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
                android.util.Log.d("JobsFragment", "==========================================");
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);

                // Log failure details
                android.util.Log.e("JobsFragment", "========== CLAIM BOOKING FAILED ==========");
                android.util.Log.e("JobsFragment", "Error Type: " + t.getClass().getSimpleName());
                android.util.Log.e("JobsFragment", "Error Message: " + t.getMessage());
                android.util.Log.e("JobsFragment", "❌ Network/Connection Error", t);
                android.util.Log.e("JobsFragment", "==========================================");

                String errorMessage = "Lỗi kết nối: ";
                if (t instanceof java.net.UnknownHostException) {
                    errorMessage += "Không thể kết nối server!";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage += "Timeout!";
                } else {
                    errorMessage += t.getMessage();
                }

                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload when returning to this fragment
        loadAvailableJobs();
    }
}

