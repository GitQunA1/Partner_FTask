package com.example.partner_ftask.ui.fragment;

import android.app.AlertDialog;
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
import com.example.partner_ftask.data.model.BookingPartner;
import com.example.partner_ftask.data.model.PageResponse;
import com.example.partner_ftask.ui.activity.BookingDetailActivity;
import com.example.partner_ftask.ui.adapter.MyJobsAdapter;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyJobsFragment extends Fragment implements MyJobsAdapter.OnJobActionListener {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private MyJobsAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private ApiService apiService;

    private String currentStatus = "ALL";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_jobs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        tabLayout = view.findViewById(R.id.tab_layout);
        recyclerView = view.findViewById(R.id.recycler_view_my_jobs);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);

        // Setup RecyclerView
        adapter = new MyJobsAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Initialize API service
        apiService = ApiClient.getApiService();

        // Setup tabs
        setupTabs();

        // Setup swipe refresh
        swipeRefresh.setOnRefreshListener(() -> loadMyJobs());

        // Load data
        loadMyJobs();
    }

    private void setupTabs() {
        tabLayout.addTab(tabLayout.newTab().setText("Tất cả"));
        tabLayout.addTab(tabLayout.newTab().setText("Đã nhận"));
        tabLayout.addTab(tabLayout.newTab().setText("Đang làm"));
        tabLayout.addTab(tabLayout.newTab().setText("Hoàn thành"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                switch (position) {
                    case 0:
                        currentStatus = "ALL";
                        break;
                    case 1:
                        currentStatus = "JOINED";
                        break;
                    case 2:
                        currentStatus = "WORKING";
                        break;
                    case 3:
                        currentStatus = "COMPLETED";
                        break;
                }
                loadMyJobs();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void loadMyJobs() {
        showLoading(true);
        tvEmpty.setVisibility(View.GONE);

        // Load all bookings and filter by partner status
        // In a real app, you should have a dedicated API endpoint for partner's bookings
        // Note: API uses 1-based pagination, not 0-based
        apiService.getBookings(null, 1, 50, null, null, null, null, null)
                .enqueue(new Callback<ApiResponse<PageResponse<Booking>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PageResponse<Booking>>> call, Response<ApiResponse<PageResponse<Booking>>> response) {
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<PageResponse<Booking>> apiResponse = response.body();
                            if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                                List<Booking> allBookings = apiResponse.getResult().getContent();
                                List<Booking> myBookings = filterMyBookings(allBookings);

                                if (myBookings != null && !myBookings.isEmpty()) {
                                    // Có data → Hiện RecyclerView, ẩn empty text
                                    recyclerView.setVisibility(View.VISIBLE);
                                    tvEmpty.setVisibility(View.GONE);
                                    adapter.setBookings(myBookings);
                                } else {
                                    // Không có data → Ẩn RecyclerView, hiện empty text
                                    recyclerView.setVisibility(View.GONE);
                                    tvEmpty.setVisibility(View.VISIBLE);
                                    adapter.setBookings(new ArrayList<>());  // Clear adapter
                                }
                            } else {
                                Toast.makeText(requireContext(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(requireContext(), "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PageResponse<Booking>>> call, Throwable t) {
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private List<Booking> filterMyBookings(List<Booking> bookings) {
        List<Booking> filtered = new ArrayList<>();

        if (bookings == null) return filtered;

        // Get current partner ID
        com.example.partner_ftask.utils.PreferenceManager prefManager =
            new com.example.partner_ftask.utils.PreferenceManager(requireContext());
        int currentPartnerId = prefManager.getPartnerId();

        android.util.Log.d("MyJobsFragment", "Filter - Current Partner ID: " + currentPartnerId);
        android.util.Log.d("MyJobsFragment", "Filter - Current Status Filter: " + currentStatus);
        android.util.Log.d("MyJobsFragment", "Filter - Total bookings to check: " + bookings.size());

        // ⚠️ IMPORTANT: If no partnerId, don't show any bookings
        if (currentPartnerId <= 0) {
            android.util.Log.w("MyJobsFragment", "⚠️ No Partner ID found! Cannot filter bookings.");
            android.util.Log.w("MyJobsFragment", "Please implement proper authentication to get Partner ID from token.");
            return filtered;  // Return empty list
        }

        for (Booking booking : bookings) {
            // Check if this booking has partners
            if (booking.getPartners() != null && !booking.getPartners().isEmpty()) {
                // Find current partner in this booking by matching Partner ID
                BookingPartner currentPartner = null;
                for (BookingPartner bp : booking.getPartners()) {
                    if (bp.getPartner() != null && bp.getPartner().getId() == currentPartnerId) {
                        currentPartner = bp;
                        android.util.Log.d("MyJobsFragment", "✅ Found current partner in booking #" + booking.getId());
                        break;
                    }
                }

                // ❌ REMOVED FALLBACK - Don't assume first partner is current user
                // This was causing the bug where different partners see the same bookings

                // If current partner found in this booking
                if (currentPartner != null) {
                    String partnerStatus = currentPartner.getStatus();

                    android.util.Log.d("MyJobsFragment", "Booking #" + booking.getId() +
                        " - Partner Status: " + partnerStatus +
                        " - Service: " + (booking.getVariant() != null ? booking.getVariant().getName() : "N/A"));

                    // Filter based on selected tab
                    if ("ALL".equals(currentStatus)) {
                        // Show all bookings where current partner is involved
                        filtered.add(booking);
                    } else {
                        // Filter by specific partner status
                        if (currentStatus.equals(partnerStatus)) {
                            filtered.add(booking);
                            android.util.Log.d("MyJobsFragment", "  ✅ Added to filtered list");
                        } else {
                            android.util.Log.d("MyJobsFragment", "  ❌ Skipped (status doesn't match)");
                        }
                    }
                }
            }
        }

        android.util.Log.d("MyJobsFragment", "Filter result: " + filtered.size() + " bookings");
        return filtered;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onJobClick(Booking booking) {
        // Open detail activity
        Intent intent = new Intent(requireContext(), BookingDetailActivity.class);
        intent.putExtra("booking_id", booking.getId());
        startActivity(intent);
    }

    @Override
    public void onStartJob(Booking booking) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Bắt đầu công việc")
                .setMessage("Bạn có muốn bắt đầu công việc này không?")
                .setPositiveButton("Bắt đầu", (dialog, which) -> {
                    startBooking(booking.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onCompleteJob(Booking booking) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hoàn thành công việc")
                .setMessage("Bạn có chắc chắn đã hoàn thành công việc này?")
                .setPositiveButton("Hoàn thành", (dialog, which) -> {
                    completeBooking(booking.getId());
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onCancelJob(Booking booking) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Hủy công việc")
                .setMessage("Bạn có chắc chắn muốn hủy công việc này?")
                .setPositiveButton("Hủy việc", (dialog, which) -> {
                    cancelBooking(booking.getId());
                })
                .setNegativeButton("Đóng", null)
                .show();
    }

    private void startBooking(int bookingId) {
        showLoading(true);

        // Log request details
        android.util.Log.d("MyJobsFragment", "========== START BOOKING REQUEST ==========");
        android.util.Log.d("MyJobsFragment", "Booking ID: " + bookingId);
        android.util.Log.d("MyJobsFragment", "Endpoint: POST /partners/bookings/" + bookingId + "/start");

        // Check token
        com.example.partner_ftask.utils.PreferenceManager prefManager =
            new com.example.partner_ftask.utils.PreferenceManager(requireContext());
        String token = prefManager.getAccessToken();
        android.util.Log.d("MyJobsFragment", "Token exists: " + (token != null && !token.isEmpty()));

        apiService.startBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                showLoading(false);

                // Log response details
                android.util.Log.d("MyJobsFragment", "========== START BOOKING RESPONSE ==========");
                android.util.Log.d("MyJobsFragment", "HTTP Status Code: " + response.code());
                android.util.Log.d("MyJobsFragment", "Response Success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    android.util.Log.d("MyJobsFragment", "API Code: " + apiResponse.getCode());
                    android.util.Log.d("MyJobsFragment", "API Message: " + apiResponse.getMessage());

                    if (apiResponse.getResult() != null) {
                        Booking booking = apiResponse.getResult();
                        android.util.Log.d("MyJobsFragment", "Booking Status: " + booking.getStatus());
                        if (booking.getPartners() != null && !booking.getPartners().isEmpty()) {
                            android.util.Log.d("MyJobsFragment", "Partner Status: " + booking.getPartners().get(0).getStatus());
                        }
                    }

                    if (apiResponse.getCode() == 200) {
                        android.util.Log.d("MyJobsFragment", "✅ START SUCCESS!");
                        Toast.makeText(requireContext(), "Đã bắt đầu công việc!", Toast.LENGTH_SHORT).show();
                        loadMyJobs();
                    } else {
                        android.util.Log.e("MyJobsFragment", "❌ START FAILED - API Error Code: " + apiResponse.getCode());
                        Toast.makeText(requireContext(), "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Get error body
                    String errorBody = "No error body";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            android.util.Log.e("MyJobsFragment", "Error Body: " + errorBody);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("MyJobsFragment", "Failed to read error body", e);
                    }

                    android.util.Log.e("MyJobsFragment", "❌ START FAILED - HTTP " + response.code());

                    String errorMessage = "Lỗi khi bắt đầu (Code: " + response.code() + ")";
                    if (response.code() == 401) {
                        errorMessage = "Chưa đăng nhập hoặc token hết hạn!";
                    } else if (response.code() == 403) {
                        errorMessage = "Bạn không có quyền bắt đầu việc này!";
                    } else if (response.code() == 404) {
                        errorMessage = "Không tìm thấy booking!";
                    } else if (response.code() == 400) {
                        errorMessage = "Trạng thái không hợp lệ để bắt đầu! (Phải JOINED trước)";
                    }

                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
                android.util.Log.d("MyJobsFragment", "==========================================");
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);

                // Log failure details
                android.util.Log.e("MyJobsFragment", "========== START BOOKING FAILED ==========");
                android.util.Log.e("MyJobsFragment", "Error Type: " + t.getClass().getSimpleName());
                android.util.Log.e("MyJobsFragment", "Error Message: " + t.getMessage());
                android.util.Log.e("MyJobsFragment", "❌ Network/Connection Error", t);
                android.util.Log.e("MyJobsFragment", "==========================================");

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

    private void completeBooking(int bookingId) {
        showLoading(true);

        // Log request details
        android.util.Log.d("MyJobsFragment", "========== COMPLETE BOOKING REQUEST ==========");
        android.util.Log.d("MyJobsFragment", "Booking ID: " + bookingId);
        android.util.Log.d("MyJobsFragment", "Endpoint: POST /partners/bookings/" + bookingId + "/complete");

        // Check token
        com.example.partner_ftask.utils.PreferenceManager prefManager =
            new com.example.partner_ftask.utils.PreferenceManager(requireContext());
        String token = prefManager.getAccessToken();
        android.util.Log.d("MyJobsFragment", "Token exists: " + (token != null && !token.isEmpty()));

        apiService.completeBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                showLoading(false);

                // Log response details
                android.util.Log.d("MyJobsFragment", "========== COMPLETE BOOKING RESPONSE ==========");
                android.util.Log.d("MyJobsFragment", "HTTP Status Code: " + response.code());
                android.util.Log.d("MyJobsFragment", "Response Success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    android.util.Log.d("MyJobsFragment", "API Code: " + apiResponse.getCode());
                    android.util.Log.d("MyJobsFragment", "API Message: " + apiResponse.getMessage());

                    if (apiResponse.getResult() != null) {
                        Booking booking = apiResponse.getResult();
                        android.util.Log.d("MyJobsFragment", "Booking Status: " + booking.getStatus());
                        if (booking.getPartners() != null && !booking.getPartners().isEmpty()) {
                            android.util.Log.d("MyJobsFragment", "Partner Status: " + booking.getPartners().get(0).getStatus());
                        }
                    }

                    if (apiResponse.getCode() == 200) {
                        android.util.Log.d("MyJobsFragment", "✅ COMPLETE SUCCESS!");
                        Toast.makeText(requireContext(), "Đã hoàn thành công việc!", Toast.LENGTH_SHORT).show();
                        loadMyJobs();
                    } else {
                        android.util.Log.e("MyJobsFragment", "❌ COMPLETE FAILED - API Error Code: " + apiResponse.getCode());
                        Toast.makeText(requireContext(), "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Get error body
                    String errorBody = "No error body";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            android.util.Log.e("MyJobsFragment", "Error Body: " + errorBody);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("MyJobsFragment", "Failed to read error body", e);
                    }

                    android.util.Log.e("MyJobsFragment", "❌ COMPLETE FAILED - HTTP " + response.code());

                    String errorMessage = "Lỗi khi hoàn thành (Code: " + response.code() + ")";
                    if (response.code() == 401) {
                        errorMessage = "Chưa đăng nhập hoặc token hết hạn!";
                    } else if (response.code() == 403) {
                        errorMessage = "Bạn không có quyền hoàn thành việc này!";
                    } else if (response.code() == 404) {
                        errorMessage = "Không tìm thấy booking!";
                    } else if (response.code() == 400) {
                        errorMessage = "Trạng thái không hợp lệ! (Phải WORKING trước)";
                    }

                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
                android.util.Log.d("MyJobsFragment", "==========================================");
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);

                // Log failure details
                android.util.Log.e("MyJobsFragment", "========== COMPLETE BOOKING FAILED ==========");
                android.util.Log.e("MyJobsFragment", "Error Type: " + t.getClass().getSimpleName());
                android.util.Log.e("MyJobsFragment", "Error Message: " + t.getMessage());
                android.util.Log.e("MyJobsFragment", "❌ Network/Connection Error", t);
                android.util.Log.e("MyJobsFragment", "==========================================");

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

    private void cancelBooking(int bookingId) {
        showLoading(true);

        // Log request details
        android.util.Log.d("MyJobsFragment", "========== CANCEL BOOKING REQUEST ==========");
        android.util.Log.d("MyJobsFragment", "Booking ID: " + bookingId);
        android.util.Log.d("MyJobsFragment", "Endpoint: POST /partners/bookings/" + bookingId + "/cancel");

        // Check token
        com.example.partner_ftask.utils.PreferenceManager prefManager =
            new com.example.partner_ftask.utils.PreferenceManager(requireContext());
        String token = prefManager.getAccessToken();
        android.util.Log.d("MyJobsFragment", "Token exists: " + (token != null && !token.isEmpty()));

        apiService.cancelBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                showLoading(false);

                // Log response details
                android.util.Log.d("MyJobsFragment", "========== CANCEL BOOKING RESPONSE ==========");
                android.util.Log.d("MyJobsFragment", "HTTP Status Code: " + response.code());
                android.util.Log.d("MyJobsFragment", "Response Success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    android.util.Log.d("MyJobsFragment", "API Code: " + apiResponse.getCode());
                    android.util.Log.d("MyJobsFragment", "API Message: " + apiResponse.getMessage());

                    if (apiResponse.getResult() != null) {
                        Booking booking = apiResponse.getResult();
                        android.util.Log.d("MyJobsFragment", "Booking Status: " + booking.getStatus());
                        if (booking.getPartners() != null && !booking.getPartners().isEmpty()) {
                            android.util.Log.d("MyJobsFragment", "Partner Status: " + booking.getPartners().get(0).getStatus());
                        }
                    }

                    if (apiResponse.getCode() == 200) {
                        android.util.Log.d("MyJobsFragment", "✅ CANCEL SUCCESS!");
                        Toast.makeText(requireContext(), "Đã hủy công việc!", Toast.LENGTH_SHORT).show();
                        loadMyJobs();
                    } else {
                        android.util.Log.e("MyJobsFragment", "❌ CANCEL FAILED - API Error Code: " + apiResponse.getCode());
                        Toast.makeText(requireContext(), "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Get error body
                    String errorBody = "No error body";
                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            android.util.Log.e("MyJobsFragment", "Error Body: " + errorBody);
                        }
                    } catch (Exception e) {
                        android.util.Log.e("MyJobsFragment", "Failed to read error body", e);
                    }

                    android.util.Log.e("MyJobsFragment", "❌ CANCEL FAILED - HTTP " + response.code());

                    String errorMessage = "Lỗi khi hủy (Code: " + response.code() + ")";
                    if (response.code() == 401) {
                        errorMessage = "Chưa đăng nhập hoặc token hết hạn!";
                    } else if (response.code() == 403) {
                        errorMessage = "Bạn không có quyền hủy việc này!";
                    } else if (response.code() == 404) {
                        errorMessage = "Không tìm thấy booking!";
                    } else if (response.code() == 400) {
                        errorMessage = "Không thể hủy ở trạng thái hiện tại!";
                    }

                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
                android.util.Log.d("MyJobsFragment", "==========================================");
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                showLoading(false);

                // Log failure details
                android.util.Log.e("MyJobsFragment", "========== CANCEL BOOKING FAILED ==========");
                android.util.Log.e("MyJobsFragment", "Error Type: " + t.getClass().getSimpleName());
                android.util.Log.e("MyJobsFragment", "Error Message: " + t.getMessage());
                android.util.Log.e("MyJobsFragment", "❌ Network/Connection Error", t);
                android.util.Log.e("MyJobsFragment", "==========================================");

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
        loadMyJobs();
    }
}

