package com.example.matrix_events.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.example.matrix_events.activities.MainActivity;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.ProfileManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textview.MaterialTextView;

/**
 * Fragment responsible for managing user application settings.
 * <p>
 * This fragment allows users to:
 * <ul>
 * <li>Toggle push notification preferences for Admin and Organizer updates.</li>
 * <li>View Terms and Conditions.</li>
 * <li>Log out of the application.</li>
 * <li>Permanently delete their profile and associated data.</li>
 * </ul>
 * </p>
 * <p>
 * It implements the {@link com.example.matrix_events.mvc.View} interface to synchronize
 * switch states with the {@link ProfileManager} (Model).
 * </p>
 */
public class SettingsFragment extends Fragment implements com.example.matrix_events.mvc.View {

    // UI Components
    private MaterialSwitch pushAdminSwitch;
    private MaterialSwitch pushOrganizerSwitch;
    private MaterialButton logoutButton;
    private MaterialButton deleteProfileButton;
    private MaterialTextView termsConditionsClickable;
    private MaterialButton backButton;

    // Data Management
    private String deviceId;
    private ProfileManager profileManager;
    private Profile currentProfile;

    /**
     * Flag to prevent infinite loops between model updates and UI listeners.
     * When {@code true}, switch listeners ignore changes.
     */
    private boolean isUpdatingUI = false;

    /**
     * Called to have the fragment instantiate its user interface view.
     * <p>
     * Initializes UI components, retrieves the Device ID, registers with the {@link ProfileManager},
     * and sets up all click and check change listeners.
     * </p>
     *
     * @param inflater           The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate Fragment Layout
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Get Device ID (unique per device)
        deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // Initialize all switches and buttons
        pushAdminSwitch = view.findViewById(R.id.push_admin_switch);
        pushOrganizerSwitch = view.findViewById(R.id.push_organizer_switch);
        logoutButton = view.findViewById(R.id.profile_logout_button);
        deleteProfileButton = view.findViewById(R.id.profile_delete_button);
        termsConditionsClickable = view.findViewById(R.id.terms_conditions_clickable);
        backButton = view.findViewById(R.id.settings_back_button);

        // Grab Instance of Profile Manager
        profileManager = ProfileManager.getInstance();
        // Observe Profile Manager
        profileManager.addView(this);

        // Setup Listeners
        setupButtonListeners();
        setupSwitchListeners();

        // Initial Data Load
        update();

        return view;
    }

    /**
     * Configures listeners for the Push Notification switches.
     * <p>
     * Uses the {@link #isUpdatingUI} flag to differentiate between user interactions
     * (which should update the model) and model updates (which should just update the UI).
     * </p>
     */
    private void setupSwitchListeners() {
        pushAdminSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingUI) return; // Listener Guard: Ignore programmatic changes
            if (currentProfile != null) {
                currentProfile.setPushAdminNotifications(isChecked);
                profileManager.updateProfile(currentProfile);
            }
        });

        pushOrganizerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingUI) return; // Listener Guard
            if (currentProfile != null) {
                currentProfile.setPushOrganizerNotifications(isChecked);
                profileManager.updateProfile(currentProfile);
            }
        });
    }

    /**
     * Configures click listeners for all interactive buttons.
     * <p>
     * Includes logic for Logout, Delete Profile, Back navigation, and opening
     * the Terms and Conditions fragment.
     * </p>
     */
    private void setupButtonListeners() {
        logoutButton.setOnClickListener(v ->
                showLogoutConfirmationDialog());

        deleteProfileButton.setOnClickListener(v ->
                showDeleteConfirmationDialog());

        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            } else {
                showToast("Fragment Not Attached to Activity");
            }
        });

        termsConditionsClickable.setOnClickListener(v -> {
            TermsConditionsFragment termsConditionsFragment = new TermsConditionsFragment();
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, termsConditionsFragment)
                    .addToBackStack(null) // So User Can Press Back
                    .commit();
        });
    }

    /**
     * Displays a confirmation dialog for logging out.
     * <p>
     * If confirmed, the user is navigated back to the Main Activity, effectively
     * resetting the navigation stack.
     * </p>
     */
    private void showLogoutConfirmationDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (currentProfile != null) {
                        currentProfile = null; // Clear local reference
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            showToast("Log Out Successful");
                            navigateToMain();
                        }, 800);
                    } else {
                        showToast("No profile found for this device");
                    }
                })
                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Displays a strict warning dialog for profile deletion.
     * <p>
     * If confirmed, this action triggers a cascade delete:
     * <ol>
     * <li>Removes the user from all events they have joined/organized.</li>
     * <li>Deletes the user profile from Firestore.</li>
     * <li>Redirects to the Main Activity.</li>
     * </ol>
     * </p>
     */
    private void showDeleteConfirmationDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Delete Profile Confirmation")
                .setMessage("Are you sure you want to delete your profile? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (currentProfile != null) {

                        EventManager.getInstance().removeFromAllEvents(deviceId);
                        profileManager.deleteProfile(currentProfile);

                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            showToast("Profile Deleted Successfully");
                            navigateToMain();
                        }, 800);
                    } else {
                        showToast("No profile found for this device");
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Helper method to restart the application flow by navigating to MainActivity.
     * Clears the activity task stack to prevent back navigation to the deleted profile.
     */
    private void navigateToMain() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Displays a short duration toast message.
     *
     * @param message The text to display.
     */
    private void showToast(String message){
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Cleanup method called when the fragment is destroyed.
     * Removes this view from the ProfileManager to prevent memory leaks.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        ProfileManager.getInstance().removeView(this);
    }

    /**
     * MVC Callback: Updates the UI when the Model data changes.
     * <p>
     * Fetches the latest profile data and updates the notification switches.
     * Uses {@link #isUpdatingUI} to prevent the switch listeners from firing
     * and causing an infinite update loop.
     * </p>
     */
    @Override
    public void update() {
        if (isUpdatingUI) return; // prevent re-entrant loops
        isUpdatingUI = true; // pause switch listeners

        currentProfile = profileManager.getProfileByDeviceId(deviceId);

        if (currentProfile != null) {
            pushAdminSwitch.setChecked(currentProfile.isPushAdminNotifications());
            pushOrganizerSwitch.setChecked(currentProfile.isPushOrganizerNotifications());

        } else {
            showToast("No profile found for this device");
        }
        isUpdatingUI = false; // resume listeners
    }
}