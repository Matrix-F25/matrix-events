package com.example.matrix_events.unit.managers;


import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

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
 * Robust Integration Tests for {@link NotificationManager}.
 * <p>
 * This suite verifies the interaction between the NotificationManager and Firestore.
 * It focuses on the lifecycle of notifications and the specific filtering logic
 * required for the "My Notifications" views.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NotificationManagerTest implements View {

    private static final String TAG = "NotificationManagerTest";
    private NotificationManager notificationManager;
    private CountDownLatch latch;

    // Static variables to share state between ordered tests
    private static String testNotificationId;

    // Test Data
    private static final String SENDER_DEVICE_ID = "sender_" + UUID.randomUUID();
    private static final String RECEIVER_DEVICE_ID = "receiver_" + UUID.randomUUID();
    private static final String TEST_MESSAGE = "Integration Test Message " + UUID.randomUUID();

    @Before
    public void setUp() {
        notificationManager = NotificationManager.getInstance();
        notificationManager.addView(this);
    }

    /**
     * MVC Callback: Triggered when Manager finishes a DB operation.
     */
    @Override
    public void update() {
        if (latch != null) {
            Log.d(TAG, "Update received from Manager");
            latch.countDown();
        }
    }

    // ==========================================
    // 1. Create Operation
    // ==========================================

    @Test
    public void testA_CreateNotification() throws InterruptedException {
        // Create Mock Profiles
        Profile sender = new Profile("Sender", "sender@test.com", "123", SENDER_DEVICE_ID);
        Profile receiver = new Profile("Receiver", "receiver@test.com", "456", RECEIVER_DEVICE_ID);

        Notification notification = new Notification(sender, receiver, TEST_MESSAGE, Timestamp.now());

        latch = new CountDownLatch(1);
        notificationManager.createNotification(notification);

        // Wait for Firestore to update
        assertTrue("Timed out waiting for notification creation", latch.await(10, TimeUnit.SECONDS));

        // Verify creation
        boolean found = false;
        for (Notification n : notificationManager.getNotifications()) {
            if (n.getMessage().equals(TEST_MESSAGE) && n.getReceiver().getDeviceId().equals(RECEIVER_DEVICE_ID)) {
                testNotificationId = n.getId();
                found = true;
                break;
            }
        }
        assertTrue("Notification should be found in manager", found);
        assertNotNull("Notification ID should be generated", testNotificationId);
    }

    // ==========================================
    // 2. Filtering Operations
    // ==========================================

    @Test
    public void testB_FilterNotifications() {
        // Test Received Filter
        List<Notification> received = notificationManager.getReceivedNotificationsByDeviceID(RECEIVER_DEVICE_ID);
        boolean foundInReceived = false;
        for (Notification n : received) {
            if (n.getId().equals(testNotificationId)) foundInReceived = true;
        }
        assertTrue("Should find notification in receiver's list", foundInReceived);

        // Test Sent Filter
        List<Notification> sent = notificationManager.getSentNotificationsByDeviceID(SENDER_DEVICE_ID);
        boolean foundInSent = false;
        for (Notification n : sent) {
            if (n.getId().equals(testNotificationId)) foundInSent = true;
        }
        assertTrue("Should find notification in sender's list", foundInSent);

        // Negative Test: Random User
        List<Notification> random = notificationManager.getReceivedNotificationsByDeviceID("random_id");
        boolean foundInRandom = false;
        for (Notification n : random) {
            if (n.getId().equals(testNotificationId)) foundInRandom = true;
        }
        assertFalse("Should NOT find notification in random user's list", foundInRandom);
    }

    // ==========================================
    // 3. Update Operation
    // ==========================================

    @Test
    public void testC_UpdateNotification() throws InterruptedException {
        // Fetch the notification
        Notification notification = notificationManager.getNotificationByDBID(testNotificationId);
        assertNotNull("Notification must exist for update test", notification);

        // Verify initial state (unread)
        assertFalse("Should be unread initially", notification.getReadFlag());

        // Update state
        notification.setReadFlag(true);

        latch = new CountDownLatch(1);
        notificationManager.updateNotification(notification);

        assertTrue("Timed out waiting for notification update", latch.await(10, TimeUnit.SECONDS));

        // Verify Update
        Notification updated = notificationManager.getNotificationByDBID(testNotificationId);
        assertNotNull(updated);
        assertTrue("Read flag should be updated to true", updated.getReadFlag());
    }

    // ==========================================
    // 4. Delete Operation
    // ==========================================

    @Test
    public void testD_DeleteNotification() throws InterruptedException {
        Notification notification = notificationManager.getNotificationByDBID(testNotificationId);

        if (notification != null) {
            latch = new CountDownLatch(1);
            notificationManager.deleteNotification(notification);

            assertTrue("Timed out waiting for delete", latch.await(10, TimeUnit.SECONDS));

            // Verify it's gone using getNotificationByDBID
            assertNull("Notification should be removed from cache", notificationManager.getNotificationByDBID(testNotificationId));

            // Double check existence in filtered lists
            List<Notification> received = notificationManager.getReceivedNotificationsByDeviceID(RECEIVER_DEVICE_ID);
            for (Notification n : received) {
                assertFalse("Deleted notification should not appear in filters", n.getId().equals(testNotificationId));
            }
        }
    }

    @After
    public void tearDown() {
        notificationManager.removeView(this);
    }
}