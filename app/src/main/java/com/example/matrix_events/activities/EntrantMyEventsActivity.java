package com.example.matrix_events.activities;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.EventArrayAdapter;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.mvc.View;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class EntrantMyEventsActivity extends AppCompatActivity implements View {

    private ArrayList<Event> waitlistArray;
    private EventArrayAdapter waitlistAdapter;
    private ArrayList<Event> pendingArray;
    private EventArrayAdapter pendingAdapter;
    private ArrayList<Event> acceptedArray;
    private EventArrayAdapter acceptedAdapter;
    private ArrayList<Event> declinedArray;
    private EventArrayAdapter declinedAdapter;
    private ArrayList<Event> notSelectedArray;
    private EventArrayAdapter notSelectedAdapter;

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

        waitlistArray = new ArrayList<>();
        waitlistAdapter = new EventArrayAdapter(getApplicationContext(), waitlistArray);
        ListView waitlistView = findViewById(R.id.waitlist_listview);
        waitlistView.setAdapter(waitlistAdapter);

        pendingArray = new ArrayList<>();
        pendingAdapter = new EventArrayAdapter(getApplicationContext(), pendingArray);
        ListView pendingView = findViewById(R.id.pending_listview);
        pendingView.setAdapter(pendingAdapter);

        acceptedArray = new ArrayList<>();
        acceptedAdapter = new EventArrayAdapter(getApplicationContext(), acceptedArray);
        ListView acceptedView = findViewById(R.id.accepted_listview);
        acceptedView.setAdapter(acceptedAdapter);

        declinedArray = new ArrayList<>();
        declinedAdapter = new EventArrayAdapter(getApplicationContext(), declinedArray);
        ListView declinedView = findViewById(R.id.declined_listview);
        declinedView.setAdapter(declinedAdapter);

        notSelectedArray = new ArrayList<>();
        notSelectedAdapter = new EventArrayAdapter(getApplicationContext(), notSelectedArray);
        ListView notSelectedView = findViewById(R.id.not_selected_listview);
        notSelectedView.setAdapter(notSelectedAdapter);

        buttonLogic();
        update();

        // observe event manager
        EventManager.getInstance().addView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeView(this);
    }

    private void buttonLogic(){
        Button switchToEntrantButton = findViewById(R.id.button_switch_to_org);
        switchToEntrantButton.setOnClickListener(v -> {
            // Go to the Organizer "My Events" screen
            Intent intent = new Intent(EntrantMyEventsActivity.this, OrganizerMyEventsActivity.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void update() {
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Clear arrays
        waitlistArray.clear();
        pendingArray.clear();
        acceptedArray.clear();
        declinedArray.clear();
        notSelectedArray.clear();

        // Organize and add events
        List<Event> allWaitlistedEvents = EventManager.getInstance().getEventsInWaitlist(deviceId);
        for (Event event : allWaitlistedEvents) {
            if (event.isRegistrationClosed()) {
                notSelectedArray.add(event);
            }
            else {
                waitlistArray.add(event);
            }
        }
        pendingArray.addAll(EventManager.getInstance().getEventsInPending(deviceId));
        acceptedArray.addAll(EventManager.getInstance().getEventsInAccepted(deviceId));
        declinedArray.addAll(EventManager.getInstance().getEventsInDeclined(deviceId));

        // Notify changes
        waitlistAdapter.notifyDataSetChanged();
        pendingAdapter.notifyDataSetChanged();
        acceptedAdapter.notifyDataSetChanged();
        declinedAdapter.notifyDataSetChanged();
        notSelectedAdapter.notifyDataSetChanged();
    }
}