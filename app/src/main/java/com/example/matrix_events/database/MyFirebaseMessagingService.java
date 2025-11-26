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

        String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID);

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
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");
        String notificationId = remoteMessage.getData().get("notificationId");
        String type = remoteMessage.getData().get("type"); // Admin or Organizer

        String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(),Settings.Secure.ANDROID_ID);

        ProfileManager profileManager = ProfileManager.getInstance();
        Profile profile = profileManager.getProfileByDeviceId(deviceId);

        // Check if Profile Doesn't Exist
        if (profile == null) {
            Log.w(TAG, "No Profile Found For This Device - Cannot Apply Notification Preferences.");
            return;
        }

        // Check Profile's Notification Preferences
        boolean allow = false;
        if("admin".equalsIgnoreCase(type)) {
            allow = profile.isPushAdminNotifications();
        } else if ("organizer".equalsIgnoreCase(type)) {
            allow = profile.isPushOrganizerNotifications();
        } else {
            Log.w(TAG, "Unknown Notification Type: " + type);
            return;
        }

        // Block Push Notification if User Disabled
        if (!allow) {
            Log.d(TAG, "Push Notification Blocked By User Preference: Type=" + type);
            return;
        }

        // Display a native Android push notification
        NotificationUtils.showPushNotification(this, title, message, notificationId, type);
    }
}