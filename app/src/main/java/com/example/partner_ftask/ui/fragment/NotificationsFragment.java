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
import com.example.partner_ftask.data.model.Notification;
import com.example.partner_ftask.ui.activity.BookingDetailActivity;
import com.example.partner_ftask.ui.adapter.NotificationsAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private ProgressBar progressBar;
    private View tvEmpty;
    private SwipeRefreshLayout swipeRefresh;
    private MaterialButton btnMarkAllRead;

    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        apiService = ApiClient.getApiService();

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view_notifications);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        btnMarkAllRead = view.findViewById(R.id.btn_mark_all_read);

        // Setup RecyclerView
        adapter = new NotificationsAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // Setup adapter click listener
        adapter.setOnNotificationClickListener(this::onNotificationClick);

        // Setup swipe refresh
        swipeRefresh.setOnRefreshListener(this::loadNotifications);

        // Setup mark all read button
        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());

        // Load notifications
        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        if (!swipeRefresh.isRefreshing()) {
            progressBar.setVisibility(View.VISIBLE);
        }
        tvEmpty.setVisibility(View.GONE);
        btnMarkAllRead.setVisibility(View.GONE);

        apiService.getNotifications().enqueue(new Callback<ApiResponse<List<Notification>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Notification>>> call,
                                   Response<ApiResponse<List<Notification>>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Notification>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        List<Notification> notifications = apiResponse.getResult();

                        if (notifications.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            tvEmpty.setVisibility(View.VISIBLE);
                            btnMarkAllRead.setVisibility(View.GONE);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            tvEmpty.setVisibility(View.GONE);
                            adapter.setNotifications(notifications);

                            // Show mark all read button if there are unread notifications
                            boolean hasUnread = notifications.stream()
                                    .anyMatch(n -> !n.isRead());
                            btnMarkAllRead.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                        }
                    } else {
                        showError(apiResponse.getMessage());
                    }
                } else {
                    showError("Không thể tải thông báo");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Notification>>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefresh.setRefreshing(false);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void onNotificationClick(Notification notification) {
        // Mark as read
        if (!notification.isRead()) {
            markAsRead(notification.getNotificationId());
        }

        // Handle navigation based on type
        String type = notification.getType();
        Integer relatedId = notification.getRelatedId();

        if (type != null && relatedId != null) {
            switch (type) {
                case "BOOKING":
                    // Navigate to booking detail
                    Intent intent = new Intent(getActivity(), BookingDetailActivity.class);
                    intent.putExtra("bookingId", relatedId);
                    startActivity(intent);
                    break;
                case "PAYMENT":
                    // Navigate to wallet
                    // Intent can be added if WalletActivity is needed
                    break;
                case "SYSTEM":
                    // Just mark as read, no navigation
                    break;
            }
        }
    }

    private void markAsRead(int notificationId) {
        apiService.markNotificationAsRead(notificationId)
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            // Update UI
                            adapter.updateNotification(notificationId);

                            // Update badge in MainActivity if needed
                            if (getActivity() instanceof NotificationBadgeListener) {
                                ((NotificationBadgeListener) getActivity()).updateBadge();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        // Silent fail
                    }
                });
    }

    private void markAllAsRead() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.markAllNotificationsAsRead()
                .enqueue(new Callback<ApiResponse<Void>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Void>> call,
                                           Response<ApiResponse<Void>> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            ApiResponse<Void> apiResponse = response.body();
                            if (apiResponse.getCode() == 200) {
                                // Update UI
                                adapter.markAllAsRead();
                                btnMarkAllRead.setVisibility(View.GONE);
                                Toast.makeText(getContext(),
                                        "Đã đánh dấu tất cả là đã đọc",
                                        Toast.LENGTH_SHORT).show();

                                // Update badge
                                if (getActivity() instanceof NotificationBadgeListener) {
                                    ((NotificationBadgeListener) getActivity()).updateBadge();
                                }
                            } else {
                                showError(apiResponse.getMessage());
                            }
                        } else {
                            showError("Không thể đánh dấu đã đọc");
                        }
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        showError("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    public interface NotificationBadgeListener {
        void updateBadge();
    }
}

