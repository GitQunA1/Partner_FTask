package com.example.partner_ftask.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.partner_ftask.R;
import com.example.partner_ftask.data.model.Review;
import com.example.partner_ftask.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {

    private List<Review> reviews = new ArrayList<>();

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews != null ? reviews : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);
        holder.bind(review);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    static class ReviewViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCustomerName;
        private final TextView tvDate;
        private final TextView tvStar1, tvStar2, tvStar3, tvStar4, tvStar5;
        private final TextView tvRatingText;
        private final TextView tvDescription;
        private final TextView tvBookingId;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCustomerName = itemView.findViewById(R.id.tv_customer_name);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvStar1 = itemView.findViewById(R.id.tv_star1);
            tvStar2 = itemView.findViewById(R.id.tv_star2);
            tvStar3 = itemView.findViewById(R.id.tv_star3);
            tvStar4 = itemView.findViewById(R.id.tv_star4);
            tvStar5 = itemView.findViewById(R.id.tv_star5);
            tvRatingText = itemView.findViewById(R.id.tv_rating_text);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvBookingId = itemView.findViewById(R.id.tv_booking_id);
        }

        public void bind(Review review) {
            // Customer name
            tvCustomerName.setText(review.getCustomerName());

            // Date
            tvDate.setText(DateTimeUtils.formatDate(review.getCreatedAt()));

            // Rating stars
            int rating = review.getRating();
            int filledColor = itemView.getContext().getColor(R.color.star_filled);
            int emptyColor = itemView.getContext().getColor(R.color.star_empty);

            tvStar1.setTextColor(rating >= 1 ? filledColor : emptyColor);
            tvStar2.setTextColor(rating >= 2 ? filledColor : emptyColor);
            tvStar3.setTextColor(rating >= 3 ? filledColor : emptyColor);
            tvStar4.setTextColor(rating >= 4 ? filledColor : emptyColor);
            tvStar5.setTextColor(rating >= 5 ? filledColor : emptyColor);

            tvRatingText.setText("(" + rating + ".0)");

            // Description
            if (review.getDescription() != null && !review.getDescription().isEmpty()) {
                tvDescription.setText(review.getDescription());
                tvDescription.setVisibility(View.VISIBLE);
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Booking ID
            tvBookingId.setText("Booking #" + review.getBookingId());
        }
    }
}

