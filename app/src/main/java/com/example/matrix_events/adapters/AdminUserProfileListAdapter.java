package com.example.matrix_events.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.matrix_events.R;
import com.example.matrix_events.entities.Profile;

import java.util.List;
import java.util.Set;

public class AdminUserProfileListAdapter extends ArrayAdapter<Profile> {

    private final Set<String> organizerDeviceIds;

    public AdminUserProfileListAdapter(@NonNull Context context,
                                       @NonNull List<Profile> profiles,
                                       @NonNull Set<String> organizerDeviceIds) {
        super(context, 0, profiles);
        this.organizerDeviceIds = organizerDeviceIds;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.admin_user_profile_list_item, parent, false);
        }

        Profile profile = getItem(position);

        TextView nameText = convertView.findViewById(R.id.admin_list_item_user_name);
        TextView emailText = convertView.findViewById(R.id.admin_list_item_user_email);
        TextView phoneText = convertView.findViewById(R.id.admin_list_item_user_phone);
        TextView organizerText = convertView.findViewById(R.id.admin_list_item_user_is_organizer);

        if (profile != null) {
            String name = profile.getName();
            String email = profile.getEmail();
            String phone = profile.getPhoneNumber();

            if (phone == null || phone.trim().isEmpty()
                    || phone.equalsIgnoreCase("Phone Number (Optional)")) {
                phone = "N/A";
            }

            nameText.setText("Name: " + safe(name));
            emailText.setText("Email: " + safe(email));
            phoneText.setText("Phone: " + phone);

            String deviceId = profile.getDeviceId();
            boolean isOrganizer = deviceId != null && organizerDeviceIds.contains(deviceId);

            organizerText.setText("Organizer: " + (isOrganizer ? "Yes" : "No"));
        }

        return convertView;
    }

    private String safe(String value) {
        return value == null ? "N/A" : value;
    }
}
