package com.example.matrix_events.activities;

import android.content.Intent;
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
import com.example.matrix_events.adapters.EventRecyclerAdapter;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.fragments.EventDetailFragment;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.mvc.View;

import java.util.ArrayList;
import java.util.List;

public class EntrantMyEventsActivity extends AppCompatActivity implements View {

    enum Selection {
        Waitlist,
        NotSelected,
        Accepted,
        Declined,
        Pending
    }
    private Selection selection = Selection.Waitlist;
    private ArrayList<Event> eventArray;
    private EventRecyclerAdapter eventAdapter;
    private TextView listTitleTextview;

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
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_my_events))
                .commit();

        eventArray = new ArrayList<>();
        eventAdapter = new EventRecyclerAdapter(getApplicationContext(), eventArray);
        ListView eventListview = findViewById(R.id.myevents_entrant_listview);
        eventListview.setAdapter(eventAdapter);

        eventListview.setOnItemClickListener(((parent, view, position, id) -> {
            Log.d("DEBUG", "event clicked");
            Event selectedEvent = eventArray.get(position);
            EventDetailFragment fragment = EventDetailFragment.newInstance(selectedEvent);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit();
        }));

        // Go to the Organizer "My Events" screen
        Button switchToEntrantButton = findViewById(R.id.button_switch_to_org);
        switchToEntrantButton.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantMyEventsActivity.this, OrganizerMyEventsActivity.class);
            startActivity(intent);
            finish();
        });

        listTitleTextview = findViewById(R.id.myevents_list_title_textview);

        Button waitlistButton = findViewById(R.id.myevents_waitlisted_button);
        waitlistButton.setOnClickListener(v -> {
            selection = Selection.Waitlist;
            update();
        });
        Button notSelectedButton = findViewById(R.id.myevents_not_selected_button);
        notSelectedButton.setOnClickListener(v -> {
            selection = Selection.NotSelected;
            update();
        });
        Button pendingButton = findViewById(R.id.myevents_pending_button);
        pendingButton.setOnClickListener(v -> {
            selection = Selection.Pending;
            update();
        });
        Button acceptedButton = findViewById(R.id.myevents_accepted_button);
        acceptedButton.setOnClickListener(v -> {
            selection = Selection.Accepted;
            update();
        });
        Button declinedButton = findViewById(R.id.myevents_declined_button);
        declinedButton.setOnClickListener(v -> {
            selection = Selection.Declined;
            update();
        });

        update();

        // observe event manager
        EventManager.getInstance().addView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeView(this);
    }

    @Override
    public void update() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

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