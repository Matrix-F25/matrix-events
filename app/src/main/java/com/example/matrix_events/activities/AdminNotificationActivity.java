package com.example.matrix_events.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.NotificationArrayAdapter;
import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.fragments.AdminNavigationBarFragment;

import java.util.ArrayList;

public class AdminNotificationActivity extends AppCompatActivity {

    private ArrayList<Notification> notifications;
    private NotificationArrayAdapter notificationArrayAdapter;

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
    }


}
