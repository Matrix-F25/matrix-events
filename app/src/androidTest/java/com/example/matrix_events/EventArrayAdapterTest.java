package com.example.matrix_events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.matrix_events.adapters.EventArrayAdapter;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Profile;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Instrumented Unit Tests for {@link EventArrayAdapter}.
 * <p>
 * Verifies that the adapter correctly binds event data to the view and handles
 * the Admin-specific "Delete" button logic.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class EventArrayAdapterTest {

    private Context context;
    private ArrayList<Event> testData;
    private Event testEvent;

    @Before
    public void setUp() {
        // Use Material Theme to prevent InflateException for Material components
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context = new ContextThemeWrapper(targetContext, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar);

        testData = new ArrayList<>();

        // Create a dummy event
        Profile organizer = new Profile("Org", "email", "123", "id");

        // FIX: Create timestamps in the future to pass Event constructor validation
        long currentTime = new Date().getTime();
        long hour = 3600 * 1000; // 1 hour in milliseconds

        // Ensure chronological order: Reg Start < Reg End < Event Start < Event End
        Timestamp regStart = new Timestamp(new Date(currentTime + hour));
        Timestamp regEnd = new Timestamp(new Date(currentTime + 2 * hour));
        Timestamp evtStart = new Timestamp(new Date(currentTime + 3 * hour));
        Timestamp evtEnd = new Timestamp(new Date(currentTime + 4 * hour));

        testEvent = new Event(
                "Test Gala",
                "Description",
                organizer,
                "Grand Hall",
                evtStart, evtEnd,
                100, 0,
                regStart, regEnd,
                false, null, null, false, null
        );

        testData.add(testEvent);
    }

    /**
     * Test A: Verify Basic View Binding.
     * <p>
     * Ensures the Title and Location are set correctly on the TextViews.
     * </p>
     */
    @Test
    public void testViewBinding() {
        EventArrayAdapter adapter = new EventArrayAdapter(context, testData);
        View view = adapter.getView(0, null, new android.widget.LinearLayout(context));

        TextView titleView = view.findViewById(R.id.title_textview);
        TextView locationView = view.findViewById(R.id.location_textview);

        assertNotNull("Title TextView should exist", titleView);
        assertEquals("Test Gala", titleView.getText().toString());
        assertEquals("Grand Hall", locationView.getText().toString());
    }

    /**
     * Test B: Verify Admin Mode (Delete Button Visible).
     * <p>
     * <b>Scenario:</b> Adapter initialized with {@code isAdmin = true}.<br>
     * <b>Expected:</b> Delete button is VISIBLE.
     * </p>
     */
    @Test
    public void testAdminMode_DeleteVisible() {
        // Initialize with isAdmin = true
        EventArrayAdapter adapter = new EventArrayAdapter(context, testData, true, event -> {});
        View view = adapter.getView(0, null, new android.widget.LinearLayout(context));

        ImageButton deleteButton = view.findViewById(R.id.admin_delete_button);
        assertNotNull("Delete button should exist", deleteButton);
        assertEquals("Delete button should be visible for admins", View.VISIBLE, deleteButton.getVisibility());
    }

    /**
     * Test C: Verify User Mode (Delete Button Hidden).
     * <p>
     * <b>Scenario:</b> Adapter initialized with {@code isAdmin = false}.<br>
     * <b>Expected:</b> Delete button is GONE.
     * </p>
     */
    @Test
    public void testUserMode_DeleteHidden() {
        // Initialize with isAdmin = false
        EventArrayAdapter adapter = new EventArrayAdapter(context, testData, false, null);
        View view = adapter.getView(0, null, new android.widget.LinearLayout(context));

        ImageButton deleteButton = view.findViewById(R.id.admin_delete_button);
        assertNotNull("Delete button should exist", deleteButton);

        // Use GONE as per your adapter code logic
        assertEquals("Delete button should be GONE for regular users", View.GONE, deleteButton.getVisibility());
    }

    /**
     * Test D: Verify Delete Listener.
     * <p>
     * <b>Scenario:</b> Admin clicks the delete button.<br>
     * <b>Expected:</b> The callback is fired with the correct event object.
     * </p>
     */
    @Test
    public void testDeleteClickListener() {
        AtomicBoolean wasCalled = new AtomicBoolean(false);

        EventArrayAdapter.OnEventDeleteListener listener = event -> {
            wasCalled.set(true);
            assertEquals("Test Gala", event.getName());
        };

        // Admin mode enabled
        EventArrayAdapter adapter = new EventArrayAdapter(context, testData, true, listener);
        View view = adapter.getView(0, null, new android.widget.LinearLayout(context));

        ImageButton deleteButton = view.findViewById(R.id.admin_delete_button);
        deleteButton.performClick();

        assertTrue("Delete callback should be triggered", wasCalled.get());
    }
}