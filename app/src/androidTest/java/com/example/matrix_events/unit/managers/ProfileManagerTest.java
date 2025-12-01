package com.example.matrix_events.unit.managers;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.ProfileManager;
import com.example.matrix_events.mvc.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class ProfileManagerTest {

    private ProfileManager profileManager;
    private CountDownLatch latch;
    private View testView;
    private Profile testProfile;

    @Before
    public void setUp() {
        profileManager = ProfileManager.getInstance();
        latch = new CountDownLatch(1);
        testView = new View() {
            @Override
            public void update() {
                latch.countDown();
            }
        };
        profileManager.addView(testView);
    }

    @Test
    public void testCreateAndReadProfile() throws InterruptedException {
        // Create a new profile
        String testDeviceId = "test-device-" + System.currentTimeMillis();
        testProfile = new Profile("Test User", "test@user.com", "1234567890", testDeviceId);
        profileManager.createProfile(testProfile);

        // Wait for Firebase to update
        latch.await(10, TimeUnit.SECONDS);

        // Find the profile in the manager
        Profile foundProfile = profileManager.getProfileByDeviceId(testDeviceId);
        assertNotNull("Profile should be found in ProfileManager", foundProfile);
        testProfile.setId(foundProfile.getId()); // Set ID for cleanup

        assertEquals("Profile name should match", "Test User", foundProfile.getName());
        assertEquals("Profile email should match", "test@user.com", foundProfile.getEmail());
    }

    @After
    public void tearDown() {
        // Clean up the created profile
        if (testProfile != null && testProfile.getId() != null) {
            latch = new CountDownLatch(1);
            profileManager.deleteProfile(testProfile);
            try {
                // Wait for deletion to complete
                latch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        profileManager.removeView(testView);
    }
}
