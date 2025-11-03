package com.example.matrix_events.fragments;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;

public class SettingsFragment extends Fragment {

    private MaterialSwitch switchAdmin;
    private MaterialSwitch switchOrganizer;
    private MaterialSwitch switchGeolocation;
    private MaterialSwitch switchDeviceId;
    private MaterialButton logoutButton;
    private MaterialButton deleteAccountButton;

    private MaterialButton backButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        // Initialize all switches and buttons
        switchAdmin = view.findViewById(R.id.switch_admin);
        switchOrganizer = view.findViewById(R.id.switch_organizer);
        switchGeolocation = view.findViewById(R.id.switch_geolocation);
        switchDeviceId = view.findViewById(R.id.switch_device_id);
        logoutButton = view.findViewById(R.id.profile_logout_button);
        deleteAccountButton = view.findViewById(R.id.profile_delete_account_button);
        backButton = view.findViewById(R.id.back_button);

        //  Call Setup Switch Listener Methods
        setupSwitchListeners();
        setupButtonListeners();

        return view;
    }

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

    private void setupButtonListeners() {
        logoutButton.setOnClickListener(v ->
                showToast("Logged out successfully"));

        deleteAccountButton.setOnClickListener(v ->
                showToast("Account deleted"));

        backButton.setOnClickListener(v -> {
            requireActivity()
                    .getSupportFragmentManager()
                    .popBackStack();
        });
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
        }
    }
}