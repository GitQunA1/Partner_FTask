package com.example.partner_ftask.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.io.IOException;
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
    private TextView tvPageInfo;
    private MaterialButton btnPrevPage;
    private MaterialButton btnNextPage;
    private MaterialButton btnApplyFilter;
    private ChipGroup chipGroupFilter;
    private Chip chipAll, chipPending, chipNearby;
    private Spinner spinnerSort;
    private View layoutEmpty;
    private View layoutPagination;

    private ApiService apiService;

    // Pagination
    private int currentPage = 1;
    private int totalPages = 1;
    private int totalElements = 0;
    private final int pageSize = 10;

    // Filter parameters (according to API documentation)
    private String currentStatus = null;      // Status filter: null = all, "PENDING", "COMPLETED", etc.
    private String fromDate = null;           // Filter from date (ISO 8601)
    private String toDate = null;             // Filter to date (ISO 8601)
    private Double minPrice = null;           // Min price filter
    private Double maxPrice = null;           // Max price filter
    private String addressFilter = null;      // Address search filter
    private Integer customerIdFilter = null;  // Customer ID filter
    private Integer partnerIdFilter = null;   // Partner ID filter
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
        tvPageInfo = view.findViewById(R.id.tv_page_info);
        btnPrevPage = view.findViewById(R.id.btn_prev_page);
        btnNextPage = view.findViewById(R.id.btn_next_page);
        btnApplyFilter = view.findViewById(R.id.btn_apply_filter);
        chipGroupFilter = view.findViewById(R.id.chip_group_filter);
        chipAll = view.findViewById(R.id.chip_all);
        chipPending = view.findViewById(R.id.chip_pending);
        chipNearby = view.findViewById(R.id.chip_nearby);
        spinnerSort = view.findViewById(R.id.spinner_sort);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        layoutPagination = view.findViewById(R.id.layout_pagination);
        Button btnRefresh = view.findViewById(R.id.btn_refresh);

        // Setup RecyclerView
        adapter = new BookingAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Initialize API service
        apiService = ApiClient.getApiService();

        // Setup sort spinner
        setupSortSpinner();

        // Setup filter chips
        setupFilterChips();

        // Setup pagination buttons
        setupPaginationButtons();

        // Setup swipe refresh
        swipeRefresh.setOnRefreshListener(() -> loadJobs());

        // Setup refresh button in empty state
        btnRefresh.setOnClickListener(v -> {
            currentPage = 1;
            loadJobs();
        });

        // Setup apply filter button
        btnApplyFilter.setOnClickListener(v -> {
            currentPage = 1; // Reset to page 1 when filter changes
            loadJobs();
        });

        // Check token before loading
        checkTokenAndLoadData();
    }

    private void setupSortSpinner() {
        // Note: Current API does not support sorting parameters
        // Keeping spinner for future enhancement or client-side sorting
        String[] sortOptions = {
            "Mặc định",
            "Sắp xếp (chưa hỗ trợ)"
        };

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            sortOptions
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(spinnerAdapter);

        // Disable spinner since API doesn't support sort yet
        spinnerSort.setEnabled(false);
        spinnerSort.setAlpha(0.5f);
    }

    private void setupFilterChips() {
        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chip_all)) {
                // Show all available statuses (PENDING, PARTIALLY_ACCEPTED)
                // Can use comma separated: "PENDING,PARTIALLY_ACCEPTED"
                currentStatus = "PENDING,PARTIALLY_ACCEPTED";
            } else if (checkedIds.contains(R.id.chip_pending)) {
                // Only PENDING status
                currentStatus = "PENDING";
            } else if (checkedIds.contains(R.id.chip_nearby)) {
                // TODO: Implement nearby filter with address parameter
                Toast.makeText(requireContext(), "Chức năng 'Gần tôi' đang phát triển", Toast.LENGTH_SHORT).show();
                chipAll.setChecked(true);
            }
        });
    }

    private void setupPaginationButtons() {
        btnPrevPage.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                loadJobs();
            }
        });

        btnNextPage.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadJobs();
            }
        });
    }

    private void updatePaginationUI() {
        // Update page info text
        tvPageInfo.setText("Trang " + currentPage + "/" + totalPages + " (" + totalElements + " việc)");

        // Enable/disable buttons
        btnPrevPage.setEnabled(currentPage > 1);
        btnNextPage.setEnabled(currentPage < totalPages);

        // Show/hide pagination layout
        if (totalPages > 1) {
            layoutPagination.setVisibility(View.VISIBLE);
        } else {
            layoutPagination.setVisibility(View.GONE);
        }
    }

    private void checkTokenAndLoadData() {
        com.example.partner_ftask.utils.PreferenceManager prefManager =
            new com.example.partner_ftask.utils.PreferenceManager(requireContext());
        String token = prefManager.getAccessToken();

        android.util.Log.d("JobsFragment", "Token exists: " + (token != null && !token.isEmpty()));

        if (token == null || token.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            tvEmpty.setText("Vui lòng đăng nhập để xem công việc");
            Toast.makeText(requireContext(),
                "Bạn chưa đăng nhập! Vui lòng vào tab Cá nhân để đăng nhập.",
                Toast.LENGTH_LONG).show();
        } else {
            loadJobs();
        }
    }

    private void loadJobs() {
        showLoading(true);
        layoutEmpty.setVisibility(View.GONE);

        android.util.Log.d("JobsFragment", "========== LOAD JOBS ==========");
        android.util.Log.d("JobsFragment", "Page: " + currentPage);
        android.util.Log.d("JobsFragment", "Page Size: " + pageSize);
        android.util.Log.d("JobsFragment", "Status Filter: " + currentStatus);

        // Call API according to documentation:
        // GET /bookings?page={page}&size={size}&status={status}&fromDate={fromDate}&toDate={toDate}
        //              &minPrice={minPrice}&maxPrice={maxPrice}&address={address}
        //              &customerId={customerId}&partnerId={partnerId}
        apiService.getBookings(
            currentPage,            // page (Integer, starts from 1)
            pageSize,               // size (Integer, items per page)
            currentStatus,          // status (String, comma separated)
            null,                   // fromDate (String, ISO 8601)
            null,                   // toDate (String, ISO 8601)
            null,                   // minPrice (Double)
            null,                   // maxPrice (Double)
            null,                   // address (String)
            null,                   // customerId (Integer)
            null                    // partnerId (Integer)
        ).enqueue(new Callback<ApiResponse<PageResponse<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<PageResponse<Booking>>> call, Response<ApiResponse<PageResponse<Booking>>> response) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);

                android.util.Log.d("JobsFragment", "Response Code: " + response.code());
                android.util.Log.d("JobsFragment", "Response Success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<PageResponse<Booking>> apiResponse = response.body();

                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        PageResponse<Booking> pageResponse = apiResponse.getResult();
                        List<Booking> bookings = pageResponse.getContent();

                        // Update pagination info
                        totalPages = pageResponse.getTotalPages();
                        totalElements = (int) pageResponse.getTotalElements();
                        // PageResponse uses 0-based page number, convert to 1-based for display
                        currentPage = pageResponse.getPageNumber() + 1; // Convert to 1-based

                        android.util.Log.d("JobsFragment", "Total bookings: " + totalElements);
                        android.util.Log.d("JobsFragment", "Total pages: " + totalPages);
                        android.util.Log.d("JobsFragment", "Current page: " + currentPage);

                        updatePaginationUI();

                        // Filter for available bookings (PENDING or PARTIALLY_ACCEPTED)
                        List<Booking> availableBookings = filterAvailableBookings(bookings);

                        if (!availableBookings.isEmpty()) {
                            recyclerView.setVisibility(View.VISIBLE);
                            layoutEmpty.setVisibility(View.GONE);
                            adapter.setBookings(availableBookings);
                        } else {
                            recyclerView.setVisibility(View.GONE);
                            layoutEmpty.setVisibility(View.VISIBLE);
                            tvEmpty.setText("Không có công việc khả dụng");
                            adapter.setBookings(new ArrayList<>());
                        }
                    } else {
                        String errorMsg = apiResponse.getMessage() != null ? apiResponse.getMessage() : "Lỗi không xác định";
                        layoutEmpty.setVisibility(View.VISIBLE);
                        recyclerView.setVisibility(View.GONE);
                        tvEmpty.setText("Lỗi: " + errorMsg);
                        Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Handle error response
                    String errorMessage = "Lỗi tải dữ liệu (Code: " + response.code() + ")";
                    if (response.code() == 401) {
                        errorMessage = "Lỗi xác thực! Vui lòng đăng nhập lại.";
                    } else if (response.code() == 404) {
                        errorMessage = "Không tìm thấy API endpoint!";
                    }

                    layoutEmpty.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                    tvEmpty.setText(errorMessage);
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<PageResponse<Booking>>> call, Throwable t) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);

                android.util.Log.e("JobsFragment", "API Call Failed", t);

                String errorMessage = "Lỗi kết nối: ";
                if (t instanceof java.net.UnknownHostException) {
                    errorMessage += "Không thể kết nối đến server!";
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage += "Timeout!";
                } else {
                    errorMessage += t.getMessage();
                }

                layoutEmpty.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                tvEmpty.setText(errorMessage);
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Filter bookings để chỉ hiển thị những booking còn available (PENDING hoặc PARTIALLY_ACCEPTED)
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
                        loadJobs(); // Reload list
                    } else {
                        android.util.Log.e("JobsFragment", "❌ CLAIM FAILED - API Error Code: " + apiResponse.getCode());
                        Toast.makeText(requireContext(), "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Get error body
                    String errorBody = "No error body";
                    String parsedErrorMessage = null;

                    try {
                        if (response.errorBody() != null) {
                            errorBody = response.errorBody().string();
                            android.util.Log.e("JobsFragment", "Error Body: " + errorBody);

                            // Try to parse error message from JSON
                            try {
                                org.json.JSONObject errorJson = new org.json.JSONObject(errorBody);
                                if (errorJson.has("message")) {
                                    parsedErrorMessage = errorJson.getString("message");
                                }
                            } catch (org.json.JSONException jsonEx) {
                                android.util.Log.e("JobsFragment", "Failed to parse error JSON", jsonEx);
                            }
                        }
                    } catch (Exception e) {
                        android.util.Log.e("JobsFragment", "Failed to read error body", e);
                    }

                    android.util.Log.e("JobsFragment", "❌ CLAIM FAILED - HTTP " + response.code());

                    String errorMessage;
                    if (response.code() == 400) {
                        if (parsedErrorMessage != null && parsedErrorMessage.contains("Address")) {
                            errorMessage = "⚠️ Lỗi dữ liệu: Địa chỉ không tồn tại. Vui lòng liên hệ hỗ trợ.";
                        } else {
                            errorMessage = "Yêu cầu không hợp lệ!";
                        }
                    } else if (response.code() == 401) {
                        errorMessage = "Chưa đăng nhập hoặc token hết hạn!";
                    } else if (response.code() == 403) {
                        errorMessage = "Bạn không có quyền nhận việc này!";
                    } else if (response.code() == 404) {
                        errorMessage = "Không tìm thấy booking này!";
                    } else if (response.code() == 409) {
                        errorMessage = "Booking đã được nhận hoặc không còn khả dụng!";
                    } else {
                        errorMessage = "Lỗi khi nhận việc (Code: " + response.code() + ")";
                        if (parsedErrorMessage != null) {
                            errorMessage += "\n" + parsedErrorMessage;
                        }
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

    private String parseErrorMessage(Response<?> response, String defaultMessage) {
        String errorMessage = defaultMessage + " (Code: " + response.code() + ")";
        
        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();
                android.util.Log.e("JobsFragment", "Error Body: " + errorBody);
                
                Gson gson = new Gson();
                ApiResponse<?> errorResponse = gson.fromJson(errorBody, ApiResponse.class);
                
                if (errorResponse != null && errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty()) {
                    errorMessage = errorResponse.getMessage();
                }
            }
        } catch (IOException | JsonSyntaxException e) {
            android.util.Log.e("JobsFragment", "Failed to parse error body", e);
        }
        
        if (errorMessage.equals(defaultMessage + " (Code: " + response.code() + ")")) {
            if (response.code() == 401) {
                errorMessage = "Chưa đăng nhập hoặc token hết hạn!";
            } else if (response.code() == 403) {
                errorMessage = "Bạn không có quyền thực hiện thao tác này!";
            } else if (response.code() == 404) {
                errorMessage = "Không tìm thấy dữ liệu!";
            } else if (response.code() == 409) {
                errorMessage = "Dữ liệu đã được cập nhật hoặc không còn khả dụng!";
            } else if (response.code() == 500) {
                errorMessage = "Lỗi server! Vui lòng thử lại sau.";
            }
        }
        
        return errorMessage;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload when returning to this fragment
        loadJobs();
    }
}

