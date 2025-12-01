package com.example.matrix_events.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.example.matrix_events.R;
import com.example.matrix_events.activities.NotificationActivity;

import java.util.Random;

public class NotificationUtils {

    // Channel ID used for all Matrix Push Notifications
    private static final String CHANNEL_ID = "matrix_events_channel";
    private static final String TAG = "NotificationUtils";

    public static void showPushNotification(Context context, String title, String message, String notificationId, String type) {
        createNotificationChannel(context);

        // Android 13+ Permission Check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission missing. Notification skipped.");
                return;
            }
        }

        // Build Intent to Open the Notification SeeMore Fragment
        Intent intent = new Intent(context, NotificationActivity.class);
        intent.putExtra("notificationId", notificationId);
        intent.putExtra("type", type);
        intent.putExtra("action", "OPEN_SEE_MORE");

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        int requestCode = (notificationId != null) ? notificationId.hashCode() : new Random().nextInt();

        int flags = PendingIntent.FLAG_UPDATE_CURRENT; // Updates extras if ID matches
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE; // Required for Android 12+
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                requestCode,
                intent,
                flags
        );

        // Build and Display Notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message)) // long text support
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL); // Sound + Vibration

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(requestCode, builder.build());
    }

    // Create Notification Channel (Required on Android 8+).
    private static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);

            // Return early if channel already exists
            if (notificationManager.getNotificationChannel(CHANNEL_ID) != null) {
                return;
            }

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Matrix Events Updates",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for event changes and announcements");
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);

            notificationManager.createNotificationChannel(channel);
        }
    }
}