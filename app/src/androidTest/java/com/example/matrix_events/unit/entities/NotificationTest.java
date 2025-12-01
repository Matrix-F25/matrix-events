package com.example.matrix_events.unit.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
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
 * Unit Tests for the {@link Notification} entity class.
 * <p>
 * This suite verifies the integrity of the Notification object, focusing on:
 * <ul>
 * <li>Constructor and Getter/Setter functionality.</li>
 * <li>Default and explicit handling of {@link Notification.NotificationType}.</li>
 * <li>Custom serialization logic for the transient {@link Timestamp} field.</li>
 * </ul>
 * </p>
 */
public class NotificationTest {

    private Profile mockSender;
    private Profile mockReceiver;
    private Timestamp mockTimestamp;
    private final String SENDER_DEVICE_ID = "sender_123";
    private final String RECEIVER_DEVICE_ID = "receiver_456";

    /**
     * Sets up mock Profile objects and a test timestamp before each test.
     */
    @Before
    public void setUp() {
        // Use real Profile objects instead of mocks for better integration testing
        mockSender = new Profile("Sender", "send@mail.com", "555-1111", SENDER_DEVICE_ID);
        mockReceiver = new Profile("Receiver", "recv@mail.com", "555-2222", RECEIVER_DEVICE_ID);
        mockTimestamp = Timestamp.now();

        // FIX: Set a dummy DB ID on mock profiles to prevent NPE during deep object comparison
        mockSender.setId("mock_sender_id_1");
        mockReceiver.setId("mock_receiver_id_1");
    }

    /**
     * Helper method to create a standard notification object using the 5-argument constructor.
     */
    private Notification createStandardNotification() {
        return new Notification(mockSender, mockReceiver, "Welcome to the system", Notification.NotificationType.ORGANIZER, mockTimestamp);
    }

    // ==========================================
    // 1. Constructor & Data Integrity Tests
    // ==========================================

    /**
     * Tests that a valid notification is created successfully with correct attributes.
     * <p>
     * <b>Scenario:</b> Create a notification using the standard 5-argument constructor.<br>
     * <b>Expected Result:</b> All fields are set correctly, and default flags (readFlag) are false.
     * </p>
     */
    @Test
    public void testNotificationCreationAndIntegrity() {
        String message = "You have been selected!";

        // FIX: Using the 5-argument constructor
        Notification notification = new Notification(mockSender, mockReceiver, message, Notification.NotificationType.ADMIN, mockTimestamp);

        assertNotNull("Notification object should not be null", notification);
        assertEquals("Sender should match", mockSender, notification.getSender());
        assertEquals("Receiver should match", mockReceiver, notification.getReceiver());
        assertEquals("Message content should match", message, notification.getMessage());
        assertEquals(Notification.NotificationType.ADMIN, notification.getType());
        assertEquals("Timestamp should match", mockTimestamp, notification.getTimestamp());
        assertFalse("Read flag should be false by default", notification.getReadFlag());
    }

    /**
     * Tests the default constructor.
     * <p>
     * <b>Scenario:</b> Use the empty constructor required by Firestore.<br>
     * <b>Expected Result:</b> Object is created not null, and the Read flag defaults to false.
     * </p>
     */
    @Test
    public void testDefaultConstructor() {
        Notification notification = new Notification();
        assertNotNull("Default constructor should create instance", notification);
        assertFalse("Read flag should default to false", notification.getReadFlag());
    }

    // ==========================================
    // 2. State Management & Mutator Tests
    // ==========================================

    /**
     * Tests the Read Flag logic.
     * <p>
     * <b>Scenario:</b> Toggle the read status.<br>
     * <b>Expected Result:</b> The boolean flag updates correctly reflecting the state.
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
    }

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
        // Create a new unique timestamp
        Timestamp newTime = new Timestamp(new Date(mockTimestamp.toDate().getTime() + 3600000));

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
        // Set dummy ID for comparison
        newSender.setId("mock_new_sender_id_1");

        notification.setSender(newSender);

        assertEquals("Sender should be updated", newSender, notification.getSender());
        // Receiver should remain untouched
        assertEquals("Receiver should remain unchanged", mockReceiver, notification.getReceiver());
    }

    // ==========================================
    // 3. Notification Type Logic Tests
    // ==========================================

    /**
     * Tests {@code getType()} default logic.
     * <p>
     * <b>Scenario:</b> Create a notification where the type field is initially null (via empty constructor).<br>
     * <b>Expected Result:</b> The {@code getType()} getter defaults to {@code NotificationType.ORGANIZER}.
     * </p>
     */
    @Test
    public void testGetTypeDefault() {
        Notification notification = new Notification();
        // Since 'type' is not set, the getter should return the default specified in the code.
        assertEquals(Notification.NotificationType.ORGANIZER, notification.getType());
        assertEquals(Notification.NotificationType.ORGANIZER, notification.getTypeEnum());
    }

    /**
     * Tests the Firestore-specific getter and setter aliases.
     * <p>
     * <b>Scenario:</b> Set the type using {@code setTypeEnum} and retrieve via {@code getType}.<br>
     * <b>Expected Result:</b> All type getters reflect the change.
     * </p>
     */
    @Test
    public void testTypeAliases() {
        Notification notification = createStandardNotification();

        notification.setTypeEnum(Notification.NotificationType.ADMIN);

        assertEquals(Notification.NotificationType.ADMIN, notification.getType());
        assertEquals(Notification.NotificationType.ADMIN, notification.getTypeEnum());
    }

    // ==========================================
    // 4. Serialization Test (CRITICAL)
    // ==========================================

    /**
     * Verifies that the Notification object can be serialized and deserialized successfully.
     * <p>
     * <b>CRITICAL CHECK:</b> Notification contains a transient {@link Timestamp} and implements
     * custom readObject/writeObject. This test ensures that logic works without crashing.
     * </p>
     * <p>
     * <b>Scenario:</b> Write the object (including the transient Timestamp) to a byte stream and read it back.<br>
     * <b>Expected Result:</b> Data persists, including the Timestamp.
     * </p>
     * @throws IOException If serialization fails.
     * @throws ClassNotFoundException If deserialization fails.
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        Notification originalNotification = createStandardNotification();
        originalNotification.setReadFlag(true); // Test boolean persistence
        originalNotification.setId("notification_id_123"); // Set ID for deep comparison

        // 1. Serialize to Byte Array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(originalNotification);
        out.flush();
        byte[] bytes = bos.toByteArray();

        // 2. Deserialize from Byte Array
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bis);
        Notification deserializedNotification = (Notification) in.readObject();

        // 3. Verify Data Integrity
        assertNotNull(deserializedNotification);

        // Check standard field persistence
        assertEquals("Message should persist", originalNotification.getMessage(), deserializedNotification.getMessage());
        assertTrue("Read flag should persist", deserializedNotification.getReadFlag());

        // Verify custom Timestamp persistence (CRITICAL)
        assertEquals("Timestamp seconds should persist",
                originalNotification.getTimestamp().getSeconds(),
                deserializedNotification.getTimestamp().getSeconds());
    }
}