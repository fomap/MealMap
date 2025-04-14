package com.example.mealmap;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmap.Adapters.IngredientsAdapter;
import com.example.mealmap.Adapters.InstructionsAdapter;
import com.example.mealmap.Listeners.InstructionsListener;
import com.example.mealmap.Listeners.RecipeDetailsListener;
import com.example.mealmap.MealPlanning.MealPlanningActivity;
import com.example.mealmap.Models.InstructionsResponse;
import com.example.mealmap.Models.RecipeDetailsResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeDetailsActivity extends AppCompatActivity {

    int id;
    TextView textView_meal_name;
    TextView textView_recipe_type_ready_time;
    ImageView imageView_mealImage;
    RecyclerView recycler_meal_ingredients, recycler_meal_instructions;
    RequestManager manager;
    ProgressDialog dialog;
    IngredientsAdapter ingredientsAdapter;
    InstructionsAdapter instructionsAdapter;
    Toolbar toolbar;

    private RecipeDetailsResponse currentRecipeDetails;
    Button btn_save_to_collections;

    private String UID;

    private String source;
    private String day;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        source = getIntent().getStringExtra("source");
        day = getIntent().getStringExtra("day");

        findViews();

        id = Integer.parseInt(getIntent().getStringExtra("id"));

        manager = new RequestManager(this);
        manager.getInstructions(instructionsListener, id);
        manager.getRecipeDetails(recipeDetailsListener, id);
        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading details...");
        dialog.show();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Recipe Details");
        }

        source = getIntent().getStringExtra("source");
        day = getIntent().getStringExtra("day");


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            UID = currentUser.getUid();
            Toast.makeText(this, "UID: " + UID, Toast.LENGTH_SHORT).show();
        }


        btn_save_to_collections.setOnClickListener(view -> {
            showSaveDialog();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        handleBackNavigation();
        return true;
    }

    public void onSaveButtonClick(View view) {
        showSaveDialog();
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handleBackNavigation();
    }

    private void handleBackNavigation() {
        if ("mealPlan".equals(source) && day != null) {
            Intent intent = new Intent(this, MealPlanningActivity.class);
            intent.putExtra("selectedDay", day);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } else {
            super.onBackPressed();
        }
        finish();
    }

    private void showDayOfWeekPopup() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.day_of_week_popup);

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.8);
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
        }


        CheckBox chkMonday = dialog.findViewById(R.id.chk_monday);
        CheckBox chkTuesday = dialog.findViewById(R.id.chk_tuesday);
        CheckBox chkWednesday = dialog.findViewById(R.id.chk_wednesday);
        CheckBox chkThursday = dialog.findViewById(R.id.chk_thursday);
        CheckBox chkFriday = dialog.findViewById(R.id.chk_friday);
        CheckBox chkSaturday = dialog.findViewById(R.id.chk_saturday);
        CheckBox chkSunday = dialog.findViewById(R.id.chk_sunday);
        Button btnSaveToDB = dialog.findViewById(R.id.btn_saveToDB);


        btnSaveToDB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (chkMonday.isChecked()) {saveToDP("Monday");}
                if (chkTuesday.isChecked()) {saveToDP("Tuesday");}
                if (chkWednesday.isChecked()) {saveToDP("Wednesday");}
                if (chkThursday.isChecked()) {saveToDP("Thursday");}
                if (chkFriday.isChecked()) {saveToDP("Friday");}
                if (chkSaturday.isChecked()) {saveToDP("Saturday");}
                if (chkSunday.isChecked()) {saveToDP("Sunday");}
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void saveToDP(String dayOfWeek) {
        if (currentRecipeDetails == null) {
            Toast.makeText(this, "Recipe data not loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference dayRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.getUid())
                .child("MealPlan")
                .child(dayOfWeek)
                .child(String.valueOf(id));



        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("id", currentRecipeDetails.id);
        recipeData.put("title", currentRecipeDetails.title);
        recipeData.put("image", currentRecipeDetails.image);


        dayRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Toast.makeText(RecipeDetailsActivity.this,
                            "Recipe already exists in " + dayOfWeek,
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                Map<String, Object> recipeData = new HashMap<>();
                recipeData.put("id", currentRecipeDetails.id);
                recipeData.put("title", currentRecipeDetails.title);
                recipeData.put("image", currentRecipeDetails.image);

                dayRef.setValue(recipeData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(RecipeDetailsActivity.this,
                                    "Added to " + dayOfWeek,
                                    Toast.LENGTH_SHORT).show();

                            Intent resultIntent = new Intent();
                            resultIntent.putExtra("newRecipeKey", id);
                            setResult(RESULT_OK, resultIntent);
                        })
                        .addOnFailureListener(e -> {
                           Toast.makeText(RecipeDetailsActivity.this,"Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle possible errors
            }
        });

    }


private void showSaveDialog() {
    Dialog dialog = new Dialog(this);
    dialog.setContentView(R.layout.collection_selection_popup);

    LinearLayout mealPlanLayout = dialog.findViewById(R.id.mealPlanLayout);
    LinearLayout playlistLayout = dialog.findViewById(R.id.playlistLayout);

    Button btnCreate = dialog.findViewById(R.id.btn_create_playlist);
    btnCreate.setOnClickListener(v -> showCreatePlaylistDialog(playlistLayout));

    mealPlanLayout.removeAllViews();

    String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday", "Sunday"};
    for (String day : days) {
        CheckBox cb = new CheckBox(this);
        cb.setText(day);
        cb.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        mealPlanLayout.addView(cb);
    }


    loadPlaylists(playlistLayout);

    dialog.findViewById(R.id.btn_save).setOnClickListener(v -> {
        saveSelectedCollections(mealPlanLayout, playlistLayout);
        dialog.dismiss();
    });

    dialog.show();
}

    private void saveSelectedCollections(LinearLayout mealPlanLayout, LinearLayout playlistLayout) {

        for (int i = 0; i < mealPlanLayout.getChildCount(); i++) {
            CheckBox cb = (CheckBox) mealPlanLayout.getChildAt(i);
            if (cb.isChecked()) {
                saveToCollection("mealPlan", cb.getText().toString());
            }
        }

        for (int i = 0; i < playlistLayout.getChildCount(); i++) {
            CheckBox cb = (CheckBox) playlistLayout.getChildAt(i);
            if (cb.isChecked()) {
                saveToCollection("playlists", cb.getText().toString());
            }
        }
    }


    private void loadPlaylists(LinearLayout layout) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        DatabaseReference playlistsRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.getUid())
                .child("playlists");

        playlistsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                layout.removeAllViews();
                for (DataSnapshot playlist : snapshot.getChildren()) {
                    CheckBox checkBox = new CheckBox(RecipeDetailsActivity.this);
                    checkBox.setText(playlist.getKey());
                    layout.addView(checkBox);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError(error.toException());
            }
        });
    }

    private void showCreatePlaylistDialog(LinearLayout playlistLayout) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Playlist");

        final EditText input = new EditText(this);
        input.setHint("Enter playlist name");
        builder.setView(input);

        builder.setPositiveButton("Create", (dialog, which) -> {
            String playlistName = input.getText().toString().trim();
            if (!playlistName.isEmpty()) {
                createPlaylist(playlistName, playlistLayout);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void createPlaylist(String playlistName, LinearLayout playlistLayout) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || playlistName.isEmpty()) return;

        DatabaseReference playlistsRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.getUid())
                .child("playlists")
                .child(playlistName);


        playlistsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    playlistsRef.setValue(true).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            CheckBox newPlaylistCheckbox = new CheckBox(RecipeDetailsActivity.this);
                            newPlaylistCheckbox.setText(playlistName);
                            playlistLayout.addView(newPlaylistCheckbox);
                        }
                    });
                } else {
                    Toast.makeText(RecipeDetailsActivity.this,
                            "Playlist already exists!",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(RecipeDetailsActivity.this,
                        "Error checking playlists: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void saveToCollection(String collectionType, String collectionKey) {

        String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(UID)
                .child(collectionType)
                .child(collectionKey)
                .child(String.valueOf(currentRecipeDetails.id));

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    Map<String, Object> recipeData = new HashMap<>();
                    recipeData.put("id", currentRecipeDetails.id);
                    recipeData.put("title", currentRecipeDetails.title);
                    recipeData.put("image", currentRecipeDetails.image);

                    ref.setValue(recipeData)
                            .addOnSuccessListener(aVoid -> showSaveSuccess(collectionKey))
                            .addOnFailureListener(e -> showError(e));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError(error.toException());
            }
        });
    }

    private void showSaveSuccess(String collectionName) {
        String message = "Recipe saved to " + collectionName;
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Helper methods
    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void setResultWithData(String key) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("newRecipeKey", key);
        setResult(RESULT_OK, resultIntent);
    }

    private void showError(Exception e) {
        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.e("SAVE_RECIPE", "Error saving recipe", e);
    }




















    private final RecipeDetailsListener recipeDetailsListener = new RecipeDetailsListener() {
        @Override
        public void didFetch(RecipeDetailsResponse response, String message) {
            dialog.dismiss();
            currentRecipeDetails = response;


            textView_meal_name.setText(response.title);
            Picasso.get().load(response.image).into(imageView_mealImage);
            String mealType;
            if (response.dishTypes != null && !response.dishTypes.isEmpty()) {
                mealType = response.dishTypes.get(0);
            } else {
                mealType = "Meal";
            }

            String readyTime = response.readyInMinutes + " minutes";
            textView_recipe_type_ready_time.setText(mealType + " • " + readyTime);
            recycler_meal_ingredients.setLayoutManager(new LinearLayoutManager(RecipeDetailsActivity.this));
            ingredientsAdapter = new IngredientsAdapter(RecipeDetailsActivity.this, response.extendedIngredients);
            recycler_meal_ingredients.setAdapter(ingredientsAdapter);

        }
        @Override
        public void didError(String message) {
            Toast.makeText(RecipeDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    };
    private void findViews() {
        textView_meal_name = findViewById(R.id.textView_meal_name);
        imageView_mealImage = findViewById(R.id.imageView_mealImage);
        recycler_meal_ingredients = findViewById(R.id.recycler_meal_ingredients);
        textView_recipe_type_ready_time = findViewById(R.id.textView_recipe_type_ready_time);
        recycler_meal_instructions = findViewById(R.id.recycler_meal_instructions);
        btn_save_to_collections = findViewById(R.id.btn_save_to_collections);
       // btn_add_to_meal_plan = findViewById(R.id.btn_add_to_meal_plan);
    }

    private final InstructionsListener instructionsListener = new InstructionsListener() {
        @Override
        public void didFetch(List<InstructionsResponse> response, String message) {

            recycler_meal_instructions.setHasFixedSize(true);
            recycler_meal_instructions.setLayoutManager(new LinearLayoutManager(RecipeDetailsActivity.this, LinearLayoutManager.VERTICAL, false));
            instructionsAdapter = new InstructionsAdapter(RecipeDetailsActivity.this, response);
            recycler_meal_instructions.setAdapter(instructionsAdapter);
        }
        @Override
        public void didError(String message) {

        }
    };
}