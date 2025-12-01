package com.example.matrix_events.unit.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import com.example.matrix_events.entities.Notification;
import com.example.matrix_events.entities.Profile;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

/**
 * Robust Unit Tests for the {@link Notification} entity class.
 * <p>
 * This suite verifies the integrity of the Notification object, ensuring that data
 * is stored correctly, state flags (read/unread) function as expected, and the object
 * can be safely serialized for Intent passing.
 * </p>
 * <p>
 * <b>Coverage Includes:</b>
 * <ul>
 * <li><b>Constructor Validation:</b> Ensures sender, receiver, and message data is correctly assigned.</li>
 * <li><b>State Management:</b> Verifies the default unread state and the toggle logic.</li>
 * <li><b>Mutator Logic:</b> Tests updating messages and timestamps.</li>
 * <li><b>Serialization:</b> Verifies that the object (and its non-serializable Timestamp fields)
 * can be passed between components without crashing.</li>
 * </ul>
 * </p>
 */
public class NotificationTest {

    private Profile mockSender;
    private Profile mockReceiver;
    private Timestamp now;

    /**
     * Sets up the test environment before each test method.
     * <p>
     * Initializes real Profile objects for the sender and receiver interaction
     * and captures the current timestamp.
     * </p>
     */
    @Before
    public void setUp() {
        mockSender = new Profile("Sender Name", "sender@test.com", "555-0001", "device_sender");
        // FIX: Set a dummy DB ID so DBObject.equals() does not crash with NPE
        mockSender.setId("mock_sender_id_1");

        mockReceiver = new Profile("Receiver Name", "receiver@test.com", "555-0002", "device_receiver");
        // FIX: Set a dummy DB ID so DBObject.equals() does not crash with NPE
        mockReceiver.setId("mock_receiver_id_1");

        now = Timestamp.now();
    }

    /**
     * Helper method to create a standard notification object.
     */
    private Notification createStandardNotification() {
        return new Notification(mockSender, mockReceiver, "Welcome to the system", now);
    }

    // ==========================================
    // 1. Constructor & Data Integrity Tests
    // ==========================================

    /**
     * Tests that a valid notification is created successfully with correct attributes.
     * <p>
     * <b>Expected Result:</b> The Notification object is instantiated, and all getter methods
     * return the exact objects passed into the constructor.
     * </p>
     */
    @Test
    public void testNotificationCreation() {
        String message = "You have been selected!";
        Notification notification = new Notification(mockSender, mockReceiver, message, now);

        assertNotNull("Notification object should not be null", notification);
        assertEquals("Sender should match", mockSender, notification.getSender());
        assertEquals("Receiver should match", mockReceiver, notification.getReceiver());
        assertEquals("Message content should match", message, notification.getMessage());
        assertEquals("Timestamp should match", now, notification.getTimestamp());
    }

    /**
     * Tests the default constructor.
     * <p>
     * <b>Scenario:</b> Firestore often requires a no-argument constructor to map data.<br>
     * <b>Expected Result:</b> Object is created not null, though fields may be null until set.
     * </p>
     */
    @Test
    public void testDefaultConstructor() {
        Notification notification = new Notification();
        assertNotNull("Default constructor should create instance", notification);
        // By default, boolean primitives are false
        assertFalse("Read flag should default to false", notification.getReadFlag());
    }

    // ==========================================
    // 2. State Management (Read/Unread)
    // ==========================================

    /**
     * Tests the Read Flag logic.
     * <p>
     * <b>Scenario:</b>
     * <ol>
     * <li>Verify default state is Unread (false).</li>
     * <li>Mark as Read (true).</li>
     * <li>Mark as Unread (false) again.</li>
     * </ol>
     * <b>Expected Result:</b> The boolean flag updates correctly reflecting the UI state.
     * </p>
     */
    @Test
    public void testReadFlagState() {
        Notification notification = createStandardNotification();

        // 1. Check Default
        assertFalse("New notification should be unread by default", notification.getReadFlag());

        // 2. Mark Read
        notification.setReadFlag(true);
        assertTrue("Notification should be marked as read", notification.getReadFlag());

        // 3. Mark Unread (e.g., user marks as unread in UI)
        notification.setReadFlag(false);
        assertFalse("Notification should be marked as unread", notification.getReadFlag());
    }

    // ==========================================
    // 3. Mutator Tests
    // ==========================================

    /**
     * Tests the setters for updating notification details.
     * <p>
     * <b>Scenario:</b> Update the message body and the timestamp of an existing notification.<br>
     * <b>Expected Result:</b> Getters return the new values.
     * </p>
     */
    @Test
    public void testUpdateDetails() {
        Notification notification = createStandardNotification();

        String newMessage = "Update: Event cancelled";
        Timestamp newTime = new Timestamp(new Date(now.toDate().getTime() + 3600)); // +1 hour

        notification.setMessage(newMessage);
        notification.setTimestamp(newTime);

        assertEquals("Message should be updated", newMessage, notification.getMessage());
        assertEquals("Timestamp should be updated", newTime, notification.getTimestamp());
    }

    /**
     * Tests updating the profiles attached to the notification.
     * <p>
     * <b>Scenario:</b> The sender profile is updated (e.g., profile picture change propagated).<br>
     * <b>Expected Result:</b> The notification holds the reference to the new profile object.
     * </p>
     */
    @Test
    public void testUpdateProfiles() {
        Notification notification = createStandardNotification();
        Profile newSender = new Profile("New Name", "new@test.com", "123", "dev_new");
        // FIX: Set a dummy DB ID for the new sender as well
        newSender.setId("mock_new_sender_id_1");

        notification.setSender(newSender);

        assertEquals("Sender should be updated", newSender, notification.getSender());
        // Receiver should remain untouched
        assertEquals("Receiver should remain unchanged", mockReceiver, notification.getReceiver());
    }

    // ==========================================
    // 4. Serialization Test
    // ==========================================

    /**
     * Verifies that the Notification object can be serialized.
     * <p>
     * <b>CRITICAL CHECK:</b> Notification contains a transient {@link Timestamp} and implements
     * custom readObject/writeObject. This test ensures that logic works without crashing.
     * </p>
     * <p>
     * <b>Scenario:</b> Serialize to byte stream -> Deserialize back to object.<br>
     * <b>Expected Result:</b> Data persists, including the Timestamp.
     * </p>
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        Notification original = createStandardNotification();
        original.setReadFlag(true); // Set non-default state
        original.setId("notification_id_123"); // Set ID for strict equality checks

        // 1. Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(original);
        out.flush();
        byte[] bytes = bos.toByteArray();

        // 2. Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bis);
        Notification deserialized = (Notification) in.readObject();

        // 3. Verify
        assertNotNull(deserialized);
        assertNotSame("Deserialized object should be a new instance", original, deserialized);

        assertEquals("Message should persist", original.getMessage(), deserialized.getMessage());
        assertEquals("Read Flag should persist", original.getReadFlag(), deserialized.getReadFlag());

        // Verify nested objects (assuming Profile is Serializable)
        assertEquals("Sender name should persist",
                original.getSender().getName(),
                deserialized.getSender().getName());

        // Verify Timestamp persistence
        // Note: Comparing seconds/nanos because object references differ after serialization
        assertEquals("Timestamp seconds should persist",
                original.getTimestamp().getSeconds(),
                deserialized.getTimestamp().getSeconds());
    }
}