package com.example.matrix_events.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;

/**
 * A simple Fragment responsible for displaying the application's Terms and Conditions.
 * <p>
 * This fragment provides a read-only view of the legal terms and includes a
 * dedicated back button to return the user to the previous screen (typically Settings).
 * </p>
 */
public class TermsConditionsFragment extends Fragment {

    /**
     * Required empty public constructor.
     * <p>
     * Android fragments must have a default zero-argument constructor to allow
     * the system to instantiate them (e.g., during configuration changes).
     * </p>
     */
    public TermsConditionsFragment() {}

    /**
     * Called to have the fragment instantiate its user interface view.
     * <p>
     * Inflates the layout containing the Terms text and initializes the back button
     * listener to handle navigation via the FragmentManager's back stack.
     * </p>
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     * any views in the fragment.
     * @param container          If non-null, this is the parent view that the fragment's
     * UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate Fragment Layout
        View view = inflater.inflate(R.layout.fragment_terms_conditions, container, false);

        // Back Button Functionality
        Button backButton = view.findViewById(R.id.terms_back_button_top);
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                // Return to the previous fragment (SettingsFragment)
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return view;
    }
}