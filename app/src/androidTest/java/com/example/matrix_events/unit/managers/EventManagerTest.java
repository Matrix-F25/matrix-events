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
 * <li>Business Logic (Removing a user from all events).</li>
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
    private static Event testEvent;
    private static String testEventId;
    private static final String TEST_USER_ID = "test_user_device_999";

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
    private Event createMockEvent(String name, boolean isRegClosed) {
        Profile organizer = new Profile("Test Org", "org@test.com", "555-1234", "org_device_id");
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
        // Note: Real state depends on Cloud Functions, but we simulate flags here
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
        testEvent = createMockEvent("Integration Test Event", false);
        latch = new CountDownLatch(1);

        eventManager.createEvent(testEvent);

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
        pending.add(TEST_USER_ID);
        event.setPendingList(pending);

        latch = new CountDownLatch(1);
        eventManager.updateEvent(event);
        assertTrue("Timed out waiting for update", latch.await(10, TimeUnit.SECONDS));

        // 3. Verify Filter: getEventsInPending
        List<Event> pendingEvents = eventManager.getEventsInPending(TEST_USER_ID);
        boolean found = false;
        for (Event e : pendingEvents) {
            if (e.getId().equals(testEventId)) found = true;
        }
        assertTrue("Event should appear in user's Pending filter", found);

        // 4. Verify NOT in other lists
        assertTrue("Should not be in Accepted", eventManager.getEventsInAccepted(TEST_USER_ID).isEmpty());
    }

    // ==========================================
    // 2. Complex Business Logic Tests
    // ==========================================

    /**
     * User Story: User deletes their profile.
     * Expected: User is removed from ALL event lists (Waitlist, Pending, etc).
     */
    @Test
    public void testD_RemoveFromAllEvents() throws InterruptedException {
        // 1. Verify user is currently in the Pending list from previous test
        Event before = eventManager.getEventByDBID(testEventId);
        assertTrue("User should be in pending list before removal", before.inPendingList(TEST_USER_ID));

        // 2. Perform Removal Action
        latch = new CountDownLatch(1); // Expect update from the event modification
        eventManager.removeFromAllEvents(TEST_USER_ID);

        assertTrue("Timed out waiting for batch removal update", latch.await(10, TimeUnit.SECONDS));

        // 3. Verify Removal
        Event after = eventManager.getEventByDBID(testEventId);
        assertFalse("User should be removed from pending list", after.inPendingList(TEST_USER_ID));
        assertFalse("User should be removed from waitlist", after.inWaitList(TEST_USER_ID));
        assertFalse("User should be removed from accepted list", after.inAcceptedList(TEST_USER_ID));
    }

    @Test
    public void testE_DeleteEvent() throws InterruptedException {
        Event event = eventManager.getEventByDBID(testEventId);
        if (event != null) {
            latch = new CountDownLatch(1);
            eventManager.deleteEvent(event);
            assertTrue("Timed out deleting event", latch.await(10, TimeUnit.SECONDS));

            assertNull("Event should be gone", eventManager.getEventByDBID(testEventId));
        }
    }

    // Cleanup Listener
    @After
    public void tearDown() {
        eventManager.removeView(this);
    }
}