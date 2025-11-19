package com.example.matrix_events.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.AdminUserProfileListAdapter;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.fragments.AdminNavigationBarFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.mvc.View;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdminProfileActivity extends AppCompatActivity implements View {

    private static final String TAG = "AdminProfileActivity";

    private static final String PROFILES_COLLECTION = "profiles";
    private static final String EVENTS_COLLECTION = "events";

    private ListView profileListView;
    private ProgressBar loadingIndicator;

    private AdminUserProfileListAdapter adapter;
    private final Set<String> organizerDeviceIds = new HashSet<>();
    private final List<Profile> profileList = new ArrayList<>();
    private ProfileManager profileManager;
    private EventManager eventManager;

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

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.admin_navigation_bar_fragment, AdminNavigationBarFragment.newInstance(R.id.nav_admin_profile))
                .commit();

        profileListView = findViewById(R.id.admin_profile_list_view);
        loadingIndicator = findViewById(R.id.admin_profile_loading_indicator);

        profileManager = ProfileManager.getInstance();
        eventManager = EventManager.getInstance();

        profileManager.addView(this);
        eventManager.addView(this);

        adapter = new AdminUserProfileListAdapter(
                this,
                profileList,
                organizerDeviceIds
        );
        profileListView.setAdapter(adapter);
        showLoading(true);
    }

    @Override
    public void update() {
        Log.d(TAG, "AdminProfileActivity.update() – refreshing profiles from managers");
        profileList.clear();
        List<Profile> profilesFromManager = profileManager.getProfiles();
        if (profilesFromManager != null) {
            profileList.addAll(profilesFromManager);
        }

        organizerDeviceIds.clear();
        List<Event> eventsFromManager = eventManager.getEvents();
        if (eventsFromManager != null) {
            for (Event event : eventsFromManager) {
                if (event == null || event.getOrganizer() == null) continue;

                String deviceId = event.getOrganizer().getDeviceId();
                if (deviceId != null && !deviceId.trim().isEmpty()) {
                    organizerDeviceIds.add(deviceId);
                }
            }
        }

        Log.d(TAG, "Organizer device IDs: " + organizerDeviceIds.toString());

        runOnUiThread(() -> {
            adapter.notifyDataSetChanged();
            showLoading(false);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileManager != null) {
            profileManager.removeView(this);
        }
        if (eventManager != null) {
            eventManager.removeView(this);
        }
    }


    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingIndicator.setVisibility(android.view.View.VISIBLE);
            profileListView.setVisibility(android.view.View.GONE);
        } else {
            loadingIndicator.setVisibility(android.view.View.GONE);
            profileListView.setVisibility(android.view.View.VISIBLE);
        }
    }
}
