package com.example.matrix_events.fragments;

// Imports
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.ProfileManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textview.MaterialTextView;

/*
Need to make a View and follow MV
implements com.example.matrix_events.mvc.View
*/
//SettingsFragment Class
public class SettingsFragment extends Fragment {

    // Declarations
    private MaterialSwitch emailAdminSwitch;
    private MaterialSwitch emailOrganizerSwitch;
    private MaterialSwitch phoneAdminSwitch;
    private MaterialSwitch phoneOrganizerSwitch;
    private MaterialButton logoutButton;
    private MaterialButton deleteAccountButton;
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

        // Initialize all switches and buttons
        emailAdminSwitch = view.findViewById(R.id.email_admin_switch);
        emailOrganizerSwitch = view.findViewById(R.id.email_organizer_switch);
        phoneAdminSwitch = view.findViewById(R.id.phone_admin_switch);
        phoneOrganizerSwitch = view.findViewById(R.id.phone_organizer_switch);
        logoutButton = view.findViewById(R.id.profile_logout_button);
        deleteAccountButton = view.findViewById(R.id.profile_delete_account_button);
        termsConditionsClickable = view.findViewById(R.id.terms_conditions_clickable);
        backButton = view.findViewById(R.id.settings_back_button);

        // Grab Instance of Profile Manager
        profileManager = ProfileManager.getInstance();

        // Get Device ID (unique per device)
        @SuppressLint("HardwareIds") String deviceId = Settings.Secure.getString(requireContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        // High-Level View of Methods Called
        // Load and Populate User Profile, Also calls setupSwitchListeners
        loadProfile(deviceId);
        // Set Button On Click Listeners
        setupButtonListeners();

        return view;
    }
    // Load and Populate User's Notification Preferences
    private void loadProfile(String deviceId) {
        currentProfile = profileManager.getProfileByDeviceId(deviceId);

        if (currentProfile != null) {
            emailAdminSwitch.setChecked(currentProfile.isEmailAdminNotifications());
            emailOrganizerSwitch.setChecked(currentProfile.isEmailOrganizerNotifications());
            phoneAdminSwitch.setChecked(currentProfile.isPhoneAdminNotifications());
            phoneOrganizerSwitch.setChecked(currentProfile.isPhoneOrganizerNotifications());

            setupSwitchListeners(deviceId);
        } else {
            // Broken Toast
            // Toast.makeText(this, "No profile found for this device.", Toast.LENGTH_LONG).show();
        }
    }
    // Method to Set Up All Switch Listeners
    private void setupSwitchListeners(String deviceId) {
        emailAdminSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                showToast("Admin Notifications On " + (isChecked ? "enabled" : "disabled")));

        emailOrganizerSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                showToast("Organizer Notifications On " + (isChecked ? "enabled" : "disabled")));

        phoneAdminSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                showToast("Geolocation Enabled " + (isChecked ? "enabled" : "disabled")));

        phoneOrganizerSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
                showToast("Device ID tracking Enabled " + (isChecked ? "enabled" : "disabled")));
    }

    // Method to Set Up all Button Listeners
    private void setupButtonListeners() {
        logoutButton.setOnClickListener(v ->
                showToast("Logged out successfully"));

        deleteAccountButton.setOnClickListener(v ->
                showToast("Account deleted"));

        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
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

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}