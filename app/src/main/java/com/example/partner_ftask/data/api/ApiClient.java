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
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    android.util.Log.d("API_LOG", message);
                }
            });
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

            // Add custom request/response logger
            httpClient.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    Request request = chain.request();

                    // Log request details
                    android.util.Log.d("API_REQUEST", "╔════════════════════════════════════════════════════════════");
                    android.util.Log.d("API_REQUEST", "║ REQUEST: " + request.method() + " " + request.url());
                    android.util.Log.d("API_REQUEST", "║ Headers:");
                    for (String name : request.headers().names()) {
                        String value = request.headers().get(name);
                        // Hide full token for security, show only first 20 chars
                        if ("Authorization".equals(name) && value != null && value.length() > 27) {
                            value = value.substring(0, 27) + "...";
                        }
                        android.util.Log.d("API_REQUEST", "║   " + name + ": " + value);
                    }
                    android.util.Log.d("API_REQUEST", "╚════════════════════════════════════════════════════════════");

                    long startTime = System.currentTimeMillis();
                    Response response = chain.proceed(request);
                    long duration = System.currentTimeMillis() - startTime;

                    // Log response details
                    android.util.Log.d("API_RESPONSE", "╔════════════════════════════════════════════════════════════");
                    android.util.Log.d("API_RESPONSE", "║ RESPONSE: " + request.method() + " " + request.url());
                    android.util.Log.d("API_RESPONSE", "║ Status Code: " + response.code() + " " + response.message());
                    android.util.Log.d("API_RESPONSE", "║ Duration: " + duration + "ms");
                    android.util.Log.d("API_RESPONSE", "║ Response Headers:");
                    for (String name : response.headers().names()) {
                        android.util.Log.d("API_RESPONSE", "║   " + name + ": " + response.headers().get(name));
                    }
                    android.util.Log.d("API_RESPONSE", "╚════════════════════════════════════════════════════════════");

                    return response;
                }
            });

            httpClient.addInterceptor(logging);

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
                            android.util.Log.d("API_AUTH", "✅ Token added to request");
                        } else {
                            android.util.Log.w("API_AUTH", "⚠️ No token found!");
                        }
                    }

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });

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

