package com.example.matrix_events.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.utils.NotificationUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";

    // Shared Preferences keys (Make sure these match where you save user settings!)
    private static final String PREFS_NAME = "MatrixEventsPrefs";
    private static final String KEY_ALLOW_ADMIN = "allow_admin_push";
    private static final String KEY_ALLOW_ORGANIZER = "allow_organizer_push";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token);

        String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Direct Firestore query to Update FCM Token for a Profile
        FirebaseFirestore.getInstance()
                .collection("profiles")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Only one profile per device
                        var doc = querySnapshot.getDocuments().get(0);
                        // Update the FCM token field in Database
                        doc.getReference().update("FCMToken", token);
                        Log.d(TAG, "FCM token saved to profile: " + doc.getId());
                    } else {
                        Log.w(TAG, "No profile found for this deviceId");
                    }
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to update FCM token", e)
                );
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "FCM message received from: " + remoteMessage.getFrom());

        Map<String, String> data = remoteMessage.getData();
        if (data.size() > 0) {
            handleDataMessage(data);
        } else if (remoteMessage.getNotification() != null) {
            // Fallback: If only a notification payload is sent (not recommended for your requirements),
            // we treat it as a generic message.
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            // We can't determine type/id easily here, so we default.
            showNotification(title, body, "0", "generic");
        }
    }

    private void handleDataMessage(Map<String, String> data) {
        String title = data.get("title");
        String message = data.get("message");
        String type = data.get("type"); // e.g., "admin", "organizer"
        String notificationId = data.get("notificationId");

        if (notificationId == null) {
            notificationId = String.valueOf(System.currentTimeMillis());
        }

        // 1. CHECK PREFERENCES LOCALLY (Synchronous & Fast)
        // We avoid Firestore here to prevent the service from being killed before it finishes.
        if (shouldShowNotification(type)) {
            showNotification(title, message, notificationId, type);
        } else {
            Log.d(TAG, "Notification suppressed due to user preference: " + type);
        }
    }

    private boolean shouldShowNotification(String type) {
        if (type == null) return true; // Default to showing if no type specified

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (type.equalsIgnoreCase("admin")) {
            // Default to true if not set
            return prefs.getBoolean(KEY_ALLOW_ADMIN, true);
        } else if (type.equalsIgnoreCase("organizer")) {
            return prefs.getBoolean(KEY_ALLOW_ORGANIZER, true);
        }

        return true; // Unknown types show by default
    }

    private void showNotification(String title, String message, String notificationId, String type) {
        // Pass to your Utility class to handle the actual PendingIntent creation
        NotificationUtils.showPushNotification(
                getApplicationContext(),
                title,
                message,
                notificationId,
                type
        );
    }
}