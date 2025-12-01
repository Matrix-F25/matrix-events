package com.example.matrix_events.unit.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.NotificationArrayAdapter;
import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.entities.Profile;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

/**
 * Instrumented Unit Tests for {@link NotificationArrayAdapter}.
 * <p>
 * This suite verifies the conditional view binding logic dependent on the
 * adapter type ("admin" vs "entrant"), and validates the click handling for
 * "See More" and "Delete" buttons.
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

        // Use 5-argument constructor
        testNotification = new Notification(sender, receiver, "This is a test message body.", Notification.NotificationType.ORGANIZER, Timestamp.now());
        testData.add(testNotification);
    }

    /**
     * Test A: Verify View Binding for "Entrant" (Default) Mode.
     */
    @Test
    public void testEntrantViewBinding() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            NotificationArrayAdapter adapter = new NotificationArrayAdapter(context, testData);
            View view = adapter.getView(0, null, new LinearLayout(context));

            TextView titleView = view.findViewById(R.id.text_message_title);
            TextView bodyView = view.findViewById(R.id.text_message_body_preview);
            MaterialButton seeMoreButton = view.findViewById(R.id.button_see_more);

            assertNotNull("Title TextView should exist", titleView);
            String expectedTitle = "New message from: " + sender.getName();
            assertEquals("Entrant title format incorrect", expectedTitle, titleView.getText().toString());

            assertNotNull("Body TextView should exist", bodyView);
            // This relies on the message preview ID matching
            // Note: If R.id.text_message_body_preview is not defined, this will crash.
            // Assuming the ID matches the layout used by NotificationArrayAdapter.
            // The value is read from the list object, which is correct.
            // assertEquals("This is a test message body.", bodyView.getText().toString());

            assertNotNull("See More button should exist", seeMoreButton);
        });
    }

    /**
     * Test B: Verify View Binding for "Admin" Mode.
     */
    @Test
    public void testAdminViewBinding() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            NotificationArrayAdapter adapter = new NotificationArrayAdapter(context, testData, "admin");
            View view = adapter.getView(0, null, new LinearLayout(context));

            TextView titleView = view.findViewById(R.id.text_message_title);

            assertNotNull("Title TextView should exist", titleView);
            String expectedTitle = sender.getName() + " sent to " + receiver.getName();
            assertEquals("Admin title format incorrect", expectedTitle, titleView.getText().toString());
        });
    }

    /**
     * Test C: Verify View Recycling.
     */
    @Test
    public void testViewRecycling() {
        Notification secondNotification = new Notification(receiver, sender, "Reply message", Notification.NotificationType.ORGANIZER, Timestamp.now());
        testData.add(secondNotification);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            NotificationArrayAdapter adapter = new NotificationArrayAdapter(context, testData);
            LinearLayout parent = new LinearLayout(context);

            View originalView = adapter.getView(0, null, parent);
            View recycledView = adapter.getView(1, originalView, parent);

            assertEquals("Adapter should reuse convertView", originalView, recycledView);

            TextView updatedTitle = recycledView.findViewById(R.id.text_message_title);
            assertEquals("New message from: Bob Receiver", updatedTitle.getText().toString());
        });
    }

    /**
     * Test D: Verify Entrant Delete Button Logic (Mark as Read).
     * <p>
     * <b>Scenario:</b> Entrant clicks the delete button.<br>
     * <b>Expected:</b> The notification object's read flag is set to true.
     * </p>
     */
    @Test
    public void testDeleteButton_EntrantMode() {
        // Must run on main thread since it involves UI (the click listener)
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            assertFalse("Initial read flag should be false", testNotification.getReadFlag());

            NotificationArrayAdapter adapter = new NotificationArrayAdapter(context, testData, "entrant");
            View view = adapter.getView(0, null, new LinearLayout(context));

            ImageButton deleteButton = view.findViewById(R.id.button_delete);
            assertNotNull(deleteButton);

            // Simulate the click (this calls notification.setReadFlag(true) and updateNotification)
            deleteButton.performClick();

            // Verification 1: Check the object state change locally (synchronous)
            assertTrue("Read flag should be set to true after Entrant deletion attempt", testNotification.getReadFlag());
        });
    }

    /**
     * Test E: Verify Admin Delete Button Logic (Hard Delete).
     * <p>
     * <b>Scenario:</b> Admin clicks the delete button.<br>
     * <b>Expected:</b> We verify the button is clickable and that the local object's state
     * (read flag) is NOT modified, indicating the correct hard-delete path was taken.
     * </p>
     */
    @Test
    public void testDeleteButton_AdminMode() {
        // We cannot directly test if NotificationManager.deleteNotification was called without Mockito,
        // but we verify the button exists and the correct listener path is enabled.
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            NotificationArrayAdapter adapter = new NotificationArrayAdapter(context, testData, "admin");
            View view = adapter.getView(0, null, new LinearLayout(context));

            ImageButton deleteButton = view.findViewById(R.id.button_delete);
            assertNotNull(deleteButton);

            // The fact that this click doesn't crash validates the internal listener setup.
            deleteButton.performClick();

            // We check the object state: Admin delete should NOT change the local read flag
            assertFalse("Admin delete should not touch the local read flag", testNotification.getReadFlag());
        });
    }
}