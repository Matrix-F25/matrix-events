package com.example.matrix_events.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.matrix_events.R;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.fragments.EventDetailFragment;
import com.example.matrix_events.fragments.NavigationBarFragment;
import com.example.matrix_events.managers.EventManager;

import java.util.ArrayList;
import java.util.List;

public class EventSearchActivity extends AppCompatActivity {

    private ListView eventListView;
    private ArrayAdapter<String> adapter;
    private List<String> eventNames = new ArrayList<>();
    private com.example.matrix_events.mvc.View eventView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_search);

        eventListView = findViewById(R.id.event_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventNames);
        eventListView.setAdapter(adapter);

        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Event clickedEvent = EventManager.getInstance().getEvents().get(position);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main, EventDetailFragment.newInstance(clickedEvent.getId()))
                        .addToBackStack(null)
                        .commit();
            }
        });

        eventView = new com.example.matrix_events.mvc.View() {
            @Override
            public void update() {
                runOnUiThread(() -> {
                    eventNames.clear();
                    for (Event event : EventManager.getInstance().getEvents()) {
                        eventNames.add(event.getName());
                    }
                    adapter.notifyDataSetChanged();
                });
            }
        };

        EventManager.getInstance().addView(eventView);

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, NavigationBarFragment.newInstance(R.id.nav_event_search))
                .commit();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (eventView != null) {
            eventView.update();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeView(eventView);
    }
}