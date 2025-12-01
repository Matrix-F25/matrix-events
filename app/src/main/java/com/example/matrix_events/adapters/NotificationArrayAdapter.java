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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.example.matrix_events.R;
import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.fragments.NotificationSeeMoreFragment;
import com.example.matrix_events.managers.NotificationManager;
import com.google.android.material.button.MaterialButton;


import java.util.ArrayList;

/**
 * A custom Array Adapter for displaying {@link Notification} objects in a ListView.
 * <p>
 * This adapter handles the display logic for notification list items, including
 * showing a preview of the message, distinguishing between Admin and Entrant view context,
 * and handling "See More" (deep linking) and "Delete" actions.
 * </p>
 */
public class NotificationArrayAdapter extends ArrayAdapter<Notification> {

    private final String adapterType;

    /**
     * Constructs a new {@code NotificationArrayAdapter} with the default "entrant" view type.
     *
     * @param context   The current context.
     * @param arrayList The list of {@link Notification} objects to display.
     */
    public NotificationArrayAdapter(@NonNull Context context, @NonNull ArrayList<Notification> arrayList) {
        this(context, arrayList, "entrant");
    }

    /**
     * Constructs a new {@code NotificationArrayAdapter}.
     *
     * @param context   The current context.
     * @param arrayList The list of {@link Notification} objects to display.
     * @param adapterType The view context, typically "admin" or "entrant", dictating button actions and title formatting.
     */
    public NotificationArrayAdapter(@NonNull Context context, @NonNull ArrayList<Notification> arrayList, @NonNull String adapterType) {
        super(context, 0, arrayList);
        this.adapterType = adapterType;
    }

    /**
     * Gets a View that displays the data at the specified position in the data set.
     * <p>
     * This method handles:
     * <ul>
     * <li>Binding the notification title based on the {@code adapterType}.</li>
     * <li>Setting up the "See More" button for deep linking to {@link NotificationSeeMoreFragment}.</li>
     * <li>Configuring the "Delete" button to either soft-delete (mark as read) or hard-delete (for admin).</li>
     * </ul>
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_notification_message, parent, false);
        }

        Notification notification = getItem(position);

        TextView titleTextview = convertView.findViewById(R.id.text_message_title);
        if (titleTextview != null && notification != null) {
            String title;
            // the notification title the admin sees
            if ("admin".equals(adapterType)) {
                title = notification.getSender().getName() + " sent to " + notification.getReceiver().getName();
                // the notification title the entrant sees
            } else {
                title = "New message from: " + notification.getSender().getName();
            }
            titleTextview.setText(title);
        }

        TextView bodyPreviewTextview = convertView.findViewById(R.id.text_message_body_preview);
        if (bodyPreviewTextview != null && notification != null) {
            bodyPreviewTextview.setText(notification.getMessage());
        }

        // "See More" button functionality
        MaterialButton seeMoreButton = convertView.findViewById(R.id.button_see_more);
        if (seeMoreButton != null) {
            seeMoreButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (notification == null) return;

                    AppCompatActivity activity = (AppCompatActivity) getContext();
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();

                    NotificationSeeMoreFragment fragment = NotificationSeeMoreFragment.newInstance(notification, adapterType);

                    // FIX: Use R.id.content_fragment_container (the correct spot for content)
                    // instead of R.id.main (the whole screen).
                    fragmentManager.beginTransaction()
                            .replace(R.id.content_fragment_container, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        // delete notification button functionality
        ImageButton deleteButton = convertView.findViewById(R.id.button_delete);
        if (deleteButton != null && notification != null) {
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // if we're currently an admin trying to delete a message
                    if ("admin".equals(adapterType)) {
                        NotificationManager.getInstance().deleteNotification(notification);
                        // if we're currently an entrant trying to delete a message
                    } else {
                        // Soft delete: Mark as read
                        notification.setReadFlag(true);
                        NotificationManager.getInstance().updateNotification(notification);
                    }
                }
            });
        }

        return convertView;
    }
}