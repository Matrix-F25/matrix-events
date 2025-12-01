package com.example.matrix_events.unit.adapters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.PosterAdapter;
import com.example.matrix_events.entities.Poster;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * Instrumented Unit Tests for {@link PosterAdapter}.
 * <p>
 * This suite verifies the RecyclerView Adapter logic, including:
 * <ul>
 * <li>Correct item counting.</li>
 * <li>ViewHolder creation and view inflation.</li>
 * <li>Data binding (ensuring Glide runs on the main thread).</li>
 * <li>Delete button listener attachment.</li>
 * </ul>
 * </p>
 */
@RunWith(AndroidJUnit4.class)
public class PosterAdapterTest {

    private Context context;
    private List<Poster> testData;
    private PosterAdapter adapter;

    @Before
    public void setUp() {
        // Use a ContextThemeWrapper with an AppTheme to ensure styles/attributes resolve correctly during inflation
        Context targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context = new ContextThemeWrapper(targetContext, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_NoActionBar);

        testData = new ArrayList<>();
        testData.add(new Poster("https://example.com/1.jpg", "event_1", "file_1.jpg"));
        testData.add(new Poster("https://example.com/2.jpg", "event_2", "file_2.jpg"));

        adapter = new PosterAdapter(context, testData);
    }

    /**
     * Test A: Verify Item Count.
     * <p>
     * Ensures the adapter reports the correct size of the dataset.
     * </p>
     */
    @Test
    public void testItemCount() {
        assertEquals("Item count should match list size", 2, adapter.getItemCount());
    }

    /**
     * Test B: Verify ViewHolder Creation.
     * <p>
     * Ensures {@code onCreateViewHolder} inflates the layout correctly and returns
     * a valid {@code PosterViewHolder} holding the expected views.
     * </p>
     */
    @Test
    public void testCreateViewHolder() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            // Create a dummy parent to simulate RecyclerView behavior
            FrameLayout parent = new FrameLayout(context);

            PosterAdapter.PosterViewHolder holder = adapter.onCreateViewHolder(parent, 0);

            assertNotNull("ViewHolder should not be null", holder);
            assertNotNull("ItemView should not be null", holder.itemView);

            // Verify internal views are found
            ImageView posterView = holder.itemView.findViewById(R.id.poster_image_view);
            ImageButton deleteButton = holder.itemView.findViewById(R.id.poster_delete_button);

            assertNotNull("Poster ImageView should exist in layout", posterView);
            assertNotNull("Delete Button should exist in layout", deleteButton);
        });
    }

    /**
     * Test C: Verify Bind View Holder (Data Binding).
     * <p>
     * Executes {@code onBindViewHolder} on the main thread (required by Glide).
     * Verifies that the binding logic runs without crashing and views are accessible.
     * </p>
     */
    @Test
    public void testBindViewHolder() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FrameLayout parent = new FrameLayout(context);
            PosterAdapter.PosterViewHolder holder = adapter.onCreateViewHolder(parent, 0);

            // Bind data at position 0
            adapter.onBindViewHolder(holder, 0);

            // Since we cannot easily check the internal state of Glide without complex mocking,
            // the primary success criteria here is that onBindViewHolder executes without
            // throwing exceptions (like Main Thread violations or NullPointers).

            // We can check that the delete button is visible
            ImageButton deleteButton = holder.itemView.findViewById(R.id.poster_delete_button);
            assertEquals("Delete button should be visible", View.VISIBLE, deleteButton.getVisibility());
        });
    }

    /**
     * Test D: Verify Delete Button Interaction.
     * <p>
     * Checks that an {@link android.view.View.OnClickListener} is attached to the delete button
     * during the binding process.
     * </p>
     */
    @Test
    public void testDeleteListenerAttached() {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> {
            FrameLayout parent = new FrameLayout(context);
            PosterAdapter.PosterViewHolder holder = adapter.onCreateViewHolder(parent, 0);

            adapter.onBindViewHolder(holder, 0);

            ImageButton deleteButton = holder.itemView.findViewById(R.id.poster_delete_button);

            // Verify a listener was attached
            assertTrue("Delete button should have an OnClickListener attached", deleteButton.hasOnClickListeners());

            // Note: We do not call performClick() here because it would trigger
            // PosterManager.getInstance().deletePoster(), which interacts with
            // Firebase singletons that might not be initialized in this isolated test context.
            // verifying the listener presence confirms the Adapter wiring is correct.
        });
    }
}