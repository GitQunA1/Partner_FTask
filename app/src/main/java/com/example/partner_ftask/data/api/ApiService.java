package com.example.partner_ftask.data.api;

import com.example.partner_ftask.data.model.ApiResponse;
import com.example.partner_ftask.data.model.Booking;
import com.example.partner_ftask.data.model.PageResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

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

