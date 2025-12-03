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

/**
 * Activity responsible for displaying the user's list of received notifications.
 * <p>
 * This activity implements the core logic for the push notification deep link flow:
 * <ol>
 * <li>It listens for incoming Intents containing a {@code notificationId}.</li>
 * <li>It registers as a {@link View} to wait for the asynchronous notification data to load.</li>
 * <li>Once the data is ready, it automatically navigates to the {@link NotificationSeeMoreFragment}
 * for the specific notification that was clicked in the system tray.</li>
 * </ol>
 * </p>
 */
public class NotificationActivity extends AppCompatActivity implements View {

    private static final String TAG = "NotificationActivity";
    private ArrayList<Notification> notifications;
    private NotificationArrayAdapter notificationArrayAdapter;
    private String deviceId;

    // Deep Link State vars
    private String pendingNotificationId = null; // Store the ID until data is ready
    private String pendingType = null;
    private boolean isDataLoaded = false; // Flag to track if data has loaded

    /**
     * Called when the activity is first created.
     * <p>
     * Initializes the ListView, sets up the Navigation Bar, registers with the {@link NotificationManager},
     * and processes any incoming Intent data.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied.
     */
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

        // 1. Initialize Navigation Bar (Only on first creation)
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_notifications))
                    .commit();
        }

        // 2. Setup ListView
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        notifications = new ArrayList<>();
        ListView notificationListView = findViewById(R.id.notification_listview);
        notificationArrayAdapter = new NotificationArrayAdapter(this, notifications);
        notificationListView.setAdapter(notificationArrayAdapter);

        // 3. MVC Observation
        NotificationManager.getInstance().addView(this);

        // 4. Handle Incoming Intent (Cold Start)
        handleIntent(getIntent());

        // 5. Load Data
        update();
    }

    /**
     * Called when the activity is relaunched with a new intent (e.g., when a new push notification
     * arrives while the app is running in the background).
     *
     * @param intent The new intent that was started for the activity.
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Essential for subsequent calls to getIntent()
        handleIntent(intent);

        // If data is already loaded, try to navigate immediately
        if (isDataLoaded) {
            checkPendingNavigation();
        }
    }

    /**
     * Processes the incoming Intent to extract deep link parameters.
     *
     * @param intent The Intent received by the activity.
     */
    private void handleIntent(Intent intent) {
        if (intent == null) return;

        String idFromIntent = intent.getStringExtra("notificationId");
        String typeFromIntent = intent.getStringExtra("type");

        if (idFromIntent != null) {
            pendingNotificationId = idFromIntent;
            pendingType = typeFromIntent;
            Log.d(TAG, "Push notification Intent received with ID: " + pendingNotificationId);
        }
    }

    /**
     * Attempts to navigate to the {@link NotificationSeeMoreFragment} if a pending notification
     * ID exists and the corresponding data has been loaded into the manager's cache.
     * <p>
     * This method is the core of the deep linking solution, resolving the race condition between
     * UI click (Intent) and asynchronous database loading (MVC {@code update}).
     * </p>
     */
    private void checkPendingNavigation() {
        if (pendingNotificationId != null) {
            Log.d(TAG, "Attempting navigation for ID: " + pendingNotificationId);

            // Fetch notification object from Manager
            Notification target = NotificationManager.getInstance().getNotificationByDBID(pendingNotificationId);

            if (target != null) {
                Log.i(TAG, "Target found. Opening SeeMoreFragment.");

                String adapterType = (pendingType != null) ? pendingType : "entrant";

                NotificationSeeMoreFragment fragment = NotificationSeeMoreFragment.newInstance(target, adapterType);

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                // IMPORTANT: Replace the container that currently holds the ListView
                transaction.replace(R.id.content_fragment_container, fragment);
                transaction.addToBackStack("notification_detail");
                transaction.commit();

                // Reset state so we don't navigate again automatically
                pendingNotificationId = null;
                pendingType = null;
            } else {
                Log.w(TAG, "Target notification NOT found in loaded data yet.");
            }
        }
    }

    /**
     * Called when the activity is destroyed.
     * Removes this activity from the {@link NotificationManager}'s list of observers.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationManager.getInstance().removeView(this);
    }

    /**
     * MVC Callback: Updates the UI when the Model data changes.
     * <p>
     * Filters the received notifications (by device ID) to only show unread messages,
     * updates the ListView adapter, and then attempts to complete any pending deep navigation.
     * </p>
     */
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
