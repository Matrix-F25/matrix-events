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

/**
 * Activity responsible for displaying and editing the user's personal profile.
 * <p>
 * This class serves as a <b>View</b> in the MVC architecture. It allows users to:
 * <ul>
 * <li>View their current details (Name, Email, Phone).</li>
 * <li>Update their profile information in the database.</li>
 * <li>Navigate to the application settings.</li>
 * </ul>
 * It observes the {@link ProfileManager} to ensure the displayed data is always synchronized
 * with the Firestore database.
 * </p>
 */
public class ProfileActivity extends AppCompatActivity implements View {
    // Declarations
    private TextInputEditText profileName;
    private TextInputEditText profileEmail;
    private TextInputEditText profilePhoneNumber;

    // Declaration of ProfileManager and Current Profile
    private ProfileManager profileManager;
    private Profile currentProfile;
    private String deviceId;

    /**
     * Called when the activity is starting.
     * <p>
     * Initializes the UI layout, sets up the bottom navigation bar, and retrieves the
     * unique Android Device ID. It also registers click listeners for the "Update" and
     * "Settings" buttons and registers this activity as an observer of the {@link ProfileManager}.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied.
     */
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

    /**
     * Reads input from the text fields and updates the user's profile in the database.
     * <p>
     * This method performs basic validation and sanitization:
     * <ul>
     * <li>Trims whitespace from inputs.</li>
     * <li>Converts an empty phone number string to {@code null} to maintain database consistency.</li>
     * </ul>
     * Upon completion, it triggers an asynchronous update via {@link ProfileManager#updateProfile(Profile)}.
     * </p>
     */
    private void updateProfile() {
        if (currentProfile == null) {
            Toast.makeText(this, "No profile to update.", Toast.LENGTH_LONG).show();
            return;
        }

        String name = Objects.requireNonNull(profileName.getText()).toString().trim();
        String email = Objects.requireNonNull(profileEmail.getText()).toString().trim();
        String phoneNumber = profilePhoneNumber.getText().toString().trim();

        // Handle optional phone number
        if (phoneNumber.equals("")) {
            phoneNumber = null;
        }

        currentProfile.setName(name);
        currentProfile.setEmail(email);
        currentProfile.setPhoneNumber(phoneNumber);

        profileManager.updateProfile(currentProfile);

        Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Navigates to the {@link SettingsFragment}.
     * <p>
     * Opens the settings screen as a fragment overlay on top of the current activity,
     * adding the transaction to the back stack so users can navigate back.
     * </p>
     */
    private void openSettings() {
        SettingsFragment settingsFragment = new SettingsFragment();

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, settingsFragment)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Cleanup method called when the activity is destroyed.
     * <p>
     * Unregisters this activity from the {@link ProfileManager} to prevent memory leaks.
     * </p>
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProfileManager.getInstance().removeView(this);
    }

    /**
     * MVC Callback: Updates the UI when the Model data changes.
     * <p>
     * This method retrieves the specific profile associated with the current {@code deviceId}.
     * If the profile exists, it populates the {@link TextInputEditText} fields with the
     * user's current Name, Email, and Phone Number.
     * </p>
     */
    @Override
    public void update() {
        currentProfile = profileManager.getProfileByDeviceId(deviceId);

        if (currentProfile != null) {
            profileName.setText(currentProfile.getName());
            profileEmail.setText(currentProfile.getEmail());
            profilePhoneNumber.setText(currentProfile.getPhoneNumber());
        }
    }
}