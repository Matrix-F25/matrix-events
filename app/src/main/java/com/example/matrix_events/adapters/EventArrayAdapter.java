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

public class EventArrayAdapter extends ArrayAdapter<Event> {

    private boolean isAdmin;
    private OnEventDeleteListener deleteListener;

    public EventArrayAdapter(@NonNull Context context, @NonNull ArrayList<Event> arrayList, boolean isAdmin, OnEventDeleteListener listener) {
        super(context, 0, arrayList);
        this.isAdmin = isAdmin;
        this.deleteListener = listener;
    }

    public EventArrayAdapter(@NonNull Context context, @NonNull ArrayList<Event> arrayList) {
        this(context, arrayList, false, null);
    }

    public interface OnEventDeleteListener {
        void onDeleteClick(Event event);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_event, parent, false);
        }

        Event event = getItem(position);

        TextView titleTextview = convertView.findViewById(R.id.title_textview);
        assert titleTextview != null;
        titleTextview.setText(event.getName());

        TextView startDateTimeTextview = convertView.findViewById(R.id.date_time_textview);
        assert startDateTimeTextview != null;
        startDateTimeTextview.setText(TimestampConverter.convertFirebaseTimestampToString(event.getEventStartDateTime()));

        TextView locationTextview = convertView.findViewById(R.id.location_textview);
        assert locationTextview != null;
        locationTextview.setText(event.getLocation().getName());

        ImageView posterImageView = convertView.findViewById(R.id.poster_imageview);
        assert posterImageView != null;

        // to show the poster without clicking on the actual event
        if (event.getPoster() != null && event.getPoster().getImageUrl() != null) { // if an event has a poster and the poster url exists
            Glide.with(getContext())
                    .load(event.getPoster().getImageUrl())
                    .placeholder(R.drawable.placeholder)
                    .into(posterImageView);
        } else { // show the placeholder image if a poster wasn't uploaded
            posterImageView.setImageResource(R.drawable.placeholder);
        }

        ImageButton deleteButton = convertView.findViewById(R.id.admin_delete_button);
        if (isAdmin) {
            deleteButton.setVisibility(View.VISIBLE);
            deleteButton.setOnClickListener(v -> {
                if (deleteListener != null) {
                    deleteListener.onDeleteClick(event);
                }
            });
        } else if (deleteButton != null) {
            deleteButton.setVisibility(View.GONE);
        }

        return convertView;
    }
}
