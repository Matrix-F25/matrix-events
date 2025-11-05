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
import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.fragments.EventDetailFragment;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.NotificationManager;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.mvc.View;
import com.google.firebase.Timestamp;

import java.util.ArrayList;

public class EventSearchActivity extends AppCompatActivity implements View {

    ArrayList<Event> events;
    EventArrayAdapter eventArrayAdapter;
    ListView eventListView;

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
        eventListView = findViewById(R.id.event_listview);
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
//        Timestamp eventStart = Timestamp.now();
//        Timestamp eventEnd = Timestamp.now();
//        Timestamp registrationStart = Timestamp.now();
//        Timestamp registrationEnd = Timestamp.now();
//        Event sampleEvent = new Event(
//                "Campus Hackathon 2025",             // name
//                "A 24-hour hackathon for students",  // description
//                organizer,                           // organizer
//                location,                            // location
//                eventStart,                          // start time
//                eventEnd,                            // end time
//                200,                                 // event capacity
//                50,                                  // waitlist capacity
//                registrationStart,                   // registration start
//                registrationEnd,                     // registration end
//                true,                                // is reoccurring
//                null,                                // reoccurring end date (none yet)
//                ReoccurringType.Weekly,              // reoccurring type
//                true,                                // geolocation tracking enabled
//                poster                               // poster
//        );
//        EventManager.getInstance().createEvent(sampleEvent);
//
//        Timestamp timestamp = Timestamp.now();
//        Profile albert = ProfileManager.getInstance().getProfileByDeviceId("9bef0d831b027a09");
//        Profile nikolai = ProfileManager.getInstance().getProfileByDeviceId("25053a74eaf65030");
//        Notification message = new Notification(nikolai, albert, "Test notification, hello!", timestamp);
//        NotificationManager.getInstance().createNotification(message);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeView(this);
    }

    @Override
    public void update() {
        events.clear();
        events.addAll(EventManager.getInstance().getEvents());
        eventArrayAdapter.notifyDataSetChanged();
    }
}