package com.example.matrix_events.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.matrix_events.R;
import com.example.matrix_events.activities.NotificationActivity;

public class NotificationUtils {

    // Channel ID used for all Matrix Push Notifications
    private static final String CHANNEL_ID = "matrix_events_channel";

    public static void showPushNotification(Context context, String title, String message, String notificationId, String type) {
        createNotificationChannel(context);

        // Android 13+ Permission Check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            int permissionCheck = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
            );

            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                Log.w("NotificationUtils", "POST_NOTIFICATIONS not granted. Cannot show notification.");
                return;
            }
        }

        // Build Intent to Open the Notification SeeMore Fragment
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra("notificationId", notificationId);
        intent.putExtra("type", type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId.hashCode(), // ensures unique back stack per notification
                intent,  // unique ID per notification
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Build Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // long text support
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(notificationId.hashCode(), builder.build());
    }

    // Create Notification Channel (Required on Android 8+).
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) return; // already created
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Matrix Events",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for the Matrix Events app");
            notificationManager.createNotificationChannel(channel);
        }
    }
}