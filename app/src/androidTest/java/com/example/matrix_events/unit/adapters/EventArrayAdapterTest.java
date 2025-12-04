package com.example.matrix_events.unit.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.EventArrayAdapter;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Poster;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.utils.TimestampConverter;
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
 * Verifies that the adapter correctly binds event data to the view, handles
 * the Admin-specific "Delete" button logic, and supports view recycling.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class EventArrayAdapterTest {

    private Context context;
    private ArrayList<Event> testData;
    private Event eventNoPoster;
    private Event eventWithPoster;

    @Before
    public void setUp() {
        // Use Material Theme to prevent InflateException for Material components (like ImageButton/CardView)
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context = new ContextThemeWrapper(targetContext, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar);

        testData = new ArrayList<>();

        Profile organizer = new Profile("Org", "email", "123", "id");

        // FIX: Create timestamps in the future to pass Event constructor validation
        long currentTime = new Date().getTime();
        long hour = 3600 * 1000;

        Timestamp regStart = new Timestamp(new Date(currentTime + hour));
        Timestamp regEnd = new Timestamp(new Date(currentTime + 2 * hour));
        Timestamp evtStart = new Timestamp(new Date(currentTime + 3 * hour));
        Timestamp evtEnd = new Timestamp(new Date(currentTime + 4 * hour));

        // 1. Event without a poster (Should show placeholder)
        eventNoPoster = new Event(
                "Test Gala",
                "Description",
                organizer,
                "Grand Hall",
                evtStart, evtEnd,
                100, 0,
                regStart, regEnd,
                false, null, null, false, null
        );

        // 2. Event with a poster (Should attempt to load URL)
        Poster poster = new Poster("https://example.com/poster.jpg", "evt_id_2", "poster.jpg");
        eventWithPoster = new Event(
                "Poster Party",
                "Has a poster",
                organizer,
                "Art Gallery",
                evtStart, evtEnd,
                50, 0,
                regStart, regEnd,
                false, null, null, false, poster
        );

        testData.add(eventNoPoster);
        testData.add(eventWithPoster);
    }

    /**
     * Test A: Verify Comprehensive View Binding.
     * <p>
     * Ensures the Title, Location, and formatted Date are set correctly on the TextViews.
     * </p>
     */
    @Test
    public void testViewBinding_TextAndDate() {
        // Run UI operations on Main Thread to satisfy Glide's requirements
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            EventArrayAdapter adapter = new EventArrayAdapter(context, testData);
            // Pass a parent view (LinearLayout) to allow proper layout parameter calculation
            View view = adapter.getView(0, null, new LinearLayout(context));

            TextView titleView = view.findViewById(R.id.title_textview);
            TextView locationView = view.findViewById(R.id.location_textview);
            TextView dateView = view.findViewById(R.id.date_time_textview);

            assertNotNull("Title TextView should exist", titleView);
            assertEquals("Test Gala", titleView.getText().toString());

            assertNotNull("Location TextView should exist", locationView);
            assertEquals("Grand Hall", locationView.getText().toString());

            // Verify Timestamp Converter integration
            assertNotNull("Date/Time TextView should exist", dateView);
            String expectedDate = TimestampConverter.convertFirebaseTimestampToString(eventNoPoster.getEventStartDateTime());
            assertEquals("Date should match converted timestamp string", expectedDate, dateView.getText().toString());
        });
    }

    /**
     * Test B: Verify Placeholder logic when no poster exists.
     */
    @Test
    public void testViewBinding_NoPoster() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            EventArrayAdapter adapter = new EventArrayAdapter(context, testData);
            // Get view for index 0 (eventNoPoster)
            View view = adapter.getView(0, null, new LinearLayout(context));

            ImageView posterView = view.findViewById(R.id.poster_imageview);
            assertNotNull("Poster ImageView should exist", posterView);
            assertEquals("Poster view should be visible", View.VISIBLE, posterView.getVisibility());

            // Note: Checking specific drawable resources on an ImageView in instrumentation
            // tests without Bitmap comparison is difficult, but we verified the view exists
            // and doesn't crash the adapter logic.
        });
    }

    /**
     * Test C: Verify View Recycling.
     * <p>
     * Ensures that when `convertView` is passed to `getView`, the adapter reuses it
     * instead of inflating a new one, and updates the data binding correctly.
     * </p>
     */
    @Test
    public void testViewRecycling() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            EventArrayAdapter adapter = new EventArrayAdapter(context, testData);
            LinearLayout parent = new LinearLayout(context);

            // 1. Get the view for position 0
            View originalView = adapter.getView(0, null, parent);
            TextView titleView = originalView.findViewById(R.id.title_textview);
            assertEquals("Test Gala", titleView.getText().toString());

            // 2. Ask for view at position 1, passing the original view to recycle
            View recycledView = adapter.getView(1, originalView, parent);

            // 3. Verify it is the SAME object instance
            assertEquals("Adapter should reuse the convertView", originalView, recycledView);

            // 4. Verify the data was updated to Event 1's data
            TextView updatedTitle = recycledView.findViewById(R.id.title_textview);
            assertEquals("Poster Party", updatedTitle.getText().toString());
        });
    }

    /**
     * Test D: Verify Admin Mode (Delete Button Visible).
     */
    @Test
    public void testAdminMode_DeleteVisible() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            // Initialize with isAdmin = true
            EventArrayAdapter adapter = new EventArrayAdapter(context, testData, true, event -> {});
            View view = adapter.getView(0, null, new LinearLayout(context));

            ImageButton deleteButton = view.findViewById(R.id.admin_delete_button);
            assertNotNull("Delete button should exist", deleteButton);
            assertEquals("Delete button should be visible for admins", View.VISIBLE, deleteButton.getVisibility());
        });
    }

    /**
     * Test E: Verify User Mode (Delete Button Hidden).
     */
    @Test
    public void testUserMode_DeleteHidden() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            // Initialize with isAdmin = false
            EventArrayAdapter adapter = new EventArrayAdapter(context, testData, false, null);
            View view = adapter.getView(0, null, new LinearLayout(context));

            ImageButton deleteButton = view.findViewById(R.id.admin_delete_button);
            assertNotNull("Delete button should exist", deleteButton);

            // Use GONE as per your adapter code logic
            assertEquals("Delete button should be GONE for regular users", View.GONE, deleteButton.getVisibility());
        });
    }

    /**
     * Test F: Verify Delete Listener Callback.
     */
    @Test
    public void testDeleteClickListener() {
        AtomicBoolean wasCalled = new AtomicBoolean(false);

        EventArrayAdapter.OnEventDeleteListener listener = event -> {
            wasCalled.set(true);
            // Verify correct event is passed back
            assertEquals("Test Gala", event.getName());
        };

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            // Admin mode enabled
            EventArrayAdapter adapter = new EventArrayAdapter(context, testData, true, listener);
            View view = adapter.getView(0, null, new LinearLayout(context));

            ImageButton deleteButton = view.findViewById(R.id.admin_delete_button);
            deleteButton.performClick();
        });

        assertTrue("Delete callback should be triggered on click", wasCalled.get());
    }
}