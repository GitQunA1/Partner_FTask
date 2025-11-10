package com.example.partner_ftask.ui.fragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.example.partner_ftask.utils.PreferenceManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class JobsFragment extends Fragment implements BookingAdapter.OnBookingClickListener {

    private RecyclerView recyclerView;
    private BookingAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private View tvEmpty;
    private ApiService apiService;
    private Button btnPrevious, btnNext, btnFromDate, btnToDate;
    private TextView tvPageInfo;

    // Use 1-based indexing for currentPage to align with the backend API
    private int currentPage = 1;
    private int totalPages = 1;
    private final int PAGE_SIZE = 5;

    private String fromDate, toDate;
    private final Calendar fromCalendar = Calendar.getInstance();
    private final Calendar toCalendar = Calendar.getInstance();

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
        btnPrevious = view.findViewById(R.id.btn_previous);
        btnNext = view.findViewById(R.id.btn_next);
        tvPageInfo = view.findViewById(R.id.tv_page_info);
        btnFromDate = view.findViewById(R.id.btn_from_date);
        btnToDate = view.findViewById(R.id.btn_to_date);

        // Setup RecyclerView
        adapter = new BookingAdapter(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Initialize API service
        apiService = ApiClient.getApiService();

        // Setup swipe refresh
        swipeRefresh.setOnRefreshListener(() -> {
            currentPage = 1; // Reset to first page
            loadAvailableJobs();
        });

        btnPrevious.setOnClickListener(v -> {
            if (currentPage > 1) {
                currentPage--;
                loadAvailableJobs();
            }
        });

        btnNext.setOnClickListener(v -> {
            if (currentPage < totalPages) {
                currentPage++;
                loadAvailableJobs();
            }
        });

        btnFromDate.setOnClickListener(v -> showDatePickerDialog(true));
        btnToDate.setOnClickListener(v -> showDatePickerDialog(false));

        // Check token before loading
        checkTokenAndLoadData();
    }

    private void showDatePickerDialog(boolean isFromDate) {
        Calendar calendar = isFromDate ? fromCalendar : toCalendar;
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDate(isFromDate, calendar);
            currentPage = 1; // Reset to first page when date changes
            loadAvailableJobs();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private void updateDate(boolean isFromDate, Calendar calendar) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        if (isFromDate) {
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);
            fromDate = sdf.format(calendar.getTime());
            btnFromDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
        } else {
            calendar.set(Calendar.HOUR_OF_DAY, 23);
            calendar.set(Calendar.MINUTE, 59);
            calendar.set(Calendar.SECOND, 59);
            calendar.set(Calendar.MILLISECOND, 999);
            toDate = sdf.format(calendar.getTime());
            btnToDate.setText(new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.getTime()));
        }
    }

    private void checkTokenAndLoadData() {
        PreferenceManager prefManager = new PreferenceManager(requireContext());
        String token = prefManager.getAccessToken();

        if (token == null || token.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            Toast.makeText(requireContext(), "Bạn chưa đăng nhập! Vui lòng vào tab Cá nhân để đăng nhập.", Toast.LENGTH_LONG).show();
        } else {
            loadAvailableJobs();
        }
    }

    private void loadAvailableJobs() {
        showLoading(true);
        tvEmpty.setVisibility(View.GONE);

        List<String> statuses = Arrays.asList("PARTIALLY_ACCEPTED", "PENDING");

        Log.d("JobsFragment", "Loading jobs. Page: " + currentPage + ", From: " + fromDate + ", To: " + toDate);

        apiService.getAvailableBookings(statuses, currentPage, PAGE_SIZE, fromDate, toDate, null, null, null)
                .enqueue(new Callback<ApiResponse<PageResponse<Booking>>>() {
                    @Override
                    public void onResponse(@NonNull Call<ApiResponse<PageResponse<Booking>>> call, @NonNull Response<ApiResponse<PageResponse<Booking>>> response) {
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<PageResponse<Booking>> apiResponse = response.body();
                            if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                                PageResponse<Booking> pageResponse = apiResponse.getResult();
                                totalPages = pageResponse.getTotalPages();
//                                currentPage = pageResponse.getPageNumber();

                                updatePaginationControls();

                                List<Booking> availableBookings = pageResponse.getContent();
                                if (availableBookings != null && !availableBookings.isEmpty()) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    tvEmpty.setVisibility(View.GONE);
                                    adapter.setBookings(availableBookings);
                                } else {
                                    recyclerView.setVisibility(View.GONE);
                                    tvEmpty.setVisibility(View.VISIBLE);
                                    adapter.setBookings(new ArrayList<>()); // Clear adapter
                                }
                            } else {
                                Toast.makeText(requireContext(), "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        } else {
                            String errorMessage = parseErrorMessage(response, "Lỗi khi tải dữ liệu");
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ApiResponse<PageResponse<Booking>>> call, @NonNull Throwable t) {
                        showLoading(false);
                        swipeRefresh.setRefreshing(false);
                        Log.e("JobsFragment", "API Call Failed", t);
                        Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void updatePaginationControls() {
        tvPageInfo.setText(String.format(Locale.getDefault(), "Trang %d / %d", currentPage, totalPages));
        btnPrevious.setEnabled(currentPage > 1);
        btnNext.setEnabled(currentPage < totalPages);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBookingClick(Booking booking) {
        Intent intent = new Intent(requireContext(), BookingDetailActivity.class);
        intent.putExtra("booking_id", booking.getId());
        startActivity(intent);
    }

    @Override
    public void onActionClick(Booking booking) {
        claimBooking(booking.getId());
    }

    private void claimBooking(int bookingId) {
        showLoading(true);
        apiService.claimBooking(bookingId).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<Booking>> call, @NonNull Response<ApiResponse<Booking>> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.getCode() == 200) {
                        Toast.makeText(requireContext(), "Nhận việc thành công!", Toast.LENGTH_SHORT).show();
                        loadAvailableJobs(); // Reload list
                    } else {
                        Toast.makeText(requireContext(), "Lỗi: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    String errorMessage = parseErrorMessage(response, "Lỗi khi nhận việc");
                    Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<Booking>> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e("JobsFragment", "Claim Booking Failed", t);
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String parseErrorMessage(Response<?> response, String defaultMessage) {
        if (response.errorBody() != null) {
            try {
                String errorBodyString = response.errorBody().string();
                Log.e("JobsFragment", "Raw Error Body: " + errorBodyString);
                try {
                    ApiResponse<?> apiResponse = new Gson().fromJson(errorBodyString, ApiResponse.class);
                    if (apiResponse != null && apiResponse.getMessage() != null && !apiResponse.getMessage().isEmpty()) {
                        return apiResponse.getMessage();
                    }
                } catch (JsonSyntaxException e) {
                    Log.e("JobsFragment", "Failed to parse error body as JSON", e);
                }
                return errorBodyString;
            } catch (IOException e) {
                Log.e("JobsFragment", "Error reading error body", e);
            }
        }
        return defaultMessage + " (Code: " + response.code() + ")";
    }
}
