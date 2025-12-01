package com.example.matrix_events.unit.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.matrix_events.adapters.NotificationArrayAdapter;
import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.entities.Profile;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Date;

/**
 * Instrumented Unit Tests for {@link NotificationArrayAdapter}.
 * <p>
 * This suite verifies the conditional view binding logic dependent on the
 * adapter type ("admin" vs "entrant").
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class NotificationArrayAdapterTest {

    private Context context;
    private ArrayList<Notification> testData;
    private Notification testNotification;
    private Profile sender;
    private Profile receiver;

    @Before
    public void setUp() {
        // Use Material Theme for MaterialButton compatibility
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context = new ContextThemeWrapper(targetContext, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar);

        testData = new ArrayList<>();

        // Create Mock Profiles
        sender = new Profile("Alice Sender", "alice@test.com", "123", "device_alice");
        receiver = new Profile("Bob Receiver", "bob@test.com", "456", "device_bob");

        // Create Notification
        testNotification = new Notification(sender, receiver, "This is a test message body.", Timestamp.now());
        testData.add(testNotification);
    }

    /**
     * Test A: Verify View Binding for "Entrant" (Default) Mode.
     * <p>
     * <b>Scenario:</b> Adapter initialized with default constructor (or "entrant").<br>
     * <b>Expected:</b> Title should format as "New message from: [Sender Name]".
     * </p>
     */
    @Test
    public void testEntrantViewBinding() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            // Default constructor implies "entrant"
            NotificationArrayAdapter adapter = new NotificationArrayAdapter(context, testData);
            View view = adapter.getView(0, null, new LinearLayout(context));

            TextView titleView = view.findViewById(com.example.matrix_events.R.id.text_message_title);
            TextView bodyView = view.findViewById(com.example.matrix_events.R.id.text_message_body_preview);
            MaterialButton seeMoreButton = view.findViewById(com.example.matrix_events.R.id.button_see_more);

            assertNotNull("Title TextView should exist", titleView);
            // Verify Entrant Format
            String expectedTitle = "New message from: " + sender.getName();
            assertEquals("Entrant title format incorrect", expectedTitle, titleView.getText().toString());

            assertNotNull("Body TextView should exist", bodyView);
            assertEquals("This is a test message body.", bodyView.getText().toString());

            assertNotNull("See More button should exist", seeMoreButton);
        });
    }

    /**
     * Test B: Verify View Binding for "Admin" Mode.
     * <p>
     * <b>Scenario:</b> Adapter initialized with "admin" type.<br>
     * <b>Expected:</b> Title should format as "[Sender] sent to [Receiver]".
     * </p>
     */
    @Test
    public void testAdminViewBinding() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            // Explicitly pass "admin"
            NotificationArrayAdapter adapter = new NotificationArrayAdapter(context, testData, "admin");
            View view = adapter.getView(0, null, new LinearLayout(context));

            TextView titleView = view.findViewById(com.example.matrix_events.R.id.text_message_title);

            assertNotNull("Title TextView should exist", titleView);
            // Verify Admin Format
            String expectedTitle = sender.getName() + " sent to " + receiver.getName();
            assertEquals("Admin title format incorrect", expectedTitle, titleView.getText().toString());
        });
    }

    /**
     * Test C: Verify View Recycling.
     * <p>
     * Ensures that views are reused correctly.
     * </p>
     */
    @Test
    public void testViewRecycling() {
        // Add a second item to test data change
        Notification secondNotification = new Notification(receiver, sender, "Reply message", Timestamp.now());
        testData.add(secondNotification);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            NotificationArrayAdapter adapter = new NotificationArrayAdapter(context, testData);
            LinearLayout parent = new LinearLayout(context);

            // 1. Get initial view
            View originalView = adapter.getView(0, null, parent);
            TextView titleView = originalView.findViewById(com.example.matrix_events.R.id.text_message_title);
            assertEquals("New message from: Alice Sender", titleView.getText().toString());

            // 2. Recycle it for position 1
            View recycledView = adapter.getView(1, originalView, parent);

            // 3. Verify object reuse
            assertEquals("Adapter should reuse convertView", originalView, recycledView);

            // 4. Verify data update
            TextView updatedTitle = recycledView.findViewById(com.example.matrix_events.R.id.text_message_title);
            // Second msg is from Bob (Receiver of first msg)
            assertEquals("New message from: Bob Receiver", updatedTitle.getText().toString());
        });
    }
}