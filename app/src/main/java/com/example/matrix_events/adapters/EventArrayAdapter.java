package com.example.matrix_events.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.matrix_events.R;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.utils.TimestampConverter;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * A custom Array Adapter for displaying {@link Event} objects in a ListView.
 * <p>
 * This adapter binds event details (Title, Date/Time, Location, Poster) to the
 * {@code item_event} layout. It also handles specific logic for Administrators,
 * such as displaying a "Delete" button for event removal.
 * </p>
 */
public class EventArrayAdapter extends ArrayAdapter<Event> {

    private boolean isAdmin;
    private OnEventDeleteListener deleteListener;

    /**
     * Interface definition for a callback to be invoked when an event is deleted (Admin only).
     */
    public interface OnEventDeleteListener {
        /**
         * Called when the delete button is clicked for a specific event.
         * @param event The event to be deleted.
         */
        void onDeleteClick(Event event);
    }

    /**
     * Constructs a new {@code EventArrayAdapter} with Admin capabilities.
     *
     * @param context   The current context.
     * @param arrayList The list of {@link Event} objects to display.
     * @param isAdmin   {@code true} to show admin-specific controls (delete button), {@code false} otherwise.
     * @param listener  The listener to handle delete button clicks.
     */
    public EventArrayAdapter(@NonNull Context context, @NonNull ArrayList<Event> arrayList, boolean isAdmin, OnEventDeleteListener listener) {
        super(context, 0, arrayList);
        this.isAdmin = isAdmin;
        this.deleteListener = listener;
    }

    /**
     * Constructs a new {@code EventArrayAdapter} for standard users (No admin controls).
     *
     * @param context   The current context.
     * @param arrayList The list of {@link Event} objects to display.
     */
    public EventArrayAdapter(@NonNull Context context, @NonNull ArrayList<Event> arrayList) {
        this(context, arrayList, false, null);
    }

    /**
     * Gets a View that displays the data at the specified position in the data set.
     * <p>
     * Recycles the view if possible, binds event text fields, loads the poster image using Glide,
     * and toggles the Admin delete button visibility.
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
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);
        }

        Event event = getItem(position);
        if (event == null) {
            return convertView;
        }

        // Bind Title
        TextView titleTextview = convertView.findViewById(R.id.title_textview);
        if (titleTextview != null) {
            titleTextview.setText(event.getName());
        }

        // Bind Date/Time using utility converter
        TextView startDateTimeTextview = convertView.findViewById(R.id.date_time_textview);
        if (startDateTimeTextview != null) {
            startDateTimeTextview.setText(TimestampConverter.convertFirebaseTimestampToString(event.getEventStartDateTime()));
        }

        // Bind Location
        TextView locationTextview = convertView.findViewById(R.id.location_textview);
        if (locationTextview != null) {
            locationTextview.setText(event.getLocation());
        }

        // Bind Poster Image
        ImageView posterImageView = convertView.findViewById(R.id.poster_imageview);
        if (posterImageView != null) {
            View posterContainer = (View) posterImageView.getParent();
            if (event.getPoster() != null && event.getPoster().getImageUrl() != null) {
                if (posterContainer != null) {
                    posterContainer.setVisibility(View.VISIBLE);
                }
                posterImageView.setVisibility(View.VISIBLE);
                Glide.with(getContext())
                        .load(event.getPoster().getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .into(posterImageView);
            } else {
                if (posterContainer != null) {
                    posterContainer.setVisibility(View.GONE);
                } else {
                    posterImageView.setVisibility(View.GONE);
                }
            }
        }

        // Handle Admin Delete Button Visibility & Click
        ImageButton deleteButton = convertView.findViewById(R.id.admin_delete_button);
        if (deleteButton != null) {
            if (isAdmin) {
                deleteButton.setVisibility(View.VISIBLE);
                deleteButton.setOnClickListener(v -> {
                    if (deleteListener != null) {
                        deleteListener.onDeleteClick(event);
                    }
                });
            } else {
                deleteButton.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}