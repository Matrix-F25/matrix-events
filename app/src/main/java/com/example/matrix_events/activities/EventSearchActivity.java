package com.example.matrix_events.activities;

// Imports
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.EventRecyclerAdapter;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.fragments.EventDetailFragment;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.mvc.View;

import java.util.ArrayList;

import com.example.matrix_events.utils.EventFilter;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.button.MaterialButton;

import java.util.*;

public class EventSearchActivity extends AppCompatActivity implements View {
    // Attributes
    private TextInputEditText searchBar;
    private MaterialAutoCompleteTextView filterDropdown;
    private MaterialButton dateFilterButton, clearFiltersButton;
    private RecyclerView eventRecyclerView;


    private List<Event> allEvents;
    private List<Event> displayedEvents;
    private EventRecyclerAdapter eventRecyclerAdapter;

    private EventFilter.FilterCriteria criteria = new EventFilter.FilterCriteria();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_search);

        // Layout Setup
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Navigation Bar
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_event_search))
                .commit();

        // Initializations
        searchBar = findViewById(R.id.search_bar);
        filterDropdown = findViewById(R.id.filter_dropdown);
        dateFilterButton = findViewById(R.id.date_filter_button);
        clearFiltersButton = findViewById(R.id.clear_filters_button);
        eventRecyclerView = findViewById(R.id.event_recyclerview);

        // Events list
        allEvents = new ArrayList<>();
        eventRecyclerAdapter = new EventRecyclerAdapter(allEvents, event -> {
            EventDetailFragment fragment = EventDetailFragment.newInstance(event);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        eventRecyclerView = findViewById(R.id.event_recyclerview);
        eventRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventRecyclerView.setAdapter(eventRecyclerAdapter);

        // Methods to Set Up Search Bar and Filters
        setupSearch();
        setupDropdown();
        setupDatePicker();
        setupClearButton();

        // Observe Event Manager
        EventManager.getInstance().addView(this);
        // Initial Update
        update();
    }

//        Profile organizer = new Profile(
//                "Alice Johnson",
//                "alice.johnson@example.com",
//                "+1-780-555-0123",
//                "device123"
//        );
//        Geolocation location = new Geolocation(
//                "University Hall",
//                -113.5244,
//                53.5232
//        );
//        Poster poster = new Poster("https://example.com/posters/hackathon2025.png");
//        // Start from current time
//        Calendar calendar = Calendar.getInstance();
//        // Registration starts in 2 minutes
//        calendar.add(Calendar.MINUTE, 1);
//        Timestamp registrationStart = new Timestamp(calendar.getTime());
//        // Registration ends 2 minutes after it starts
//        calendar.add(Calendar.MINUTE, 1);
//        Timestamp registrationEnd = new Timestamp(calendar.getTime());
//        // Event starts 2 minutes after registration closes
//        calendar.add(Calendar.MINUTE, 1);
//        Timestamp eventStart = new Timestamp(calendar.getTime());
//        // Event ends 2 minutes after it starts
//        calendar.add(Calendar.MINUTE, 1);
//        Timestamp eventEnd = new Timestamp(calendar.getTime());
//        // Reoccurring end is 2 minutes after event ends
//        calendar.add(Calendar.MINUTE, 1);
//        Timestamp reoccurringEnd = new Timestamp(calendar.getTime());
//        // Create event (chronologically valid)
//        Event sampleEvent = new Event(
//                "Campus Hackathon 2025",             // name
//                "A short test hackathon for debugging",  // description
//                organizer,                           // organizer
//                location,                            // location
//                eventStart,                          // event start
//                eventEnd,                            // event end
//                1,                                   // event capacity
//                50,                                  // waitlist capacity
//                registrationStart,                   // registration start
//                registrationEnd,                     // registration end
//                true,                                // is reoccurring
//                reoccurringEnd,                      // reoccurring end
//                ReoccurringType.Weekly,              // reoccurring type
//                true,                                // geolocation tracking
//                poster                               // poster
//        );
//        // Add to manager
//        EventManager.getInstance().createEvent(sampleEvent);

//        Timestamp timestamp = Timestamp.now();
//        Profile albert = ProfileManager.getInstance().getProfileByDeviceId("9bef0d831b027a09");
//        Profile nikolai = ProfileManager.getInstance().getProfileByDeviceId("25053a74eaf65030");
//        Notification message = new Notification(nikolai, albert, "Test notification, hello!", timestamp);
//        NotificationManager.getInstance().createNotification(message);

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeView(this);
    }

    @Override
    public void update() {
        allEvents.clear();
        allEvents.addAll(EventManager.getInstance().getEventsRegistrationNotClosed());
        eventRecyclerAdapter.notifyDataSetChanged();
    }

    private void setupSearch() {
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                criteria.query = s.toString();
                applyFilters();
            }
        });
    }

    private void setupDropdown() {
        final String[] options = {"All", "Filled", "Available"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, options);
        filterDropdown.setAdapter(adapter);
        filterDropdown.setText("All", false);
        filterDropdown.setOnItemClickListener((p, v, pos, id) -> {
            criteria.availability = options[pos];
            applyFilters();
        });
    }

    private void setupDatePicker() {
        dateFilterButton.setOnClickListener(v -> {
            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .build();
            picker.addOnPositiveButtonClickListener(sel -> {
                criteria.date = new Date(sel);
                dateFilterButton.setText(android.text.format.DateFormat.format("MMM d", criteria.date));
                applyFilters();
            });
            picker.show(getSupportFragmentManager(), "date_picker");
        });
    }

    private void setupClearButton() {
        clearFiltersButton.setOnClickListener(v -> {
            criteria.query = "";
            criteria.availability = "All";
            criteria.date = null;

            searchBar.setText("");
            filterDropdown.setText("All", false);
            dateFilterButton.setText("");
            applyFilters();
        });
    }

    private void applyFilters() {
        displayedEvents.clear();
        displayedEvents.addAll(EventFilter.filterEvents(allEvents, criteria));
        eventRecyclerAdapter.notifyDataSetChanged();
    }
}