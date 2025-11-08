package com.example.matrix_events.database;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class DBConnectorTest implements DBListener<DBConnectorTest.TestObject> {

    // A simple DBObject for testing
    public static class TestObject extends DBObject {
        private String testField;

        public TestObject() {}
        public TestObject(String testField) { this.testField = testField; }

        public String getTestField() { return testField; }
        public void setTestField(String testField) { this.testField = testField; }
    }

    private DBConnector<TestObject> connector;
    private CountDownLatch latch;
    private TestObject testObject;
    private volatile List<TestObject> currentObjects;

    @Before
    public void setUp() {
        latch = new CountDownLatch(1);
        connector = new DBConnector<>("test_collection", this, TestObject.class);
    }

    @Test
    public void testCreateUpdateDelete() throws InterruptedException {
        // 1. Create
        testObject = new TestObject("Initial Value");
        connector.createAsync(testObject);

        // Wait for creation to be confirmed by the listener
        if (!latch.await(10, TimeUnit.SECONDS)) fail("Timeout waiting for creation");

        TestObject foundObject = findTestObject(currentObjects);
        assertNotNull("Object not found after creation", foundObject);
        assertEquals("Initial Value", foundObject.getTestField());
        testObject.setId(foundObject.getId()); // Set ID for update/delete

        // 2. Update
        latch = new CountDownLatch(1);
        testObject.setTestField("Updated Value");
        connector.updateAsync(testObject);
        if (!latch.await(10, TimeUnit.SECONDS)) fail("Timeout waiting for update");

        foundObject = findTestObject(currentObjects);
        assertNotNull("Object not found after update", foundObject);
        assertEquals("Updated Value", foundObject.getTestField());

        // 3. Delete
        latch = new CountDownLatch(1);
        connector.deleteAsync(testObject);
        if (!latch.await(10, TimeUnit.SECONDS)) fail("Timeout waiting for deletion");

        foundObject = findTestObject(currentObjects);
        assertNull("Object found after deletion", foundObject);
    }

    @Override
    public void readAllAsync_Complete(List<TestObject> objects) {
        currentObjects = objects;
        latch.countDown();
    }

    private TestObject findTestObject(List<TestObject> objects) {
        if (objects == null || testObject == null) return null;
        for (TestObject obj : objects) {
            if (testObject.getTestField().equals(obj.getTestField())) {
                return obj;
            }
        }
        return null;
    }

    @After
    public void tearDown() {
        // Just in case, clean up any leftover objects
        if (currentObjects != null) {
            for (TestObject obj : currentObjects) {
                connector.deleteAsync(obj);
            }
        }
    }
}
