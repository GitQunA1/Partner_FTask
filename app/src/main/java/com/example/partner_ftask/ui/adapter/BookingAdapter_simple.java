package com.example.partner_ftask.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private final OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);

        void onActionClick(Booking booking);
    }

    public BookingAdapter(OnBookingClickListener listener) {
        this.listener = listener;
    }

    public void setBookings(List<Booking> bookings) {
        this.bookings = bookings != null ? bookings : new ArrayList<>();
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
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvServiceName;
        private final TextView tvCustomerName;
        private final TextView tvDate;
        private final TextView tvTime;
        private final TextView tvDateLabel;
        private final TextView tvDuration;
        private final TextView tvPrice;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvDateLabel = itemView.findViewById(R.id.tv_date_label);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }

        public void bind(Booking booking) {
            // Service name
            if (booking.getVariant() != null) {
                tvServiceName.setText(booking.getVariant().getName());
            } else {
                tvServiceName.setText("N/A");
            }

            // Customer name (in header)
            if (booking.getCustomer() != null) {
                String fullName = booking.getCustomer().getFullName();
                tvCustomerName.setText(fullName != null && !fullName.isEmpty() ? fullName : "Khách hàng");
            } else {
                tvCustomerName.setText("Khách hàng");
            }

            // Date: "03/07/2023"
            tvDate.setText(DateTimeUtils.formatDate(booking.getStartAt()));

            // Time range: "07:00 - 11:00"
            if (booking.getVariant() != null) {
                int duration = booking.getVariant().getDuration();
                tvTime.setText(DateTimeUtils.formatTimeRange(booking.getStartAt(), duration));

                // Duration: "4 giờ"
                tvDuration.setText(duration + " giờ");
            } else {
                tvTime.setText(DateTimeUtils.formatTime(booking.getStartAt()));
                tvDuration.setText("");
            }

            // Date label: "Hôm nay", "Ngày mai", etc.
            tvDateLabel.setText(DateTimeUtils.getDateLabel(booking.getStartAt()));

            // Price: "300,000₫"
            tvPrice.setText(DateTimeUtils.formatCurrency(booking.getTotalPrice()));

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookingClick(booking);
                }
            });
        }
    }
}

