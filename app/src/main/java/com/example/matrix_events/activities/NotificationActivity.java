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

public class NotificationActivity extends AppCompatActivity implements View {

    private ArrayList<Notification> notifications;
    private NotificationArrayAdapter notificationArrayAdapter;
    private ListView notificationListView;
    private String deviceId;

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
        notificationListView = findViewById(R.id.notification_listview);
        notificationArrayAdapter = new NotificationArrayAdapter(this, notifications);
        notificationListView.setAdapter(notificationArrayAdapter);

        NotificationManager.getInstance().addView(this);
        update();
    }

    @Override
    public void update() {
        notifications.clear();
        notifications.addAll(NotificationManager.getInstance().getReceivedNotificationsByDeviceID(deviceId));
        notificationArrayAdapter.notifyDataSetChanged();
    }
}
