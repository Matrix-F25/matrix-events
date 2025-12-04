package com.example.matrix_events.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.matrix_events.R;
import com.example.matrix_events.entities.Poster;
import com.example.matrix_events.managers.PosterManager;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * A RecyclerView Adapter for displaying and managing a collection of event {@link Poster} images.
 * <p>
 * This adapter is designed primarily for administrative interfaces (e.g., an "All Images" dashboard)
 * where viewing and deleting posters is required. It uses {@link Glide} for efficient
 * asynchronous image loading and interacts directly with the {@link PosterManager} singleton
 * to handle permanent deletions.
 * </p>
 */
public class PosterAdapter extends RecyclerView.Adapter<PosterAdapter.PosterViewHolder> {

    private final Context context;
    private final List<Poster> posterList;

    /**
     * Constructs a new PosterAdapter.
     *
     * @param context    The current context (required by Glide for image loading). Cannot be null.
     * @param posterList The data source containing {@link Poster} objects. Cannot be null.
     */
    public PosterAdapter(@NonNull Context context, @NonNull List<Poster> posterList) {
        this.context = context;
        this.posterList = posterList;
    }

    /**
     * Called when RecyclerView needs a new {@link PosterViewHolder} of the given type to represent an item.
     * <p>
     * This method inflates the {@code item_poster} layout.
     * </p>
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to an adapter position.
     * @param viewType The view type of the new View.
     * @return A new PosterViewHolder that holds a View of the given view type.
     */
    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.item_poster, parent, false);

        return new PosterViewHolder(view);
    }

    /**
     * Called by RecyclerView to display the data at the specified position.
     * <p>
     * This method performs two main tasks:
     * <ol>
     * <li><b>Image Loading:</b> Uses {@link Glide} to fetch the image from the poster's URL.
     * A placeholder (notification logo) is shown while loading.</li>
     * <li><b>Delete Logic:</b> Sets an {@code OnClickListener} on the delete button which triggers
     * {@link PosterManager#deletePoster(Poster)}.</li>
     * </ol>
     * </p>
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the item at the given position.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull PosterViewHolder holder, int position) {
        Poster poster = posterList.get(position);

        Glide.with(context)
                .load(poster.getImageUrl())
                .placeholder(R.drawable.ic_notification_logo) // the temp image to be replaced
                .into(holder.posterView);

        // to delete a poster
        holder.deleteButton.setOnClickListener(v -> {
            PosterManager.getInstance().deletePoster(poster);
        });
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of posters.
     */
    @Override
    public int getItemCount() {
        return posterList.size();
    }

    /**
     * A ViewHolder class that describes an item view and metadata about its place within the RecyclerView.
     * <p>
     * It holds references to the {@link ImageView} for the poster and the {@link ImageButton} for deletion.
     * </p>
     */
    public static class PosterViewHolder extends RecyclerView.ViewHolder {
        ImageView posterView;
        ImageButton deleteButton;

        public PosterViewHolder(@NonNull View itemView) {
            super(itemView);
            posterView = itemView.findViewById(R.id.poster_image_view);
            deleteButton = itemView.findViewById(R.id.poster_delete_button);
        }
    }
}