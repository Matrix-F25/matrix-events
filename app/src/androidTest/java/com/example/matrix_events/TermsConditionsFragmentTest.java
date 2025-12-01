package com.example.matrix_events;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.provider.Settings;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.matrix_events.activities.ProfileActivity;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.fragments.TermsConditionsFragment;
import com.example.matrix_events.managers.ProfileManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * UI/Integration Tests for {@link TermsConditionsFragment}.
 * <p>
 * This class tests the display and navigation logic of the Terms and Conditions screen.
 * Since this fragment is deeply nested, the setup method handles the navigation
 * sequence required to reach it.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class TermsConditionsFragmentTest {

    @Rule
    public ActivityScenarioRule<ProfileActivity> activityRule =
            new ActivityScenarioRule<>(ProfileActivity.class);

    private ProfileManager profileManager;
    private Profile testProfile;
    private String deviceId;

    /**
     * Sets up the test environment.
     * <p>
     * 1. Creates a dummy profile.<br>
     * 2. Navigates: Profile -> Settings -> Terms & Conditions.
     * </p>
     */
    @Before
    public void setUp() throws InterruptedException {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        profileManager = ProfileManager.getInstance();

        // 1. Create Dummy Profile
        testProfile = new Profile("Terms Tester", "terms@test.com", "555-1234", deviceId);
        profileManager.createProfile(testProfile);
        Thread.sleep(2000); // Wait for Firestore

        // 2. Navigate: Profile -> Settings
        onView(withId(R.id.profile_settings_button)).perform(click());
        Thread.sleep(1000);

        // 3. Navigate: Settings -> Terms & Conditions
        onView(withId(R.id.terms_conditions_clickable)).perform(click());
        Thread.sleep(1000);
    }

    /**
     * Verifies that the Terms and Conditions fragment elements are displayed.
     * <p>
     * <b>Scenario:</b> User navigates to Terms page.<br>
     * <b>Expected Result:</b> The custom back button unique to this fragment is visible.
     * </p>
     */
    @Test
    public void testFragmentElementsDisplayed() {
        // Verify the Back Button specific to this fragment is visible
        onView(withId(R.id.terms_back_button_top)).check(matches(isDisplayed()));
    }

    /**
     * Verifies the Back Button functionality.
     * <p>
     * <b>Scenario:</b> User clicks the 'Back' button on the Terms page.<br>
     * <b>Expected Result:</b> The app pops the back stack and returns to the Settings Fragment.
     * </p>
     */
    @Test
    public void testBackButtonFunctionality() throws InterruptedException {
        // Click Back
        onView(withId(R.id.terms_back_button_top)).perform(click());

        Thread.sleep(1000); // Wait for transaction

        // Verify we returned to Settings Fragment
        // We check for a view unique to Settings, like the admin push switch
        onView(withId(R.id.push_admin_switch)).check(matches(isDisplayed()));
    }

    /**
     * Cleanup: Delete the dummy profile.
     */
    @After
    public void tearDown() {
        if (testProfile != null) {
            Profile p = profileManager.getProfileByDeviceId(deviceId);
            if (p != null) {
                profileManager.deleteProfile(p);
            }
        }
    }
}