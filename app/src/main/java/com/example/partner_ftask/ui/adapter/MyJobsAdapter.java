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
import com.example.partner_ftask.data.model.BookingPartner;
import com.example.partner_ftask.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class MyJobsAdapter extends RecyclerView.Adapter<MyJobsAdapter.MyJobViewHolder> {

    private List<Booking> bookings = new ArrayList<>();
    private OnJobActionListener listener;

    public interface OnJobActionListener {
        void onJobClick(Booking booking);
        void onStartJob(Booking booking);
        void onCompleteJob(Booking booking);
        void onCancelJob(Booking booking);
    }

    public MyJobsAdapter(OnJobActionListener listener) {
        this.listener = listener;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyJobViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_booking, parent, false);
        return new MyJobViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyJobViewHolder holder, int position) {
        Booking booking = bookings.get(position);
        holder.bind(booking);
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class MyJobViewHolder extends RecyclerView.ViewHolder {
        private TextView tvServiceName;
        private TextView tvStatus;
        private TextView tvStartTime;
        private TextView tvAddress;
        private TextView tvCustomerName;
        private TextView tvPrice;
        private Button btnAction;

        public MyJobViewHolder(@NonNull View itemView) {
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

            // Get partner status
            String partnerStatus = getPartnerStatus(booking);

            // Set status
            tvStatus.setText(getStatusText(partnerStatus));
            tvStatus.setBackgroundColor(getStatusColor(partnerStatus));

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

            // Setup action button
            setupActionButton(booking, partnerStatus);

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onJobClick(booking);
                }
            });
        }

        private String getPartnerStatus(Booking booking) {
            if (booking.getPartners() != null && !booking.getPartners().isEmpty()) {
                // Assuming the first partner in the list is current user
                // In real app, you should filter by current partner ID
                BookingPartner bookingPartner = booking.getPartners().get(0);
                return bookingPartner.getStatus();
            }
            return "UNKNOWN";
        }

        private String getStatusText(String status) {
            switch (status) {
                case "JOINED":
                    return "Đã nhận";
                case "WORKING":
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
                case "JOINED":
                    return 0xFF2196F3; // Blue
                case "WORKING":
                    return 0xFFFF9800; // Orange
                case "COMPLETED":
                    return 0xFF4CAF50; // Green
                case "CANCELLED":
                    return 0xFFF44336; // Red
                default:
                    return 0xFF757575;
            }
        }

        private void setupActionButton(Booking booking, String partnerStatus) {
            btnAction.setVisibility(View.VISIBLE);

            switch (partnerStatus) {
                case "JOINED":
                    btnAction.setText("Bắt đầu");
                    btnAction.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onStartJob(booking);
                        }
                    });
                    break;
                case "WORKING":
                    btnAction.setText("Hoàn thành");
                    btnAction.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onCompleteJob(booking);
                        }
                    });
                    break;
                case "COMPLETED":
                case "CANCELLED":
                    btnAction.setVisibility(View.GONE);
                    break;
                default:
                    btnAction.setVisibility(View.GONE);
            }
        }
    }
}

