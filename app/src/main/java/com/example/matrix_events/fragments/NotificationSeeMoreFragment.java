package com.example.matrix_events.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.matrix_events.R;
import com.example.matrix_events.entities.Notification;
import com.google.android.material.chip.Chip;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Fragment responsible for displaying the detailed content of a single {@link Notification}.
 * <p>
 * This fragment is typically opened when a user clicks an item in the notification list
 * or clicks a system push notification (deep linking). It handles formatting the
 * message, sender information, and timestamp into a readable format.
 * </p>
 */
public class NotificationSeeMoreFragment extends Fragment {

    private static final String ARG_NOTIFICATION = "notification";
    private static final String ARG_ADAPTER_TYPE = "adapterType";
    private Notification notification;
    private String adapterType;

    /**
     * Factory method to create a new instance of this fragment.
     * <p>
     * Since the {@link Notification} object is complex, it must be passed via
     * {@link Bundle} as a Serializable object.
     * </p>
     *
     * @param notification The {@link Notification} object to display.
     * @param adapterType  The context type (e.g., "admin", "organizer") which dictates title formatting.
     * @return A new instance of NotificationSeeMoreFragment.
     */
    public static NotificationSeeMoreFragment newInstance(Notification notification, String adapterType) {
        NotificationSeeMoreFragment fragment = new NotificationSeeMoreFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_NOTIFICATION, notification);
        args.putString(ARG_ADAPTER_TYPE, adapterType);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Called to do initial creation of a fragment.
     * <p>
     * Retrieves the {@link Notification} object and adapter type from the arguments bundle.
     * </p>
     *
     * @param savedInstanceState If the fragment is being re-created from a previous saved state.
     */
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            notification = (Notification) getArguments().getSerializable(ARG_NOTIFICATION);
            adapterType = getArguments().getString(ARG_ADAPTER_TYPE);
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * <p>
     * Binds the data from the {@link Notification} object to the TextViews and Chips,
     * formats the Firebase {@link Timestamp}, and sets up the close button logic.
     * </p>
     *
     * @param inflater           The LayoutInflater object that can be used to inflate views.
     * @param container          The parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state.
     * @return The View for the fragment's UI.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notification_see_more, container, false);

        if (notification != null) {
            TextView titleTextView = view.findViewById(R.id.text_message_title);
            TextView bodyTextView = view.findViewById(R.id.text_message_body);
            Chip dateChip = view.findViewById(R.id.chip_date);
            Chip timeChip = view.findViewById(R.id.chip_time);
            ImageButton closeButton = view.findViewById(R.id.button_close);

            String title;

            // Title formatting based on context
            if ("admin".equals(adapterType)) {
                title = notification.getSender().getName() + " sent to " + notification.getReceiver().getName();
            } else {
                title = "New message from: " + notification.getSender().getName();
            }

            titleTextView.setText(title);
            bodyTextView.setText(notification.getMessage());

            // Format Timestamp
            Timestamp timestamp = notification.getTimestamp();
            if (timestamp != null) {
                SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
                String formattedTime = timeFormat.format(timestamp.toDate());

                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
                String formattedDate = dateFormat.format(timestamp.toDate());

                dateChip.setText(formattedDate);
                timeChip.setText(formattedTime);
            }

            // Close button logic
            closeButton.setOnClickListener(v -> {
                // Return to the previous screen (NotificationActivity)
                getParentFragmentManager().popBackStack();
            });
        }

        return view;
    }
}