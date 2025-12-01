package com.example.matrix_events.activities;

import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.NotificationArrayAdapter;
import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.fragments.AdminNavigationBarFragment;
import com.example.matrix_events.managers.NotificationManager;
import com.example.matrix_events.mvc.View;

import java.util.ArrayList;

/**
 * Activity responsible for the Administrator's view of system notifications.
 * <p>
 * This activity acts as a comprehensive log for Admins, allowing them to view <b>all</b>
 * notifications sent within the system, regardless of the sender or receiver.
 * It implements {@link View} to stay synchronized with the {@link NotificationManager}.
 * </p>
 * <p>
 * Unlike the standard user notification screen, this activity configures the
 * {@link NotificationArrayAdapter} in "admin" mode, which enables specific administrative
 * actions (like permanently deleting a message from the database).
 * </p>
 */
public class AdminNotificationActivity extends AppCompatActivity implements View {

    private ArrayList<Notification> notifications;
    private NotificationArrayAdapter notificationArrayAdapter;

    /**
     * Called when the activity is starting.
     * <p>
     * Initializes the UI layout and performs the following specific setups:
     * <ul>
     * <li><b>Navigation:</b> Loads the {@link AdminNavigationBarFragment} highlighting the Notifications tab.</li>
     * <li><b>Adapter:</b> Initializes the {@link NotificationArrayAdapter} with the "admin" flag.
     * This alters the list item layout to show both Sender and Receiver names and enables hard-delete functionality.</li>
     * <li><b>MVC Registration:</b> Registers this activity as an observer of {@link NotificationManager}.</li>
     * </ul>
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.admin_navigation_bar_fragment, AdminNavigationBarFragment.newInstance(R.id.nav_admin_notifications))
                .commit();

        notifications = new ArrayList<>();
        ListView adminNotificationList = findViewById(R.id.notification_listview);

        // Initialize adapter in "admin" mode
        notificationArrayAdapter = new NotificationArrayAdapter(this, notifications, "admin");
        adminNotificationList.setAdapter(notificationArrayAdapter);

        update();

        NotificationManager.getInstance().addView(this);
    }

    /**
     * Cleanup method called when the activity is destroyed.
     * <p>
     * Unregisters this activity from the {@link NotificationManager} to prevent memory leaks.
     * </p>
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationManager.getInstance().removeView(this);
    }

    /**
     * MVC Callback: Updates the list when the Model data changes.
     * <p>
     * This method fetches the complete list of <b>all</b> notifications from
     * {@link NotificationManager#getNotifications()} (no filtering by device ID)
     * and refreshes the adapter.
     * </p>
     */
    @Override
    public void update() {
        notifications.clear();
        notifications.addAll(NotificationManager.getInstance().getNotifications());
        if (notificationArrayAdapter != null) {
            notificationArrayAdapter.notifyDataSetChanged();
        }
    }
}