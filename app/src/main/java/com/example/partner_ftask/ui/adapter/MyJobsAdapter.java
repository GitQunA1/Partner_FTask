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
                .inflate(R.layout.item_booking_new, parent, false);
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
        private TextView tvDistrict;
        private TextView tvDate;
        private TextView tvTime;
        private TextView tvDayInfo;
        private TextView tvDuration;
        private TextView tvPrice;

        public MyJobViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvDistrict = itemView.findViewById(R.id.tv_district);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvDayInfo = itemView.findViewById(R.id.tv_day_info);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvPrice = itemView.findViewById(R.id.tv_price);
        }

        public void bind(Booking booking) {
            // Set service name
            if (booking.getVariant() != null) {
                tvServiceName.setText(booking.getVariant().getName());
            }

            // Set district
            if (booking.getAddress() != null && booking.getAddress().getDistrict() != null) {
                tvDistrict.setText(booking.getAddress().getDistrict());
            }

            // Set date and time
            if (booking.getStartAt() != null) {
                tvDate.setText(DateTimeUtils.formatDate(booking.getStartAt()));
                tvTime.setText(DateTimeUtils.formatTimeRange(booking.getStartAt(), booking.getVariant()));
                tvDayInfo.setText(DateTimeUtils.formatDayInfo(booking.getStartAt()));
            }

            // Set duration
            if (booking.getVariant() != null) {
                int hours = booking.getVariant().getDuration() / 60;
                tvDuration.setText(hours + " giờ");
            }

            // Set price
            tvPrice.setText(DateTimeUtils.formatCurrency(booking.getTotalPrice()));

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onJobClick(booking);
                }
            });
        }
    }
}
