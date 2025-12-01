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
 * A custom ArrayAdapter for displaying {@link Notification} objects in a ListView.
 * <p>
 * This adapter handles the UI logic for individual notification items, including dynamic text formatting
 * based on the user type ("admin" vs "entrant") and handling interactions for the "See More"
 * and "Delete" buttons.
 * </p>
 */
public class NotificationArrayAdapter extends ArrayAdapter<Notification> {

    private final String adapterType;

    /**
     * Constructs a new {@code NotificationArrayAdapter} with the default "entrant" view type.
     *
     * @param context   The current context.
     * @param arrayList The data source of {@link Notification} objects.
     */
    public NotificationArrayAdapter(@NonNull Context context, @NonNull ArrayList<Notification> arrayList) {
        this(context, arrayList, "entrant");
    }

    /**
     * Constructs a new {@code NotificationArrayAdapter} with a specified view type.
     *
     * @param context     The current context.
     * @param arrayList   The data source of {@link Notification} objects.
     * @param adapterType The type of user viewing the list. Accepted values are "admin" or "entrant".
     * <ul>
     * <li><b>"admin"</b>: Displays "Sender sent to Receiver" and enables permanent deletion.</li>
     * <li><b>"entrant"</b>: Displays "New message from: Sender" and enables marking as read.</li>
     * </ul>
     */
    public NotificationArrayAdapter(@NonNull Context context, @NonNull ArrayList<Notification> arrayList, @NonNull String adapterType) {
        super(context, 0, arrayList);
        this.adapterType = adapterType;
    }

    /**
     * Provides a view for an AdapterView (ListView, GridView, etc.).
     * <p>
     * This method inflates the {@code item_notification_message} layout and binds the notification data
     * to the view elements. It also sets up the click listeners for:
     * <ul>
     * <li><b>See More Button:</b> Navigates to the {@link NotificationSeeMoreFragment}.</li>
     * <li><b>Delete Button:</b> Performs an action based on the {@code adapterType}.
     * If "admin", it permanently deletes the notification.
     * If "entrant", it marks the notification as read (soft delete).
     * </li>
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
                    AppCompatActivity activity = (AppCompatActivity) getContext();
                    FragmentManager fragmentManager = activity.getSupportFragmentManager();

                    NotificationSeeMoreFragment fragment = NotificationSeeMoreFragment.newInstance(notification, adapterType);

                    fragmentManager.beginTransaction()
                            .replace(R.id.main, fragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }

        // delete notification button functionality
        ImageButton deleteButton = convertView.findViewById(R.id.button_delete);
        if (deleteButton != null) {
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // if we're currently an admin trying to delete a message
                    if ("admin".equals(adapterType)) {
                        // Hard delete from database
                        NotificationManager.getInstance().deleteNotification(notification);
                    } else {
                        // Soft delete (mark as read) for entrants
                        notification.setReadFlag(true);
                        NotificationManager.getInstance().updateNotification(notification);
                    }
                }
            });
        }

        return convertView;
    }
}