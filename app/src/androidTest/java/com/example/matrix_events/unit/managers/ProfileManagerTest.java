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

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Robust Integration Tests for {@link ProfileManager}.
 * <p>
 * This suite verifies the interaction between the ProfileManager and Firestore.
 * It uses {@link FixMethodOrder} to execute tests sequentially, simulating a full
 * lifecycle of a user profile.
 * </p>
 * <p>
 * <b>Coverage Includes:</b>
 * <ul>
 * <li><b>CRUD Operations:</b> Creating, Reading, Updating, and Deleting profiles.</li>
 * <li><b>Existence Checks:</b> Verifying profiles exist by Device ID.</li>
 * <li><b>Lookup Logic:</b> Retrieving profiles by both Firestore ID and Device ID.</li>
 * <li><b>MVC Pattern:</b> Ensuring the Manager notifies Views upon data changes.</li>
 * </ul>
 * </p>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ProfileManagerTest implements View {

    private static final String TAG = "ProfileManagerTest";
    private ProfileManager profileManager;
    private CountDownLatch latch;

    // Static variables to share state between ordered tests (A -> B -> C -> D)
    private static String testProfileDbId;
    // Use a random UUID for Device ID to prevent collisions with real data or previous test runs
    private static final String TEST_DEVICE_ID = "test_device_" + UUID.randomUUID().toString();
    private static final String TEST_NAME = "Integration User";
    private static final String TEST_EMAIL = "integration@test.com";

    @Before
    public void setUp() {
        profileManager = ProfileManager.getInstance();
        profileManager.addView(this);
    }

    /**
     * MVC Callback: Triggered when ProfileManager finishes a DB operation (Create/Update/Delete).
     */
    @Override
    public void update() {
        if (latch != null) {
            Log.d(TAG, "Update received from ProfileManager");
            latch.countDown();
        }
    }

    // ==========================================
    // 1. Create Operation
    // ==========================================

    @Test
    public void testA_CreateProfile() throws InterruptedException {
        // Create a new profile object
        Profile profile = new Profile(TEST_NAME, TEST_EMAIL, "555-0199", TEST_DEVICE_ID);
        latch = new CountDownLatch(1);

        profileManager.createProfile(profile);

        // Wait for Firestore to update and call update()
        assertTrue("Timed out waiting for profile creation", latch.await(10, TimeUnit.SECONDS));

        // Verify the profile is now in the local cache
        Profile found = profileManager.getProfileByDeviceId(TEST_DEVICE_ID);
        assertNotNull("Profile should be found in manager after creation", found);
        assertEquals("Name should match input", TEST_NAME, found.getName());

        // Capture the generated DB ID for subsequent tests
        testProfileDbId = found.getId();
        assertNotNull("Profile Firestore ID should be generated", testProfileDbId);
    }

    // ==========================================
    // 2. Read & Existence Operations
    // ==========================================

    @Test
    public void testB_ReadAndExistence() {
        // 1. Test Existence Check
        assertTrue("Profile should exist for test device ID", profileManager.doesProfileExist(TEST_DEVICE_ID));
        assertFalse("Random device ID should not exist", profileManager.doesProfileExist("non_existent_" + UUID.randomUUID()));

        // 2. Test Get by Device ID
        Profile byDevice = profileManager.getProfileByDeviceId(TEST_DEVICE_ID);
        assertNotNull("Should retrieve profile by Device ID", byDevice);
        assertEquals("Email should match", TEST_EMAIL, byDevice.getEmail());

        // 3. Test Get by DB ID
        Profile byDbId = profileManager.getProfileByDBID(testProfileDbId);
        assertNotNull("Should retrieve profile by DB ID", byDbId);
        assertEquals("Device IDs should match between lookups", TEST_DEVICE_ID, byDbId.getDeviceId());
    }

    // ==========================================
    // 3. Update Operation
    // ==========================================

    @Test
    public void testC_UpdateProfile() throws InterruptedException {
        // Fetch current profile
        Profile profile = profileManager.getProfileByDBID(testProfileDbId);
        assertNotNull("Profile must exist for update test", profile);

        // Modify fields
        String newName = "Updated Name " + UUID.randomUUID().toString();
        profile.setName(newName);

        // Update: Changed from setNotificationsEnabled to setAdminPushNotifications based on Profile refactor
        profile.setAdminPushNotifications(false);

        latch = new CountDownLatch(1);
        profileManager.updateProfile(profile);

        // Wait for update confirmation
        assertTrue("Timed out waiting for profile update", latch.await(10, TimeUnit.SECONDS));

        // Verify update in local cache
        Profile updated = profileManager.getProfileByDBID(testProfileDbId);
        assertEquals("Name should be updated", newName, updated.getName());
        assertFalse("Admin Push Notifications should be disabled", updated.isAdminPushNotifications());
    }

    // ==========================================
    // 4. Delete Operation
    // ==========================================

    @Test
    public void testD_DeleteProfile() throws InterruptedException {
        Profile profile = profileManager.getProfileByDBID(testProfileDbId);

        // Ensure we have something to delete
        if (profile != null) {
            latch = new CountDownLatch(1);
            profileManager.deleteProfile(profile);

            // Wait for deletion confirmation
            assertTrue("Timed out waiting for profile deletion", latch.await(10, TimeUnit.SECONDS));

            // Verify deletion
            assertNull("Profile should be removed from cache by DB ID", profileManager.getProfileByDBID(testProfileDbId));
            assertFalse("Profile should not exist by device ID", profileManager.doesProfileExist(TEST_DEVICE_ID));
        }
    }

    @After
    public void tearDown() {
        profileManager.removeView(this);
    }
}