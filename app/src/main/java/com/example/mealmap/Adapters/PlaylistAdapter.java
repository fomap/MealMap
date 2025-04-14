package com.example.mealmap.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmap.Playlist.Playlist;
import com.example.mealmap.R;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.ViewHolder> {
    private List<Playlist> playlists = new ArrayList<>();

    public void updatePlaylists(List<Playlist> newPlaylists) {
        playlists.clear();
        playlists.addAll(newPlaylists);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        holder.bind(playlist);
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtName;
        private final TextView txtCount;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txt_playlist_name);
            txtCount = itemView.findViewById(R.id.txt_recipe_count);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Playlist playlist = playlists.get(position);
                    Context context = itemView.getContext();

                    Intent intent = new Intent(context, FragmentHostActivity.class);
                    intent.putExtra("collectionType", "playlists");
                    intent.putExtra("collectionKey", playlist.getKey());
                    context.startActivity(intent);
                }
            });
        }

        void bind(Playlist playlist) {

            txtName.setText(playlist.getKey());

            txtCount.setText(playlist.getRecipeCount() + " recipes");
        }
    }
}
