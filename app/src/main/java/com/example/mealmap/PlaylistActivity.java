package com.example.mealmap;

import android.content.Intent;
import android.os.Bundle;

import com.example.mealmap.Adapters.PlaylistAdapter;
import com.example.mealmap.MealPlanning.MealPlanningActivity;
import com.example.mealmap.Playlist.Playlist;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private PlaylistAdapter adapter;
    private DatabaseReference playlistsRef;
    private String UID;

    private ValueEventListener playlistsListener;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) UID = user.getUid();
        playlistsRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(UID).child("playlists");


        recyclerView = findViewById(R.id.recycler_playlist);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PlaylistAdapter();
        recyclerView.setAdapter(adapter);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.bottomNav_playlist);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Class<?> targetActivity = null;

            if (itemId == R.id.bottomNav_today) {
                targetActivity = MainActivity.class;
            } else if (itemId == R.id.bottomNav_groceryList) {
                targetActivity = GroceryListActivity.class;
            } else if (itemId == R.id.bottomNav_playlist) {
                targetActivity = PlaylistActivity.class;
            } else if (itemId == R.id.bottomNav_mealPlan) {
                targetActivity = MealPlanningActivity.class;
            }

            if (targetActivity != null) {
                Intent intent = new Intent(getApplicationContext(), targetActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });


        loadPlaylists();
    }

    private void loadPlaylists() {
        playlistsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Playlist> playlists = new ArrayList<>();
                for (DataSnapshot playlistSnapshot : snapshot.getChildren()) {
                    String playlistKey = playlistSnapshot.getKey();
                    int recipeCount = (int) playlistSnapshot.getChildrenCount();

                    Playlist playlist = new Playlist();
                    playlist.setKey(playlistKey);
                    playlist.setRecipeCount(recipeCount);
                    playlists.add(playlist);
                }
                adapter.updatePlaylists(playlists);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PlaylistActivity.this, "Error loading playlists", Toast.LENGTH_SHORT).show();
            }
        };
        playlistsRef.addValueEventListener(playlistsListener);
    }




    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (playlistsRef != null && playlistsListener != null) {
            playlistsRef.removeEventListener(playlistsListener);
        }
    }


}