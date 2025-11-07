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

/**
 * Activity responsible for displaying a list of notifications received by the current user.
 * It implements the {@link View} interface to automatically update the list when new
 * notifications are received or existing ones are changed.
 */
public class NotificationActivity extends AppCompatActivity implements View {

    private ArrayList<Notification> notifications;
    private NotificationArrayAdapter notificationArrayAdapter;
    private String deviceId;

    /**
     * Called when the activity is first created.
     * This method sets up the user interface, initializes the notification list and its adapter,
     * retrieves the user's device ID, and registers with the {@link NotificationManager}
     * to receive updates.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
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
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_notifications))
                .commit();

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        notifications = new ArrayList<>();
        ListView notificationListView = findViewById(R.id.notification_listview);
        notificationArrayAdapter = new NotificationArrayAdapter(this, notifications);
        notificationListView.setAdapter(notificationArrayAdapter);

        update();

        // observe notification manager
        NotificationManager.getInstance().addView(this);
    }

    /**
     * Called when the activity is about to be destroyed.
     * This method unregisters the activity from the {@link NotificationManager} to prevent
     * memory leaks.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationManager.getInstance().removeView(this);
    }

    /**
     * Updates the UI by fetching the latest notifications for the current user.
     * This method is called by the {@link NotificationManager} whenever the notification data changes.
     * It clears the existing list, retrieves the new list of received notifications, and notifies
     * the adapter to refresh the display.
     */
    @Override
    public void update() {
        notifications.clear();
        notifications.addAll(NotificationManager.getInstance().getReceivedNotificationsByDeviceID(deviceId));
        notificationArrayAdapter.notifyDataSetChanged();
    }
}