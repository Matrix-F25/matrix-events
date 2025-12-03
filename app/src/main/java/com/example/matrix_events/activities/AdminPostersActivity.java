package com.example.matrix_events.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.matrix_events.R;
import com.example.matrix_events.adapters.PosterAdapter;
import com.example.matrix_events.entities.Poster;
import com.example.matrix_events.fragments.AdminNavigationBarFragment;
import com.example.matrix_events.managers.PosterManager;
import com.example.matrix_events.mvc.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity responsible for the Administrator's gallery view of all event posters.
 * <p>
 * This activity provides a dashboard for Admins to monitor uploaded content.
 * Unlike the standard event lists, this view utilizes a {@link RecyclerView} with a
 * {@link GridLayoutManager} to display posters in a 2-column grid format, maximizing
 * visual real estate for image review.
 * </p>
 * <p>
 * It implements {@link View} to observe the {@link PosterManager}, ensuring that if a poster
 * is deleted (either from this screen or elsewhere), the gallery updates immediately.
 * </p>
 */
public class AdminPostersActivity extends AppCompatActivity implements View {

    private RecyclerView postersRecyclerView;
    private PosterAdapter posterAdapter;
    private List<Poster> posterList;

    /**
     * Called when the activity is starting.
     * <p>
     * Initializes the UI layout and performs the following specific setups:
     * <ul>
     * <li><b>Navigation:</b> Loads the {@link AdminNavigationBarFragment} highlighting the Posters tab.</li>
     * <li><b>Layout Manager:</b> Configures the RecyclerView with a {@link GridLayoutManager} (span count 2).</li>
     * <li><b>Adapter:</b> Initializes the {@link PosterAdapter} with the current list of posters.</li>
     * <li><b>MVC Registration:</b> Registers this activity as an observer of {@link PosterManager}.</li>
     * </ul>
     * </p>
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     * shut down then this Bundle contains the data it most recently supplied.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_posters);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.admin_navigation_bar_fragment, AdminNavigationBarFragment.newInstance(R.id.nav_admin_posters))
                .commit();

        // Initialize Data Source
        posterList = new ArrayList<>(PosterManager.getInstance().getPosters());
        posterAdapter = new PosterAdapter(this, posterList);

        // Setup RecyclerView with Grid Layout
        postersRecyclerView = findViewById(R.id.posters_recycler_view);
        GridLayoutManager posterLayout = new GridLayoutManager(this, 2); // 2 columns

        postersRecyclerView.setLayoutManager(posterLayout);
        postersRecyclerView.setAdapter(posterAdapter);

        // Register as Observer
        PosterManager.getInstance().addView(this);
    }

    /**
     * Cleanup method called when the activity is destroyed.
     * <p>
     * Unregisters this activity from the {@link PosterManager} to prevent memory leaks.
     * </p>
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        PosterManager.getInstance().removeView(this);
    }

    /**
     * MVC Callback: Updates the gallery when the Model data changes.
     * <p>
     * This method clears the current local list and repopulates it with the fresh data
     * from {@link PosterManager#getPosters()}. It then notifies the adapter to refresh
     * the grid view.
     * </p>
     */
    @Override
    public void update() {
        posterList.clear();
        posterList.addAll(PosterManager.getInstance().getPosters());
        if (posterAdapter != null) {
            posterAdapter.notifyDataSetChanged();
        }
    }
}