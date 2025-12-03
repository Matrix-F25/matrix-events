package com.example.matrix_events.activities;

import android.content.Intent;
import android.media.Image;
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

/**
 * Activity responsible for the main Administrator Dashboard.
 * <p>
 * This activity acts as the central hub for administrative actions. It provides a high-level
 * statistical overview of the application's current state (total users, events, images, etc.).
 * </p>
 * <p>
 * <b>Architecture Note:</b> This class implements {@link View} and observes <b>multiple</b>
 * {@link Model} singletons simultaneously:
 * <ul>
 * <li>{@link ProfileManager} - To count users and admins.</li>
 * <li>{@link EventManager} - To count active events.</li>
 * <li>{@link PosterManager} - To count uploaded images.</li>
 * <li>{@link NotificationManager} - To count system messages.</li>
 * </ul>
 * </p>
 */
public class AdminActivity extends AppCompatActivity implements View {

    private TextView statsTextView;

    /**
     * Called when the activity is starting.
     * <p>
     * Initializes the UI layout and performs the following setups:
     * <ol>
     * <li><b>Navigation:</b> Loads the {@link AdminNavigationBarFragment} (defaulting to Home).</li>
     * <li><b>Role Switching:</b> Configures the button to return to the {@link OrganizerMyEventsActivity}.</li>
     * <li><b>Observer Registration:</b> Registers this activity as a listener for <i>all four</i> data managers
     * so that the statistics update in real-time if data changes while the admin is watching.</li>
     * </ol>
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied.
     */
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

    /**
     * Cleanup method called when the activity is destroyed.
     * <p>
     * Unregisters this activity from <b>all</b> observed managers to prevent memory leaks
     * and ensure the activity does not attempt to update UI elements after destruction.
     * </p>
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProfileManager.getInstance().removeView(this);
        EventManager.getInstance().removeView(this);
        PosterManager.getInstance().removeView(this);
        NotificationManager.getInstance().removeView(this);
    }

    /**
     * MVC Callback: Updates the dashboard statistics when any Model data changes.
     * <p>
     * This method aggregates data from all managers to provide a system health report.
     * It calculates:
     * <ul>
     * <li>Total number of User Profiles.</li>
     * <li>Number of users with Admin privileges.</li>
     * <li>Total number of Events.</li>
     * <li>Total number of Posters (images).</li>
     * <li>Total number of Notifications sent.</li>
     * </ul>
     * </p>
     */
    @Override
    public void update() {
        if (statsTextView != null) {
            int totalProfiles = ProfileManager.getInstance().getProfiles().size();
            int adminProfiles = 0;
            // Calculate specific metric: How many profiles are admins?
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