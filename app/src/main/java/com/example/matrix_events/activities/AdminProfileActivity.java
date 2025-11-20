package com.example.matrix_events.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AdminProfileActivity extends AppCompatActivity {

    private static final String TAG = "AdminProfileActivity";

    private static final String PROFILES_COLLECTION = "profiles";
    private static final String EVENTS_COLLECTION = "events";

    private ListView profileListView;
    private ProgressBar loadingIndicator;

    private AdminUserProfileListAdapter adapter;
    private final List<Profile> profiles = new ArrayList<>();
    private final Set<String> organizerDeviceIds = new HashSet<>();

    private FirebaseFirestore db;

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

        // Nav bar fragment
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.admin_navigation_bar_fragment,
                        AdminNavigationBarFragment.newInstance(R.id.nav_admin_profile))
                .commit();

        db = FirebaseFirestore.getInstance();

        profileListView = findViewById(R.id.admin_profile_list_view);
        loadingIndicator = findViewById(R.id.admin_profile_loading_indicator);

        adapter = new AdminUserProfileListAdapter(this, profiles, organizerDeviceIds);
        profileListView.setAdapter(adapter);


        showLoading(true);
        loadProfilesThenEvents();
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingIndicator.setVisibility(View.VISIBLE);
            profileListView.setVisibility(View.GONE);
        } else {
            loadingIndicator.setVisibility(View.GONE);
            profileListView.setVisibility(View.VISIBLE);
        }
    }

    private void loadProfilesThenEvents() {
        db.collection(PROFILES_COLLECTION)
                .get()
                .addOnSuccessListener(this::handleProfilesLoaded)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load profiles", e);
                    Toast.makeText(this, "Failed to load profiles: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    showLoading(false);
                });
    }

    private void handleProfilesLoaded(QuerySnapshot snapshot) {
        profiles.clear();

        for (DocumentSnapshot doc : snapshot) {
            Profile profile = doc.toObject(Profile.class);
            if (profile != null) {
                profile.setId(doc.getId());

                Boolean adminVal = doc.getBoolean("admin");
                if (adminVal != null) {
                    profile.setAdmin(adminVal);
                }

                profiles.add(profile);
            }
        }
        loadEventsAndMarkOrganizers();
    }

    private void loadEventsAndMarkOrganizers() {
        db.collection(EVENTS_COLLECTION)
                .get()
                .addOnSuccessListener(this::handleEventsLoaded)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load events", e);
                    Toast.makeText(this, "Failed to load events: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    // Even if events fail, show profiles list
                    showLoading(false);
                    adapter.notifyDataSetChanged();
                });
    }

    @SuppressWarnings("unchecked")
    private void handleEventsLoaded(QuerySnapshot snapshot) {
        organizerDeviceIds.clear();

        for (DocumentSnapshot doc : snapshot) {
            Map<String, Object> organizerMap = (Map<String, Object>) doc.get("organizer");
            if (organizerMap == null) continue;

            Object deviceIdObj = organizerMap.get("deviceId");
            if (deviceIdObj instanceof String) {
                String deviceId = (String) deviceIdObj;
                if (deviceId != null && !deviceId.trim().isEmpty()) {
                    organizerDeviceIds.add(deviceId);
                }
            }
        }

        Log.d(TAG, "Organizer device IDs: " + organizerDeviceIds);

        adapter.notifyDataSetChanged();
        showLoading(false);
    }
}
