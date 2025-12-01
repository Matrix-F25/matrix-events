package com.example.matrix_events.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.NotificationArrayAdapter;
import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.fragments.NotificationSeeMoreFragment;
import com.example.matrix_events.managers.NotificationManager;
import com.example.matrix_events.mvc.View;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity implements View {
    private static final String TAG = "NotificationActivity";
    private ArrayList<Notification> notifications;
    private NotificationArrayAdapter notificationArrayAdapter;
    private String deviceId;
    private String pendingNotificationId = null; // Store the ID until data is ready
    private String pendingType = null;
    private boolean isDataLoaded = false; // Flag to track if data has loaded

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Navigation Bar Fragment
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_notifications))
                .commit();

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        notifications = new ArrayList<>();
        ListView notificationListView = findViewById(R.id.notification_listview);
        notificationArrayAdapter = new NotificationArrayAdapter(this, notifications);
        notificationListView.setAdapter(notificationArrayAdapter);

        // observe notification manager
        NotificationManager.getInstance().addView(this);

        // Check for incoming Notification ID
        handleIntent(getIntent());

        update();
    }

    // Handle Incoming Intent from Cold Start and when App is Running
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Update the activity's intent
        handleIntent(intent);
        // If data is already loaded, check if we need to navigate instantly
        if (isDataLoaded) {
            checkPendingNavigation();
        }
    }

    // Process the Intent and Store ID
    private void handleIntent(Intent intent) {
        if (intent == null) return;

        String idFromIntent = intent.getStringExtra("notificationId");
        String typeFromIntent = intent.getStringExtra("type");

        if (idFromIntent != null) {
            pendingNotificationId = idFromIntent;
            pendingType = typeFromIntent;
            Log.d(TAG, "Push notification received with ID: " + pendingNotificationId + ", type=" + pendingType);
        }
    }

    // Execute Navigation Only after Data is Loaded
    private void checkPendingNavigation() {
        if (pendingNotificationId != null) {
            Log.d(TAG, "Attempting navigation for ID: " + pendingNotificationId);

            Notification target = NotificationManager.getInstance().getNotificationByDBID(pendingNotificationId);

            if (target != null) {
                Log.i(TAG, "Target notification found. Navigating to SeeMore.");

                String adapterType = (pendingType != null) ? pendingType : "entrant";

                // Ensure 'Notification' implements Serializable or Parcelable!
                NotificationSeeMoreFragment fragment = NotificationSeeMoreFragment.newInstance(target, adapterType);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                // IMPORTANT: Replace the container that currently holds the ListView
                transaction.replace(R.id.content_fragment_container, fragment);
                transaction.addToBackStack("notification_detail");
                transaction.commit();

                pendingNotificationId = null;
                pendingType = null;
            } else {
                Log.w(TAG, "Target notification NOT found in loaded data.");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationManager.getInstance().removeView(this);
    }

    @Override
    public void update() {
        notifications.clear();
        List<Notification> allRecvNotifications = NotificationManager.getInstance().getReceivedNotificationsByDeviceID(deviceId);
        // NULL CHECK: Prevent crash if Manager returns null
        if (allRecvNotifications != null) {
            for (Notification notification : allRecvNotifications) {
                if (!notification.getReadFlag()) {
                    notifications.add(notification);
                }
            }
        } else {
            Log.w(TAG, "NotificationManager returned null list.");
        }

        if (notificationArrayAdapter != null) {
            notificationArrayAdapter.notifyDataSetChanged();
        }

        // Mark data as loaded so onNewIntent knows it can proceed
        isDataLoaded = true;
        checkPendingNavigation();
    }
}