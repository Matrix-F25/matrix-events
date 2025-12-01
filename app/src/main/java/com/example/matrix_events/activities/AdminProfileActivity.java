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
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.fragments.AdminNavigationBarFragment;
import com.example.matrix_events.fragments.AdminProfileDetailsFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.mvc.View;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity responsible for the Administrator's view of user profiles.
 * <p>
 * This activity provides a dashboard for Admins to:
 * <ul>
 * <li><b>Browse:</b> View a list of all registered users.</li>
 * <li><b>Filter:</b> Toggle between viewing "All Profiles" and only "Organizers" (users who have created events).</li>
 * <li><b>Manage:</b> Delete user profiles (which cascades to remove them from events or cancel events they organized).</li>
 * </ul>
 * It implements {@link View} to observe changes in the {@link ProfileManager} and uses the
 * {@link ProfileArrayAdapter.Listener} interface to handle delete actions triggered from the list rows.
 * </p>
 */
public class AdminProfileActivity extends AppCompatActivity implements View, ProfileArrayAdapter.Listener {
    private ArrayList<Profile> profiles;
    private ProfileArrayAdapter profileArrayAdapter;
    private TabLayout tabLayout;

    /**
     * Called when the activity is starting.
     * <p>
     * Initializes the UI, sets up the Admin Navigation Bar, and configures the ListView with
     * the {@link ProfileArrayAdapter}. It also sets up listeners for:
     * <ul>
     * <li><b>List Item Clicks:</b> Opens the {@link AdminProfileDetailsFragment} for the selected user.</li>
     * <li><b>Tab Selection:</b> Triggers {@link #update()} to filter the list based on the selected tab.</li>
     * </ul>
     * Finally, it registers this activity as an observer of the {@link ProfileManager}.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied.
     */
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
        ListView adminProfileListView = findViewById(R.id.profile_listview);
        // Pass 'this' as the listener for delete actions, and 'true' to enable the delete button
        profileArrayAdapter = new ProfileArrayAdapter(this, profiles, true, this);
        adminProfileListView.setAdapter(profileArrayAdapter);

        adminProfileListView.setOnItemClickListener((parent, view, position, id) -> {
            Profile selectedProfile = profiles.get(position);
            AdminProfileDetailsFragment fragment = AdminProfileDetailsFragment.newInstance(selectedProfile);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        tabLayout = findViewById(R.id.profile_tabs);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                update();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });

        update();

        ProfileManager.getInstance().addView(this);
    }

    /**
     * Cleanup method called when the activity is destroyed.
     * <p>
     * Unregisters this activity from the {@link ProfileManager} to prevent memory leaks.
     * </p>
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        ProfileManager.getInstance().removeView(this);
    }

    /**
     * MVC Callback: Updates the list when the Model data changes or the filter tab changes.
     * <p>
     * This method fetches the master list of profiles and filters it based on the currently selected tab:
     * <ul>
     * <li><b>Tab 0 (All Profiles):</b> Displays every user in the system.</li>
     * <li><b>Tab 1 (Organizers):</b> Cross-references {@link EventManager} to find users who are listed
     * as the organizer for at least one event. Only these users are displayed.</li>
     * </ul>
     * </p>
     */
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

                // go through all events to check for organizer status
                for (Event event : allEvents) {
                    // if the event organizer's device id matches the profile's device id, then the profile is an organizer
                    if (event.getOrganizer().getDeviceId().equals(profile.getDeviceId())) {
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

    /**
     * Handles the deletion of a user profile triggered from the list adapter.
     * <p>
     * This method displays an {@link AlertDialog} to confirm the destructive action.
     * If confirmed, it performs a two-step deletion process:
     * <ol>
     * <li><b>Clean Up Events:</b> Calls {@link EventManager#removeFromAllEvents(String)} to remove the user
     * from waitlists/attendee lists, or cancel events they organized.</li>
     * <li><b>Delete Profile:</b> Calls {@link ProfileManager#deleteProfile(Profile)} to remove the user record.</li>
     * </ol>
     * </p>
     *
     * @param deviceID The unique device ID of the profile to be deleted.
     */
    @Override
    public void cancelProfile(String deviceID) {

        Profile profileToDelete = ProfileManager.getInstance().getProfileByDeviceId(deviceID);

        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete " + profileToDelete.getName() + "? This will also cancel and delete any events they organized.")
                .setPositiveButton("Delete", (dialog, which) -> {

                    EventManager.getInstance().removeFromAllEvents(deviceID); // first, remove the user from all events
                    ProfileManager.getInstance().deleteProfile(profileToDelete); // then, delete the profile

                    Toast.makeText(this, "Profile deleted.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}