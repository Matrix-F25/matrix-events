package com.example.matrix_events.fragments;

// Imports
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.example.matrix_events.managers.ProfileManager;

// TermsConditionsFragment Class
public class TermsConditionsFragment extends Fragment implements com.example.matrix_events.mvc.View {

    // Required Empty Constructor
    public TermsConditionsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate Fragment Layout
        View view = inflater.inflate(R.layout.fragment_terms_conditions, container, false);

        // Back Button Functionality
        Button backButton = view.findViewById(R.id.terms_back_button);
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }

    // MV Implementation methods
    @Override
    public void onDestroy() {
        super.onDestroy();
        ProfileManager.getInstance().removeView(this);
    }

    @Override
    public void update() {

    }
}
