package com.example.partner_ftask.data.api;

import com.example.partner_ftask.data.model.ApiResponse;
import com.example.partner_ftask.data.model.AuthResponse;
import com.example.partner_ftask.data.model.Booking;
import com.example.partner_ftask.data.model.PageResponse;
import com.example.partner_ftask.data.model.Review;
import com.example.partner_ftask.data.model.TopUpResponse;
import com.example.partner_ftask.data.model.Transaction;
import com.example.partner_ftask.data.model.VerifyOtpRequest;
import com.example.partner_ftask.data.model.Wallet;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ==================== AUTHENTICATION ====================

    // Verify OTP and login/register
    @POST("auth/verify-otp")
    Call<ApiResponse<AuthResponse>> verifyOtp(@Body VerifyOtpRequest request);

    // ==================== WALLET ====================

    // Get wallet information
    @GET("users/wallet")
    Call<ApiResponse<Wallet>> getUserWallet();

    // Top-up wallet
    @POST("wallets/top-up")
    Call<ApiResponse<TopUpResponse>> topUpWallet(
            @Query("amount") double amount,
            @Query("returnUrl") String returnUrl
    );

    // Withdrawal from wallet
    @POST("wallets/withdrawal")
    Call<ApiResponse<Wallet>> withdrawalWallet(@Query("amount") double amount);

    // Get user transactions with pagination
    @GET("users/transactions")
    Call<ApiResponse<PageResponse<Transaction>>> getUserTransactions(
            @Query("page") int page,
            @Query("size") int size
    );

    // ==================== REVIEWS ====================

    // Get partner reviews
    @GET("partners/{partnerId}/reviews")
    Call<ApiResponse<List<Review>>> getPartnerReviews(@Path("partnerId") int partnerId);

    // ==================== BOOKINGS ====================

    // Get list of available bookings
    @GET("bookings")
    Call<ApiResponse<PageResponse<Booking>>> getBookings(
            @Query("status") String status,
            @Query("page") int page,
            @Query("size") int size,
            @Query("fromDate") String fromDate,
            @Query("toDate") String toDate,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("address") String address
    );

    // Get booking detail
    @GET("bookings/{id}")
    Call<ApiResponse<Booking>> getBookingDetail(@Path("id") int id);

    // Claim a booking
    @POST("partners/bookings/{bookingId}/claim")
    Call<ApiResponse<Booking>> claimBooking(@Path("bookingId") int bookingId);

    // Start working on a booking
    @POST("partners/bookings/{bookingId}/start")
    Call<ApiResponse<Booking>> startBooking(@Path("bookingId") int bookingId);

    // Complete a booking
    @POST("partners/bookings/{bookingId}/complete")
    Call<ApiResponse<Booking>> completeBooking(@Path("bookingId") int bookingId);

    // Cancel a booking
    @POST("partners/bookings/{bookingId}/cancel")
    Call<ApiResponse<Booking>> cancelBooking(@Path("bookingId") int bookingId);
}

