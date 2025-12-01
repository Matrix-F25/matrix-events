package com.example.matrix_events.activities;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
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

/**
 * Activity responsible for the Administrator's view of all events in the system.
 * <p>
 * This activity acts as a master list for Admins, allowing them to:
 * <ul>
 * <li><b>Browse:</b> View every event currently registered in the database.</li>
 * <li><b>Inspect:</b> Open detailed views of specific events via {@link EventDetailFragment}.</li>
 * <li><b>Delete:</b> Permanently remove events using the {@link EventArrayAdapter.OnEventDeleteListener} interface.</li>
 * </ul>
 * It implements {@link View} to observe changes in the {@link EventManager}.
 * </p>
 */
public class AdminEventsActivity extends AppCompatActivity implements View, EventArrayAdapter.OnEventDeleteListener {

    private ArrayList<Event> events;
    private EventArrayAdapter eventArrayAdapter;
    private ListView eventListView;

    /**
     * Called when the activity is starting.
     * <p>
     * Initializes the UI layout (reusing the search layout structure) and performs the following specific setups:
     * <ul>
     * <li><b>Navigation:</b> Loads the {@link AdminNavigationBarFragment}.</li>
     * <li><b>Adapter:</b> Initializes {@link EventArrayAdapter} with {@code isAdmin = true}, enabling delete buttons on list items.</li>
     * <li><b>Click Listener:</b> Sets up the list to open {@link EventDetailFragment} in Admin mode when an item is clicked.</li>
     * <li><b>MVC Registration:</b> Registers this activity as an observer of {@link EventManager}.</li>
     * </ul>
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied.
     */
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

        // Reuse layout but update title for Admin context
        TextView title = findViewById(R.id.event_search_title_static);
        title.setText("Admin Events");

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.navigation_bar_fragment, AdminNavigationBarFragment.newInstance(R.id.nav_admin_events))
                .commit();

        events = new ArrayList<>();
        eventListView = findViewById(R.id.event_search_listview);

        // Initialize adapter with admin permissions (true) and 'this' as the delete listener
        eventArrayAdapter = new EventArrayAdapter(this, events, true, this);

        eventListView.setAdapter(eventArrayAdapter);

        eventListView.setOnItemClickListener((parent, view, position, id) -> {
            Event selectedEvent = events.get(position);
            // Open fragment with admin permissions (true)
            EventDetailFragment fragment = EventDetailFragment.newInstance(selectedEvent, true);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        update();

        EventManager.getInstance().addView(this);
    }

    /**
     * Cleanup method called when the activity is destroyed.
     * <p>
     * Unregisters this activity from the {@link EventManager} to prevent memory leaks.
     * </p>
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventManager.getInstance().removeView(this);
    }

    /**
     * MVC Callback: Updates the list when the Model data changes.
     * <p>
     * Fetches the complete list of all events from {@link EventManager#getEvents()}
     * and refreshes the adapter.
     * </p>
     */
    @Override
    public void update() {
        events.clear();
        events.addAll(EventManager.getInstance().getEvents());
        if (eventArrayAdapter != null) {
            eventArrayAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Callback method invoked when the "Delete" button is clicked on an event list item.
     * <p>
     * This method enforces a safety check via an {@link AlertDialog}. If confirmed, it triggers
     * {@link EventManager#cancelEventAndNotifyUsers(Event, String)}, which:
     * <ol>
     * <li>Sends a cancellation notification to all attendees (Waitlist, Pending, Accepted).</li>
     * <li>Deletes the event and its associated poster from the database.</li>
     * </ol>
     * </p>
     *
     * @param event The {@link Event} selected for deletion.
     */
    @Override
    public void onDeleteClick(Event event) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete '" + event.getName() + "'? This cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    String adminMessage = "Urgent: The event '" + event.getName() + "' has been cancelled by the admin. Sorry!";
                    EventManager.getInstance().cancelEventAndNotifyUsers(event, adminMessage);

                    Toast.makeText(this, "Event deleted and users notified.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}