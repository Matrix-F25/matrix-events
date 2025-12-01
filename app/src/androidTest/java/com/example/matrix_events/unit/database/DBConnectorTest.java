package com.example.matrix_events.unit.database;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

import com.example.matrix_events.database.DBConnector;
import com.example.matrix_events.database.DBListener;
import com.example.matrix_events.database.DBObject;

@RunWith(AndroidJUnit4.class)
public class DBConnectorTest implements DBListener<DBConnectorTest.TestObject> {

    // 1. Define Test Object
    public static class TestObject extends DBObject {
        private String testField;
        // Default constructor needed for Firestore
        public TestObject() {}
        public TestObject(String testField) { this.testField = testField; }
        public String getTestField() { return testField; }
        public void setTestField(String testField) { this.testField = testField; }
    }

    private DBConnector<TestObject> connector;
    private CountDownLatch latch;
    private volatile List<TestObject> currentObjects = new ArrayList<>(); // volatile for thread safety
    private String uniqueCollectionName;

    @Before
    public void setUp() {
        // 2. ISOLATION: Generate a unique collection name for THIS specific test run.
        // This prevents "dirty" data from previous tests interfering.
        uniqueCollectionName = "test_coll_" + UUID.randomUUID().toString();

        // Reset state
        currentObjects = new ArrayList<>();

        // Initialize connector
        // Note: The listener will likely fire immediately with an empty list upon connection
        connector = new DBConnector<>(uniqueCollectionName, this, TestObject.class);
    }

    @Test
    public void testFullCrudLifecycle() throws InterruptedException {
        // --- STEP 1: CREATE ---
        latch = new CountDownLatch(1); // Reset latch
        String uniqueValue = "Value_" + UUID.randomUUID().toString();
        TestObject newObj = new TestObject(uniqueValue);

        connector.createAsync(newObj);

        // Wait for the snapshot listener to fire and contain our object
        assertTrue("Timeout waiting for Creation", latch.await(10, TimeUnit.SECONDS));

        // Verify Creation
        TestObject createdObj = findObjectByValue(uniqueValue);
        assertNotNull("Object was not found in the list after creation", createdObj);
        assertNotNull("Created object should have a Firestore ID", createdObj.getId());

        // --- STEP 2: UPDATE ---
        latch = new CountDownLatch(1); // Reset latch for next op
        String updatedValue = "Updated_" + UUID.randomUUID().toString();

        // Modifying the object we retrieved ensures we have the correct ID
        createdObj.setTestField(updatedValue);
        connector.updateAsync(createdObj);

        assertTrue("Timeout waiting for Update", latch.await(10, TimeUnit.SECONDS));

        // Verify Update
        TestObject updatedObj = findObjectById(createdObj.getId());
        assertNotNull("Object lost during update", updatedObj);
        assertEquals("Field value was not updated", updatedValue, updatedObj.getTestField());

        // --- STEP 3: DELETE ---
        latch = new CountDownLatch(1); // Reset latch
        connector.deleteAsync(updatedObj);

        assertTrue("Timeout waiting for Delete", latch.await(10, TimeUnit.SECONDS));

        // Verify Deletion
        TestObject deletedObj = findObjectById(createdObj.getId());
        assertNull("Object should not exist after deletion", deletedObj);
    }

    /**
     * Edge Case: Attempting to update an object that has no ID.
     * The DBConnector should handle this gracefully (check logs) and not crash.
     */
    @Test
    public void testUpdateWithoutId_ShouldNotCrash() {
        TestObject noIdObj = new TestObject("No ID");
        // This should trigger the "Cannot update object..." log in your connector
        // and return safely.
        try {
            connector.updateAsync(noIdObj);
        } catch (Exception e) {
            fail("Connector crashed on invalid update: " + e.getMessage());
        }
    }

    // --- DBListener Implementation ---
    @Override
    public void readAllAsync_Complete(List<TestObject> objects) {
        this.currentObjects = objects;
        // Signal that the data has changed
        if (latch != null) {
            latch.countDown();
        }
    }

    // --- Helpers ---

    private TestObject findObjectByValue(String val) {
        for (TestObject obj : currentObjects) {
            if (val.equals(obj.getTestField())) return obj;
        }
        return null;
    }

    private TestObject findObjectById(String id) {
        for (TestObject obj : currentObjects) {
            if (id.equals(obj.getId())) return obj;
        }
        return null;
    }
}