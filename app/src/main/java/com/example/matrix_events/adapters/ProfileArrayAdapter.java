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

/**
 * A custom Array Adapter for displaying {@link Profile} objects in a ListView.
 * <p>
 * This adapter inflates the {@code item_profile_list} layout. It displays the profile's name
 * and optionally provides a "Cancel/Remove" button. This is useful for lists where an
 * admin or organizer needs to remove a user from a specific context (e.g., removing an
 * entrant from an event).
 * </p>
 */
public class ProfileArrayAdapter extends ArrayAdapter<Profile> {

    /**
     * Interface definition for a callback to be invoked when the "Cancel" action is triggered.
     */
    public interface Listener {
        /**
         * Called when the cancel/remove button is clicked for a specific profile.
         *
         * @param deviceID The unique device ID of the profile to be removed/cancelled.
         */
        void cancelProfile(String deviceID);
    }

    private boolean cancelEnabled;
    private Listener listener;

    /**
     * Constructs a new {@code ProfileArrayAdapter}.
     *
     * @param context       The current context.
     * @param arrayList     The list of {@link Profile} objects to display.
     * @param cancelEnabled {@code true} to show the cancel/remove button, {@code false} to hide it.
     * @param listener      The listener to handle cancel button clicks. Can be null if {@code cancelEnabled} is false.
     */
    public ProfileArrayAdapter(@NonNull Context context, @NonNull ArrayList<Profile> arrayList,
                               boolean cancelEnabled,
                               Listener listener) {
        super(context, 0, arrayList);
        this.cancelEnabled = cancelEnabled;
        this.listener = listener;
    }

    /**
     * Gets a View that displays the data at the specified position in the data set.
     * <p>
     * This method recycles the view if possible (convertView), binds the Profile name to the TextView,
     * and toggles the visibility of the Cancel button based on the {@code cancelEnabled} flag.
     * </p>
     *
     * @param position    The position of the item within the adapter's data set of the item whose view we want.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // 1. Inflate View if null
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_profile_list, parent, false);
        }

        // 2. Get Data Object
        Profile profile = getItem(position);
        if (profile == null) {
            return convertView; // Safety check
        }

        // 3. Bind UI Elements (Standard Null Checks)
        TextView profileNameTextview = convertView.findViewById(R.id.item_profile_list_name_textview);
        if (profileNameTextview != null) {
            profileNameTextview.setText(profile.getName());
        }

        ImageButton cancelButton = convertView.findViewById(R.id.item_profile_list_cancel_button);

        // 4. Handle Logic
        if (cancelButton != null) {
            if (cancelEnabled) {
                cancelButton.setVisibility(View.VISIBLE);
                cancelButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.cancelProfile(profile.getDeviceId());
                    }
                });
            } else {
                cancelButton.setVisibility(View.INVISIBLE);
                cancelButton.setOnClickListener(null); // Clear listener to prevent leaks
            }
        }

        return convertView;
    }
}