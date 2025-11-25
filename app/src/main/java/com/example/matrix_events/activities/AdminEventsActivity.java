package com.example.matrix_events.activities;

import android.os.Bundle;
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
import com.example.matrix_events.fragments.AdminNavigationBarFragment;
import com.example.matrix_events.fragments.EventDetailFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.NotificationManager;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.mvc.View;
import com.google.firebase.Timestamp;
import androidx.appcompat.app.AlertDialog;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AdminEventsActivity extends AppCompatActivity implements View, EventArrayAdapter.OnEventDeleteListener {

    private ArrayList<Event> events;
    private EventArrayAdapter eventArrayAdapter;
    private ListView eventListView;

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

        TextView title = findViewById(R.id.event_search_title_static);
        title.setText("Admin Events");

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, AdminNavigationBarFragment.newInstance(R.id.nav_admin_events))
                .commit();

        events = new ArrayList<>();
        eventListView = findViewById(R.id.event_search_listview);

        eventArrayAdapter = new EventArrayAdapter(this, events, true, this);

        eventListView.setAdapter(eventArrayAdapter);

        eventListView.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = events.get(position);
            EventDetailFragment fragment = EventDetailFragment.newInstance(selectedEvent, true);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        update();

        EventManager.getInstance().addView(this);
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
        if (eventArrayAdapter != null) {
            eventArrayAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onDeleteClick(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete '" + event.getName() + "'? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    cancelAndNotify(event);
                    Toast.makeText(this, "Event deleted and users notified.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void cancelAndNotify(Event event) {
        List<String> usersToNotify = new ArrayList<>();

        if (event.getWaitList() != null) usersToNotify.addAll(event.getWaitList());
        if (event.getPendingList() != null) usersToNotify.addAll(event.getPendingList());
        if (event.getAcceptedList() != null) usersToNotify.addAll(event.getAcceptedList());

        Profile sender = event.getOrganizer();
        String message = "Event '" + event.getName() + "' has been cancelled. Sorry!";
        Timestamp currentTime = Timestamp.now();

        // this loops through all the users to notify and creates a notification for each
        for (String deviceId : usersToNotify) {
            Profile receiver = ProfileManager.getInstance().getProfileByDeviceId(deviceId);
            // if the profile exists, create a notification
            if (receiver != null) {
                Notification notification = new Notification(sender, receiver, message, currentTime);
                NotificationManager.getInstance().createNotification(notification);
            }
        }

        EventManager.getInstance().deleteEvent(event);
    }
}