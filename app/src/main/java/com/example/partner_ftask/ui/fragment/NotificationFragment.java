package com.example.partner_ftask.ui.fragment;

import android.os.Bundle;
import android.util.Log;
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
import com.example.partner_ftask.data.model.UnreadCountResponse;
import com.example.partner_ftask.ui.adapter.NotificationAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationFragment extends Fragment {
    private static final String TAG = "NotificationFragment";

    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView tvUnreadCount;
    private MaterialButton btnMarkAllRead;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ApiService apiService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        apiService = ApiClient.getApiService();

        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_view_notifications);
        progressBar = view.findViewById(R.id.progress_bar);
        tvEmpty = view.findViewById(R.id.tv_empty);
        tvUnreadCount = view.findViewById(R.id.tv_unread_count);
        btnMarkAllRead = view.findViewById(R.id.btn_mark_all_read);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh);

        // Setup RecyclerView
        adapter = new NotificationAdapter(new ArrayList<>(), this::onNotificationClick);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Setup button
        btnMarkAllRead.setOnClickListener(v -> markAllAsRead());

        // Setup swipe refresh
        swipeRefreshLayout.setOnRefreshListener(this::loadNotifications);

        // Load data
        loadUnreadCount();
        loadNotifications();

        return view;
    }

    private void loadNotifications() {
        setLoading(true);

        apiService.getAllNotifications().enqueue(new Callback<ApiResponse<List<Notification>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Notification>>> call,
                                 Response<ApiResponse<List<Notification>>> response) {
                setLoading(false);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Notification>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        List<Notification> notifications = apiResponse.getResult();

                        if (notifications.isEmpty()) {
                            showEmpty(true);
                        } else {
                            showEmpty(false);
                            adapter.updateNotifications(notifications);
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
                setLoading(false);
                swipeRefreshLayout.setRefreshing(false);
                showError("Lỗi kết nối: " + t.getMessage());
                Log.e(TAG, "Load notifications failed", t);
            }
        });
    }

    private void loadUnreadCount() {
        apiService.getUnreadCount().enqueue(new Callback<ApiResponse<UnreadCountResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UnreadCountResponse>> call,
                                 Response<ApiResponse<UnreadCountResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<UnreadCountResponse> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        int unreadCount = apiResponse.getResult().getUnreadCount();
                        updateUnreadCount(unreadCount);
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UnreadCountResponse>> call, Throwable t) {
                Log.e(TAG, "Load unread count failed", t);
            }
        });
    }

    private void onNotificationClick(Notification notification) {
        if (!notification.isRead()) {
            markAsRead(notification.getId());
        }

        // TODO: Navigate to detail screen based on notification type
        Toast.makeText(requireContext(), notification.getMessage(), Toast.LENGTH_SHORT).show();
    }

    private void markAsRead(int notificationId) {
        apiService.markNotificationAsRead(notificationId).enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ Marked notification as read: " + notificationId);

                    // Update UI
                    adapter.markAsRead(notificationId);
                    loadUnreadCount();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                Log.e(TAG, "Mark as read failed", t);
            }
        });
    }

    private void markAllAsRead() {
        progressBar.setVisibility(View.VISIBLE);

        apiService.markAllNotificationsAsRead().enqueue(new Callback<ApiResponse<String>>() {
            @Override
            public void onResponse(Call<ApiResponse<String>> call, Response<ApiResponse<String>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(requireContext(), "Đã đọc tất cả thông báo", Toast.LENGTH_SHORT).show();

                    // Reload notifications
                    loadNotifications();
                    loadUnreadCount();
                } else {
                    showError("Không thể đánh dấu đã đọc");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<String>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showEmpty(boolean isEmpty) {
        tvEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        btnMarkAllRead.setEnabled(!isEmpty);
    }

    private void updateUnreadCount(int count) {
        if (count > 0) {
            tvUnreadCount.setVisibility(View.VISIBLE);
            tvUnreadCount.setText(String.valueOf(count));
        } else {
            tvUnreadCount.setVisibility(View.GONE);
        }
    }

    private void showError(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }
}

