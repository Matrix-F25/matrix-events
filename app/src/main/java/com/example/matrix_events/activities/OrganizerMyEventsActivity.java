package com.example.matrix_events.activities;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.Image;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.EventArrayAdapter;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.fragments.EventCreateFragment;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.fragments.OrganizerEventFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.mvc.View;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

/**
 * Activity responsible for the Organizer's dashboard view.
 * <p>
 * This activity allows users to manage the events they have created. It provides functionality to:
 * <ul>
 * <li><b>Create Events:</b> Via a floating action button that opens the {@link EventCreateFragment}.</li>
 * <li><b>Filter Events:</b> Toggle between "Registration Open" (Active) and "Registration Closed" (Past/Processing) events.</li>
 * <li><b>Navigate Roles:</b> Switch to the Entrant view or, if permissions allow, the Admin view.</li>
 * </ul>
 * It implements {@link View} to observe changes in the {@link EventManager}.
 * </p>
 */
public class OrganizerMyEventsActivity extends AppCompatActivity implements View {

    /**
     * Enumeration for the filter states of the event list.
     */
    enum Selection {
        /**
         * Events where registration is currently open or upcoming.
         */
        NotClosed,
        /**
         * Events where registration has ended (e.g., lottery is processing or event is finished).
         */
        Closed
    }
    private Selection selection = Selection.NotClosed;
    private String deviceId;
    private ArrayList<Event> eventArray;
    private EventArrayAdapter eventAdapter;
    private TextView listTitleTextview;
    private Button createEventButton;

    private MaterialButton registrationNotClosedButton;
    private MaterialButton registrationClosedButton;

    /**
     * Called when the activity is starting.
     * <p>
     * Initializes the UI, sets up the list adapter, and configures navigation.
     * Specific initialization logic includes:
     * <ul>
     * <li><b>Admin Check:</b> Queries {@link ProfileManager} to see if the current user is an Admin.
     * If true, the "Switch to Admin" button is made visible.</li>
     * <li><b>Fragment Management:</b> Adds a listener to the BackStack to hide the "Create Event" button
     * when the creation fragment is overlaying the screen.</li>
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
        setContentView(R.layout.activity_organizer_my_events);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });

        // Navigation Bar Setup
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_my_events))
                .commit();

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // List and Adapter Setup
        eventArray = new ArrayList<>();
        eventAdapter = new EventArrayAdapter(getApplicationContext(), eventArray);
        ListView eventListview = findViewById(R.id.organizer_listview);
        eventListview.setAdapter(eventAdapter);

        // Click Listener to manage specific events
        eventListview.setOnItemClickListener(((parent, view, position, id) -> {
            Log.d("DEBUG", "event clicked");
            Event selectedEvent = eventArray.get(position);
            OrganizerEventFragment fragment = OrganizerEventFragment.newInstance(selectedEvent);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }));

        // Go to the Entrant "My Events" Activity
        Button switchToEntrantButton = findViewById(R.id.organizer_switch_to_entrant_button);
        if (switchToEntrantButton != null) {
            switchToEntrantButton.setOnClickListener(v -> {
                Intent intent = new Intent(OrganizerMyEventsActivity.this, EntrantMyEventsActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // Go to the Admin Activity, if profile has necessary permissions
        Button switchToAdminButton = findViewById(R.id.organizer_switch_to_admin_button);
        switchToAdminButton.setVisibility(INVISIBLE);

        // Check for Admin privileges
        Profile currentProfile = ProfileManager.getInstance().getProfileByDeviceId(deviceId);
        if (currentProfile != null && currentProfile.isAdmin()) {
            switchToAdminButton.setVisibility(VISIBLE);
            switchToAdminButton.setOnClickListener(v -> {
                Intent intent = new Intent(OrganizerMyEventsActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // Create Event Button Logic
        createEventButton = findViewById(R.id.organizer_create_event_button);
        if (createEventButton != null) {
            createEventButton.setOnClickListener(v -> {
                Fragment fragment = new EventCreateFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment).addToBackStack(null).commit();
            });
        }

        // Manage Visibility of Create Button when Fragment is Open
        // This prevents the button from floating on top of the creation form
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                if (createEventButton != null) createEventButton.setVisibility(INVISIBLE);
            } else {
                if (createEventButton != null) createEventButton.setVisibility(VISIBLE);
            }
        });

        listTitleTextview = findViewById(R.id.organizer_list_title_textview);

        // Filter Button Setup
        registrationNotClosedButton = findViewById(R.id.organizer_reg_not_closed_button);
        registrationClosedButton = findViewById(R.id.organizer_reg_closed_button);

        registrationNotClosedButton.setOnClickListener(v -> {
            selection = Selection.NotClosed;
            updateButtonStyles();
            update();
        });
        registrationClosedButton.setOnClickListener(v -> {
            selection = Selection.Closed;
            updateButtonStyles();
            update();
        });

        // Initial update
        updateButtonStyles();
        update();

        // observe event manager
        EventManager.getInstance().addView(this);
    }

    /**
     * Cleanup method called when the activity is destroyed.
     * <p>
     * Unregisters this activity from the {@link EventManager} to prevent memory leaks.
     * </p>
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeView(this);
    }

    /**
     * Toggles the visual appearance of the filter buttons based on the current {@link Selection}.
     * <p>
     * The selected button is styled with a solid green background and white text.
     * The unselected button is styled with a white background and green outline/text.
     * </p>
     */
    private void updateButtonStyles() {
        int green = Color.parseColor("#388E3C");
        int white = Color.WHITE;

        if (selection == Selection.NotClosed) {
            // Not Closed is Selected (Green Filled)
            registrationNotClosedButton.setBackgroundTintList(ColorStateList.valueOf(green));
            registrationNotClosedButton.setTextColor(white);
            registrationNotClosedButton.setStrokeWidth(0);

            // Closed is Unselected (Outlined)
            registrationClosedButton.setBackgroundTintList(ColorStateList.valueOf(white));
            registrationClosedButton.setTextColor(green);
            registrationClosedButton.setStrokeColor(ColorStateList.valueOf(green));
            registrationClosedButton.setStrokeWidth(2); // approx 1dp or 2px
        } else {
            // Closed is Selected (Green Filled)
            registrationClosedButton.setBackgroundTintList(ColorStateList.valueOf(green));
            registrationClosedButton.setTextColor(white);
            registrationClosedButton.setStrokeWidth(0);

            // Not Closed is Unselected (Outlined)
            registrationNotClosedButton.setBackgroundTintList(ColorStateList.valueOf(white));
            registrationNotClosedButton.setTextColor(green);
            registrationNotClosedButton.setStrokeColor(ColorStateList.valueOf(green));
            registrationNotClosedButton.setStrokeWidth(2);
        }
    }

    /**
     * MVC Callback: Updates the list when the Model data changes or filter changes.
     * <p>
     * Fetches events organized by the current user ({@code deviceId}) from the {@link EventManager}, which follows the Singleton pattern as shown below.
     * <ul>
     * <li><b>NotClosed:</b> Calls {@link EventManager#getOrganizerEventsRegistrationNotClosed(String)}.</li>
     * <li><b>Closed:</b> Calls {@link EventManager#getOrganizerEventsRegistrationClosed(String)}.</li>
     * </ul>
     * </p>
     */
    @Override
    public void update() {
        eventArray.clear();
        switch (selection) {
            case NotClosed: {
                listTitleTextview.setText("Upcoming and Registration Open");
                eventArray.addAll(EventManager.getInstance().getOrganizerEventsRegistrationNotClosed(deviceId));
                break;
            }
            case Closed: {
                listTitleTextview.setText("Registration Closed");
                eventArray.addAll(EventManager.getInstance().getOrganizerEventsRegistrationClosed(deviceId));
                break;
            }
        }
        eventAdapter.notifyDataSetChanged();
    }
}