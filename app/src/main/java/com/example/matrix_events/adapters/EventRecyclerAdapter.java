package com.example.matrix_events.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matrix_events.R;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.utils.TimestampConverter;

import java.util.ArrayList;
import java.util.List;

public class EventRecyclerAdapter extends RecyclerView.Adapter<EventRecyclerAdapter.EventViewHolder> {
    // Attributes
    private final List<Event> events;
    private final OnEventClickListener  listener;

    // Constructor
    public EventRecyclerAdapter(List<Event> events, OnEventClickListener listener) {
        this.events = events;
        this.listener = listener;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_event_list_view, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        Event event = events.get(position);

        holder.titleTextView.setText(event.getName());
        holder.dateTimeTextView.setText(TimestampConverter.convertFirebaseTimestampToString(event.getEventStartDateTime()));
        holder.locationTextView.setText(event.getLocation().getName());
        holder.posterImageView.setImageResource(R.drawable.placeholder);

        // Handle click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    // ViewHolder class
    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, dateTimeTextView, locationTextView;
        ImageView posterImageView;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_textview);
            dateTimeTextView = itemView.findViewById(R.id.date_time_textview);
            locationTextView = itemView.findViewById(R.id.location_textview);
            posterImageView = itemView.findViewById(R.id.poster_imageview);
        }
    }

    // Interface for click callbacks
    public interface OnEventClickListener {
        void onEventClick(Event event);
    }
}
