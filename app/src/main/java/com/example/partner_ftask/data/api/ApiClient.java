package com.example.partner_ftask.data.api;

import android.content.Context;

import com.example.partner_ftask.utils.PreferenceManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://ftask.anhtudev.works/";
    private static ApiService apiService;
    private static Context appContext;

    public static void init(Context context) {
        appContext = context.getApplicationContext();
    }

    public static ApiService getApiService() {
        if (apiService == null) {
            // Simple logging - only errors
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(message ->
                android.util.Log.d("API", message)
            );
            logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            // Add auth interceptor
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder();

                    // Add authorization header if token exists
                    if (appContext != null) {
                        PreferenceManager prefManager = new PreferenceManager(appContext);
                        String token = prefManager.getAccessToken();
                        if (token != null && !token.isEmpty()) {
                            requestBuilder.header("Authorization", "Bearer " + token);
                        }
                    }

                    Request request = requestBuilder.build();

                    // Log request
                    android.util.Log.d("API", request.method() + " " + request.url());

                    long startTime = System.currentTimeMillis();
                    Response response = chain.proceed(request);
                    long duration = System.currentTimeMillis() - startTime;

                    // Log response
                    android.util.Log.d("API", "Response: " + response.code() + " (" + duration + "ms)");

                    return response;
                }
            });

            httpClient.addInterceptor(logging);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(httpClient.build())
                    .build();

            apiService = retrofit.create(ApiService.class);
        }
        return apiService;
    }
}

