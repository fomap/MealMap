package com.example.mealmap;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmap.Adapters.IngredientsAdapter;
import com.example.mealmap.Listeners.RecipeDetailsListener;
import com.example.mealmap.Models.RecipeDetailsResponse;
import com.squareup.picasso.Picasso;

public class RecipeDetailsActivity extends AppCompatActivity {

    int id;
    TextView textView_meal_name;
    TextView textView_recipe_type_ready_time;
    ImageView imageView_mealImage;
    RecyclerView recycler_meal_ingredients;
    RequestManager manager;
    ProgressDialog dialog;
    IngredientsAdapter ingredientsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_details);

        findViews();

        id = Integer.parseInt(getIntent().getStringExtra("id"));

        manager = new RequestManager(this);
        manager.getRecipeDetails(recipeDetailsListener, id);
        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading details...");
        dialog.show();

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
    recycler_meal_ingredients = findViewById(R.id.recycler_meal_ingredients);
    textView_recipe_type_ready_time = findViewById(R.id.textView_recipe_type_ready_time);
    }
}