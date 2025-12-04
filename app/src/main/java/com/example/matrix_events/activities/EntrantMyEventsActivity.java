package com.example.matrix_events.activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
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

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.EventArrayAdapter;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.fragments.EventDetailFragment;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.mvc.View;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity responsible for displaying the events an entrant is participating in.
 * <p>
 * This class serves as a dashboard for the user to view their status across different events.
 * It filters events into five distinct categories based on the entrant's progression through
 * the event lifecycle:
 * <ul>
 * <li><b>Waitlist:</b> Events where the user is waiting for the lottery draw.</li>
 * <li><b>Not Selected:</b> Events where the lottery occurred, and the user was not chosen.</li>
 * <li><b>Pending:</b> Events where the user won the lottery but hasn't accepted/declined yet.</li>
 * <li><b>Accepted:</b> Events the user is confirmed to attend.</li>
 * <li><b>Declined:</b> Events the user was invited to but chose not to attend.</li>
 * </ul>
 * This activity implements {@link View} to observe changes in the {@link EventManager}.
 * </p>
 */
public class EntrantMyEventsActivity extends AppCompatActivity implements View {

    /**
     * Enumeration representing the currently selected filter tab.
     */
    enum Selection {
        Waitlist,
        NotSelected,
        Accepted,
        Declined,
        Pending
    }
    private Selection selection = Selection.Waitlist;
    private String deviceId;
    private ArrayList<Event> eventArray;
    private EventArrayAdapter eventAdapter;
    private TextView listTitleTextview;

    private MaterialButtonToggleGroup toggleGroup;
    private MaterialButton waitlistButton, notSelectedButton, pendingButton, acceptedButton, declinedButton;

    /**
     * Called when the activity is starting.
     * <p>
     * Initializes the UI, sets up the navigation bar, configures the list adapter,
     * and attaches listeners to the filter toggle buttons. It also handles the
     * switch between Entrant and Organizer views.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_entrant_my_events);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Navigation Bar
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_my_events))
                .commit();

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Setup List and Adapter
        eventArray = new ArrayList<>();
        eventAdapter = new EventArrayAdapter(getApplicationContext(), eventArray);
        ListView eventListview = findViewById(R.id.entrant_listview);
        eventListview.setAdapter(eventAdapter);

        // Click Listener to view Event Details
        eventListview.setOnItemClickListener(((parent, view, position, id) -> {
            Log.d("DEBUG", "event clicked");
            Event selectedEvent = eventArray.get(position);
            EventDetailFragment fragment = EventDetailFragment.newInstance(selectedEvent);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit();
        }));

        // Go to the Organizer "My Events" Activity
        Button switchToOrganizerButton = findViewById(R.id.entrant_switch_to_org_button);
        switchToOrganizerButton.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantMyEventsActivity.this, OrganizerMyEventsActivity.class);
            startActivity(intent);
            finish();
        });

        listTitleTextview = findViewById(R.id.entrant_list_title_textview);

        // Setup Filter Buttons
        toggleGroup = findViewById(R.id.filter_toggle_group);
        waitlistButton = findViewById(R.id.entrant_waitlisted_button);
        notSelectedButton = findViewById(R.id.entrant_not_selected_button);
        pendingButton = findViewById(R.id.entrant_pending_button);
        acceptedButton = findViewById(R.id.entrant_accepted_button);
        declinedButton = findViewById(R.id.entrant_declined_button);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.entrant_waitlisted_button) selection = Selection.Waitlist;
                else if (checkedId == R.id.entrant_not_selected_button) selection = Selection.NotSelected;
                else if (checkedId == R.id.entrant_pending_button) selection = Selection.Pending;
                else if (checkedId == R.id.entrant_accepted_button) selection = Selection.Accepted;
                else if (checkedId == R.id.entrant_declined_button) selection = Selection.Declined;

                updateButtonStyles();
                update();
            }
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
     * Updates the visual style of the filter buttons based on the current {@link Selection}.
     * <p>
     * The selected button is highlighted with a solid green background and white text.
     * Unselected buttons are styled with a white background and green text/outline.
     * </p>
     */
    private void updateButtonStyles() {
        int green = Color.parseColor("#388E3C");
        int white = Color.WHITE;

        MaterialButton[] buttons = {waitlistButton, pendingButton, acceptedButton, declinedButton, notSelectedButton};

        for (MaterialButton btn : buttons) {
            if (btn == null) continue;

            boolean isSelected = false;
            if (selection == Selection.Waitlist && btn == waitlistButton) isSelected = true;
            else if (selection == Selection.Pending && btn == pendingButton) isSelected = true;
            else if (selection == Selection.Accepted && btn == acceptedButton) isSelected = true;
            else if (selection == Selection.Declined && btn == declinedButton) isSelected = true;
            else if (selection == Selection.NotSelected && btn == notSelectedButton) isSelected = true;

            if (isSelected) {
                btn.setBackgroundTintList(ColorStateList.valueOf(green));
                btn.setTextColor(white);
                btn.setStrokeWidth(0);
            } else {
                btn.setBackgroundTintList(ColorStateList.valueOf(white));
                btn.setTextColor(green);
                btn.setStrokeColor(ColorStateList.valueOf(green));
                btn.setStrokeWidth(2); // approx 1dp
            }
        }
    }

    /**
     * MVC Callback: Updates the list when the Model data changes or a filter is selected.
     * <p>
     * This method clears the current list and populates it with events from the {@link EventManager}
     * based on the current {@link Selection}.
     * </p>
     * <p>
     * <b>Special Logic for Waitlists:</b>
     * <ul>
     * <li><b>Waitlist Selection:</b> Shows events where the user is in the waitlist AND
     * registration is NOT closed (lottery hasn't happened yet).</li>
     * <li><b>Not Selected Selection:</b> Shows events where the user is in the waitlist BUT
     * registration IS closed (lottery happened, and they were not picked).</li>
     * </ul>
     * </p>
     */
    @Override
    public void update() {
        eventArray.clear();
        switch (selection) {
            case Waitlist: {
                listTitleTextview.setText("Waitlisted:");
                List<Event> allWaitlistedEvents = EventManager.getInstance().getEventsInWaitlist(deviceId);
                for (Event event : allWaitlistedEvents) {
                    if (!event.isRegistrationClosed()) {
                        eventArray.add(event);
                    }
                }
                break;
            }
            case NotSelected: {
                listTitleTextview.setText("Not Selected:");
                List<Event> allWaitlistedEvents = EventManager.getInstance().getEventsInWaitlist(deviceId);
                for (Event event : allWaitlistedEvents) {
                    if (event.isRegistrationClosed()) {
                        eventArray.add(event);
                    }
                }
                break;
            }
            case Pending: {
                listTitleTextview.setText("Pending:");
                eventArray.addAll(EventManager.getInstance().getEventsInPending(deviceId));
                break;
            }
            case Accepted: {
                listTitleTextview.setText("Accepted:");
                eventArray.addAll(EventManager.getInstance().getEventsInAccepted(deviceId));
                break;
            }
            case Declined: {
                listTitleTextview.setText("Declined:");
                eventArray.addAll(EventManager.getInstance().getEventsInDeclined(deviceId));
                break;
            }
        }
        eventAdapter.notifyDataSetChanged();
    }
}