package com.example.matrix_events.activities;

import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.entities.ReoccurringType;
import com.example.matrix_events.fragments.EventDetailFragment;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.mvc.View;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Activity responsible for searching, browsing, and filtering the list of events.
 * <p>
 * This class serves as a <b>View</b> in the MVC architecture, observing the {@link EventManager}
 * for data updates. It provides a search interface allowing users to find events based on
 * text queries and status criteria.
 * </p>
 * <p>
 * <b>Filtering Logic:</b>
 * The activity applies an "AND" condition to filters:
 * <ul>
 * <li><b>Text Search:</b> Matches against Event Name OR Location (case-insensitive).</li>
 * <li><b>Status Dropdown:</b> Filters by logical state (Upcoming, Registration Open, Past, or All).</li>
 * </ul>
 * </p>
 */
public class EventSearchActivity extends AppCompatActivity implements View {

    // Data structures
    ArrayList<Event> allEvents;
    ArrayList<Event> events;
    EventArrayAdapter eventArrayAdapter;

    // State variables
    private String currentSearchQuery = "";

    // Filter Constants
    private static final String FILTER_ALL = "All Events";
    private static final String FILTER_UPCOMING = "Upcoming";
    private static final String FILTER_REG_OPEN = "Registration Open";
    private static final String FILTER_CLOSED = "Past / Closed";

    // Set default filter status to "Registration Open" to match XML
    private String currentFilterStatus = FILTER_REG_OPEN;

    private AutoCompleteTextView filterDropdown;

    /**
     * Called when the activity is starting.
     * <p>
     * Performs the following initialization tasks:
     * <ol>
     * <li>Sets up the UI layout and edge-to-edge display.</li>
     * <li>Initializes the bottom navigation bar.</li>
     * <li>Configures the {@link ListView} and {@link EventArrayAdapter}.</li>
     * <li>Sets up the text search listener to trigger real-time filtering.</li>
     * <li>Configures the status dropdown menu.</li>
     * <li>Registers this activity as an observer of the {@link EventManager}.</li>
     * </ol>
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently
     * supplied in {@link #onSaveInstanceState}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_search);

        // Handle Window Insets for Edge-to-Edge display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Navigation Bar
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_event_search))
                .commit();

        // Request FCM Token as soon as User Logs in because User must have a Profile to receive Notifications
        FirebaseMessaging.getInstance().getToken()
                .addOnSuccessListener(token -> {
                    String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    ProfileManager profileManager = ProfileManager.getInstance();
                    Profile profile = profileManager.getProfileByDeviceId(deviceId);

                    if (profile != null && (profile.getFCMToken() == null || !profile.getFCMToken().equals(token))) {
                        profile.setFCMToken(token);
                        profileManager.updateProfile(profile);
                    }
                });

        // Initialize Lists and Adapter
        allEvents = new ArrayList<>();
        events = new ArrayList<>();
        eventArrayAdapter = new EventArrayAdapter(getApplicationContext(), events);
        ListView eventListView = findViewById(R.id.event_search_listview);
        eventListView.setAdapter(eventArrayAdapter);

        // Link the Empty View
        TextView emptyTextView = findViewById(R.id.empty_list_textview);
        if (emptyTextView != null) {
            eventListView.setEmptyView(emptyTextView);
        }

        // Initialize Inputs
        TextInputEditText searchInput = findViewById(R.id.search_input);
        filterDropdown = findViewById(R.id.filter_autocomplete_textview);

        // Setup Filter Dropdown
        setupFilterDropdown();

        // Setup Search Listener (Updates Results when User Types)
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {}
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery = s.toString();
                filterEvents();
            }
        });

        // Setup Item Click Listener (Open Event Details)
        eventListView.setOnItemClickListener(((parent, view, position, id) -> {
            Log.d("DEBUG", "event clicked");
            Event selectedEvent = events.get(position);
            EventDetailFragment fragment = EventDetailFragment.newInstance(selectedEvent);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit();
        }));

        // Initial Data Load
        update();

        // Observe Event Manager
        EventManager.getInstance().addView(this);
    }

    /**
     * Configures the exposed dropdown menu (AutoCompleteTextView) for filtering events.
     * <p>
     * It populates the adapter with the filter constants (e.g., "All Events", "Upcoming")
     * and sets the initial selection to "Registration Open". It also attaches a listener
     * to trigger {@link #filterEvents()} whenever the user selects a new option.
     * </p>
     */
    private void setupFilterDropdown() {
        String[] filters = new String[]{FILTER_ALL, FILTER_UPCOMING, FILTER_REG_OPEN, FILTER_CLOSED};
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, filters);
        filterDropdown.setAdapter(filterAdapter);

        // Set default selection text to "Registration Open"
        // The 'false' parameter prevents the dropdown list from popping open immediately
        filterDropdown.setText(FILTER_REG_OPEN, false);

        filterDropdown.setOnItemClickListener((parent, view, position, id) -> {
            currentFilterStatus = parent.getItemAtPosition(position).toString();
            filterEvents();
        });
    }

    /**
     * Core filtering logic that updates the visible event list.
     * <p>
     * This method iterates through the master list ({@code allEvents}) and checks each event
     * against two criteria:
     * <ol>
     * <li><b>Text Search:</b> Checks if the query string is contained within the Event Name OR Location.</li>
     * <li><b>Status Filter:</b>
     * <ul>
     * <li>{@code FILTER_UPCOMING}: Uses {@link Event#isBeforeEventStart()}.</li>
     * <li>{@code FILTER_REG_OPEN}: Uses {@link Event#isRegistrationOpen()}.</li>
     * <li>{@code FILTER_CLOSED}: Uses {@link Event#isEventComplete()}.</li>
     * </ul>
     * </li>
     * </ol>
     * Only events satisfying <b>BOTH</b> criteria are added to the display list ({@code events}).
     * Finally, it notifies the adapter to refresh the UI.
     * </p>
     */
    private void filterEvents() {
        events.clear();
        String lowerQuery = currentSearchQuery.toLowerCase().trim();

        for (Event e : allEvents) {
            boolean matchesSearch = false;
            boolean matchesFilter = false;

            // 1. Check Search Text (Name or Location)
            if (lowerQuery.isEmpty() ||
                    e.getName().toLowerCase().contains(lowerQuery) ||
                    e.getLocation().toLowerCase().contains(lowerQuery)) {
                matchesSearch = true;
            }

            // 2. Check Dropdown Filter
            switch (currentFilterStatus) {
                case FILTER_UPCOMING:
                    matchesFilter = e.isBeforeEventStart();
                    break;
                case FILTER_REG_OPEN:
                    matchesFilter = e.isRegistrationOpen();
                    break;
                case FILTER_CLOSED:
                    matchesFilter = e.isEventComplete();
                    break;
                default:
                    matchesFilter = true;
                    break;
            }

            // 3. If BOTH match, add to list
            if (matchesSearch && matchesFilter) {
                events.add(e);
            }
        }
        eventArrayAdapter.notifyDataSetChanged();
    }

    /**
     * Cleanup method called when the activity is destroyed.
     * <p>
     * Removes this activity from the {@link EventManager}'s observer list to prevent memory leaks.
     * </p>
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeView(this);
    }

    /**
     * MVC Callback: Updates the list when the Model data changes.
     * <p>
     * This method fetches the complete, fresh list of events from the {@link EventManager}.
     * It then calls {@link #filterEvents()} to re-apply the user's current search and
     * filter criteria to the new data set.
     * </p>
     */
    @Override
    public void update() {
        allEvents.clear();
        // Load ALL events from the Manager so we can filter them locally.
        allEvents.addAll(EventManager.getInstance().getEvents());

        filterEvents();
    }
}