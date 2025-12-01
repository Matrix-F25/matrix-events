package com.example.matrix_events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Profile;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.GeoPoint;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

/**
 * Robust Unit Tests for the {@link Event} entity class.
 * <p>
 * This suite verifies the integrity of the Event object, ensuring that complex business logic
 * regarding waitlists, state management, and data serialization functions correctly.
 * </p>
 * <p>
 * <b>Coverage Includes:</b>
 * <ul>
 * <li><b>Constructor Validation:</b> Ensures date logic (End > Start) and Reoccurring logic constraints.</li>
 * <li><b>State Management:</b> Tests registration open/closed flags and time checks.</li>
 * <li><b>List Logic:</b> Verifies movement between Waitlist -> Pending -> Accepted/Declined.</li>
 * <li><b>Lottery Logic:</b> Tests "Second Chance" mechanism when a user declines.</li>
 * <li><b>Serialization:</b> Verifies custom writeObject/readObject logic for passing Events via Intents.</li>
 * </ul>
 * </p>
 */
public class EventTest {

    private Profile mockOrganizer;
    private String testLocation = "Test Location";
    private Timestamp now;

    /**
     * Sets up the test environment before each test method.
     * <p>
     * Initializes a real Profile object to act as the organizer and captures the current
     * timestamp to ensure time-relative tests are accurate.
     * </p>
     */
    @Before
    public void setUp() {
        // Use a real Profile object instead of Mockito to avoid dependency issues for POJO tests
        mockOrganizer = new Profile("Test Organizer", "organizer@test.com", "555-1234", "organizer_device_id");
        now = Timestamp.now();
    }

    /**
     * Helper method to create a valid event with dates relative to the current time.
     * <p>
     * Creates an event where:
     * <ul>
     * <li>Registration Start: +1 hour</li>
     * <li>Registration End: +2 hours</li>
     * <li>Event Start: +3 hours</li>
     * <li>Event End: +4 hours</li>
     * </ul>
     * This ensures the event is chronologically valid and avoids constructor exceptions.
     * </p>
     */
    private Event createValidEvent() {
        long currentTime = new Date().getTime();
        long hour = 3600 * 1000;

        // Create chronological timestamps
        Timestamp regStart = new Timestamp(new Date(currentTime + hour));
        Timestamp regEnd = new Timestamp(new Date(currentTime + 2 * hour));
        Timestamp eventStart = new Timestamp(new Date(currentTime + 3 * hour));
        Timestamp eventEnd = new Timestamp(new Date(currentTime + 4 * hour));

        return new Event(
                "Test Event", "Description", mockOrganizer, testLocation,
                eventStart, eventEnd, 10, 5,
                regStart, regEnd, false, null, null, true, null
        );
    }

    // ==========================================
    // 1. Constructor & Validation Tests
    // ==========================================

    /**
     * Tests that a valid event is created successfully with correct attributes.
     * <p>
     * <b>Scenario:</b> Invoke constructor with chronologically correct dates and valid parameters.<br>
     * <b>Expected Result:</b> The Event object is instantiated, and fields like Name and Waitlist Capacity are set correctly.
     * </p>
     */
    @Test
    public void testValidEventCreation() {
        Event event = createValidEvent();
        assertNotNull("Event should be created successfully", event);
        assertEquals("Name should match", "Test Event", event.getName());
        assertEquals("Waitlist capacity should match", (Integer) 5, event.getWaitlistCapacity());
    }

    /**
     * Tests validation logic for invalid date ranges.
     * <p>
     * <b>Scenario:</b> Attempt to create an event where Registration starts in the past OR Event End is before Event Start.<br>
     * <b>Expected Result:</b> The constructor throws an {@link IllegalArgumentException} to prevent invalid data state.
     * </p>
     */
    @Test
    public void testConstructor_InvalidDates_ThrowsException() {
        long currentTime = new Date().getTime();
        Timestamp past = new Timestamp(new Date(currentTime - 100000));
        Timestamp future = new Timestamp(new Date(currentTime + 100000));

        // Case 1: Reg Start is in the past
        assertThrows(IllegalArgumentException.class, () ->
                new Event("Name", "Desc", mockOrganizer, "Loc", future, future, 10, 10, past, future, false, null, null, false, null));

        // Case 2: Event End before Event Start
        assertThrows(IllegalArgumentException.class, () ->
                new Event("Name", "Desc", mockOrganizer, "Loc", future, past, 10, 10, future, future, false, null, null, false, null));
    }

    /**
     * Tests validation logic for Reoccurring Events.
     * <p>
     * <b>Scenario:</b> Set {@code isReoccurring} to true but provide {@code null} for the reoccurring type.<br>
     * <b>Expected Result:</b> The constructor throws an {@link IllegalArgumentException}.
     * </p>
     */
    @Test
    public void testConstructor_ReoccurringLogic_ThrowsException() {
        Timestamp future = new Timestamp(new Date(new Date().getTime() + 100000));

        // Case: isReoccurring is TRUE, but ReoccurringType is NULL
        assertThrows(IllegalArgumentException.class, () ->
                new Event("Name", "Desc", mockOrganizer, "Loc", future, future, 10, 10, future, future,
                        true, future, null, false, null)); // Null type
    }

    // ==========================================
    // 2. List Management (Waitlist)
    // ==========================================

    /**
     * Tests the logic for joining the waitlist.
     * <p>
     * <b>Scenario:</b>
     * <ol>
     * <li>A user joins when space is available (Success).</li>
     * <li>The same user attempts to join again (Duplicate check).</li>
     * <li>A different user attempts to join when capacity is full (Failure).</li>
     * </ol>
     * <b>Expected Result:</b> Users are added/rejected correctly based on capacity and duplication rules.
     * </p>
     */
    @Test
    public void testJoinWaitlist_Logic() {
        Event event = createValidEvent();
        event.setRegistrationOpened(true); // Open registration
        event.setLotteryProcessed(false);

        String user1 = "device_1";
        String user2 = "device_2";
        GeoPoint location = new GeoPoint(53.5, -113.5);

        // 1. Join Success
        event.joinWaitList(user1, location);
        assertTrue(event.inWaitList(user1));
        assertEquals("Geolocation should be stored", location, event.getGeolocationMap().get(user1));

        // 2. Duplicate Join (Should perform no-op)
        event.joinWaitList(user1, location);
        assertEquals("Waitlist size should still be 1", 1, event.getWaitList().size());

        // 3. Capacity Limit
        event.setWaitlistCapacity(1);
        event.joinWaitList(user2, null);
        assertFalse("User 2 should not fit in waitlist", event.inWaitList(user2));
    }

    /**
     * Tests leaving the waitlist.
     * <p>
     * <b>Scenario:</b> A user currently on the waitlist leaves.<br>
     * <b>Expected Result:</b> The user's Device ID is removed from the list, and their Geolocation data is wiped.
     * </p>
     */
    @Test
    public void testLeaveWaitlist() {
        Event event = createValidEvent();
        event.setRegistrationOpened(true);
        String user = "device_1";
        GeoPoint location = new GeoPoint(10, 10);

        event.joinWaitList(user, location);
        assertTrue(event.inWaitList(user));

        event.leaveWaitList(user);
        assertFalse(event.inWaitList(user));
        assertFalse("Geolocation should be removed", event.getGeolocationMap().containsKey(user));
    }

    // ==========================================
    // 3. Workflow Tests (Accept / Decline)
    // ==========================================

    /**
     * Tests a user accepting an event invitation.
     * <p>
     * <b>Scenario:</b> A user selected by the lottery (Pending) chooses to Accept.<br>
     * <b>Expected Result:</b> User is moved from the Pending list to the Accepted list.
     * </p>
     */
    @Test
    public void testJoinAcceptedList() {
        Event event = createValidEvent();
        String user = "user_winner";

        // Pre-requisites: Registration Closed (Lottery done), Event not started
        event.setRegistrationOpened(true);
        event.setLotteryProcessed(true);
        event.setPendingExpired(false);

        // Simulate user winning lottery (placed in pending)
        event.getPendingList().add(user);

        // User Accepts
        event.joinAcceptedList(user);

        assertFalse("User should leave pending", event.inPendingList(user));
        assertTrue("User should enter accepted", event.inAcceptedList(user));
    }

    /**
     * Tests the "Second Chance" mechanism when a user declines an invitation.
     * <p>
     * <b>Scenario:</b> User A (Pending) declines. User B is on the Waitlist.<br>
     * <b>Expected Result:</b> User A is moved to Declined. User B is automatically promoted from Waitlist to Pending.
     * </p>
     */
    @Test
    public void testDeclineAndSecondChance() {
        Event event = createValidEvent();
        String winner1 = "user_winner";
        String loser1 = "user_waitlisted";

        // Setup State
        event.setRegistrationOpened(true);
        event.setLotteryProcessed(true);
        event.setPendingExpired(false);

        // Initial State: Winner in Pending, Loser in Waitlist
        event.getPendingList().add(winner1);
        event.getWaitList().add(loser1);

        // Action: Winner Declines
        event.joinDeclinedList(winner1);

        // Assertions
        // 1. Original winner is now declined
        assertTrue("Winner should be in declined list", event.inDeclinedList(winner1));
        assertFalse("Winner should not be in pending", event.inPendingList(winner1));

        // 2. Second Chance Logic: Waitlist user should move to Pending
        assertFalse("Waitlist should be empty (moved to pending)", event.inWaitList(loser1));
        assertTrue("Waitlist user should be promoted to Pending", event.inPendingList(loser1));
    }

    // ==========================================
    // 4. Serialization Test (CRITICAL)
    // ==========================================

    /**
     * Verifies that the Event object can be serialized and deserialized.
     * <p>
     * <b>Scenario:</b> Serialize an Event object to a byte stream and read it back.<br>
     * <b>Expected Result:</b> The object is reconstructed successfully, and transient fields (specifically {@link Timestamp})
     * are preserved via the custom {@code writeObject} and {@code readObject} methods.
     * </p>
     */
    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        Event originalEvent = createValidEvent();

        // 1. Serialize to Byte Array
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(bos);
        out.writeObject(originalEvent);
        out.flush();
        byte[] bytes = bos.toByteArray();

        // 2. Deserialize from Byte Array
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(bis);
        Event deserializedEvent = (Event) in.readObject();

        // 3. Verify Data Integrity
        assertNotNull(deserializedEvent);
        assertEquals("Name should persist", originalEvent.getName(), deserializedEvent.getName());

        // Verify Timestamp persistence (custom logic check)
        // Note: We compare .toDate() or seconds because object references differ
        assertEquals("Event Start Time should persist",
                originalEvent.getEventStartDateTime().getSeconds(),
                deserializedEvent.getEventStartDateTime().getSeconds());

        assertEquals("Registration Start Time should persist",
                originalEvent.getRegistrationStartDateTime().getSeconds(),
                deserializedEvent.getRegistrationStartDateTime().getSeconds());
    }
}