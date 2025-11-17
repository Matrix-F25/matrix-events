package com.example.matrix_events.activities;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

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
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.EventArrayAdapter;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.fragments.EventCreateFragment;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.mvc.View;

import java.util.ArrayList;

public class OrganizerMyEventsActivity extends AppCompatActivity implements View {
    enum Selection {
        NotClosed,
        Closed
    }
    private Selection selection = Selection.NotClosed;
    private String deviceId;
    private ArrayList<Event> eventArray;
    private EventArrayAdapter eventAdapter;
    private TextView listTitleTextview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_organizer_my_events);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets sb = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(sb.left, sb.top, sb.right, sb.bottom);
            return insets;
        });
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_my_events))
                .commit();

        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        eventArray = new ArrayList<>();
        eventAdapter = new EventArrayAdapter(getApplicationContext(), eventArray);
        ListView eventListview = findViewById(R.id.organizer_listview);
        eventListview.setAdapter(eventAdapter);

        eventListview.setOnItemClickListener(((parent, view, position, id) -> {
            Log.d("DEBUG", "event clicked");
        }));

        // Go to the Entrant "My Events" Activity
        Button switchToEntrantButton = findViewById(R.id.organizer_switch_to_entrant_button);
        if (switchToEntrantButton != null) {
            switchToEntrantButton.setOnClickListener(v -> {
                Intent intent = new Intent(OrganizerMyEventsActivity.this, EntrantMyEventsActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // Go to the Admin Activity, if profile has necessary permissions
        Button switchToAdminButton = findViewById(R.id.organizer_switch_to_admin_button);
        switchToAdminButton.setVisibility(INVISIBLE);
        Profile currentProfile = ProfileManager.getInstance().getProfileByDeviceId(deviceId);
        if (currentProfile.isAdmin()) {
            switchToAdminButton.setVisibility(VISIBLE);
            switchToAdminButton.setOnClickListener(v -> {
                Intent intent = new Intent(OrganizerMyEventsActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            });
        }

        // Create Event
        Button createEventButton = findViewById(R.id.organizer_create_event_button);
        if (createEventButton != null) {
            createEventButton.setOnClickListener(v -> {
                Fragment fragment = new EventCreateFragment();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main, fragment).addToBackStack(null).commit();
            });
        }

        listTitleTextview = findViewById(R.id.organizer_list_title_textview);

        Button registrationNotClosedButton = findViewById(R.id.organizer_reg_not_closed_button);
        registrationNotClosedButton.setOnClickListener(v -> {
            selection = Selection.NotClosed;
            update();
        });
        Button registrationClosedButton = findViewById(R.id.organizer_reg_closed_button);
        registrationClosedButton.setOnClickListener(v -> {
            selection = Selection.Closed;
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
        eventArray.clear();
        switch (selection) {
            case NotClosed: {
                listTitleTextview.setText("Upcoming and Registration Open");
                eventArray.addAll(EventManager.getInstance().getOrganizerEventsRegistrationNotClosed(deviceId));
                break;
            }
            case Closed: {
                listTitleTextview.setText("Registration Closed");
                eventArray.addAll(EventManager.getInstance().getOrganizerEventsRegistrationClosed(deviceId));
                break;
            }
        }
        eventAdapter.notifyDataSetChanged();
    }
}