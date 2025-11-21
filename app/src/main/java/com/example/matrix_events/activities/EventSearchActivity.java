package com.example.matrix_events.activities;

import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.EventArrayAdapter;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Geolocation;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.entities.ReoccurringType;
import com.example.matrix_events.fragments.EventDetailFragment;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.mvc.View;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;

public class EventSearchActivity extends AppCompatActivity implements View {
    ArrayList<Event> allEvents = new ArrayList<>();
    ArrayList<Event> events = new ArrayList<>();
    EventArrayAdapter eventArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_event_search))
                .commit();

        eventArrayAdapter = new EventArrayAdapter(getApplicationContext(), events);
        ListView eventListView = findViewById(R.id.event_search_listview);
        eventListView.setAdapter(eventArrayAdapter);
        TextInputEditText searchInput = findViewById(R.id.search_input);

        // Updates Results, when User Types
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) {}
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEvents(s.toString());
            }
        });

        eventListView.setOnItemClickListener(((parent, view, position, id) -> {
            Log.d("DEBUG", "event clicked");
            Event selectedEvent = events.get(position);
            EventDetailFragment fragment = EventDetailFragment.newInstance(selectedEvent);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit();
        }));

        update();

        // observe event manager
        EventManager.getInstance().addView(this);


        // -----------------------------
        // --- TESTING PURPOSES ONLY ---
        // -----------------------------

//        Profile organizer = new Profile(
//                "Alice Johnson",
//                "alice.johnson@example.com",
//                "+1-780-555-0123",
//                Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID)
//        );
//        Geolocation location = new Geolocation(
//                "Yo Mamas House",
//                -113.5244,
//                53.5232
//        );
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
//                null                               // poster
//        );
//        // Add to manager
//        EventManager.getInstance().createEvent(sampleEvent);

        // -----------------------------
    }

    private void filterEvents(String query) {
        events.clear();
        if (query == null || query.trim().isEmpty()) {
            events.addAll(allEvents);
        } else {
            String lower = query.toLowerCase();
            for (Event e : allEvents) {
                if (e.getName().toLowerCase().contains(lower) ||
                    e.getLocation().getName().toLowerCase().contains(lower)) {
                    events.add(e);
                }
            }
        }
        eventArrayAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeView(this);
    }

    @Override
    public void update() {
        allEvents.clear();
        allEvents.addAll(EventManager.getInstance().getEventsRegistrationNotClosed());
        events.clear();
        events.addAll(allEvents);
        eventArrayAdapter.notifyDataSetChanged();
    }
}