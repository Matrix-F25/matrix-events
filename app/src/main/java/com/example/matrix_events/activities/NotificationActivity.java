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

    private ArrayList<Notification> notifications;
    private NotificationArrayAdapter notificationArrayAdapter;
    private String deviceId;
    private String pendingNotificationId = null; // Store the ID until data is ready
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

        // Check for incoming Notification ID
        handleIntent(getIntent());

        // observe notification manager
        NotificationManager.getInstance().addView(this);
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
        String idFromIntent = intent.getStringExtra("notificationId");
        if (idFromIntent != null) {
            this.pendingNotificationId = idFromIntent;
            Log.d("NotificationActivity", "Push notification received with ID: " + pendingNotificationId);
        }
    }

    // Execute Navigation Only after Data is Loaded
    private void checkPendingNavigation() {
        if (pendingNotificationId != null) {
            Log.d("NotificationActivity", "Attempting navigation for ID: " + pendingNotificationId);
            Notification target = NotificationManager.getInstance().getNotificationByDBID(pendingNotificationId);
            if (target != null) {
                Log.d("NotificationActivity", "Target notification found. Navigating to SeeMore.");
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.content_fragment_container, NotificationSeeMoreFragment.newInstance(target, "entrant"))
                        .addToBackStack("notification_detail")
                        .commit();
                pendingNotificationId = null;
            } else {
                Log.w("NotificationActivity", "Target notification NOT found in loaded data.");
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
        for (Notification notification : allRecvNotifications) {
            if (!notification.getReadFlag()) {
                notifications.add(notification);
            }
        }

        if (notificationArrayAdapter != null) {
            notificationArrayAdapter.notifyDataSetChanged();
        }
        // Mark data as loaded and check for pending navigation
        isDataLoaded = true;
        checkPendingNavigation();
    }
}