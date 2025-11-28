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

public class SettingsFragment extends Fragment implements com.example.matrix_events.mvc.View {
    // Declarations
    private String deviceId;
    private boolean isUpdatingUI = false; // Listener Guard Flag
    private MaterialSwitch emailAdminSwitch;
    private MaterialSwitch emailOrganizerSwitch;
    private MaterialSwitch phoneAdminSwitch;
    private MaterialSwitch phoneOrganizerSwitch;
    private MaterialButton logoutButton;
    private MaterialButton deleteProfileButton;
    private MaterialTextView termsConditionsClickable;
    private MaterialButton backButton;

    // Declaration of ProfileManager and Current Profile
    private ProfileManager profileManager;
    private Profile currentProfile;

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
        emailAdminSwitch = view.findViewById(R.id.email_admin_switch);
        emailOrganizerSwitch = view.findViewById(R.id.email_organizer_switch);
        phoneAdminSwitch = view.findViewById(R.id.phone_admin_switch);
        phoneOrganizerSwitch = view.findViewById(R.id.phone_organizer_switch);
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

        update();

        return view;
    }

    // Method to Set Up All Switch Listeners
    private void setupSwitchListeners() {
        emailAdminSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingUI) return; // Listener Guard
            currentProfile.setEmailAdminNotifications(isChecked);
            profileManager.updateProfile(currentProfile);
        });

        emailOrganizerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingUI) return; // Listener Guard
            currentProfile.setEmailOrganizerNotifications(isChecked);
            profileManager.updateProfile(currentProfile);
        });

        phoneAdminSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingUI) return; // Listener Guard
            currentProfile.setPhoneAdminNotifications(isChecked);
            profileManager.updateProfile(currentProfile);
        });

        phoneOrganizerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isUpdatingUI) return; // Listener Guard
            currentProfile.setPhoneOrganizerNotifications(isChecked);
            profileManager.updateProfile(currentProfile);
        });
    }

    // Method to Set Up all Button Listeners
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

    // Logout Confirmation Dialog
    private void showLogoutConfirmationDialog() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (currentProfile != null) {
                        currentProfile = null; // WIP: Find best way to log out of account.
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

    /*
    private void logoutUser() {
        // Optional: clear your locally cached profile
        ProfileManager.getInstance().setCurrentProfile(null);

        // Optional: clear shared preferences if you store login info
        requireActivity()
            .getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply();
    }
    */

    // Delete Profile Confirmation Dialog
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

    // Redirects User To The Main Page
    private void navigateToMain() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    // Toast Method For Simplicity
    private void showToast(String message){
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    // MV Implementation methods
    @Override
    public void onDestroy() {
        super.onDestroy();
        ProfileManager.getInstance().removeView(this);
    }

    // Load and Populate User's Notification Preferences
    @Override
    public void update() {
        if (isUpdatingUI) return; // prevent re-entrant loops
        isUpdatingUI = true; // pause switch listeners

        currentProfile = profileManager.getProfileByDeviceId(deviceId);

        if (currentProfile != null) {
            emailAdminSwitch.setChecked(currentProfile.isEmailAdminNotifications());
            emailOrganizerSwitch.setChecked(currentProfile.isEmailOrganizerNotifications());
            phoneAdminSwitch.setChecked(currentProfile.isPhoneAdminNotifications());
            phoneOrganizerSwitch.setChecked(currentProfile.isPhoneOrganizerNotifications());

        } else {
            showToast("No profile found for this device");
        }
        isUpdatingUI = false; // resume listeners
    }
}