package com.example.partner_ftask.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.partner_ftask.MainActivity;
import com.example.partner_ftask.R;
import com.example.partner_ftask.data.api.ApiClient;
import com.example.partner_ftask.data.api.ApiService;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "partner_ftask_notifications";
    private ApiService apiService;

    @Override
    public void onCreate() {
        super.onCreate();
        apiService = ApiClient.getApiService();
        createNotificationChannel();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "📩 Notification received from: " + remoteMessage.getFrom());

        // Lấy title và body từ notification hoặc data
        String title = null;
        String body = null;
        String type = null;

        if (remoteMessage.getNotification() != null) {
            // Có notification payload
            title = remoteMessage.getNotification().getTitle();
            body = remoteMessage.getNotification().getBody();
            Log.d(TAG, "Notification Payload - Title: " + title + ", Body: " + body);
        }

        // Lấy thêm data từ data payload (ưu tiên cao hơn)
        if (remoteMessage.getData() != null && !remoteMessage.getData().isEmpty()) {
            if (remoteMessage.getData().containsKey("title")) {
                title = remoteMessage.getData().get("title");
            }
            if (remoteMessage.getData().containsKey("body")) {
                body = remoteMessage.getData().get("body");
            }
            if (remoteMessage.getData().containsKey("message")) {
                body = remoteMessage.getData().get("message");
            }
            if (remoteMessage.getData().containsKey("type")) {
                type = remoteMessage.getData().get("type");
            }
            Log.d(TAG, "Data Payload: " + remoteMessage.getData());
        }

        // Default values nếu null
        if (title == null) title = "Thông báo mới";
        if (body == null) body = "Bạn có thông báo mới";

        // Hiển thị notification
        sendNotification(title, body, type, remoteMessage.getData());
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        Log.d(TAG, "🔑 New FCM Token: " + token);

        // Gửi token mới lên backend
        sendTokenToServer(token);
    }

    /**
     * Gửi FCM token lên backend
     */
    private void sendTokenToServer(String token) {
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Token is empty, cannot send to server");
            return;
        }

        // Get existing user data from preferences
        com.example.partner_ftask.utils.PreferenceManager preferenceManager =
            new com.example.partner_ftask.utils.PreferenceManager(getApplicationContext());

        String fullName = preferenceManager.getFullName();

        // Create request with FCM token and keep existing data
        com.example.partner_ftask.data.model.UpdateUserInfoRequest request =
            new com.example.partner_ftask.data.model.UpdateUserInfoRequest(
                token,
                fullName != null && !fullName.isEmpty() ? fullName : null,
                null // Don't update gender
            );

        apiService.updateUserInfo(request).enqueue(new retrofit2.Callback<com.example.partner_ftask.data.model.ApiResponse<com.example.partner_ftask.data.model.User>>() {
            @Override
            public void onResponse(retrofit2.Call<com.example.partner_ftask.data.model.ApiResponse<com.example.partner_ftask.data.model.User>> call,
                                 retrofit2.Response<com.example.partner_ftask.data.model.ApiResponse<com.example.partner_ftask.data.model.User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "✅ FCM Token sent to server successfully");
                } else {
                    Log.e(TAG, "❌ Failed to send FCM token: " + response.code());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.example.partner_ftask.data.model.ApiResponse<com.example.partner_ftask.data.model.User>> call, Throwable t) {
                Log.e(TAG, "❌ Error sending FCM token: " + t.getMessage());
            }
        });
    }

    /**
     * ⚠️ BẮT BUỘC: Hiển thị notification khi app ở FOREGROUND hoặc nhận DATA payload
     */
    private void sendNotification(String title, String body, String type, java.util.Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Thêm data vào intent để xử lý khi click notification
        if (type != null) {
            intent.putExtra("notification_type", type);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
            Log.d(TAG, "✅ Notification displayed: " + title);
        }
    }

    /**
     * Tạo Notification Channel cho Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null && manager.getNotificationChannel(CHANNEL_ID) == null) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Partner FTask Notifications",
                        NotificationManager.IMPORTANCE_HIGH
                );
                channel.setDescription("Thông báo về đơn hàng, ví và hoạt động");
                channel.enableLights(true);
                channel.enableVibration(true);
                manager.createNotificationChannel(channel);
                Log.d(TAG, "✅ Notification channel created");
            }
        }
    }
}

