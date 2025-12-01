package com.example.matrix_events.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.matrix_events.utils.NotificationUtils;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

/**
 * Service responsible for handling incoming Firebase Cloud Messages (FCM).
 * <p>
 * This service runs in the background and handles two main tasks:
 * <ol>
 * <li><b>Token Refresh:</b> Updates the user's Firestore profile when a new FCM token is generated.</li>
 * <li><b>Message Reception:</b> Intercepts data payloads, checks user preferences (Admin/Organizer),
 * and triggers the {@link NotificationUtils} to display a system notification.</li>
 * </ol>
 * </p>
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";

    // Shared Preferences keys (Must match where you save user settings in the app)
    private static final String PREFS_NAME = "MatrixEventsPrefs";
    private static final String KEY_ALLOW_ADMIN = "allow_admin_push";
    private static final String KEY_ALLOW_ORGANIZER = "allow_organizer_push";

    /**
     * Called when a new token for this device is generated.
     * <p>
     * This typically happens on first app install or if the user clears app data.
     * We immediately update the 'FCMToken' field in the user's Firestore profile.
     * </p>
     *
     * @param token The new token.
     */
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
                        // Use explicit type instead of 'var' for compatibility
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);

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

    /**
     * Called when a message is received.
     * <p>
     * Logic:
     * 1. Check if the message contains a data payload.
     * 2. Extract the 'type' (Admin/Organizer) and 'notificationId'.
     * 3. Check local SharedPreferences to see if the user has muted this type.
     * 4. If allowed, show the notification.
     * </p>
     *
     * @param remoteMessage Object representing the message received from Firebase.
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "FCM message received from: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            handleDataMessage(remoteMessage.getData());
        } else if (remoteMessage.getNotification() != null) {
            // Fallback: If a "Display Notification" is sent instead of a "Data Message",
            // we handle it here. Note: This only runs if the app is in the FOREGROUND.
            // Background notifications of this type are handled by the System Tray automatically.
            String title = remoteMessage.getNotification().getTitle();
            String body = remoteMessage.getNotification().getBody();
            showNotification(title, body, "0", "generic");
        }
    }

    /**
     * Processes the data payload from the push notification.
     *
     * @param data The Key-Value map from the message.
     */
    private void handleDataMessage(Map<String, String> data) {
        String title = data.get("title");
        String message = data.get("message");
        String type = data.get("type"); // Expected values: "admin", "organizer"
        String notificationId = data.get("notificationId");

        // Fallback for ID if missing
        if (notificationId == null) {
            notificationId = String.valueOf(System.currentTimeMillis());
        }

        // CHECK PREFERENCES LOCALLY (Synchronous & Fast)
        // We avoid Firestore here to prevent the service from being killed before it finishes.
        if (shouldShowNotification(type)) {
            showNotification(title, message, notificationId, type);
        } else {
            Log.d(TAG, "Notification suppressed due to user preference: " + type);
        }
    }

    /**
     * Checks SharedPreferences to see if the user wants to receive this type of notification.
     *
     * @param type The type of notification ("admin" or "organizer").
     * @return {@code true} if allowed or type is unknown, {@code false} if muted.
     */
    private boolean shouldShowNotification(String type) {
        if (type == null) return true; // Default to showing if no type specified

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        if (type.equalsIgnoreCase("admin")) {
            // Default to true if preference is not set
            return prefs.getBoolean(KEY_ALLOW_ADMIN, true);
        } else if (type.equalsIgnoreCase("organizer")) {
            return prefs.getBoolean(KEY_ALLOW_ORGANIZER, true);
        }

        return true; // Unknown types show by default
    }

    /**
     * triggers the actual system notification UI.
     */
    private void showNotification(String title, String message, String notificationId, String type) {
        // Pass to your Utility class to handle the actual PendingIntent creation and display
        NotificationUtils.showPushNotification(
                getApplicationContext(),
                title,
                message,
                notificationId,
                type
        );
    }
}