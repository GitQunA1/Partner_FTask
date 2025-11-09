package com.example.partner_ftask.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.partner_ftask.R;
import com.example.partner_ftask.data.model.Notification;
import com.example.partner_ftask.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private List<Notification> notifications;
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public NotificationAdapter(List<Notification> notifications, OnNotificationClickListener listener) {
        this.notifications = notifications != null ? notifications : new ArrayList<>();
        this.listener = listener;
    }

    public void updateNotifications(List<Notification> newNotifications) {
        this.notifications = newNotifications != null ? newNotifications : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void markAsRead(int notificationId) {
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getId() == notificationId) {
                notifications.get(i).setRead(true);
                notifyItemChanged(i);
                break;
            }
        }
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notifications.get(position);
        holder.bind(notification, listener);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final View rootView;
        private final View unreadIndicator;
        private final TextView tvTitle;
        private final TextView tvMessage;
        private final TextView tvTime;
        private final TextView tvType;
        private final TextView tvNewBadge;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView;
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvType = itemView.findViewById(R.id.tv_type);
            tvNewBadge = itemView.findViewById(R.id.tv_new_badge);
        }

        public void bind(Notification notification, OnNotificationClickListener listener) {
            // Title
            if (tvTitle != null) {
                String title = notification.getTitle();
                tvTitle.setText(title != null ? title : "Thông báo");
            }

            // Message
            if (tvMessage != null) {
                String message = notification.getMessage();
                tvMessage.setText(message != null ? message : "");
            }

            // Time
            if (tvTime != null) {
                String createdAt = notification.getCreatedAt();
                if (createdAt != null && !createdAt.isEmpty()) {
                    tvTime.setText(DateTimeUtils.formatDateTime(createdAt));
                } else {
                    tvTime.setText("--/--/----");
                }
            }

            // Type
            if (tvType != null) {
                String type = notification.getType();
                if (type != null) {
                    String displayType = getDisplayType(type);
                    tvType.setText(displayType);
                    tvType.setVisibility(View.VISIBLE);
                } else {
                    tvType.setVisibility(View.GONE);
                }
            }

            // Read/Unread status
            boolean isRead = notification.isRead();

            // Unread indicator (left bar)
            if (unreadIndicator != null) {
                unreadIndicator.setVisibility(isRead ? View.GONE : View.VISIBLE);
            }

            // NEW badge (bottom right)
            if (tvNewBadge != null) {
                tvNewBadge.setVisibility(isRead ? View.GONE : View.VISIBLE);
            }

            // Background color for unread
            if (!isRead) {
                rootView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.unread_background));
            } else {
                rootView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            }

            // Click listener
            rootView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNotificationClick(notification);
                }
            });
        }

        private String getDisplayType(String type) {
            if (type == null) return "";

            switch (type.toUpperCase()) {
                case "BOOKING":
                    return "ĐƠN HÀNG";
                case "WALLET":
                    return "VÍ";
                case "SYSTEM":
                    return "HỆ THỐNG";
                case "PROMOTION":
                    return "KHUYẾN MÃI";
                default:
                    return type.toUpperCase();
            }
        }
    }
}

