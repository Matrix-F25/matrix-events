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
import com.example.matrix_events.fragments.NotificationSeeMore;
import com.example.matrix_events.managers.NotificationManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class NotificationArrayAdapter extends ArrayAdapter<Notification> {
    public NotificationArrayAdapter(@NonNull Context context, @NonNull ArrayList<Notification> arrayList) {
        super(context, 0, arrayList);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_notification_message, parent, false);
        }

        Notification notification = getItem(position);

        TextView titleTextview = convertView.findViewById(R.id.text_message_title);
        if (titleTextview != null && notification != null) {
            String title = "New message from: " + notification.getSender().getName();
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

                    NotificationSeeMore fragment = NotificationSeeMore.newInstance(notification);

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
                    if (notification != null) {
                        NotificationManager.getInstance().deleteNotification(notification);
                    }
                }
            });
        }

        return convertView;
    }
}
