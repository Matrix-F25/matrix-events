package com.example.matrix_events.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.matrix_events.R;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.ProfileManager;

import android.view.View;

import java.util.ArrayList;

public class AdminProfileArrayAdapter extends ArrayAdapter<Profile> {

    public AdminProfileArrayAdapter(@NonNull Context context, @NonNull ArrayList<Profile> arrayList) {
        super(context, 0, arrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_profile_list, parent, false);
        }

        Profile profile = getItem(position);

        // sets the profile name
        if (profile != null) {
            TextView profileNameTextview = convertView.findViewById(R.id.item_profile_list_name_textview);
            if (profileNameTextview != null) {
                profileNameTextview.setText(profile.getName());
            }


            ImageButton deleteProfileButton = convertView.findViewById(R.id.item_profile_list_cancel_button);
            if (deleteProfileButton != null) {
                deleteProfileButton.setVisibility(View.VISIBLE);

                // deletes the profile from the DB when "X" is clicked
                deleteProfileButton.setOnClickListener(v -> {
                    ProfileManager.getInstance().deleteProfile(profile);
                });
            }
        }

        return convertView;
    }
}