package com.example.matrix_events.activities;

import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.matrix_events.R;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.fragments.SettingsFragment;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.mvc.View;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

/**
 * Activity responsible for displaying and editing the user's profile information.
 * <p>
 * This class acts as the <b>Controller</b> in the MVC architecture for the Profile screen.
 * It interacts with the {@link ProfileManager} (Model) to fetch and update user data,
 * and updates the UI (View) to reflect the current state of the {@link Profile}.
 * </p>
 * <p>
 * <b>Key Features:</b>
 * <ul>
 * <li>View and Edit Name, Email, and Phone Number.</li>
 * <li>Upload and update Profile Picture using the device's gallery.</li>
 * <li>Navigate to the Settings screen.</li>
 * <li>Automatic data synchronization via the {@link View} interface.</li>
 * </ul>
 * </p>
 */
public class ProfileActivity extends AppCompatActivity implements View {

    // UI Components
    private ShapeableImageView profilePictureView;
    private TextInputEditText profileName;
    private TextInputEditText profileEmail;
    private TextInputEditText profilePhoneNumber;

    // Image Picking Logic
    private ActivityResultLauncher<String> pickImageLauncher;
    private Uri selectedImageUri;

    // Data Management
    private ProfileManager profileManager;
    private Profile currentProfile;
    private String deviceId;

    /**
     * Called when the activity is first created.
     * <p>
     * Initializes the UI layout, sets up the Navigation Bar, registers the Image Picker,
     * and subscribes to the {@link ProfileManager} for data updates.
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently
     * supplied in {@link #onSaveInstanceState}.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        // Handle Window Insets for Edge-to-Edge display
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

        // Get Device ID (unique per device) used to identify the user
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize UI Attributes
        profilePictureView = findViewById(R.id.profile_picture_imageView);
        MaterialButton profilePictureAddButton = findViewById(R.id.profile_picture_add_button);
        profileName = findViewById(R.id.profile_name);
        profileEmail = findViewById(R.id.profile_email);
        profilePhoneNumber = findViewById(R.id.profile_phone_number);
        MaterialButton updateButton = findViewById(R.id.profile_update_button);
        ImageButton settingsButton = findViewById(R.id.profile_settings_button);

        // Grab Instance of Profile Manager (Singleton)
        profileManager = ProfileManager.getInstance();

        // Register as a View to observe model changes
        profileManager.addView(this);

        // Set Profile Picture (Default if none)
        loadProfilePictureIfAvailable();

        // Register the Image Picker (MIME type "image/*")
        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedImageUri = uri;
                        // Show preview immediately using Glide
                        Glide.with(this)
                                .load(uri)
                                .circleCrop()
                                .into(profilePictureView);

                        // Automatically upload to Firebase Storage
                        uploadSelectedProfileImage();
                    }
                }
        );

        // Set On Click Listeners
        profilePictureAddButton.setOnClickListener(v -> {
            // Launch Picker for Images
            pickImageLauncher.launch("image/*");
        });
        updateButton.setOnClickListener(v -> updateProfile());
        settingsButton.setOnClickListener(v -> openSettings());

        // Initial data load
        update();
    }

    /**
     * Loads the user's profile picture into the ImageView.
     * <p>
     * If the profile has a valid URL, it uses Glide to load the image asynchronously.
     * If no URL exists, it sets a default placeholder drawable.
     * </p>
     */
    private void loadProfilePictureIfAvailable() {
        Profile p = profileManager.getProfileByDeviceId(deviceId);
        if (p != null && p.getProfilePictureUrl() != null && !p.getProfilePictureUrl().isEmpty()) {
            Glide.with(this).load(p.getProfilePictureUrl()).into(profilePictureView);
        } else {
            // Show Default Placeholder
            profilePictureView.setImageResource(R.drawable.default_profile_picture);
        }
    }

    /**
     * Uploads the selected image URI to Firebase Storage.
     * <p>
     * This method is triggered immediately after the user selects an image from their gallery.
     * It uses {@link ProfileManager#uploadProfilePicture} to handle the upload and database update.
     * </p>
     */
    private void uploadSelectedProfileImage() {
        if (selectedImageUri == null) return;

        Profile current = profileManager.getProfileByDeviceId(deviceId);
        if (current == null) {
            Toast.makeText(this, "No profile found; please sign up first.", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d("ProfileActivity", "Uploading profile picture for deviceId=" + current.getDeviceId() + " URI=" + selectedImageUri);

        profileManager.uploadProfilePicture(selectedImageUri, current, new ProfileManager.ProfileImageUploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) {
                        Glide.with(ProfileActivity.this)
                                .load(downloadUrl)
                                .circleCrop()
                                .into(profilePictureView);
                        Toast.makeText(ProfileActivity.this, "Profile picture updated", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) { // Safe UI update
                        Toast.makeText(ProfileActivity.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }


    /**
     * Collects data from input fields and updates the user's profile in the database.
     * <p>
     * Validation Logic:
     * <ul>
     * <li>Name and Email are mandatory (though current validation is basic).</li>
     * <li>If Phone Number is empty, it is saved as {@code null} in the database.</li>
     * </ul>
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
     * Navigates to the Settings screen.
     * <p>
     * Replaces the current fragment container with the {@link SettingsFragment}.
     * This transaction is added to the back stack so the user can navigate back.
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
     * Cleanup method called when the Activity is destroyed.
     * <p>
     * Removes this Activity from the {@link ProfileManager}'s list of observers
     * to prevent memory leaks.
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
     * This method fetches the latest profile data based on the device ID and populates
     * the Name, Email, and Phone fields. If no profile is found, a warning Toast is shown.
     * </p>
     */
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