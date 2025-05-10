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
import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistViewHolder> {
    private Context context;
    private List<Playlist> playlists;
    public PlaylistAdapter(Context context, List<Playlist> playlists) {
        this.context = context;
        this.playlists = playlists;
    }
    public void updatePlaylists(List<Playlist> newPlaylists) {
        playlists.clear();
        playlists.addAll(newPlaylists);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PlaylistViewHolder(LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        Playlist playlist = playlists.get(position);
        holder.txtName.setText(playlist.getKey());
        holder.txtCount.setText(playlist.getRecipeCount() + " recipes");
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FragmentHostActivity.class);
            intent.putExtra("collectionType", "playlists");
            intent.putExtra("collectionKey", playlist.getKey());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return playlists.size();
    }
}
class PlaylistViewHolder extends RecyclerView.ViewHolder {
    TextView txtName;
    TextView txtCount;
    public PlaylistViewHolder(@NonNull View itemView) {
        super(itemView);
        txtName = itemView.findViewById(R.id.txt_playlist_name);
        txtCount = itemView.findViewById(R.id.txt_recipe_count);
    }
}
