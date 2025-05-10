package com.example.mealmap.Adapters;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.example.mealmap.MealPlanning.MealPlanFragment;
import com.example.mealmap.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FragmentHostActivity extends AppCompatActivity {
    private String currentPlaylistKey;
    private DatabaseReference playlistsRef;
    public static final String COLLECTION_TYPE = "collectionType";
    public static final String COLLECTION_KEY = "collectionKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment_host);

        currentPlaylistKey = getIntent().getStringExtra("collectionKey");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            playlistsRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(user.getUid())
                    .child("playlists");
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(currentPlaylistKey);
        }

        String collectionType = getIntent().getStringExtra(COLLECTION_TYPE);
        String collectionKey = getIntent().getStringExtra(COLLECTION_KEY);

        if (savedInstanceState == null) {
            MealPlanFragment fragment = MealPlanFragment.newInstance(collectionType, collectionKey);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_playlist, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_edit) {
            showEditNameDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void showEditNameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rename Playlist");

        final EditText input = new EditText(this);
        input.setText(currentPlaylistKey);
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty() && !newName.equals(currentPlaylistKey)) {
                renamePlaylist(newName);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void renamePlaylist(String newName) {
        DatabaseReference oldRef = playlistsRef.child(currentPlaylistKey);
        playlistsRef.child(newName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(FragmentHostActivity.this,
                            "Playlist name already exists", Toast.LENGTH_SHORT).show();
                } else {
                    performRename(oldRef, newName);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FragmentHostActivity.this,
                        "Error checking playlist", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void performRename(DatabaseReference oldRef, String newName) {
        oldRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                DatabaseReference newRef = playlistsRef.child(newName);
                newRef.setValue(snapshot.getValue(), (error, ref) -> {
                    if (error == null) {
                        oldRef.removeValue().addOnSuccessListener(aVoid -> {
                            MealPlanFragment fragment = (MealPlanFragment) getSupportFragmentManager()
                                    .findFragmentById(R.id.fragment_container);

                            if (fragment != null) {
                                fragment.refreshReference(newName);
                            }
                            currentPlaylistKey = newName;
                            if (getSupportActionBar() != null) {
                                getSupportActionBar().setTitle(newName);
                            }
                        });
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(FragmentHostActivity.this,
                        "Error accessing playlist", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
