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

import com.bumptech.glide.Glide; // for loading the images

import java.util.List;

public class PosterAdapter extends RecyclerView.Adapter<PosterAdapter.PosterViewHolder> {

    private final Context context;
    private final List<Poster> posterList;

    public PosterAdapter(@NonNull Context context, List<Poster> posterList) {
        this.context = context;
        this.posterList = posterList;
    }

    @NonNull
    @Override
    public PosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.admin_poster_item, parent, false);

        return new PosterViewHolder(view);
    }

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

    @Override
    public int getItemCount() {
        return posterList.size();
    }

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