package com.example.matrix_events.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textview.MaterialTextView;

// Need to make a View and follow MV
    // implements com.example.matrix_events.mvc.View
public class SettingsFragment extends Fragment {

    // Declarations
    private MaterialSwitch switchAdmin;
    private MaterialSwitch switchOrganizer;
    private MaterialSwitch switchGeolocation;
    private MaterialSwitch switchDeviceId;
    private MaterialButton logoutButton;
    private MaterialButton deleteAccountButton;
    private MaterialTextView terms_conditions;

    private MaterialButton backButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate Fragment Layout
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize all switches and buttons
        switchAdmin = view.findViewById(R.id.switch_admin);
        switchOrganizer = view.findViewById(R.id.switch_organizer);
        switchGeolocation = view.findViewById(R.id.switch_geolocation);
        switchDeviceId = view.findViewById(R.id.switch_device_id);
        logoutButton = view.findViewById(R.id.profile_logout_button);
        deleteAccountButton = view.findViewById(R.id.profile_delete_account_button);
        terms_conditions = view.findViewById(R.id.terms_conditions_clickable);
        backButton = view.findViewById(R.id.settings_back_button);

        // Call Setup Switch Listener Methods
        setupSwitchListeners();
        setupButtonListeners();

        return view;
    }

    // Method to Set Up All Switch Listeners
    private void setupSwitchListeners() {
        switchAdmin.setOnCheckedChangeListener((buttonView, isChecked) ->
                showToast("Admin Notifications On " + (isChecked ? "enabled" : "disabled")));

        switchOrganizer.setOnCheckedChangeListener((buttonView, isChecked) ->
                showToast("Organizer Notifications On " + (isChecked ? "enabled" : "disabled")));

        switchGeolocation.setOnCheckedChangeListener((buttonView, isChecked) ->
                showToast("Geolocation Enabled " + (isChecked ? "enabled" : "disabled")));

        switchDeviceId.setOnCheckedChangeListener((buttonView, isChecked) ->
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
        terms_conditions.setOnClickListener(v -> {
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