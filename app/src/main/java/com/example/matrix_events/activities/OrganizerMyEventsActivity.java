package com.example.matrix_events.activities;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.fragments.EventCreateFragment;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.managers.ProfileManager;

public class OrganizerMyEventsActivity extends AppCompatActivity {

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

        // Switch to Entrant
        Button switchToEntrantButton = findViewById(R.id.organizer_switch_to_entrant_button);
        if (switchToEntrantButton != null) {
            switchToEntrantButton.setOnClickListener(v -> {
                Intent intent = new Intent(OrganizerMyEventsActivity.this, EntrantMyEventsActivity.class);
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

        // Go to the Admin Activity, if profile has necessary permissions
        Button switchToAdminButton = findViewById(R.id.organizer_switch_to_admin_button);
        switchToAdminButton.setVisibility(INVISIBLE);
        String deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        Profile currentProfile = ProfileManager.getInstance().getProfileByDeviceId(deviceId);
        if (currentProfile.isAdmin()) {
            switchToAdminButton.setVisibility(VISIBLE);
            switchToAdminButton.setOnClickListener(v -> {
                Intent intent = new Intent(OrganizerMyEventsActivity.this, AdminActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}