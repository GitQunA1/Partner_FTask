package com.example.partner_ftask.ui.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.partner_ftask.R;
import com.example.partner_ftask.data.api.ApiClient;
import com.example.partner_ftask.data.api.ApiService;
import com.example.partner_ftask.data.model.ApiResponse;
import com.example.partner_ftask.data.model.Review;
import com.example.partner_ftask.ui.adapter.ReviewsAdapter;
import com.example.partner_ftask.utils.PreferenceManager;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ReviewsAdapter adapter;
    private SwipeRefreshLayout swipeRefresh;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private TextView tvAverageRating;
    private TextView tvTotalReviews;
    private TextView tvAvgStar1, tvAvgStar2, tvAvgStar3, tvAvgStar4, tvAvgStar5;

    private ApiService apiService;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        // Initialize API service and preference manager
        apiService = ApiClient.getApiService();
        preferenceManager = new PreferenceManager(this);

        // Setup toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Đánh giá của tôi");
        }

        // Initialize views
        recyclerView = findViewById(R.id.recycler_view_reviews);
        swipeRefresh = findViewById(R.id.swipe_refresh);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);
        tvAverageRating = findViewById(R.id.tv_average_rating);
        tvTotalReviews = findViewById(R.id.tv_total_reviews);
        tvAvgStar1 = findViewById(R.id.tv_avg_star1);
        tvAvgStar2 = findViewById(R.id.tv_avg_star2);
        tvAvgStar3 = findViewById(R.id.tv_avg_star3);
        tvAvgStar4 = findViewById(R.id.tv_avg_star4);
        tvAvgStar5 = findViewById(R.id.tv_avg_star5);

        // Setup RecyclerView
        adapter = new ReviewsAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Setup swipe refresh
        swipeRefresh.setOnRefreshListener(this::loadReviews);

        // Load reviews
        loadReviews();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadReviews() {
        int partnerId = preferenceManager.getPartnerId();

        if (partnerId <= 0) {
            Toast.makeText(this, "Không tìm thấy thông tin Partner", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);
        tvEmpty.setVisibility(View.GONE);

        apiService.getPartnerReviews(partnerId).enqueue(new Callback<ApiResponse<List<Review>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Review>>> call, Response<ApiResponse<List<Review>>> response) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Review>> apiResponse = response.body();
                    if (apiResponse.getCode() == 200 && apiResponse.getResult() != null) {
                        List<Review> reviews = apiResponse.getResult();

                        if (reviews.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            tvEmpty.setVisibility(View.VISIBLE);
                            displayAverageRating(0, 0);
                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            tvEmpty.setVisibility(View.GONE);
                            adapter.setReviews(reviews);

                            // Calculate and display average rating
                            double avgRating = calculateAverageRating(reviews);
                            displayAverageRating(avgRating, reviews.size());
                        }
                    } else {
                        Toast.makeText(ReviewsActivity.this, apiResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ReviewsActivity.this, "Lỗi khi tải đánh giá", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Review>>> call, Throwable t) {
                showLoading(false);
                swipeRefresh.setRefreshing(false);
                Toast.makeText(ReviewsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private double calculateAverageRating(List<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return 0;
        }

        int totalRating = 0;
        for (Review review : reviews) {
            totalRating += review.getRating();
        }

        return (double) totalRating / reviews.size();
    }

    private void displayAverageRating(double avgRating, int totalReviews) {
        // Display average rating number
        tvAverageRating.setText(String.format("%.1f", avgRating));

        // Display total reviews
        tvTotalReviews.setText("Dựa trên " + totalReviews + " đánh giá");

        // Display stars
        int filledColor = getColor(R.color.star_filled);
        int emptyColor = getColor(R.color.star_empty);
        int fullStars = (int) Math.floor(avgRating);

        tvAvgStar1.setTextColor(fullStars >= 1 ? filledColor : emptyColor);
        tvAvgStar2.setTextColor(fullStars >= 2 ? filledColor : emptyColor);
        tvAvgStar3.setTextColor(fullStars >= 3 ? filledColor : emptyColor);
        tvAvgStar4.setTextColor(fullStars >= 4 ? filledColor : emptyColor);
        tvAvgStar5.setTextColor(fullStars >= 5 ? filledColor : emptyColor);
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}

