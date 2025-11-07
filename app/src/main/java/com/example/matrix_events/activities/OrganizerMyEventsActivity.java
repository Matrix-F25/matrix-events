package com.example.matrix_events.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.example.matrix_events.fragments.EventCreateFragment;
import com.example.matrix_events.fragments.NavigationBarFragment;

/**
 * An activity that serves as the main dashboard for event organizers.
 * From this screen, an organizer can initiate the creation of a new event
 * or switch to the entrant (attendee) view of their events.
 */
public class OrganizerMyEventsActivity extends AppCompatActivity {

    /**
     * Called when the activity is first created.
     * This method initializes the user interface, including setting up window insets for edge-to-edge display,
     * loading the navigation bar, and configuring the button click listeners.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_my_events);

        // If your layout has a view with id "main"
        if (findViewById(R.id.main) != null) {
            ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
                Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
                return insets;
            });
        }

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_my_events))
                .commit();

        // --- Button Logic ---
        buttonLogic();
    }

    /**
     * Sets up the OnClickListeners for the buttons in this activity.
     * This includes handling the navigation to the entrant view and opening the
     * event creation fragment.
     */
    private void buttonLogic() {
        // Switch to Entrant
        Button switchToEntrantButton = findViewById(R.id.button_switch_to_entrant);
        if (switchToEntrantButton != null) {
            switchToEntrantButton.setOnClickListener(v -> {
                Intent intent = new Intent(OrganizerMyEventsActivity.this, EntrantMyEventsActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // Create Event
        Button createEventButton = findViewById(R.id.create_event_button);
        if (createEventButton != null) {
            createEventButton.setOnClickListener(v -> {
                Fragment fragment = new EventCreateFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main, fragment).addToBackStack(null).commit();
            });
        }
    }
}