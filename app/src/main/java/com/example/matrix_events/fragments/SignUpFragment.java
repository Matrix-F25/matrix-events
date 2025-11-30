package com.example.matrix_events.fragments;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.matrix_events.R;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.ProfileManager;

public class SignUpFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signup, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText editTextName = view.findViewById(R.id.editTextName);
        EditText editTextEmailAddress = view.findViewById(R.id.editTextEmailAddress);
        EditText editTextPhone = view.findViewById(R.id.editTextPhone);
        Button createAccountButton = view.findViewById(R.id.create_account_button);
        Button backButton = view.findViewById(R.id.signup_back_button);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // details to send to DB
                String name = editTextName.getText().toString().trim();
                String email = editTextEmailAddress.getText().toString().trim();
                String phone = editTextPhone.getText().toString().trim();
                String actualPhone;

                // check that required inputs have been made
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email)) {
                    Toast.makeText(getActivity(), "Enter both name and email.", Toast.LENGTH_LONG).show();
                    return; // breaks onClick()
                }

                // get deviceId
                String deviceId = Settings.Secure.getString(requireContext().getContentResolver(),Settings.Secure.ANDROID_ID);

                ProfileManager manager = ProfileManager.getInstance();

                // check that profile doesn't already exist
                if (manager.doesProfileExist(deviceId)) {
                    Toast.makeText(getActivity(), "Profile already exists for this device.", Toast.LENGTH_LONG).show();
                    return; // breaks onClick()
                }

                if (TextUtils.isEmpty(phone)) {
                    actualPhone = null;
                } else {
                    actualPhone = phone;
                }

                // create new profile
                Profile profile = new Profile(name, email, actualPhone, deviceId); // uses actualPhone now
                manager.createProfile(profile);
                Toast.makeText(getActivity(), "Profile created successfully. Please login to proceed", Toast.LENGTH_LONG).show();

                // sends the entrant back to the main activity, where they'd have to click "login"
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });
    }
}
