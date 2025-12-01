package com.example.matrix_events.activities;

import android.os.Bundle;
import android.provider.Settings;
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
import com.example.matrix_events.managers.NotificationManager;
import com.example.matrix_events.mvc.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity responsible for displaying a list of unread notifications to the user.
 * <p>
 * This class serves as a <b>View</b> in the MVC architecture. It observes the
 * {@link NotificationManager} (the Model) and updates the UI whenever the notification
 * data changes in the database.
 * </p>
 * <p>
 * The activity retrieves the device's unique ID to filter notifications specific to the
 * current user and utilizes a {@link NotificationArrayAdapter} to render the list items.
 * </p>
 */
public class NotificationActivity extends AppCompatActivity implements View {

    private ArrayList<Notification> notifications;
    private NotificationArrayAdapter notificationArrayAdapter;
    private String deviceId;

    /**
     * Called when the activity is first created.
     * <p>
     * Performs the following initialization tasks:
     * <ol>
     * <li>Sets up the UI layout and edge-to-edge display.</li>
     * <li>Initializes the bottom navigation bar.</li>
     * <li>Retrieves the unique Android Device ID to identify the current user.</li>
     * <li>Configures the ListView and Adapter.</li>
     * <li>Registers this activity as an observer of the {@link NotificationManager}.</li>
     * </ol>
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     * this Bundle contains the data it most recently supplied in onSaveInstanceState.
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

        // Setup Navigation Bar
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_notifications))
                .commit();

        // Get User ID
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Setup List and Adapter
        notifications = new ArrayList<>();
        ListView notificationListView = findViewById(R.id.notification_listview);
        notificationArrayAdapter = new NotificationArrayAdapter(this, notifications);
        notificationListView.setAdapter(notificationArrayAdapter);

        // Initial Data Load
        update();

        // Register as Observer
        NotificationManager.getInstance().addView(this);
    }

    /**
     * Called when the activity is destroyed.
     * <p>
     * Unregisters this activity from the {@link NotificationManager} to prevent memory leaks
     * and ensure the activity does not attempt to update UI elements after it has been destroyed.
     * </p>
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationManager.getInstance().removeView(this);
    }

    /**
     * Callback method from the MVC {@link View} interface.
     * <p>
     * This method is triggered whenever the {@link NotificationManager} data changes.
     * It performs the following logic:
     * <ol>
     * <li>Clears the current list of displayed notifications.</li>
     * <li>Fetches all notifications addressed to this device ID.</li>
     * <li><b>Filters</b> the list to include only <u>unread</u> notifications (`readFlag` is false).</li>
     * <li>Notifies the adapter to refresh the ListView.</li>
     * </ol>
     * </p>
     */
    @Override
    public void update() {
        notifications.clear();

        List<Notification> allRecvNotifications = NotificationManager.getInstance().getReceivedNotificationsByDeviceID(deviceId);

        for (Notification notification : allRecvNotifications) {
            // Only display unread notifications in this view
            if (!notification.getReadFlag()) {
                notifications.add(notification);
            }
        }

        if (notificationArrayAdapter != null) {
            notificationArrayAdapter.notifyDataSetChanged();
        }
    }
}