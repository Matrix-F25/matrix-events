package com.example.matrix_events.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matrix_events.R;

public class AdminActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Go to the Organizer "My Events" Activity
        Button switchToOrganizerButton = findViewById(R.id.admin_switch_to_org_button);
        switchToOrganizerButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, OrganizerMyEventsActivity.class);
            startActivity(intent);
            finish();
        });

        ImageView adminNotificationsButton = findViewById(R.id.notifications_admin_logo);
        adminNotificationsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminNotificationActivity.class);
            startActivity(intent);
        });

        ImageView adminPostersButton = findViewById(R.id.poster_admin_logo);
        adminPostersButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, AdminPostersActivity.class);
            startActivity(intent);
        });
    }
}
