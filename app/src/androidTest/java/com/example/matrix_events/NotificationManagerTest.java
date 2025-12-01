package com.example.matrix_events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.NotificationManager;
import com.example.matrix_events.mvc.View;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented Integration Tests for {@link NotificationManager}.
 * <p>
 * This suite verifies the interaction between the NotificationManager and the Firestore database.
 * It is critical for ensuring that the Push Notification system works, specifically verifying
 * that notifications can be retrieved by their ID (Deep Linking) and filtered by receiver.
 * </p>
 * <p>
 * <b>Test Strategy:</b> Uses {@link MethodSorters#NAME_ASCENDING} to execute tests in a specific order
 * (Create -> Read -> Filter -> Delete) to simulate the lifecycle of a notification.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotificationManagerTest implements View {

    private static final String TAG = "NotificationManagerTest";
    private NotificationManager notificationManager;
    private CountDownLatch latch;

    // Static variables to persist data across the ordered test steps
    private static Notification testNotification;
    private static String testNotificationId;
    private static String senderDeviceId;
    private static String receiverDeviceId;

    /**
     * Sets up the test environment.
     * <p>
     * Generates unique device IDs for the sender and receiver to ensure this test run
     * does not collide with existing data. Registers this test class as a View to
     * receive async updates from the Manager.
     * </p>
     */
    @Before
    public void setUp() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        // Generate unique IDs for this test run to avoid collisions
        if (senderDeviceId == null) {
            senderDeviceId = "sender_" + UUID.randomUUID().toString();
            receiverDeviceId = "receiver_" + UUID.randomUUID().toString();
        }

        notificationManager = NotificationManager.getInstance();
        notificationManager.addView(this);
    }

    /**
     * MVC Callback: Triggered when NotificationManager finishes a DB operation.
     */
    @Override
    public void update() {
        if (latch != null) {
            Log.d(TAG, "Update received from NotificationManager");
            latch.countDown();
        }
    }

    /**
     * Test A: Create a Notification.
     * <p>
     * <b>Scenario:</b> Create a new {@link Notification} object and save it to Firestore.<br>
     * <b>Expected Result:</b> The notification is successfully uploaded, and the Manager's local cache
     * updates to include it. We verify this by polling the list until the item appears.
     * </p>
     */
    @Test
    public void testA_CreateNotification() throws InterruptedException {
        // 1. Prepare Data
        Profile sender = new Profile("Sender", "sender@test.com", "555-1111", senderDeviceId);
        Profile receiver = new Profile("Receiver", "receiver@test.com", "555-2222", receiverDeviceId);

        // Use the 5-argument constructor to satisfy strict type requirements
        testNotification = new Notification(
                sender,
                receiver,
                "Test Message for Deep Linking",
                Notification.NotificationType.ADMIN,
                Timestamp.now()
        );

        // 2. Perform Create
        notificationManager.createNotification(testNotification);

        // 3. POLLING FIX: Wait for the specific data to arrive.
        // We cannot rely on a single latch because other updates (like 'patching' in NotificationManager)
        // might trigger the view update before our new item arrives.
        boolean found = false;
        long endTime = System.currentTimeMillis() + 10000; // 10 second timeout

        while (System.currentTimeMillis() < endTime) {
            for (Notification n : notificationManager.getNotifications()) {
                // Match by message and sender/receiver combo
                if (n.getMessage().equals("Test Message for Deep Linking") &&
                        n.getReceiver().getDeviceId().equals(receiverDeviceId)) {
                    testNotificationId = n.getId();
                    found = true;
                    break;
                }
            }

            if (found) break;

            // Wait 1 second before checking again
            Thread.sleep(1000);
        }

        assertTrue("Notification should be found in manager after wait", found);
        assertNotNull("Notification ID should be generated", testNotificationId);
    }

    /**
     * Test B: Lookup by DB ID (CRITICAL for Deep Linking).
     * <p>
     * <b>Scenario:</b> Call {@code getNotificationByDBID} using the ID generated in Test A.<br>
     * <b>Expected:</b> Returns the correct Notification object. This simulates the exact logic
     * used by {@code NotificationActivity} when opened via a push notification click.
     * </p>
     */
    @Test
    public void testB_GetNotificationByDBID() {
        assertNotNull("Requires Test A to pass first", testNotificationId);

        Notification result = notificationManager.getNotificationByDBID(testNotificationId);

        assertNotNull("Should find notification by ID", result);
        assertEquals("Test Message for Deep Linking", result.getMessage());
        assertEquals(Notification.NotificationType.ADMIN, result.getType());
    }

    /**
     * Test C: Filter by Receiver.
     * <p>
     * <b>Scenario:</b> Call {@code getReceivedNotificationsByDeviceID} for the test receiver.<br>
     * <b>Expected:</b> Returns a list containing our test notification, verifying the filtering logic.
     * </p>
     */
    @Test
    public void testC_FilterByReceiver() {
        List<Notification> myNotifications = notificationManager.getReceivedNotificationsByDeviceID(receiverDeviceId);

        assertFalse("Should have at least one notification", myNotifications.isEmpty());

        boolean found = false;
        for (Notification n : myNotifications) {
            if (n.getId().equals(testNotificationId)) found = true;
        }
        assertTrue("Test notification should be in receiver's list", found);
    }

    /**
     * Test D: Delete Notification.
     * <p>
     * <b>Scenario:</b> Delete the test notification.<br>
     * <b>Expected:</b> The notification is removed from the Manager's cache and ID lookup returns null.
     * </p>
     */
    @Test
    public void testD_DeleteNotification() throws InterruptedException {
        Notification target = notificationManager.getNotificationByDBID(testNotificationId);
        if (target != null) {
            // Perform delete
            notificationManager.deleteNotification(target);

            // POLLING FIX: Wait for the item to disappear.
            // Using a latch is unreliable here because other updates might fire first.
            // We repeatedly check if getNotificationByDBID returns null.
            boolean isGone = false;
            long endTime = System.currentTimeMillis() + 10000; // 10 second timeout

            while (System.currentTimeMillis() < endTime) {
                if (notificationManager.getNotificationByDBID(testNotificationId) == null) {
                    isGone = true;
                    break;
                }
                // Wait 1 second before checking again
                Thread.sleep(1000);
            }

            assertTrue("Notification should be gone from manager after wait", isGone);
        }
    }

    /**
     * Cleanup method.
     * Removes the test view from the manager to prevent memory leaks.
     */
    @After
    public void tearDown() {
        notificationManager.removeView(this);
    }
}