package com.example.partner_ftask.ui.adapter;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.partner_ftask.R;
import com.example.partner_ftask.data.model.Notification;
import com.example.partner_ftask.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.NotificationViewHolder> {

    private List<Notification> notifications = new ArrayList<>();
    private OnNotificationClickListener listener;

    public interface OnNotificationClickListener {
        void onNotificationClick(Notification notification);
    }

    public void setOnNotificationClickListener(OnNotificationClickListener listener) {
        this.listener = listener;
    }

    public void setNotifications(List<Notification> notifications) {
        this.notifications = notifications;
        notifyDataSetChanged();
    }

    public void updateNotification(int notificationId) {
        for (int i = 0; i < notifications.size(); i++) {
            if (notifications.get(i).getNotificationId() == notificationId) {
                notifications.get(i).setRead(true);
                notifyItemChanged(i);
                break;
            }
        }
    }

    public void markAllAsRead() {
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        notifyDataSetChanged();
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
        holder.bind(notification);
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    class NotificationViewHolder extends RecyclerView.ViewHolder {
        private TextView tvTitle;
        private TextView tvMessage;
        private TextView tvTime;
        private TextView tvType;
        private View unreadIndicator;

        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_notification_title);
            tvMessage = itemView.findViewById(R.id.tv_notification_message);
            tvTime = itemView.findViewById(R.id.tv_notification_time);
            tvType = itemView.findViewById(R.id.tv_notification_type);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNotificationClick(notifications.get(position));
                }
            });
        }

        public void bind(Notification notification) {
            tvTitle.setText(notification.getTitle());
            tvMessage.setText(notification.getMessage());
            tvTime.setText(DateTimeUtils.formatDateTime(notification.getCreatedAt()));

            // Set type badge
            String type = notification.getType();
            if (type != null) {
                switch (type) {
                    case "BOOKING":
                        tvType.setText("Đặt việc");
                        tvType.setBackgroundResource(R.drawable.bg_badge_blue);
                        break;
                    case "PAYMENT":
                        tvType.setText("Thanh toán");
                        tvType.setBackgroundResource(R.drawable.bg_badge_green);
                        break;
                    case "SYSTEM":
                        tvType.setText("Hệ thống");
                        tvType.setBackgroundResource(R.drawable.bg_badge_orange);
                        break;
                    default:
                        tvType.setText("Khác");
                        tvType.setBackgroundResource(R.drawable.bg_badge_gray);
                        break;
                }
                tvType.setVisibility(View.VISIBLE);
            } else {
                tvType.setVisibility(View.GONE);
            }

            // Style based on read status - only show/hide unread dot
            if (notification.isRead()) {
                unreadIndicator.setVisibility(View.GONE);
                tvTitle.setTypeface(null, Typeface.NORMAL);
            } else {
                unreadIndicator.setVisibility(View.VISIBLE);
                tvTitle.setTypeface(null, Typeface.BOLD);
            }
        }
    }
}

