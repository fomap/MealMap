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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
    Button btn_add_to_playlist, btn_add_to_meal_plan;

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
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            UID = currentUser.getUid();
            Toast.makeText(this, "UID: " + UID, Toast.LENGTH_SHORT).show();
        }

        btn_add_to_meal_plan.setOnClickListener(v -> {
            showDayOfWeekPopup();
        });

        btn_add_to_playlist.setOnClickListener( view -> {
            startActivity(new Intent(this, PlaylistActivity.class)
            );
            finish();

        });

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handleBackNavigation();
    }

    private void handleBackNavigation() {
        if ("mealPlan".equals(source)) {

            Intent intent = new Intent(this, MealPlanningActivity.class);
            intent.putExtra("selectedDay", day);
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        } else {
            startActivity(new Intent(this, MainActivity.class));
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
        // 1. Validate recipe data
        if (currentRecipeDetails == null) {
            Toast.makeText(this, "Recipe data not loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Validate user authentication
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getUid() == null) {
            Toast.makeText(this, "Not authenticated!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Create reference and get push key FIRST
        DatabaseReference dayRef = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(user.getUid())
                .child("MealPlan")
                .child(dayOfWeek);

        String pushKey = dayRef.push().getKey(); // Generate key before saving

        if (pushKey == null) {
            Toast.makeText(this, "Failed to generate recipe key", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. Prepare data with additional useful fields
        Map<String, Object> recipeData = new HashMap<>();
        recipeData.put("id", currentRecipeDetails.id);
        recipeData.put("title", currentRecipeDetails.title);
        recipeData.put("image", currentRecipeDetails.image);
        recipeData.put("pushKey", pushKey);
        // 5. Save to specific path with the generated key
        dayRef.child(pushKey).setValue(recipeData)
                .addOnSuccessListener(aVoid -> {
                    // Success - you could return the pushKey here if needed
                    Toast.makeText(this, "Added to " + dayOfWeek, Toast.LENGTH_SHORT).show();

                    // If you need to immediately use this in another activity:
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("newRecipeKey", pushKey);
                    setResult(RESULT_OK, resultIntent);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("SAVE_RECIPE", "Error saving recipe", e);
                });
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
        btn_add_to_playlist = findViewById(R.id.btn_add_to_playlist);
        btn_add_to_meal_plan = findViewById(R.id.btn_add_to_meal_plan);
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