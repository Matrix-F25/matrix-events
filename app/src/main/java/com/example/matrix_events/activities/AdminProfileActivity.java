package com.example.matrix_events.activities;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.ProfileArrayAdapter;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.fragments.AdminNavigationBarFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.NotificationManager;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.mvc.View;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.List;

public class AdminProfileActivity extends AppCompatActivity implements View, ProfileArrayAdapter.Listener {
    private ArrayList<Profile> profiles;
    private ProfileArrayAdapter profileArrayAdapter;
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
        profileArrayAdapter = new ProfileArrayAdapter(this, profiles, true, this);
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

            // the organizers tab
        } else if (selectedTab == 1) {

            for (Profile profile : allProfiles) {

                boolean isOrganizer = false;

                // go through all events
                for (Event event : allEvents) {
                    if (event.getOrganizer().getDeviceId().equals(profile.getDeviceId())) { // if the event organizer's device id matches the profile's device id, then the profile is an organizer
                        isOrganizer = true;
                        break;
                    }
                }

                if (isOrganizer) {
                    profiles.add(profile);
                }
            }
        }

        if (profileArrayAdapter != null) {
            profileArrayAdapter.notifyDataSetChanged();
        }
    }

    public void cancelProfile(String deviceID) {

        Profile profileToDelete = ProfileManager.getInstance().getProfileByDeviceId(deviceID);

        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete " + profileToDelete.getName() + "? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteProfile(profileToDelete);
                    Toast.makeText(this, "Profile deleted and removed from all events.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteProfile(Profile profile) {

        String deviceId = profile.getDeviceId();
        List<Event> allEvents = new ArrayList<>(EventManager.getInstance().getEvents());

        for (Event event : allEvents) {

            boolean isModified = false;

            // first, we check if the user is an organizer of an event
            if (event.getOrganizer().getDeviceId().equals(deviceId)) {
                notifyAndDeleteEvent(event); // if they are the organizer, it notifies all the people associated with the event. the profile, along with the event, are also deleted
                continue;
            }

            // remove users from all the lists
            if (event.getWaitList().remove(deviceId)) {
                isModified = true;
            }
            if (event.getPendingList().remove(deviceId)) {
                isModified = true;
            }
            if (event.getAcceptedList().remove(deviceId)) {
                isModified = true;
            }
            if (event.getDeclinedList().remove(deviceId)) {
                isModified = true;
            }

            // update the event if the list was modified
            // no need to update if
            if (isModified) {
                EventManager.getInstance().updateEvent(event);
            }
        }

        // delete the profile
        // profile is deleted normally if they are not the organizer of that event
        ProfileManager.getInstance().deleteProfile(profile);
    }

    // used when the profile to be deleted is an organizer
    private void notifyAndDeleteEvent(Event event) {

        List<String> usersToNotify = new ArrayList<>();

        if (event.getWaitList() != null) {
            usersToNotify.addAll(event.getWaitList());
        }
        if (event.getPendingList() != null) {
            usersToNotify.addAll(event.getPendingList());
        }
        if (event.getAcceptedList() != null) {
            usersToNotify.addAll(event.getAcceptedList());
        }

        Profile sender = event.getOrganizer();
        String message = "Event '" + event.getName() + "' has been cancelled because the organizer's account was removed.";
        Timestamp currentTime = Timestamp.now();

        for (String userId : usersToNotify) {
            Profile receiver = ProfileManager.getInstance().getProfileByDeviceId(userId);
            if (receiver != null) {
                Notification notification = new Notification(sender, receiver, message, currentTime);
                NotificationManager.getInstance().createNotification(notification);
            }
        }

        EventManager.getInstance().deleteEvent(event);
    }
}