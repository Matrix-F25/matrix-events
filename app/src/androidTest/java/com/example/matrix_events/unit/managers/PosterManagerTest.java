package com.example.matrix_events.unit.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.matrix_events.entities.Event;
import com.example.matrix_events.entities.Poster;
import com.example.matrix_events.entities.Profile;
import com.example.matrix_events.managers.EventManager;
import com.example.matrix_events.managers.PosterManager;
import com.example.matrix_events.mvc.View;
import com.google.firebase.Timestamp;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Robust Integration Tests for {@link PosterManager}.
 * <p>
 * This suite verifies the interaction between the PosterManager and Firestore.
 * It focuses on the metadata management of posters.
 * </p>
 * <p>
 * <b>Note on Storage:</b> These tests focus on the <i>database</i> side (Firestore).
 * Testing actual file uploads (Firebase Storage) requires physical files and is
 * typically done via manual QA or specialized UI tests, as it relies on specific
 * emulator file permissions.
 * </p>
 */
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PosterManagerTest implements View {

    private static final String TAG = "PosterManagerTest";
    private PosterManager posterManager;
    private EventManager eventManager;
    private CountDownLatch latch;

    // Static variables to share state between ordered tests
    private static String testPosterId;
    private static String testEventId;

    // Test Data
    private static final String TEST_EVENT_ID_REF = "event_" + UUID.randomUUID().toString();
    private static final String TEST_FILENAME = "test_img_" + UUID.randomUUID() + ".jpg";
    // We use NULL for the URL to avoid crashing Firebase Storage "getReferenceFromUrl"
    // during the delete tests (since we aren't actually uploading a real file).
    private static final String TEST_URL = null;

    @Before
    public void setUp() {
        posterManager = PosterManager.getInstance();
        posterManager.addView(this);

        eventManager = EventManager.getInstance();
        eventManager.addView(this);
    }

    /**
     * MVC Callback: Triggered when Managers finish a DB operation.
     */
    @Override
    public void update() {
        if (latch != null) {
            Log.d(TAG, "Update received from Manager");
            latch.countDown();
        }
    }

    // ==========================================
    // 1. Create Operation
    // ==========================================

    @Test
    public void testA_CreatePosterMetadata() throws InterruptedException {
        // Manually create a Poster object (simulating what happens after a successful upload)
        Poster poster = new Poster(TEST_URL, TEST_EVENT_ID_REF, TEST_FILENAME);

        latch = new CountDownLatch(1);
        posterManager.createPoster(poster);

        // Wait for Firestore to update
        assertTrue("Timed out waiting for poster creation", latch.await(10, TimeUnit.SECONDS));

        // Verify creation
        boolean found = false;
        for (Poster p : posterManager.getPosters()) {
            if (p.getFileName().equals(TEST_FILENAME) && p.getEventId().equals(TEST_EVENT_ID_REF)) {
                testPosterId = p.getId();
                found = true;
                break;
            }
        }
        assertTrue("Poster should be found in manager", found);
        assertNotNull("Poster ID should be generated", testPosterId);
    }

    // ==========================================
    // 2. Read & Update Operations
    // ==========================================

    @Test
    public void testB_UpdatePosterMetadata() throws InterruptedException {
        // Find the poster
        Poster poster = null;
        for (Poster p : posterManager.getPosters()) {
            if (p.getId().equals(testPosterId)) {
                poster = p;
                break;
            }
        }
        assertNotNull("Poster must exist for update test", poster);

        // Update the Event ID it points to
        String newEventId = "updated_event_" + UUID.randomUUID();
        poster.setEventId(newEventId);

        latch = new CountDownLatch(1);
        posterManager.updatePoster(poster);

        assertTrue("Timed out waiting for poster update", latch.await(10, TimeUnit.SECONDS));

        // Verify Update
        Poster updated = null;
        for (Poster p : posterManager.getPosters()) {
            if (p.getId().equals(testPosterId)) updated = p;
        }
        assertNotNull(updated);
        assertEquals("Event ID should be updated", newEventId, updated.getEventId());
    }

    // ==========================================
    // 3. Integration: Event <-> Poster Deletion
    // ==========================================

    /**
     * User Story: When a poster is deleted, the Event that referenced it
     * must have its 'poster' field set to null.
     */
    @Test
    public void testC_DeletePosterUpdatesEvent() throws InterruptedException {
        // 1. Create a Real Event
        Profile org = new Profile("Org", "email", "123", "dev_id");

        long now = new Date().getTime();
        long hour = 3600 * 1000;

        // Ensure proper chronological order for Event validation logic
        Timestamp regStart = new Timestamp(new Date(now + hour));
        Timestamp regEnd = new Timestamp(new Date(now + 2 * hour));
        Timestamp evtStart = new Timestamp(new Date(now + 3 * hour));
        Timestamp evtEnd = new Timestamp(new Date(now + 4 * hour));

        Event event = new Event("Poster Test Event", "Desc", org, "Loc",
                evtStart, evtEnd, 10, 0,
                regStart, regEnd, false, null, null, false, null);

        latch = new CountDownLatch(1);
        eventManager.createEvent(event);
        assertTrue("Wait for event creation", latch.await(10, TimeUnit.SECONDS));

        // Find Event ID
        for(Event e : eventManager.getEvents()) {
            if(e.getName().equals("Poster Test Event")) {
                testEventId = e.getId();
                event = e;
                break;
            }
        }
        assertNotNull(testEventId);

        // 2. Create a Poster and link it to this Event
        Poster poster = new Poster(TEST_URL, testEventId, "linked_poster.jpg");
        latch = new CountDownLatch(1);
        posterManager.createPoster(poster);
        assertTrue("Wait for poster creation", latch.await(10, TimeUnit.SECONDS));

        // Retrieve the generated poster with ID
        Poster createdPoster = null;
        for(Poster p : posterManager.getPosters()) {
            if(p.getFileName().equals("linked_poster.jpg")) createdPoster = p;
        }
        assertNotNull(createdPoster);

        // 3. Link Poster to Event manually (as the app would do)
        event.setPoster(createdPoster);
        latch = new CountDownLatch(1);
        eventManager.updateEvent(event);
        assertTrue("Wait for event update", latch.await(10, TimeUnit.SECONDS));

        // Verify Linkage
        Event linkedEvent = eventManager.getEventByDBID(testEventId);
        assertNotNull("Event should have poster", linkedEvent.getPoster());
        assertEquals(createdPoster.getId(), linkedEvent.getPoster().getId());

        // 4. DELETE THE POSTER
        latch = new CountDownLatch(1);
        // We use a latch count of 1. Both PosterManager and EventManager will trigger update().
        // PosterManager deletes the doc, and updates EventManager.
        posterManager.deletePoster(createdPoster);
        assertTrue("Wait for deletion logic", latch.await(10, TimeUnit.SECONDS));

        // Give a tiny buffer for the second async call (Event update) to propagate if not caught by latch
        Thread.sleep(1000);

        // 5. Verify Side Effect: Event's poster should now be null
        Event cleanedEvent = eventManager.getEventByDBID(testEventId);
        assertNotNull(cleanedEvent);
        assertNull("Event poster field should be nullified after poster deletion", cleanedEvent.getPoster());

        // Cleanup Event
        eventManager.deleteEvent(cleanedEvent);
    }

    // ==========================================
    // 4. Delete Operation (Cleanup)
    // ==========================================

    @Test
    public void testD_DeleteInitialTestPoster() throws InterruptedException {
        // Delete the poster created in Test A
        Poster poster = null;
        for (Poster p : posterManager.getPosters()) {
            if (p.getId().equals(testPosterId)) poster = p;
        }

        if (poster != null) {
            // We use a polling loop here because this test might receive stray updates
            // from previous tests (like EventManager updates) that trip the latch early.
            posterManager.deletePoster(poster);

            boolean found = true;
            long endTime = System.currentTimeMillis() + 10000; // 10s timeout

            while (System.currentTimeMillis() < endTime && found) {
                latch = new CountDownLatch(1);
                // Wait for an update notification OR just wait for the next tick
                latch.await(1, TimeUnit.SECONDS);

                // Check if the poster is gone
                found = false;
                for (Poster p : posterManager.getPosters()) {
                    if (p.getId().equals(testPosterId)) found = true;
                }
            }
            assertFalse("Poster should be removed after delete", found);
        }
    }

    @After
    public void tearDown() {
        posterManager.removeView(this);
        eventManager.removeView(this);
    }
}