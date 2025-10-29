package com.example.matrix_events.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.View;

import com.example.matrix_events.R;
import com.example.matrix_events.activities.EntrantMyEventsActivity;
import com.example.matrix_events.activities.EventSearchActivity;
import com.example.matrix_events.activities.NotificationActivity;
import com.example.matrix_events.activities.ProfileActivity;
import com.example.matrix_events.activities.QRCodeActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavigationBarFragment extends Fragment {

    private BottomNavigationView navigationView;
    private int selectedItemId = R.id.nav_event_search;     // Default

    public NavigationBarFragment() {
        super(R.layout.fragment_navigation_bar);
    }

    public static NavigationBarFragment newInstance(int selectedItemId) {
        NavigationBarFragment fragment = new NavigationBarFragment();
        Bundle args = new Bundle();
        args.putInt("selectedItemId", selectedItemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navigationView = view.findViewById(R.id.navigation_bar_fragment);

        if (getArguments() != null) {
            selectedItemId = getArguments().getInt("selectedItemId");
        }
        navigationView.setSelectedItemId(selectedItemId);

        navigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == selectedItemId) return true;

            Intent intent;
            if (id == R.id.nav_qrcode) {
                intent = new Intent(requireContext(), QRCodeActivity.class);
                startActivity(new Intent(requireContext(), QRCodeActivity.class));
            } else if (id == R.id.nav_notifications) {
                intent = new Intent(requireContext(), NotificationActivity.class);
                startActivity(new Intent(requireContext(), NotificationActivity.class));
            } else if (id == R.id.nav_event_search) {
                intent = new Intent(requireContext(), EventSearchActivity.class);
                startActivity(new Intent(requireContext(), EventSearchActivity.class));
            } else if (id == R.id.nav_my_events) {
                intent = new Intent(requireContext(), EntrantMyEventsActivity.class);
                startActivity(new Intent(requireContext(), EntrantMyEventsActivity.class));
            } else if (id == R.id.nav_profile) {
                intent = new Intent(requireContext(), ProfileActivity.class);
                startActivity(new Intent(requireContext(), ProfileActivity.class));
            } else {
                Log.w("NavigationFragment", "INVALID navigation context provided.");
                intent = new Intent();
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
            requireActivity().overridePendingTransition(0, 0);
            return true;
        });
    }
}