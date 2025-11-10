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
                .inflate(R.layout.item_booking_new, parent, false);
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
        private TextView tvStatusBadge;
        private TextView tvDistrict;
        private TextView tvDate;
        private TextView tvTime;
        private TextView tvDayInfo;
        private TextView tvDuration;
        private TextView tvPrice;
        private TextView tvPartnerSlots;
        private TextView tvJoinedPartners;
        private TextView tvCustomerName;
        private View layoutPartnerSlots;

        public BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceName = itemView.findViewById(R.id.tv_service_name);
            tvStatusBadge = itemView.findViewById(R.id.tv_status_badge);
            tvDistrict = itemView.findViewById(R.id.tv_district);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvDayInfo = itemView.findViewById(R.id.tv_day_info);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            tvPrice = itemView.findViewById(R.id.tv_price);
            tvPartnerSlots = itemView.findViewById(R.id.tv_partner_slots);
            tvJoinedPartners = itemView.findViewById(R.id.tv_joined_partners);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            layoutPartnerSlots = itemView.findViewById(R.id.layout_partner_slots);
        }

        public void bind(Booking booking) {
            // Set service name
            if (booking.getVariant() != null) {
                tvServiceName.setText(booking.getVariant().getName());
            }

            // Set status badge
            String statusText = getStatusBadgeText(booking.getStatus());
            tvStatusBadge.setText(statusText);

            // Set district
            if (booking.getAddress() != null && booking.getAddress().getDistrict() != null) {
                tvDistrict.setText(booking.getAddress().getDistrict());
            } else {
                tvDistrict.setText("Chưa có địa chỉ");
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

            // Set partner slots info
            int required = booking.getRequiredPartners();
            int joined = booking.getNumberOfJoinedPartner();
            if (required > 1) {
                layoutPartnerSlots.setVisibility(View.VISIBLE);
                tvPartnerSlots.setText("Cần " + required + " người");
                if (joined > 0) {
                    tvJoinedPartners.setVisibility(View.VISIBLE);
                    tvJoinedPartners.setText("• Đã có " + joined);
                } else {
                    tvJoinedPartners.setVisibility(View.GONE);
                }
            } else {
                layoutPartnerSlots.setVisibility(View.GONE);
            }

            // Set customer name
            if (booking.getCustomer() != null && booking.getCustomer().getFullName() != null) {
                tvCustomerName.setText(booking.getCustomer().getFullName());
            } else {
                tvCustomerName.setText("Khách hàng");
            }

            // Set price
            tvPrice.setText(DateTimeUtils.formatCurrency(booking.getTotalPrice()));

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBookingClick(booking);
                }
            });
        }

        private String getStatusBadgeText(String status) {
            if (status == null) return "Mới";
            switch (status) {
                case "PENDING":
                    return "Mới";
                case "PARTIALLY_ACCEPTED":
                    return "Cần thêm";
                case "ACCEPTED":
                    return "Đã đủ";
                case "WORKING":
                    return "Đang làm";
                case "COMPLETED":
                    return "Hoàn thành";
                default:
                    return status;
            }
        }
    }
}

