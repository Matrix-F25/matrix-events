package com.example.matrix_events.unit.entities;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.example.matrix_events.entities.Profile;

import org.junit.Test;

/**
 * Unit tests for the {@link Profile} model class.
 * <p>
 * This class is responsible for verifying the integrity of the Profile entity,
 * ensuring that data encapsulation (getters/setters), constructor initialization,
 * and business logic (such as default flag values and null-safety checks) function as expected.
 * </p>
 *
 * @see Profile
 */
public class ProfileTest {

    /**
     * Tests the zero-argument constructor required by Firestore.
     * <p>
     * <b>Scenario:</b> Create a new Profile using the empty constructor.<br>
     * <b>Expected Result:</b> The instance is not null, ensuring Firestore can
     * deserialize data into this object without crashing.
     * </p>
     */
    @Test
    public void testEmptyConstructor() {
        Profile profile = new Profile();
        assertNotNull("Profile instance should not be null", profile);
    }

    /**
     * Tests the parameterized constructor to ensure all fields are initialized correctly.
     * <p>
     * <b>Scenario:</b> Create a Profile with specific Name, Email, Phone, and Device ID.<br>
     * <b>Expected Result:</b> All getters return the exact values passed to the constructor,
     * and optional fields (like profile images) are initialized to safe defaults (empty strings)
     * rather than null.
     * </p>
     */
    @Test
    public void testParameterizedConstructor() {
        String name = "Neo";
        String email = "neo@matrix.com";
        String phone = "555-1234";
        String deviceId = "device_001";

        Profile profile = new Profile(name, email, phone, deviceId);

        // Verify inputs are assigned correctly
        assertEquals("Name should match constructor input", name, profile.getName());
        assertEquals("Email should match constructor input", email, profile.getEmail());
        assertEquals("Phone should match constructor input", phone, profile.getPhoneNumber());
        assertEquals("DeviceId should match constructor input", deviceId, profile.getDeviceId());

        // Verify the "Safe Defaults" logic in your constructor
        assertEquals("Profile picture URL should default to empty string", "", profile.getProfilePictureUrl());
        assertEquals("Profile picture filename should default to empty string", "", profile.getProfilePictureFileName());
    }

    /**
     * Verifies that a new Profile has specific default boolean values (flags).
     * <p>
     * <b>Scenario:</b> Instantiate a new Profile.<br>
     * <b>Expected Result:</b> All notification preferences are {@code true} (enabled) by default,
     * and the Admin status is {@code false} (disabled) by default.
     * </p>
     */
    @Test
    public void testNotificationDefaults() {
        Profile profile = new Profile("Trinity", "trinity@matrix.com", null, "device_002");

        assertTrue("Push Admin notifications should be enabled by default", profile.isPushAdminNotifications());
        assertTrue("Push Organizer notifications should be enabled by default", profile.isPushOrganizerNotifications());

        assertFalse("New profile should not be admin by default", profile.isAdmin());
    }

    /**
     * Tests the encapsulation logic (getters and setters) for various data types.
     * <p>
     * <b>Scenario:</b> Update fields using setters and retrieve them using getters.<br>
     * <b>Expected Result:</b> The values returned by the getters match the values
     * most recently passed to the setters.
     * </p>
     */
    @Test
    public void testSettersAndGetters() {
        Profile profile = new Profile();

        // Test String setters
        profile.setName("Morpheus");
        assertEquals("Morpheus", profile.getName());

        profile.setEmail("morpheus@matrix.com");
        assertEquals("morpheus@matrix.com", profile.getEmail());

        // Test Boolean setters
        profile.setAdmin(true);
        assertTrue("Admin status should be updated to true", profile.isAdmin());
    }

    /**
     * Tests the null-checking logic in {@link Profile#setProfilePictureUrl(String)}.
     * <p>
     * <b>Scenario:</b> Attempt to set the profile picture URL to {@code null}.<br>
     * <b>Expected Result:</b> The setter intercepts the null value and sets the field to an
     * empty string ("") to prevent {@link NullPointerException} in the UI layer.
     * </p>
     */
    @Test
    public void testProfilePictureNullSafety() {
        Profile profile = new Profile();

        // 1. Test setting a valid URL
        String validUrl = "https://example.com/avatar.png";
        profile.setProfilePictureUrl(validUrl);
        assertEquals(validUrl, profile.getProfilePictureUrl());

        // 2. Test setting NULL
        profile.setProfilePictureUrl(null);
        assertEquals("Setting null URL should result in empty string based on class logic", "", profile.getProfilePictureUrl());
    }

    /**
     * Verifies the storage and retrieval of the Firebase Cloud Messaging (FCM) token.
     * <p>
     * <b>Scenario:</b> Set and retrieve the FCM Token string.<br>
     * <b>Expected Result:</b> The default token is empty, and updates are persisted correctly.
     * </p>
     */
    @Test
    public void testFCMTokenHandling() {
        Profile profile = new Profile();

        // Verify default is empty
        assertEquals("", profile.getFCMToken());

        // Verify update
        profile.setFCMToken("token_12345");
        assertEquals("token_12345", profile.getFCMToken());
    }
}