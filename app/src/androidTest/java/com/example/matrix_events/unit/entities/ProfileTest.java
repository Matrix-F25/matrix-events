package com.example.matrix_events.unit.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import com.example.matrix_events.entities.Profile;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Unit tests for the {@link Profile} entity class.
 * <p>
 * This suite verifies the storage of user data, the management of notification preferences,
 * database inheritance logic, and serialization for intent passing.
 * </p>
 */
public class ProfileTest {

    private final String TEST_NAME = "John Doe";
    private final String TEST_EMAIL = "john.doe@example.com";
    private final String TEST_PHONE = "555-123-4567";
    private final String TEST_DEVICE_ID = "device_xyz_987";

    /**
     * Tests the default constructor, specifically verifying default boolean flags.
     * <p>
     * <b>Expectation:</b>
     * <ul>
     * <li>Notification flags should default to {@code true}.</li>
     * <li>Admin flag should default to {@code false}.</li>
     * </ul>
     * </p>
     */
    @Test
    public void testDefaultConstructorAndFlags() {
        Profile profile = new Profile();
        assertNotNull("Profile should be instantiated", profile);

        // Check Notification Defaults (Should be TRUE)
        assertTrue("Notifications enabled by default", profile.isNotificationsEnabled());
        assertTrue("Email Admin Notifs enabled by default", profile.isEmailAdminNotifications());
        assertTrue("Email Org Notifs enabled by default", profile.isEmailOrganizerNotifications());
        assertTrue("Phone Admin Notifs enabled by default", profile.isPhoneAdminNotifications());
        assertTrue("Phone Org Notifs enabled by default", profile.isPhoneOrganizerNotifications());

        // Check Admin Default (Should be FALSE)
        assertFalse("IsAdmin should be false by default", profile.isAdmin());
    }

    /**
     * Tests the parameterized constructor.
     */
    @Test
    public void testParameterizedConstructor() {
        Profile profile = new Profile(TEST_NAME, TEST_EMAIL, TEST_PHONE, TEST_DEVICE_ID);

        assertEquals("Name should match", TEST_NAME, profile.getName());
        assertEquals("Email should match", TEST_EMAIL, profile.getEmail());
        assertEquals("Phone should match", TEST_PHONE, profile.getPhoneNumber());
        assertEquals("Device ID should match", TEST_DEVICE_ID, profile.getDeviceId());
    }

    /**
     * Tests that setters correctly update the fields.
     */
    @Test
    public void testSetters() {
        Profile profile = new Profile();

        // String fields
        profile.setName("Jane Doe");
        profile.setEmail("jane@test.com");
        profile.setPhoneNumber("123-456");
        profile.setProfilePictureUrl("http://img.url/pic.jpg");

        assertEquals("Jane Doe", profile.getName());
        assertEquals("jane@test.com", profile.getEmail());
        assertEquals("123-456", profile.getPhoneNumber());
        assertEquals("http://img.url/pic.jpg", profile.getProfilePictureUrl());

        // Boolean fields
        profile.setNotificationsEnabled(false);
        profile.setAdmin(true);

        assertFalse("Notifications should be disabled", profile.isNotificationsEnabled());
        assertTrue("Admin should be enabled", profile.isAdmin());
    }

    /**
     * Tests inheritance from {@link com.example.matrix_events.database.DBObject}.
     * Verifies ID storage and Equality logic.
     */
    @Test
    public void testDBObjectInheritance() {
        Profile p1 = new Profile(TEST_NAME, TEST_EMAIL, null, "dev1");
        Profile p2 = new Profile("Other", "other@mail.com", null, "dev2");

        // 1. Set IDs
        String docId = "user_auth_id_123";
        p1.setId(docId);
        p2.setId(docId);

        // 2. Test Get ID
        assertEquals("ID should be retrieved", docId, p1.getId());

        // 3. Test Equality (Based on ID)
        assertEquals("Profiles with same ID should be equal", p1, p2);

        // 4. Test Inequality
        p2.setId("different_id");
        assertNotEquals("Profiles with different IDs should not be equal", p1, p2);
    }

    /**
     * Tests Serialization.
     * <p>
     * Since {@code DBObject} implements {@code Serializable}, the 'id' field
     * must persist along with the Profile's specific fields.
     * </p>
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        Profile original = new Profile(TEST_NAME, TEST_EMAIL, TEST_PHONE, TEST_DEVICE_ID);
        original.setId("persistent_id_123");
        original.setNotificationsEnabled(false); // Change a boolean default
        original.setProfilePictureUrl("http://pfp.com/image.png");

        // 1. Serialize
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(original);
        out.flush();
        byte[] bytes = bos.toByteArray();

        // 2. Deserialize
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bis);
        Profile deserialized = (Profile) in.readObject();

        // 3. Verify
        assertNotNull(deserialized);
        assertNotSame("Deserialized object should be a new instance", original, deserialized);

        // Verify DBObject field
        assertEquals("ID should persist", original.getId(), deserialized.getId());

        // Verify Profile fields
        assertEquals("Name should persist", original.getName(), deserialized.getName());
        assertEquals("Email should persist", original.getEmail(), deserialized.getEmail());
        assertEquals("DeviceID should persist", original.getDeviceId(), deserialized.getDeviceId());
        assertEquals("PFP URL should persist", original.getProfilePictureUrl(), deserialized.getProfilePictureUrl());

        // Verify Boolean persistence
        assertEquals("Notification preference should persist",
                original.isNotificationsEnabled(),
                deserialized.isNotificationsEnabled());
    }
}
