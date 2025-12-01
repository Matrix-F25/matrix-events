package com.example.matrix_events.database;

import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.utils.NotificationUtils;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "New FCM token: " + token); // Used to display debug messages in Logcat window

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

        Log.d(TAG, "FCM message received");

        // Support both notification and data payload
        final String title = remoteMessage.getData().containsKey("title")
                ? remoteMessage.getData().get("title")
                : (remoteMessage.getNotification() != null ? remoteMessage.getNotification().getTitle() : "Matrix Events");

        final String message = remoteMessage.getData().containsKey("message")
                ? remoteMessage.getData().get("message")
                : (remoteMessage.getNotification() != null ? remoteMessage.getNotification().getBody() : "(no message)");

        final String type = remoteMessage.getData().get("type");

        final String notificationId =
                remoteMessage.getData().get("notificationId") != null ?
                        remoteMessage.getData().get("notificationId") :
                        String.valueOf(System.currentTimeMillis());

        if (type == null) {
            Log.w(TAG, "Push message missing 'type' field. Ignored.");
            return;
        }

        String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);


        // Fetch profile directly from Firestore
        FirebaseFirestore.getInstance()
                .collection("profiles")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.w(TAG, "No profile found for this deviceId - cannot apply preferences.");
                        return;
                    }

                    var doc = querySnapshot.getDocuments().get(0);
                    Profile profile = doc.toObject(Profile.class);

                    if (profile == null) {
                        Log.w(TAG, "Profile object is null.");
                        return;
                    }

                    boolean allow = false;
                    if (type.equalsIgnoreCase("admin")) {
                        allow = profile.isPushAdminNotifications();
                    } else if (type.equalsIgnoreCase("organizer")) {
                        allow = profile.isPushOrganizerNotifications();
                    }

                    if (!allow) {
                        Log.d(TAG, "User disabled push notifications for type=" + type);
                        return;
                    }

                    // Finally show native push notification
                    NotificationUtils.showPushNotification(
                            getApplicationContext(),
                            title,
                            message,
                            notificationId,
                            type
                    );
                })
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to fetch profile for preferences", e)
                );
    }
}