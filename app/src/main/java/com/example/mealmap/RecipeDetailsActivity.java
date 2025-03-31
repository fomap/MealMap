package com.example.mealmap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmap.Adapters.IngredientsAdapter;
import com.example.mealmap.Adapters.InstructionsAdapter;
import com.example.mealmap.Listeners.InstructionsListener;
import com.example.mealmap.Listeners.RecipeDetailsListener;
import com.example.mealmap.Models.InstructionsResponse;
import com.example.mealmap.Models.Recipe;
import com.example.mealmap.Models.RecipeDetailsResponse;
import com.squareup.picasso.Picasso;

import java.util.List;

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
    private Recipe currentRecipe;
    Button btn_add_to_playlist, btn_add_to_meal_plan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);
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


        btn_add_to_meal_plan.setOnClickListener(v -> {

            startActivity(new Intent(this, MealPlanActivity.class));
            finish();

        });

        btn_add_to_playlist.setOnClickListener( view -> {

            startActivity(new Intent(this, PlaylistActivity.class)
            );
            finish();

        });

    }

    private final RecipeDetailsListener recipeDetailsListener = new RecipeDetailsListener() {
        @Override
        public void didFetch(RecipeDetailsResponse response, String message) {
            dialog.dismiss();
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