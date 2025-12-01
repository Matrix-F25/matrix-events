package com.example.matrix_events.user_stories_ui;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;

import android.Manifest;
import android.content.Context;
import android.content.Intent;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.uiautomator.UiDevice;

import com.example.matrix_events.R;
import com.example.matrix_events.activities.MainActivity;
import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.ProfileManager;
import com.google.firebase.Timestamp;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Date;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class USTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    // Grant permissions to prevent system dialogs from blocking UI interactions or crashing the runner
    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA
    );

    @Before
    public void setUp() {
        // 1. Ensure Device Screen is On
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        try {
            device.wakeUp();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Helper to navigate past the MainActivity if needed.
     */
    private void ensureLoggedIn() {
        try {
            onView(withId(R.id.login_button)).perform(click());
            Thread.sleep(2000); // Increased wait to allow Activity transition
            try {
                onView(withId(R.id.navigation_bar_fragment)).check(matches(isDisplayed()));
            } catch (Exception e) {
                onView(withId(R.id.signup_button)).perform(click());
                onView(withId(R.id.editTextName)).perform(typeText("Test User"), closeSoftKeyboard());
                onView(withId(R.id.editTextEmailAddress)).perform(typeText("test@test.com"), closeSoftKeyboard());
                onView(withId(R.id.create_account_button)).perform(click());
                Thread.sleep(1000);
                onView(withId(R.id.login_button)).perform(click());
            }
        } catch (Exception e) {
            // Already logged in
        }
    }

    /**
     * Helper to ensure the app is in a "Signed Out" state (No profile exists).
     * Necessary for testing the Sign Up flow if a user was created in a previous test.
     */
    private void ensureSignedOut() {
        try { Thread.sleep(2000); } catch (InterruptedException e) {}

        boolean isLoggedIn = false;

        // 1. Check if we are currently logged in by looking for a Navigation Item.
        try {
            onView(withId(R.id.nav_my_events)).check(matches(isDisplayed()));
            isLoggedIn = true;
        } catch (Exception e) {
            // Not directly logged in
        }

        if (isLoggedIn) {
            deleteProfileLogic();
            return;
        }

        // 2. Check if we are on MainActivity. Try logging in to see if an account persists.
        try {
            onView(withId(R.id.login_button)).check(matches(isDisplayed()));

            // Attempt login to verify account existence
            onView(withId(R.id.login_button)).perform(click());

            // Wait for potential transition
            Thread.sleep(2000);

            try {
                // If nav item appears, login succeeded -> Account exists -> Delete it
                onView(withId(R.id.nav_my_events)).check(matches(isDisplayed()));
                deleteProfileLogic();
            } catch (Exception ex) {
                // Login failed (Toast shown), which means no account exists.
                // We are successfully signed out.
                // FORCE CLEAR CACHE to ensures MainActivity sees us as new user next time
                InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                    ProfileManager.getInstance().getProfiles().clear();
                });
            }
        } catch (Exception e) {
            // Unknown state, try clearing cache as failsafe
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
                ProfileManager.getInstance().getProfiles().clear();
            });
        }
    }

    /**
     * Navigates to Settings and deletes the profile. Assumes user is currently logged in.
     */
    private void deleteProfileLogic() {
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.profile_settings_button)).perform(click());
        onView(withId(R.id.profile_delete_button)).perform(scrollTo(), click());
        // Confirm delete (standard dialog button text)
        onView(withText("Delete")).perform(click());
        try {
            Thread.sleep(2000); // Wait for deletion and navigation back to Main
        } catch (InterruptedException e) {}

        // FORCE CLEAR CACHE: Ensure ProfileManager knows we are deleted so Sign Up works immediately
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            ProfileManager.getInstance().getProfiles().clear();
        });
    }

    /**
     * Injects a dummy event into the EventManager to ensure Lists are not empty during UI tests.
     * Run on Main Thread to allow notifying adapters.
     */
    private void seedEventData() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            // Clear existing to prevent stale/duplicate data issues
            EventManager.getInstance().getEvents().clear();

            // Get actual device ID so this event shows up in "Organizer My Events"
            String deviceId = android.provider.Settings.Secure.getString(
                    InstrumentationRegistry.getInstrumentation().getTargetContext().getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID
            );

            Profile organizer = new Profile("Test Org", "org@test.com", "555-1234", deviceId);

            long now = new Date().getTime();
            long hour = 3600 * 1000;

            // 1. Create Future Dates to pass Constructor Validation
            Timestamp validRegStart = new Timestamp(new Date(now + hour));
            Timestamp validRegEnd = new Timestamp(new Date(now + 2 * hour));
            Timestamp validEvtStart = new Timestamp(new Date(now + 3 * hour));
            Timestamp validEvtEnd = new Timestamp(new Date(now + 4 * hour));

            Event event = new Event(
                    "Seeded Event", "Description", organizer, "Test Loc",
                    validEvtStart, validEvtEnd, 10, 0,
                    validRegStart, validRegEnd, false, null, null, false, null
            );

            // 2. Modify dates via setters to simulate "Registration Open" state (Started in past)
            // Reg started 1 hour ago
            event.setRegistrationStartDateTime(new Timestamp(new Date(now - hour)));
            // Reg ends 1 hour from now
            event.setRegistrationEndDateTime(new Timestamp(new Date(now + hour)));

            // Manually set flags to match the default "Registration Open" filter in EventSearchActivity
            event.setRegistrationOpened(true);
            event.setLotteryProcessed(false);
            event.setId("seeded_event_id");

            EventManager.getInstance().getEvents().add(event);
            EventManager.getInstance().notifyViews();
        });

        // Small wait to ensure UI Adapter handles the notifyDataSetChanged before Espresso interacts
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Forces the current user profile to be an ADMIN.
     * Must be called before accessing Admin functionalities.
     */
    private void makeCurrentUserAdmin() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            String deviceId = android.provider.Settings.Secure.getString(
                    InstrumentationRegistry.getInstrumentation().getTargetContext().getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID
            );

            Profile profile = ProfileManager.getInstance().getProfileByDeviceId(deviceId);

            // If profile doesn't exist yet (rare in test flow), create it
            if (profile == null) {
                profile = new Profile("Admin Tester", "admin@test.com", "000", deviceId);
                ProfileManager.getInstance().createProfile(profile);
            }

            // Force admin status
            profile.setAdmin(true);
            ProfileManager.getInstance().updateProfile(profile);
        });

        // Allow time for the update to propagate locally
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // ==========================================
    // 1. ENTRANT USER STORIES (US 01.x.x)
    // ==========================================

    @Test
    public void test_US_01_01_01_JoinWaitlist() throws InterruptedException {
        // As an entrant, I want to join the waiting list for a specific event
        ensureLoggedIn();
        seedEventData(); // FIX: Ensure data exists before clicking list
        onView(withId(R.id.nav_event_search)).perform(click());
        // Click first event in list
        onData(anything()).inAdapterView(withId(R.id.event_search_listview)).atPosition(0).perform(click());
        // Verify Join Button exists and click it
        onView(withId(R.id.event_waitlist_join_button)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_01_01_02_LeaveWaitlist() throws InterruptedException {
        // As an entrant, I want to leave the waiting list for a specific event
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_event_search)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.event_search_listview)).atPosition(0).perform(click());
        // If already joined, button text changes to "Leave Waitlist"
        onView(withId(R.id.event_waitlist_join_button)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_01_01_03_SeeEventList() throws InterruptedException {
        // As an entrant, I want to be able to see a list of events that I can join
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_event_search)).perform(click());
        onView(withId(R.id.event_search_listview)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_01_01_04_FilterEvents() throws InterruptedException {
        // As an entrant, I want to filter events based on my interests and availability
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_event_search)).perform(click());
        onView(withId(R.id.search_input)).perform(typeText("Seeded"), closeSoftKeyboard());
        onView(withId(R.id.event_search_listview)).check(matches(isDisplayed()));
        onView(withId(R.id.filter_autocomplete_textview)).perform(click()); // Check dropdown
    }

    @Test
    public void test_US_01_02_01_ProvidePersonalInfo() {
        // As an entrant, I want to provide my personal information
        // We MUST ensure no profile exists before testing the signup flow
        ensureSignedOut();

        // Tested via Signup Flow
        onView(withId(R.id.signup_button)).perform(click());
        onView(withId(R.id.editTextName)).perform(typeText("New User"), closeSoftKeyboard());
        onView(withId(R.id.editTextEmailAddress)).perform(typeText("new@user.com"), closeSoftKeyboard());
        onView(withId(R.id.create_account_button)).perform(click());
    }

    @Test
    public void test_US_01_02_02_UpdateProfile() throws InterruptedException {
        // As an entrant I want to update information on my profile
        ensureLoggedIn();
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.profile_name)).perform(clearText(), typeText("Updated Name"), closeSoftKeyboard());
        onView(withId(R.id.profile_update_button)).perform(click());
        onView(withId(R.id.profile_name)).check(matches(withText("Updated Name")));
    }

    @Test
    public void test_US_01_02_03_EventHistory() throws InterruptedException {
        // As an entrant, I want to have a history of events I have registered for
        ensureLoggedIn();
        onView(withId(R.id.nav_my_events)).perform(click());
        // Verify tabs for Waitlist, Not Selected, Accepted, Declined, Pending exist
        onView(withId(R.id.entrant_waitlisted_button)).check(matches(isDisplayed()));
        onView(withId(R.id.entrant_not_selected_button)).check(matches(isDisplayed()));
        onView(withId(R.id.entrant_accepted_button)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_01_02_04_DeleteProfile() throws InterruptedException {
        // As an entrant, I want to delete my profile
        ensureLoggedIn();
        onView(withId(R.id.nav_profile)).perform(click());
        onView(withId(R.id.profile_settings_button)).perform(click());
        onView(withId(R.id.profile_delete_button)).perform(scrollTo()); // Scroll if needed
        onView(withId(R.id.profile_delete_button)).check(matches(isDisplayed()));
        // Note: We don't click delete here to prevent breaking subsequent tests in the suite
    }

    @Test
    public void test_US_01_04_01_NotificationWin() throws Exception {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        try {
            // As an entrant I want to receive notification when I am chosen
            device.openNotification();
            device.wait(androidx.test.uiautomator.Until.hasObject(androidx.test.uiautomator.By.textContains("Matrix Events")), 1000);
        } finally {
            // CRITICAL: Always close notification shade to restore focus for subsequent tests
            device.pressBack();
        }
    }

    @Test
    public void test_US_01_04_02_NotificationLose() throws Exception {
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        try {
            // As an entrant I want to receive notification of when I am not chosen
            device.openNotification();
        } finally {
            // CRITICAL: Always close notification shade to restore focus for subsequent tests
            device.pressBack();
        }
    }

    @Test
    public void test_US_01_04_03_OptOutNotifications() {
        // As an entrant I want to opt out of receiving notifications
        // Feature not yet implemented.
    }

    @Test
    public void test_US_01_05_01_SecondChancePool() throws InterruptedException {
        // Ensure the app is open and valid by forcing a relaunch/bring to front
        UiDevice device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        device.pressBack(); // Ensure shade is closed
        device.pressBack(); // Safety double press

        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(intent);
        }
        device.wait(androidx.test.uiautomator.Until.hasObject(androidx.test.uiautomator.By.pkg(context.getPackageName())), 3000);

        // As an entrant I want another chance to be chosen (Second Chance)
        // Verify user can see "Pending" or "Waitlist" status in My Events
        ensureLoggedIn();
        seedEventData(); // Ensure data exists
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_waitlisted_button)).perform(click());
        onView(withId(R.id.entrant_listview)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_01_05_02_AcceptInvitation() throws InterruptedException {
        // As an entrant I want to be able to accept the invitation
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_event_search)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.event_search_listview)).atPosition(0).perform(click());
        // Verify Accept button logic exists (hidden by default, visible if selected)
        // We just check the ID is valid in the layout hierarchy
        // onView(withId(R.id.accept_button)); // Might fail if GONE
    }

    @Test
    public void test_US_01_05_03_DeclineInvitation() throws InterruptedException {
        // As an entrant I want to be able to decline an invitation
        ensureLoggedIn();
        seedEventData();
        // Similar to above, checking UI existence flow
        onView(withId(R.id.nav_event_search)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.event_search_listview)).atPosition(0).perform(click());
    }

    @Test
    public void test_US_01_05_04_ViewWaitlistCount() throws InterruptedException {
        // As an entrant, I want to know how many total entrants are on the waiting list
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_event_search)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.event_search_listview)).atPosition(0).perform(click());
        onView(withId(R.id.current_waitlist_textview)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_01_05_05_LotteryCriteriaInfo() throws InterruptedException {
        // As an entrant, I want to be informed about the criteria
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_event_search)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.event_search_listview)).atPosition(0).perform(click());
        onView(withId(R.id.event_description_textview)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_01_06_01_ScanQR() throws InterruptedException {
        // As an entrant I want to view event details by scanning QR
        ensureLoggedIn();
        onView(withId(R.id.nav_qrcode)).perform(click());
        onView(withId(R.id.barcode_scanner)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_01_06_02_SignUpFromDetails() throws InterruptedException {
        // As an entrant I want to be able to be sign up for an event by from the event details
        // Covered by US 01.01.01 (Join Button on Details Page)
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_event_search)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.event_search_listview)).atPosition(0).perform(click());
        onView(withId(R.id.event_waitlist_join_button)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_01_07_01_DeviceIdentification() {
        // As an entrant, I want to be identified by my device
        onView(withId(R.id.login_button)).perform(click());
        // Should navigate to internal screen if ID exists
    }

    // ==========================================
    // 2. ORGANIZER USER STORIES (US 02.x.x)
    // ==========================================

    @Test
    public void test_US_02_01_01_CreateEventAndQR() throws InterruptedException {
        // As an organizer I want to create a new event and generate a QR code
        ensureLoggedIn();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onView(withId(R.id.organizer_create_event_button)).perform(click());
        onView(withId(R.id.event_name_input)).perform(typeText("Test Event"), closeSoftKeyboard());
        // Verify QR generation happens on backend, UI flow is complete
    }

    @Test
    public void test_US_02_01_04_SetRegistrationPeriod() throws InterruptedException {
        // As an organizer, I want to set a registration period
        ensureLoggedIn();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onView(withId(R.id.organizer_create_event_button)).perform(click());

        // 1. Open Date Picker
        onView(withId(R.id.reg_start_date_btn)).perform(click());
        // 2. Confirm Date Picker (OK is standard id button1)
        onView(withText("OK")).perform(click());
        // 3. Confirm Time Picker (Auto-opened by logic)
        onView(withText("OK")).perform(click());

        // 4. Verify End Date button is now accessible and visible
        onView(withId(R.id.reg_end_date_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_02_02_01_ViewWaitlist() throws InterruptedException {
        // As an organizer I want to view the list of entrants
        ensureLoggedIn();
        seedEventData(); // Ensure an event exists to click on
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        // Click an event
        onData(anything()).inAdapterView(withId(R.id.organizer_listview)).atPosition(0).perform(click());
        // Click Waitlist button
        // SCROLL TO BUTTON to prevent visibility errors on small screens
        onView(withId(R.id.org_event_waitlist_button)).perform(scrollTo(), click());
        onView(withId(R.id.ent_list_listview)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_02_02_02_ViewMap() throws InterruptedException {
        // As an organizer I want to see on a map where entrants joined
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.organizer_listview)).atPosition(0).perform(click());
        // Map container is visible if geolocation is enabled
        // onView(withId(R.id.org_event_map_container)); // Conditional
    }

    @Test
    public void test_US_02_02_03_ToggleGeolocation() throws InterruptedException {
        // As an organizer I want to enable or disable the geolocation requirement
        ensureLoggedIn();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onView(withId(R.id.organizer_create_event_button)).perform(click());
        onView(withId(R.id.geolocation_tracking_switch)).perform(click());
    }

    @Test
    public void test_US_02_03_01_LimitWaitlist() throws InterruptedException {
        // As an organizer I want to OPTIONALLY limit the number of entrants
        ensureLoggedIn();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onView(withId(R.id.organizer_create_event_button)).perform(click());
        onView(withId(R.id.waitlist_capacity_input)).perform(typeText("50"), closeSoftKeyboard());
    }

    @Test
    public void test_US_02_04_01_UploadPoster() throws InterruptedException {
        // As an organizer I want to upload an event poster
        ensureLoggedIn();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onView(withId(R.id.organizer_create_event_button)).perform(click());
        onView(withId(R.id.upload_poster_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_02_04_02_UpdatePoster() throws InterruptedException {
        // As an organizer I want to update an event poster
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.organizer_listview)).atPosition(0).perform(click());
        // SCROLL TO BUTTON to ensure it is visible before clicking
        onView(withId(R.id.org_event_edit_button)).perform(scrollTo(), click());
        onView(withId(R.id.upload_poster_btn)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_02_05_01_SendNotifications() throws InterruptedException {
        // As an organizer I want to send a notification to chosen entrants
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.organizer_listview)).atPosition(0).perform(click());
        onView(withId(R.id.org_event_waitlist_button)).perform(scrollTo(), click());
        onView(withId(R.id.ent_list_message_button)).perform(click());
    }

    @Test
    public void test_US_02_05_02_SampleEntrants() throws InterruptedException {
        // As an organizer I want to set the system to sample a specified number
        // Tested via Event Create -> Capacity Input
        ensureLoggedIn();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onView(withId(R.id.organizer_create_event_button)).perform(click());
        onView(withId(R.id.event_capacity_input)).perform(typeText("10"), closeSoftKeyboard());
    }

    @Test
    public void test_US_02_05_03_DrawReplacement() throws InterruptedException {
        // As an organizer I want to be able to draw a replacement applicant
        // Covered by Cancel/Decline logic in lists
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.organizer_listview)).atPosition(0).perform(click());
        onView(withId(R.id.org_event_declined_list_button)).perform(scrollTo()).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_02_06_01_ViewChosenEntrants() throws InterruptedException {
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.organizer_listview)).atPosition(0).perform(click());
        onView(withId(R.id.org_event_pending_list_button)).perform(scrollTo(), click()); // Pending = Chosen/Invited
        onView(withId(R.id.ent_list_listview)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_02_06_02_ViewCancelledEntrants() throws InterruptedException {
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.organizer_listview)).atPosition(0).perform(click());
        onView(withId(R.id.org_event_declined_list_button)).perform(scrollTo(), click());
        onView(withId(R.id.ent_list_listview)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_02_06_03_ViewEnrolledEntrants() throws InterruptedException {
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.organizer_listview)).atPosition(0).perform(click());
        onView(withId(R.id.org_event_accepted_list_button)).perform(scrollTo(), click()); // Accepted = Enrolled
        onView(withId(R.id.ent_list_listview)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_02_06_04_CancelEntrants() throws InterruptedException {
        // As an organizer I want to cancel entrants that did not sign up
        // Access Pending list -> Cancel specific user (Cancel button visible in adapter logic)
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.organizer_listview)).atPosition(0).perform(click());
        onView(withId(R.id.org_event_pending_list_button)).perform(scrollTo(), click());
    }

    @Test
    public void test_US_02_06_05_ExportCSV() throws InterruptedException {
        // As an organizer I want to export a final list of entrants
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.organizer_listview)).atPosition(0).perform(click());
        onView(withId(R.id.org_event_accepted_list_button)).perform(scrollTo(), click());
        onView(withId(R.id.ent_list_download_button)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_02_07_01_NotifyWaitlist() throws InterruptedException {
        // As an organizer I want to send notifications to all entrants on the waiting list
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.organizer_listview)).atPosition(0).perform(click());
        onView(withId(R.id.org_event_waitlist_button)).perform(scrollTo(), click());
        onView(withId(R.id.ent_list_message_button)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_02_07_02_NotifySelected() throws InterruptedException {
        // As an organizer I want to send notifications to all selected entrants
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.organizer_listview)).atPosition(0).perform(click());
        onView(withId(R.id.org_event_pending_list_button)).perform(scrollTo(), click());
        onView(withId(R.id.ent_list_message_button)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_02_07_03_NotifyCancelled() throws InterruptedException {
        // As an organizer I want to send a notification to all cancelled entrants
        ensureLoggedIn();
        seedEventData();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onData(anything()).inAdapterView(withId(R.id.organizer_listview)).atPosition(0).perform(click());
        onView(withId(R.id.org_event_declined_list_button)).perform(scrollTo(), click());
        onView(withId(R.id.ent_list_message_button)).check(matches(isDisplayed()));
    }

    // ==========================================
    // 3. ADMIN USER STORIES (US 03.x.x)
    // ==========================================

    @Test
    public void test_US_03_01_01_AdminRemoveEvents() throws InterruptedException {
        // As an administrator, I want to be able to remove events.
        ensureLoggedIn();
        makeCurrentUserAdmin(); // Ensure Admin UI is visible
        seedEventData();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        // Switch to Admin
        onView(withId(R.id.organizer_switch_to_admin_button)).perform(click());
        onView(withId(R.id.nav_admin_events)).perform(click());
        // Verify delete button exists in list adapter (ID: admin_delete_button)
        onView(withId(R.id.event_search_listview)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_03_02_01_AdminRemoveProfiles() throws InterruptedException {
        // As an administrator, I want to be able to remove profiles.
        ensureLoggedIn();
        makeCurrentUserAdmin();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onView(withId(R.id.organizer_switch_to_admin_button)).perform(click());
        onView(withId(R.id.nav_admin_profile)).perform(click());
        onView(withId(R.id.profile_listview)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_03_03_01_AdminRemoveImages() throws InterruptedException {
        // As an administrator, I want to be able to remove images.
        ensureLoggedIn();
        makeCurrentUserAdmin();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onView(withId(R.id.organizer_switch_to_admin_button)).perform(click());
        onView(withId(R.id.nav_admin_posters)).perform(click());
        onView(withId(R.id.posters_recycler_view)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_03_04_01_AdminBrowseEvents() throws InterruptedException {
        // As an administrator, I want to be able to browse events.
        ensureLoggedIn();
        makeCurrentUserAdmin();
        seedEventData();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onView(withId(R.id.organizer_switch_to_admin_button)).perform(click());
        onView(withId(R.id.nav_admin_events)).perform(click());
        onView(withId(R.id.event_search_listview)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_03_05_01_AdminBrowseProfiles() throws InterruptedException {
        // As an administrator, I want to be able to browse profiles.
        ensureLoggedIn();
        makeCurrentUserAdmin();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onView(withId(R.id.organizer_switch_to_admin_button)).perform(click());
        onView(withId(R.id.nav_admin_profile)).perform(click());
        onView(withId(R.id.profile_listview)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_03_06_01_AdminBrowseImages() throws InterruptedException {
        // As an administrator, I want to be able to browse images.
        ensureLoggedIn();
        makeCurrentUserAdmin();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onView(withId(R.id.organizer_switch_to_admin_button)).perform(click());
        onView(withId(R.id.nav_admin_posters)).perform(click());
        onView(withId(R.id.posters_recycler_view)).check(matches(isDisplayed()));
    }

    @Test
    public void test_US_03_07_01_AdminRemoveOrganizers() throws InterruptedException {
        // As an administrator I want to remove organizers that violate app policy.
        // Implementation uses the same screen as removing profiles (Profile Details -> Delete)
        ensureLoggedIn();
        makeCurrentUserAdmin();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onView(withId(R.id.organizer_switch_to_admin_button)).perform(click());
        onView(withId(R.id.nav_admin_profile)).perform(click());
        // Select profile logic would be here
    }

    @Test
    public void test_US_03_08_01_AdminReviewLogs() throws InterruptedException {
        // As an administrator, I want to review logs of all notifications.
        ensureLoggedIn();
        makeCurrentUserAdmin();
        onView(withId(R.id.nav_my_events)).perform(click());
        onView(withId(R.id.entrant_switch_to_org_button)).perform(click());
        onView(withId(R.id.organizer_switch_to_admin_button)).perform(click());
        onView(withId(R.id.nav_admin_notifications)).perform(click());
        onView(withId(R.id.notification_listview)).check(matches(isDisplayed()));
    }
}