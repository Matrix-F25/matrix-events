package com.example.matrix_events.activities;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.EventArrayAdapter;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.fragments.AdminNavigationBarFragment;
import com.example.matrix_events.fragments.EventDetailFragment;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.mvc.View;

import java.util.ArrayList;

public class AdminEventsActivity extends AppCompatActivity implements View {

    private ArrayList<Event> events;
    private EventArrayAdapter eventArrayAdapter;
    private ListView eventListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TextView title = findViewById(R.id.event_search_title_static);
        title.setText("Admin Events");

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, AdminNavigationBarFragment.newInstance(R.id.nav_admin_events))
                .commit();

        events = new ArrayList<>();
        eventListView = findViewById(R.id.event_search_listview);
        eventArrayAdapter = new EventArrayAdapter(this, events, true);
        eventListView.setAdapter(eventArrayAdapter);

        eventListView.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = events.get(position);
            EventDetailFragment fragment = EventDetailFragment.newInstance(selectedEvent, true);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        update();

        EventManager.getInstance().addView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeView(this);
    }

    @Override
    public void update() {
        events.clear();
        events.addAll(EventManager.getInstance().getEvents());
        if (eventArrayAdapter != null) {
            eventArrayAdapter.notifyDataSetChanged();
        }
    }
}