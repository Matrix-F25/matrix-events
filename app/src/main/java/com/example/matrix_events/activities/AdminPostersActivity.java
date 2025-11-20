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

public class AdminPostersActivity extends AppCompatActivity implements View {

    private RecyclerView postersRecyclerView;
    private PosterAdapter posterAdapter;
    private List<Poster> posterList;

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

        posterList = new ArrayList<>(PosterManager.getInstance().getPosters());
        posterAdapter = new PosterAdapter(this, posterList);

        postersRecyclerView = findViewById(R.id.posters_recycler_view);
        GridLayoutManager posterLayout = new GridLayoutManager(this, 2); // how many columns we want

        postersRecyclerView.setLayoutManager(posterLayout);
        postersRecyclerView.setAdapter(posterAdapter);

        PosterManager.getInstance().addView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PosterManager.getInstance().removeView(this);
    }

    @Override
    public void update() {
        posterList.clear();
        posterList.addAll(PosterManager.getInstance().getPosters());
        if (posterAdapter != null) {
            posterAdapter.notifyDataSetChanged();
        }
    }
}