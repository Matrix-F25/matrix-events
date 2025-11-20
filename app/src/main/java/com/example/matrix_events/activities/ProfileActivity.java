package com.example.matrix_events.activities;

import android.os.Bundle;
import android.provider.Settings;
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
import com.example.matrix_events.mvc.View;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

public class ProfileActivity extends AppCompatActivity implements View {
    // Declarations
    private TextInputEditText profileName;
    private TextInputEditText profileEmail;
    private TextInputEditText profilePhoneNumber;

    // Declaration of ProfileManager and Current Profile
    private ProfileManager profileManager;
    private Profile currentProfile;
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

        // Initialize Navigation Bar Fragment
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_profile))
                .commit();

        // Get Device ID (unique per device)
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize View Attributes
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        profilePhoneNumber = findViewById(R.id.profile_phone_number);
        MaterialButton updateButton = findViewById(R.id.profile_update_button);
        ImageButton settingsButton = findViewById(R.id.profile_settings_button);

        // Grab Instance of Profile Manager
        profileManager = ProfileManager.getInstance();

        // Observe Profile Manager
        profileManager.addView(this);

        // Set On Click Listeners
        updateButton.setOnClickListener(v -> updateProfile());
        settingsButton.setOnClickListener(v -> openSettings());

        update();
    }

    // Update User's Profile
    private void updateProfile() {
        if (currentProfile == null) {
            Toast.makeText(this, "No profile to update.", Toast.LENGTH_LONG).show();
            return;
        }

        String name = Objects.requireNonNull(profileName.getText()).toString().trim();
        String email = Objects.requireNonNull(profileEmail.getText()).toString().trim();
        String phoneNumber = Objects.requireNonNull(profilePhoneNumber.getText()).toString().trim();

        currentProfile.setName(name);
        currentProfile.setEmail(email);
        currentProfile.setPhoneNumber(phoneNumber);

        profileManager.updateProfile(currentProfile);

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
    }

    // Open Settings Fragment
    private void openSettings() {
        SettingsFragment settingsFragment = new SettingsFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, settingsFragment)
                .addToBackStack(null)
                .commit();
    }

    // MV Implementation methods
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProfileManager.getInstance().removeView(this);
    }

    // Load User's Profile and Populate TextInput Fields
    @Override
    public void update() {
        currentProfile = profileManager.getProfileByDeviceId(deviceId);

        if (currentProfile != null) {
            profileName.setText(currentProfile.getName());
            profileEmail.setText(currentProfile.getEmail());
            profilePhoneNumber.setText(currentProfile.getPhoneNumber());
        } else {
            Toast.makeText(this, "No profile found for this device.", Toast.LENGTH_LONG).show();
        }
    }
}