package com.example.partner_ftask.data.api;

import com.example.partner_ftask.data.model.ApiResponse;
import com.example.partner_ftask.data.model.AuthResponse;
import com.example.partner_ftask.data.model.Booking;
import com.example.partner_ftask.data.model.District;
import com.example.partner_ftask.data.model.Notification;
import com.example.partner_ftask.data.model.PageResponse;
import com.example.partner_ftask.data.model.Review;
import com.example.partner_ftask.data.model.TopUpResponse;
import com.example.partner_ftask.data.model.Transaction;
import com.example.partner_ftask.data.model.UnreadCountResponse;
import com.example.partner_ftask.data.model.UpdateDistrictsRequest;
import com.example.partner_ftask.data.model.UpdateUserInfoRequest;
import com.example.partner_ftask.data.model.UserInfoResponse;
import com.example.partner_ftask.data.model.VerifyOtpRequest;
import com.example.partner_ftask.data.model.Wallet;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // ==================== AUTHENTICATION ====================

    // Verify OTP and login/register
    @POST("auth/verify-otp")
    Call<ApiResponse<AuthResponse>> verifyOtp(@Body VerifyOtpRequest request);

    // Get current user info
    @GET("users/me")
    Call<ApiResponse<UserInfoResponse>> getUserInfo();

    // Update user info
    @PUT("users/update-info")
    Call<ApiResponse<UserInfoResponse>> updateUserInfo(@Body UpdateUserInfoRequest request);

    // ==================== PARTNERS ====================

    // Get partner registered districts
    @GET("partners/districts")
    Call<ApiResponse<List<District>>> getPartnerDistricts();

    // Get all available districts
    @GET("districts")
    Call<ApiResponse<List<District>>> getAllDistricts();

    // Update partner districts
    @PUT("partners/districts")
    Call<ApiResponse<Void>> updatePartnerDistricts(@Body UpdateDistrictsRequest request);

    // ==================== WALLET ====================

    // Get wallet information
    @GET("users/wallet")
    Call<ApiResponse<Wallet>> getUserWallet();

    // Top-up wallet
    @POST("wallets/top-up")
    Call<ApiResponse<TopUpResponse>> topUpWallet(
            @Query("amount") double amount,
            @Query("callbackUrl") String callbackUrl
    );

    // Confirm payment after VNPAY callback
    @GET("payments/confirm")
    Call<ApiResponse<Void>> confirmPayment(
            @Query("vnp_OrderInfo") String vnpOrderInfo,
            @Query("vnp_ResponseCode") String vnpResponseCode,
            @Query("vnp_TransactionStatus") String vnpTransactionStatus
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
            @Query("statuses") List<String> statuses,
            @Query("page") int page,
            @Query("size") int size,
            @Query("fromDate") String fromDate,
            @Query("toDate") String toDate,
            @Query("minPrice") Double minPrice,
            @Query("maxPrice") Double maxPrice,
            @Query("address") String address
    );

    // NOTE: The path "bookings/available" conflicts with "bookings/{id}".
    // To fix the 500 error, you need to modify your backend to distinguish between these two paths.
    // If you are using Spring, you can change the mapping for getBookingDetail to: @GetMapping("/bookings/{id:\\d+}")
    // This will ensure that only numeric values are matched as an ID.
    @GET("bookings/available/list")
    Call<ApiResponse<PageResponse<Booking>>> getAvailableBookings(
            @Query("statuses") List<String> statuses,
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

    // ==================== NOTIFICATIONS ====================

    // Get all notifications for partner
    @GET("notifications")
    Call<ApiResponse<List<Notification>>> getNotifications();

    // Get unread notification count
    @GET("notifications/unread-count")
    Call<ApiResponse<UnreadCountResponse>> getUnreadCount();

    // Mark a notification as read
    @PUT("notifications/{notificationId}/read")
    Call<ApiResponse<Void>> markNotificationAsRead(@Path("notificationId") int notificationId);

    // Mark all notifications as read
    @PUT("notifications/read-all")
    Call<ApiResponse<Void>> markAllNotificationsAsRead();
    @POST("partners/bookings/{bookingId}/cancel")
    Call<ApiResponse<Booking>> cancelBooking(@Path("bookingId") int bookingId);
}
