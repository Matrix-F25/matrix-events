package com.example.matrix_events.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.example.matrix_events.activities.AdminActivity;
import com.example.matrix_events.activities.AdminEventsActivity;
import com.example.matrix_events.activities.AdminNotificationActivity;
import com.example.matrix_events.activities.AdminPostersActivity;
import com.example.matrix_events.activities.AdminProfileActivity;
import com.example.matrix_events.activities.EventSearchActivity;
import com.example.matrix_events.activities.ProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class AdminNavigationBarFragment extends Fragment {

    private BottomNavigationView navigationView;
    private int selectedItemId = R.id.nav_admin_home;

    public AdminNavigationBarFragment() {
        super(R.layout.fragment_admin_navigation_bar);
    }

    public static AdminNavigationBarFragment newInstance(int selectedItemId) {
        AdminNavigationBarFragment fragment = new AdminNavigationBarFragment();
        Bundle args = new Bundle();
        args.putInt("selectedItemId", selectedItemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navigationView = view.findViewById(R.id.admin_navigation_bar_fragment);

        if (getArguments() != null) {
            selectedItemId = getArguments().getInt("selectedItemId");
        }
        navigationView.setSelectedItemId(selectedItemId);

        navigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) return true;

            Intent intent;

            if (id == R.id.nav_admin_notifications) {
                intent = new Intent(requireContext(), AdminNotificationActivity.class);
            }
            else if (id == R.id.nav_admin_profile) {
                intent = new Intent(requireContext(), AdminProfileActivity.class);
            }
            else if (id == R.id.nav_admin_events) {
                intent = new Intent(requireContext(), AdminEventsActivity.class);
            }
            else if (id == R.id.nav_admin_posters) {
                intent = new Intent(requireContext(), AdminPostersActivity.class);
            }
            else if (id == R.id.nav_admin_home) {
                intent = new Intent(requireContext(), AdminActivity.class);
            }
            else {
                Log.w("AdminNavigation", "INVALID navigation context provided.");
                intent = new Intent(requireContext(), AdminActivity.class);
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            requireActivity().overridePendingTransition(0, 0);
            return true;
        });
    }
}