package com.example.matrix_events.activities;

import android.content.Intent;
import android.os.Bundle;
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
import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.entities.Poster;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.entities.ReoccurringType;
import com.example.matrix_events.fragments.EventDetailFragment;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.NotificationManager;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.mvc.View;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Activity that displays a list of available events for users to browse and sign up for.
 * It shows events for which registration has not yet closed. Implements the {@link View}
 * interface to stay synchronized with the {@link EventManager}.
 */
public class EventSearchActivity extends AppCompatActivity implements View {

    ArrayList<Event> events;
    EventArrayAdapter eventArrayAdapter;

    /**
     * Called when the activity is first created.
     * This method initializes the user interface, sets up the list view and its adapter,
     * configures the navigation bar, and registers the activity to receive updates from
     * the {@link EventManager}.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     */
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

        events = new ArrayList<>();
        eventArrayAdapter = new EventArrayAdapter(getApplicationContext(), events);
        ListView eventListView = findViewById(R.id.event_listview);
        eventListView.setAdapter(eventArrayAdapter);

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
//
//        Timestamp timestamp = Timestamp.now();
//        Profile albert = ProfileManager.getInstance().getProfileByDeviceId("9bef0d831b027a09");
//        Profile nikolai = ProfileManager.getInstance().getProfileByDeviceId("25053a74eaf65030");
//        Notification message = new Notification(albert, nikolai, "Test notification, hello!", timestamp);
//        NotificationManager.getInstance().createNotification(message);

        // -----------------------------
    }

    /**
     * Called when the activity is being destroyed.
     * This method unregisters the activity from the {@link EventManager} to prevent
     * memory leaks and stop receiving updates.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeView(this);
    }

    /**
     * Updates the UI in response to data changes from the model.
     * This method is called by the {@link EventManager} whenever the event data is modified.
     * It refreshes the list of events to show only those with open registration.
     */
    @Override
    public void update() {
        events.clear();
        events.addAll(EventManager.getInstance().getEventsRegistrationNotClosed());
        eventArrayAdapter.notifyDataSetChanged();
    }
}