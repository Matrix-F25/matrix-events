package com.example.matrix_events.unit.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.mvc.View;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Instrumented Integration Tests for the {@link ProfileManager}.
 * <p>
 * This suite tests the full CRUD (Create, Read, Update, Delete) lifecycle of a Profile
 * against the real Firebase Firestore database.
 * </p>
 * <p>
 * <b>Test Strategy:</b> We use {@link MethodSorters#NAME_ASCENDING} to run tests in a specific order.
 * This allows us to Create a profile in test B, Update that same profile in test D,
 * and Delete it in test E, verifying the entire lifecycle of the data.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProfileManagerTest implements View {

    private static final String TAG = "ProfileManagerTest";
    private ProfileManager profileManager;
    private CountDownLatch latch;

    // Static variables to share state between the ordered tests
    private static String testDeviceId;
    private static String testProfileId; // Stores the Firestore ID generated in the Create step

    /**
     * Sets up the test environment.
     * <p>
     * Initializes the ProfileManager and registers this test class as a MVC View
     * to receive asynchronous callbacks from Firestore.
     * </p>
     */
    @Before
    public void setUp() {
        profileManager = ProfileManager.getInstance();

        // Register this class as a View to handle update() callbacks
        profileManager.addView(this);

        // Generate a random Device ID once for the entire test suite execution
        // This ensures we don't overwrite real user data
        if (testDeviceId == null) {
            testDeviceId = "test_device_" + UUID.randomUUID().toString();
        }
    }

    /**
     * MVC Callback: Triggered when ProfileManager finishes a DB operation.
     */
    @Override
    public void update() {
        if (latch != null) {
            Log.d(TAG, "Update received from ProfileManager");
            latch.countDown();
        }
    }

    /**
     * Test A: Verify Singleton Pattern.
     * <p>
     * <b>Scenario:</b> Call {@code getInstance()} multiple times.<br>
     * <b>Expected:</b> The same instance is returned every time.
     * </p>
     */
    @Test
    public void testA_SingletonInstance() {
        ProfileManager instance1 = ProfileManager.getInstance();
        ProfileManager instance2 = ProfileManager.getInstance();

        assertNotNull("ProfileManager instance should not be null", instance1);
        assertEquals("ProfileManager should return the same singleton instance", instance1, instance2);
    }

    /**
     * Test B: Create a Profile.
     * <p>
     * <b>Scenario:</b> Create a new {@link Profile} and save it to Firestore.<br>
     * <b>Expected:</b> The profile is saved, the View is notified, and the profile can be retrieved locally.
     * </p>
     */
    @Test
    public void testB_CreateProfile() throws InterruptedException {
        // 1. Prepare Data
        Profile newProfile = new Profile("Test User", "test@user.com", "1234567890", testDeviceId);

        latch = new CountDownLatch(1);

        // 2. Perform Create
        profileManager.createProfile(newProfile);

        // 3. Wait for DB Callback
        boolean success = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Timed out waiting for profile creation", success);

        // 4. Verify Creation
        Profile retrieved = profileManager.getProfileByDeviceId(testDeviceId);
        assertNotNull("Created profile should be found by Device ID", retrieved);
        assertEquals("Name should match", "Test User", retrieved.getName());

        // Save ID for next tests
        testProfileId = retrieved.getId();
        assertNotNull("Firestore should have assigned an ID", testProfileId);
    }

    /**
     * Test C: Check Existence.
     * <p>
     * <b>Scenario:</b> Check if the profile exists using {@code doesProfileExist}.<br>
     * <b>Expected:</b> Returns true for our test ID, false for a fake ID.
     * </p>
     */
    @Test
    public void testC_CheckExistence() {
        assertTrue("Profile should exist for testDeviceId", profileManager.doesProfileExist(testDeviceId));
        assertFalse("Profile should NOT exist for fake ID", profileManager.doesProfileExist("fake_id_999"));
    }

    /**
     * Test D: Update Profile.
     * <p>
     * <b>Scenario:</b> Change the user's name and update Firestore.<br>
     * <b>Expected:</b> The profile in the manager reflects the new name after the update.
     * </p>
     */
    @Test
    public void testD_UpdateProfile() throws InterruptedException {
        // 1. Get existing profile
        Profile profileToUpdate = profileManager.getProfileByDBID(testProfileId);
        assertNotNull("Profile must exist to update", profileToUpdate);

        // 2. Modify
        String newName = "Updated Test User";
        profileToUpdate.setName(newName);

        latch = new CountDownLatch(1);

        // 3. Perform Update
        profileManager.updateProfile(profileToUpdate);

        boolean success = latch.await(10, TimeUnit.SECONDS);
        assertTrue("Timed out waiting for profile update", success);

        // 4. Verify
        Profile updatedProfile = profileManager.getProfileByDBID(testProfileId);
        assertEquals("Name should be updated", newName, updatedProfile.getName());
    }

    /**
     * Test E: Delete Profile (Cleanup).
     * <p>
     * <b>Scenario:</b> Delete the test profile.<br>
     * <b>Expected:</b> The profile is removed from the manager and Firestore.
     * </p>
     */
    @Test
    public void testE_DeleteProfile() throws InterruptedException {
        // 1. Get profile
        Profile profileToDelete = profileManager.getProfileByDBID(testProfileId);

        // Safe check in case previous tests failed
        if (profileToDelete != null) {
            latch = new CountDownLatch(1);

            // 2. Perform Delete
            profileManager.deleteProfile(profileToDelete);

            boolean success = latch.await(10, TimeUnit.SECONDS);
            assertTrue("Timed out waiting for profile deletion", success);

            // 3. Verify
            Profile deletedProfile = profileManager.getProfileByDBID(testProfileId);
            assertNull("Profile should be null after deletion", deletedProfile);
        }
    }
}