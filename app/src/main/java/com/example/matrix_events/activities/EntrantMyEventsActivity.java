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

public class EntrantMyEventsActivity extends AppCompatActivity implements View {

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

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        eventArray = new ArrayList<>();
        eventAdapter = new EventArrayAdapter(getApplicationContext(), eventArray);
        ListView eventListview = findViewById(R.id.entrant_listview);
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

        // Go to the Organizer "My Events" Activity
        Button switchToOrganizerButton = findViewById(R.id.entrant_switch_to_org_button);
        switchToOrganizerButton.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantMyEventsActivity.this, OrganizerMyEventsActivity.class);
            startActivity(intent);
            finish();
        });

        listTitleTextview = findViewById(R.id.entrant_list_title_textview);

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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeView(this);
    }

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