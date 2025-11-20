package com.example.matrix_events.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.matrix_events.R;
import com.example.matrix_events.entities.Profile;

import java.util.ArrayList;

public class ProfileArrayAdapter extends ArrayAdapter<Profile> {
    public interface Listener {
        void cancelProfile(String deviceID);
    }
    private boolean cancelEnabled;
    private Listener listener;       // can be null if items are not cancellable

    public ProfileArrayAdapter(@NonNull Context context, @NonNull ArrayList<Profile> arrayList,
                               boolean cancelEnabled,
                               Listener listener) {
        super(context, 0, arrayList);
        this.cancelEnabled = cancelEnabled;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_profile_list, parent, false);
        }

        Profile profile = getItem(position);
        assert profile != null;

        TextView profileNameTextview = convertView.findViewById(R.id.item_profile_list_name_textview);
        assert profileNameTextview != null;
        profileNameTextview.setText(profile.getName());

        ImageButton cancelButton = convertView.findViewById(R.id.item_profile_list_cancel_button);
        assert cancelButton != null;
        cancelButton.setVisibility(View.INVISIBLE);

        if (cancelEnabled) {
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setOnClickListener(v -> {
                listener.cancelProfile(profile.getDeviceId());
            });
        }

        return convertView;
    }
}
