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
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
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
    private View tvEmpty;
    private ApiService apiService;
    private com.example.partner_ftask.utils.PreferenceManager preferenceManager;

    private static final String KEY_CURRENT_STATUS = "current_status";
    private static final String KEY_CURRENT_TAB = "current_tab";

    private String currentStatus = "ALL";
    private int currentTabPosition = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_jobs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Restore saved state
        if (savedInstanceState != null) {
            currentStatus = savedInstanceState.getString(KEY_CURRENT_STATUS, "ALL");
            currentTabPosition = savedInstanceState.getInt(KEY_CURRENT_TAB, 0);
            android.util.Log.d("MyJobsFragment", "📦 Restored state: tab=" + currentTabPosition + ", status=" + currentStatus);
        }

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

        preferenceManager = new com.example.partner_ftask.utils.PreferenceManager(requireContext());

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

        // Restore saved tab position or select first tab
        if (currentTabPosition >= 0 && currentTabPosition < tabLayout.getTabCount()) {
            tabLayout.selectTab(tabLayout.getTabAt(currentTabPosition));
        } else if (tabLayout.getSelectedTabPosition() == -1) {
            tabLayout.selectTab(tabLayout.getTabAt(0));
            currentStatus = "ALL";
            currentTabPosition = 0;
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                currentTabPosition = position;

                switch (position) {
                    case 0: currentStatus = "ALL"; break;
                    case 1: currentStatus = "JOINED"; break;
                    case 2: currentStatus = "WORKING"; break;
                    case 3: currentStatus = "COMPLETED"; break;
                    default: currentStatus = "ALL"; break;
                }

                android.util.Log.d("MyJobsFragment", "Tab: " + position + " Status: " + currentStatus);
                loadMyJobs();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                loadMyJobs();
            }
        });
    }

    private void loadMyJobs() {
        // Sync currentStatus with currently selected tab
        if (tabLayout != null) {
            int selectedTabPosition = tabLayout.getSelectedTabPosition();
            switch (selectedTabPosition) {
                case 0: currentStatus = "ALL"; break;
                case 1: currentStatus = "JOINED"; break;
                case 2: currentStatus = "WORKING"; break;
                case 3: currentStatus = "COMPLETED"; break;
                default: currentStatus = "ALL"; break;
            }
        }

        showLoading(true);
        tvEmpty.setVisibility(View.GONE);

        // Get current partner ID
        int currentPartnerId = preferenceManager.getPartnerId();

        // Load bookings filtered by partner ID
        // Note: API uses 1-based pagination, not 0-based
        apiService.getBookings(1, 100, null, null, null, null, null, null, null, currentPartnerId, null)
                .enqueue(new Callback<ApiResponse<PageResponse<Booking>>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<PageResponse<Booking>>> call, Response<ApiResponse<PageResponse<Booking>>> response) {
                        if (!isAdded()) return;

                        showLoading(false);
                        swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<PageResponse<Booking>> apiResponse = response.body();
                            if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                                List<Booking> allBookings = apiResponse.getResult().getContent();
                                List<Booking> myBookings = filterMyBookings(allBookings);

                                if (myBookings != null && !myBookings.isEmpty()) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    tvEmpty.setVisibility(View.GONE);
                                    adapter.setBookings(myBookings);
                                } else {
                                    recyclerView.setVisibility(View.GONE);
                                    tvEmpty.setVisibility(View.VISIBLE);
                                    adapter.setBookings(new ArrayList<>());
                                }
                            } else {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        } else {
                            if (getContext() != null) {
                                Toast.makeText(getContext(), "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<PageResponse<Booking>>> call, Throwable t) {
                        if (!isAdded()) return;

                        showLoading(false);
                        swipeRefresh.setRefreshing(false);
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private List<Booking> filterMyBookings(List<Booking> bookings) {
        List<Booking> filtered = new ArrayList<>();

        if (bookings == null) return filtered;

        if (!isAdded() || preferenceManager == null) {
            return filtered;
        }

        int currentPartnerId = preferenceManager.getPartnerId();

        // If no partnerId, don't show any bookings
        if (currentPartnerId <= 0) {
            android.util.Log.w("MyJobsFragment", "No Partner ID! Cannot filter bookings.");
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
                        break;
                    }
                }

                // If current partner found in this booking
                if (currentPartner != null) {
                    String partnerStatus = currentPartner.getStatus();

                    // Filter based on selected tab
                    if ("ALL".equals(currentStatus)) {
                        filtered.add(booking);
                    } else if (currentStatus.equals(partnerStatus)) {
                        filtered.add(booking);
                    }
                }
            }
        }

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

        if (!isAdded() || preferenceManager == null) return;

        String token = preferenceManager.getAccessToken();
        android.util.Log.d("MyJobsFragment", "Token exists: " + (token != null && !token.isEmpty()));

        apiService.startBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                if (!isAdded()) return;

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
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Đã bắt đầu công việc!", Toast.LENGTH_SHORT).show();
                        }
                        loadMyJobs();
                    } else {
                        android.util.Log.e("MyJobsFragment", "❌ START FAILED - API Error Code: " + apiResponse.getCode());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    if (getContext() != null) {
                        String errorMessage = parseErrorMessage(response, "Lỗi khi bắt đầu");
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
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

                if (getContext() != null) {
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void completeBooking(int bookingId) {
        if (!isAdded() || preferenceManager == null) return;

        showLoading(true);

        android.util.Log.d("MyJobsFragment", "========== COMPLETE BOOKING REQUEST ==========");
        android.util.Log.d("MyJobsFragment", "Booking ID: " + bookingId);
        android.util.Log.d("MyJobsFragment", "Endpoint: POST /partners/bookings/" + bookingId + "/complete");

        String token = preferenceManager.getAccessToken();
        android.util.Log.d("MyJobsFragment", "Token exists: " + (token != null && !token.isEmpty()));

        apiService.completeBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                if (!isAdded()) return;

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
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Đã hoàn thành công việc!", Toast.LENGTH_SHORT).show();
                        }
                        loadMyJobs();
                    } else {
                        android.util.Log.e("MyJobsFragment", "❌ COMPLETE FAILED - API Error Code: " + apiResponse.getCode());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    if (getContext() != null) {
                        String errorMessage = parseErrorMessage(response, "Lỗi khi hoàn thành");
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
                android.util.Log.d("MyJobsFragment", "==========================================");
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                if (!isAdded()) return;

                showLoading(false);

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

                if (getContext() != null) {
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void cancelBooking(int bookingId) {
        if (!isAdded() || preferenceManager == null) return;

        showLoading(true);

        android.util.Log.d("MyJobsFragment", "========== CANCEL BOOKING REQUEST ==========");
        android.util.Log.d("MyJobsFragment", "Booking ID: " + bookingId);
        android.util.Log.d("MyJobsFragment", "Endpoint: POST /partners/bookings/" + bookingId + "/cancel");

        String token = preferenceManager.getAccessToken();
        android.util.Log.d("MyJobsFragment", "Token exists: " + (token != null && !token.isEmpty()));

        apiService.cancelBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                if (!isAdded()) return;

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
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Đã hủy công việc!", Toast.LENGTH_SHORT).show();
                        }
                        loadMyJobs();
                    } else {
                        android.util.Log.e("MyJobsFragment", "❌ CANCEL FAILED - API Error Code: " + apiResponse.getCode());
                        if (getContext() != null) {
                            Toast.makeText(getContext(), "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    if (getContext() != null) {
                        String errorMessage = parseErrorMessage(response, "Lỗi khi hủy");
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
                android.util.Log.d("MyJobsFragment", "==========================================");
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                if (!isAdded()) return;

                showLoading(false);

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

                if (getContext() != null) {
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private String parseErrorMessage(Response<?> response, String defaultMessage) {
        String errorMessage = defaultMessage + " (Code: " + response.code() + ")";
        
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                android.util.Log.e("MyJobsFragment", "Error Body: " + errorBody);
                
                Gson gson = new Gson();
                ApiResponse<?> errorResponse = gson.fromJson(errorBody, ApiResponse.class);
                
                if (errorResponse != null && errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty()) {
                    errorMessage = errorResponse.getMessage();
                }
            }
        } catch (IOException | JsonSyntaxException e) {
            android.util.Log.e("MyJobsFragment", "Failed to parse error body", e);
        }
        
        if (errorMessage.equals(defaultMessage + " (Code: " + response.code() + ")")) {
            if (response.code() == 401) {
                errorMessage = "Chưa đăng nhập hoặc token hết hạn!";
            } else if (response.code() == 403) {
                errorMessage = "Bạn không có quyền thực hiện thao tác này!";
            } else if (response.code() == 404) {
                errorMessage = "Không tìm thấy dữ liệu!";
            } else if (response.code() == 400) {
                errorMessage = "Yêu cầu không hợp lệ!";
            } else if (response.code() == 409) {
                errorMessage = "Dữ liệu đã được cập nhật hoặc không còn khả dụng!";
            } else if (response.code() == 500) {
                errorMessage = "Lỗi server! Vui lòng thử lại sau.";
            }
        }
        
        return errorMessage;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_CURRENT_STATUS, currentStatus);
        outState.putInt(KEY_CURRENT_TAB, currentTabPosition);
        android.util.Log.d("MyJobsFragment", "💾 Saving state: tab=" + currentTabPosition + ", status=" + currentStatus);
    }
}

