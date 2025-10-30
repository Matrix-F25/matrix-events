package com.example.matrix_events.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matrix_events.R;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.fragments.SettingsFragment;
import com.example.matrix_events.managers.ProfileManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileActivity extends AppCompatActivity {

    // Declarations
    private TextInputEditText profileName;
    private TextInputEditText profileEmail;
    private TextInputEditText profilePhoneNumber;
    private MaterialButton updateButton;
    private ImageButton settingsButton;

    private ProfileManager profileManager;

    // Need
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_profile))
                .commit();

        // Initialize Views
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        profilePhoneNumber = findViewById(R.id.profile_phone_number);

        updateButton = findViewById(R.id.profile_update_button);
        settingsButton = findViewById(R.id.profile_settings_button);

        // High-Level View of onCreate
        loadProfile(deviceId);
        updateButton.setOnClickListener(v -> updateProfile(deviceId));
        settingsButton.setOnClickListener(v -> openSettings(deviceId));

        // Load Users Profile and Populate TextInput Fields
        private void loadProfile(deviceId) {
            Profile profile = profileManager.getProfile(deviceId);
            if (profile != null) {
                profileName.setText(Profile.getName());
                profileEmail.setText(Profile.getEmail());
                profilePhoneNumber.setText(Profile.getPhoneNumber());
            }
        }

        // Updates Users Profile
        private void updateProfile(deviceId) {
            String name = profileName.getText().toString().trim();
            String email = profileEmail.getText().toString().trim();
            String phoneNumber = profilePhoneNumber.getText().toString().trim();

            Profile updatedProfile = new Profile(name, email, phoneNumber, deviceIdId);
            profileManager.updateProfile(updatedProfile);

            Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
        }

        // Open Settings Fragment
        private void openSettings() {
            SettingsFragment settingsFragment = new SettingsFragment();

        }
    }
}