package com.example.matrix_events.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matrix_events.R;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.fragments.AdminNavigationBarFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.NotificationManager;
import com.example.matrix_events.managers.PosterManager;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.mvc.Model;
import com.example.matrix_events.mvc.View;

public class AdminActivity extends AppCompatActivity implements View {

    private TextView statsTextView;

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

        // Admin Navigation Bar Fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.admin_navigation_bar_fragment, new AdminNavigationBarFragment())
                    .commit();
        }

        // Initialize stats textview
        statsTextView = findViewById(R.id.admin_stats_textview);

        // Register as observer
        ProfileManager.getInstance().addView(this);
        EventManager.getInstance().addView(this);
        PosterManager.getInstance().addView(this);
        NotificationManager.getInstance().addView(this);

        update();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProfileManager.getInstance().removeView(this);
        EventManager.getInstance().removeView(this);
        PosterManager.getInstance().removeView(this);
        NotificationManager.getInstance().removeView(this);
    }

    @Override
    public void update() {
        if (statsTextView != null) {
            int totalProfiles = ProfileManager.getInstance().getProfiles().size();
            int adminProfiles = 0;
            for (Profile profile : ProfileManager.getInstance().getProfiles()) {
                if (profile.isAdmin()) {
                    adminProfiles++;
                }
            }
            int events = EventManager.getInstance().getEvents().size();
            int posters = PosterManager.getInstance().getPosters().size();
            int notifications = NotificationManager.getInstance().getNotifications().size();

            String stats = "Total Profiles: " + totalProfiles + "\n" +
                    "Admin Accounts: " + adminProfiles + "\n" +
                           "Events: " + events + "\n" +
                           "Posters: " + posters + "\n" +
                           "Notifications: " + notifications;
            statsTextView.setText(stats);
        }
    }
}
