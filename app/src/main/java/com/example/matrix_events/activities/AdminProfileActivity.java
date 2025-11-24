package com.example.matrix_events.activities;

import android.os.Bundle;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.AdminProfileArrayAdapter;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.fragments.AdminNavigationBarFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.mvc.View;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class AdminProfileActivity extends AppCompatActivity implements View {

    private ArrayList<Profile> profiles;
    private AdminProfileArrayAdapter profileArrayAdapter;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.admin_navigation_bar_fragment, AdminNavigationBarFragment.newInstance(R.id.nav_admin_profile))
                .commit();

        profiles = new ArrayList<>();
        ListView adminProfileList = findViewById(R.id.profile_listview);
        profileArrayAdapter = new AdminProfileArrayAdapter(this, profiles);
        adminProfileList.setAdapter(profileArrayAdapter);

        tabLayout = findViewById(R.id.profile_tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                update();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        update();

        ProfileManager.getInstance().addView(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ProfileManager.getInstance().removeView(this);
    }

    @Override
    public void update() {

        profiles.clear();
        List<Profile> allProfiles = ProfileManager.getInstance().getProfiles();
        List<Event> allEvents = EventManager.getInstance().getEvents();
        int selectedTab = tabLayout.getSelectedTabPosition();

        // just shows all the profiles
        if (selectedTab == 0) {
            profiles.addAll(allProfiles);

        } else {
            // go through all profiles
            for (Profile profile : allProfiles) {

                boolean isOrganizer = false;

                // go through all events
                for (Event event : allEvents) {
                    if (event.getOrganizer().getDeviceId().equals(profile.getDeviceId())) { // if the event organizer's device id matches the profile's device id, then the profile is an organizer
                        isOrganizer = true;
                        break;
                    }
                }
                // the entrants tab
                if (selectedTab == 1) {
                    // add to the entrants tab if the profile is NOT an organizer
                    if (!isOrganizer) {
                        profiles.add(profile);
                    }

                    // the organizers tab
                } else if (selectedTab == 2) {
                    // add to the organizers tab if the profile is an organizer
                    if (isOrganizer) {
                        profiles.add(profile);
                    }
                }
            }
        }

        if (profileArrayAdapter != null) {
            profileArrayAdapter.notifyDataSetChanged();
        }

    }
}