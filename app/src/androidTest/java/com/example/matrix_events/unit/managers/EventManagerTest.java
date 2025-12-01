package com.example.matrix_events.unit.managers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.mvc.View;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Robust Integration Tests for {@link EventManager}.
 * <p>
 * This suite verifies the interaction between the EventManager and Firestore.
 * It covers:
 * <ul>
 * <li>Basic CRUD operations (Create, Read, Update, Delete).</li>
 * <li>Complex Filtering (Registration Status, User Lists).</li>
 * <li>Organizer-specific filtering.</li>
 * <li>Business Logic (Removing a user from all events).</li>
 * <li>Organizer Deletion Logic (Cancelling events).</li>
 * </ul>
 * </p>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class EventManagerTest implements View {

    private static final String TAG = "EventManagerTest";
    private EventManager eventManager;
    private CountDownLatch latch;

    // Static variables to share state between ordered tests
    private static String testEventId;
    private static String organizerEventId;
    private static final String TEST_PARTICIPANT_ID = "test_user_device_999";
    private static final String TEST_ORGANIZER_ID = "test_org_device_888";

    @Before
    public void setUp() {
        eventManager = EventManager.getInstance();
        eventManager.addView(this);
    }

    /**
     * MVC Callback: Triggered when EventManager finishes a DB operation.
     */
    @Override
    public void update() {
        if (latch != null) {
            Log.d(TAG, "Update received from EventManager");
            latch.countDown();
        }
    }

    /**
     * Helper to create a generic event.
     */
    private Event createMockEvent(String name, String organizerId, boolean isRegClosed) {
        Profile organizer = new Profile("Test Org", "org@test.com", "555-1234", organizerId);
        organizer.setId("org_profile_id_" + organizerId);

        long now = new Date().getTime();
        long hour = 3600 * 1000;

        // Timestamps setup
        Timestamp regStart = new Timestamp(new Date(now + hour));
        Timestamp regEnd = new Timestamp(new Date(now + 2 * hour));
        Timestamp evtStart = new Timestamp(new Date(now + 3 * hour));
        Timestamp evtEnd = new Timestamp(new Date(now + 4 * hour));

        Event event = new Event(
                name, "Description", organizer, "Test Lab",
                evtStart, evtEnd, 10, 5,
                regStart, regEnd, false, null, null, false, null
        );

        // Manually force state for testing filters
        if (isRegClosed) {
            event.setRegistrationOpened(true);
            event.setLotteryProcessed(true); // Closed
        } else {
            event.setRegistrationOpened(true);
            event.setLotteryProcessed(false); // Open
        }

        return event;
    }

    // ==========================================
    // 1. Basic CRUD Tests
    // ==========================================

    @Test
    public void testA_CreateEvent() throws InterruptedException {
        // Create a standard event for Participant testing
        Event event = createMockEvent("Integration Test Event", "some_random_org", false);
        latch = new CountDownLatch(1);

        eventManager.createEvent(event);

        // Wait for Firestore to update
        assertTrue("Timed out waiting for create", latch.await(10, TimeUnit.SECONDS));

        // Find the event to get its generated ID
        boolean found = false;
        for (Event e : eventManager.getEvents()) {
            if (e.getName().equals("Integration Test Event")) {
                testEventId = e.getId();
                found = true;
                break;
            }
        }
        assertTrue("Event should be found in manager", found);
        assertNotNull("Event ID should be generated", testEventId);
    }

    @Test
    public void testB_ReadAndFilterByRegStatus() {
        // We created an "Open" event in Test A.
        List<Event> openEvents = eventManager.getEventsRegistrationNotClosed();
        List<Event> closedEvents = eventManager.getEventsRegistrationClosed();

        boolean foundInOpen = false;
        for (Event e : openEvents) {
            if (e.getId().equals(testEventId)) foundInOpen = true;
        }

        assertTrue("Test event should be in 'Not Closed' list", foundInOpen);

        // Ensure it's NOT in the closed list
        for (Event e : closedEvents) {
            assertFalse("Test event should NOT be in 'Closed' list", e.getId().equals(testEventId));
        }
    }

    @Test
    public void testC_UpdateAndUserLists() throws InterruptedException {
        // 1. Fetch Event
        Event event = eventManager.getEventByDBID(testEventId);
        assertNotNull(event);

        // 2. Add user to Pending List manually (Simulating Lottery Win)
        List<String> pending = new ArrayList<>();
        pending.add(TEST_PARTICIPANT_ID);
        event.setPendingList(pending);

        latch = new CountDownLatch(1);
        eventManager.updateEvent(event);
        assertTrue("Timed out waiting for update", latch.await(10, TimeUnit.SECONDS));

        // 3. Verify Filter: getEventsInPending
        List<Event> pendingEvents = eventManager.getEventsInPending(TEST_PARTICIPANT_ID);
        boolean found = false;
        for (Event e : pendingEvents) {
            if (e.getId().equals(testEventId)) found = true;
        }
        assertTrue("Event should appear in user's Pending filter", found);

        // 4. Verify NOT in other lists
        assertTrue("Should not be in Accepted", eventManager.getEventsInAccepted(TEST_PARTICIPANT_ID).isEmpty());
    }

    // ==========================================
    // 2. Complex Business Logic Tests
    // ==========================================

    /**
     * User Story: User deletes their profile.
     * Expected: User is removed from ALL event lists (Waitlist, Pending, etc).
     */
    @Test
    public void testD_RemoveParticipantFromAllEvents() throws InterruptedException {
        // 1. Verify user is currently in the Pending list from previous test
        Event before = eventManager.getEventByDBID(testEventId);
        assertTrue("User should be in pending list before removal", before.inPendingList(TEST_PARTICIPANT_ID));

        // 2. Perform Removal Action
        latch = new CountDownLatch(1); // Expect update from the event modification
        eventManager.removeFromAllEvents(TEST_PARTICIPANT_ID);

        assertTrue("Timed out waiting for batch removal update", latch.await(10, TimeUnit.SECONDS));

        // 3. Verify Removal
        Event after = eventManager.getEventByDBID(testEventId);
        assertFalse("User should be removed from pending list", after.inPendingList(TEST_PARTICIPANT_ID));
    }

    // ==========================================
    // 3. Organizer Specific Tests
    // ==========================================

    @Test
    public void testE_CreateOrganizerEvent() throws InterruptedException {
        // Create an event specifically for the TEST_ORGANIZER_ID
        Event orgEvent = createMockEvent("Organizer Event", TEST_ORGANIZER_ID, false);
        latch = new CountDownLatch(1);

        eventManager.createEvent(orgEvent);
        assertTrue("Timed out waiting for create", latch.await(10, TimeUnit.SECONDS));

        // Find ID
        for (Event e : eventManager.getEvents()) {
            if (e.getName().equals("Organizer Event")) {
                organizerEventId = e.getId();
                break;
            }
        }
        assertNotNull(organizerEventId);
    }

    @Test
    public void testF_OrganizerFilters() {
        // Test getOrganizerEventsRegistrationNotClosed
        List<Event> orgEvents = eventManager.getOrganizerEventsRegistrationNotClosed(TEST_ORGANIZER_ID);
        boolean found = false;
        for (Event e : orgEvents) {
            if (e.getId().equals(organizerEventId)) found = true;
        }
        assertTrue("Should find event in organizer's open list", found);

        // Test negative case
        List<Event> otherOrgEvents = eventManager.getOrganizerEventsRegistrationNotClosed("wrong_id");
        assertTrue("Should be empty for wrong organizer", otherOrgEvents.isEmpty());
    }

    /**
     * User Story: Organizer deletes their profile.
     * Expected: All events organized by them are cancelled (deleted).
     */
    @Test
    public void testG_RemoveOrganizerFromAllEvents() throws InterruptedException {
        // 1. Verify event exists
        assertNotNull("Organizer event should exist", eventManager.getEventByDBID(organizerEventId));

        // 2. Perform Removal (This triggers cancelEventAndNotifyUsers -> deleteEvent)
        latch = new CountDownLatch(1);
        eventManager.removeFromAllEvents(TEST_ORGANIZER_ID);

        // Note: The latch counts down when the delete operation finishes and notifies the view
        assertTrue("Timed out waiting for organizer event deletion", latch.await(10, TimeUnit.SECONDS));

        // 3. Verify Event Deletion
        assertNull("Event should be deleted when organizer is removed", eventManager.getEventByDBID(organizerEventId));
    }

    @Test
    public void testH_DeleteRemainingEvent() throws InterruptedException {
        // Cleanup the first test event
        Event event = eventManager.getEventByDBID(testEventId);
        if (event != null) {
            latch = new CountDownLatch(1);
            eventManager.deleteEvent(event);
            assertTrue("Timed out deleting event", latch.await(10, TimeUnit.SECONDS));
        }
    }

    // Cleanup Listener
    @After
    public void tearDown() {
        eventManager.removeView(this);
    }
}