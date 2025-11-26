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

import com.example.matrix_events.R;
import com.example.matrix_events.activities.NotificationActivity;

public class NotificationUtils {

    // Channel ID used for all Matrix Push Notifications
    private static final String CHANNEL_ID = "matrix_events_channel";

    public static void showPushNotification(Context context, String title, String message, String notificationId, String type) {
        createNotificationChannel(context);

        // Opens NotificationActivity when Push Notification is Tapped
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra("notificationId", notificationId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        //TODO: We already have a notification ID in Database, so might not need unique one
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                notificationId.hashCode(),
                intent,  // unique ID per notification
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // long text support
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);

        // Use a unique int so multiple notifications stack
        int notifyId = notificationId.hashCode();
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            manager.notify(notifyId, builder.build());
        } else {
            // Notifications Aren't Allowed
            Log.w("NotificationUtils", "POST_NOTIFICATIONS permission not granted. Skipping notify().");
        }
    }

    // Create Notification Channel (Required on Android 8+).
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Matrix Events";
            String description = "Notifications for the Matrix Events app";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}