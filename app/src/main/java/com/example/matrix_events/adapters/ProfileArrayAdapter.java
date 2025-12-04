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
 * This adapter binds profile data (specifically the name) to the {@code item_profile_list} layout.
 * It includes a configurable "Cancel" (or Delete/Remove) button that can be toggled on or off via the constructor,
 * making this adapter reusable for both read-only lists and interactive management lists.
 * </p>
 */
public class ProfileArrayAdapter extends ArrayAdapter<Profile> {

    /**
     * Interface definition for a callback to be invoked when the "Cancel" action is triggered.
     * This is typically used for removing a user from a waitlist or deleting a profile.
     */
    public interface Listener {
        /**
         * Called when the cancel/remove button is clicked for a specific profile.
         *
         * @param deviceID The unique device ID of the profile to be processed.
         */
        void cancelProfile(String deviceID);
    }

    private boolean cancelEnabled;
    private Listener listener;       // can be null if items are not cancellable

    /**
     * Constructs a new {@code ProfileArrayAdapter}.
     *
     * @param context       The current context.
     * @param arrayList     The list of {@link Profile} objects to display.
     * @param cancelEnabled {@code true} to display the cancel/remove button, {@code false} to hide it.
     * @param listener      The callback listener to handle button clicks.
     * Can be null if {@code cancelEnabled} is false.
     */
    public ProfileArrayAdapter(@NonNull Context context, @NonNull ArrayList<Profile> arrayList,
                               boolean cancelEnabled,
                               @Nullable Listener listener) {
        super(context, 0, arrayList);
        this.cancelEnabled = cancelEnabled;
        this.listener = listener;
    }

    /**
     * Gets a View that displays the data at the specified position in the data set.
     * <p>
     * This method binds the profile name to the text view and handles the visibility
     * and click events of the cancel button based on the {@code cancelEnabled} flag.
     * </p>
     *
     * @param position    The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
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

        // Default to invisible
        cancelButton.setVisibility(View.INVISIBLE);

        // Enable button only if the flag is set
        if (cancelEnabled) {
            cancelButton.setVisibility(View.VISIBLE);
            cancelButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.cancelProfile(profile.getDeviceId());
                }
            });
        }

        return convertView;
    }
}