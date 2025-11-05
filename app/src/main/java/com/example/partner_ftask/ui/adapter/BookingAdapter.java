package com.example.partner_ftask.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.partner_ftask.R;
import com.example.partner_ftask.data.model.Booking;
import com.example.partner_ftask.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    private List<Booking> bookings = new ArrayList<>();
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
        void onActionClick(Booking booking);
    }

    public BookingAdapter(OnBookingClickListener listener) {
        this.listener = listener;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private TextView tvServiceName;
        private TextView tvStatus;
        private TextView tvStartTime;
        private TextView tvAddress;
        private TextView tvCustomerName;
        private TextView tvPrice;
        private Button btnAction;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvStartTime = itemView.findViewById(R.id.tv_start_time);
            tvAddress = itemView.findViewById(R.id.tv_address);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvPrice = itemView.findViewById(R.id.tv_price);
            btnAction = itemView.findViewById(R.id.btn_action);
        }

        public void bind(Booking booking) {
            // Set service name
            if (booking.getVariant() != null) {
                tvServiceName.setText(booking.getVariant().getName());
            }

            // Set status
            tvStatus.setText(getStatusText(booking.getStatus()));
            tvStatus.setBackgroundColor(getStatusColor(booking.getStatus()));

            // Set start time
            tvStartTime.setText(DateTimeUtils.formatDateTime(booking.getStartAt()));

            // Set address
            if (booking.getAddress() != null) {
                tvAddress.setText(booking.getAddress().getFullAddress());
            }

            // Set customer name
            if (booking.getCustomer() != null) {
                tvCustomerName.setText(booking.getCustomer().getFullName());
            }

            // Set price
            tvPrice.setText(DateTimeUtils.formatCurrency(booking.getTotalPrice()));

            // Set action button based on status
            setupActionButton(booking);

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookingClick(booking);
                }
            });

            btnAction.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onActionClick(booking);
                }
            });
        }

        private String getStatusText(String status) {
            switch (status) {
                case "PENDING":
                    return "Chờ nhận";
                case "PARTIALLY_ACCEPTED":
                    return "Đang chờ";
                case "FULLY_ACCEPTED":
                    return "Đã đủ người";
                case "IN_PROGRESS":
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
                case "FULLY_ACCEPTED":
                    return 0xFF2196F3; // Blue
                case "IN_PROGRESS":
                    return 0xFFFF9800; // Orange
                case "COMPLETED":
                    return 0xFF9E9E9E; // Gray
                case "CANCELLED":
                    return 0xFFF44336; // Red
                default:
                    return 0xFF757575;
            }
        }

        private void setupActionButton(Booking booking) {
            String status = booking.getStatus();

            if ("PENDING".equals(status) || "PARTIALLY_ACCEPTED".equals(status)) {
                btnAction.setText("Nhận việc");
                btnAction.setVisibility(View.VISIBLE);
            } else {
                btnAction.setVisibility(View.GONE);
            }
        }
    }
}

