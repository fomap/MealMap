package com.example.mealmap;

import android.os.Bundle;

import com.example.mealmap.MealPlanning.MealPlanFragment;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.mealmap.databinding.ActivityPlaylistBinding;

public class PlaylistActivity extends AppCompatActivity {
    private String playlistName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);
        playlistName = getIntent().getStringExtra("playlistName");
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(playlistName);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container,
                        MealPlanFragment.newInstance("playlists", playlistName))
                .commit();
    }
}