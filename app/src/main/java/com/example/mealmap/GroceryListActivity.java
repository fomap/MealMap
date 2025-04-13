package com.example.mealmap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.example.mealmap.Adapters.GroceryAdapter;
import com.example.mealmap.Listeners.RecipeDetailsListener;
import com.example.mealmap.MealPlanning.MealPlanningActivity;
import com.example.mealmap.Models.ExtendedIngredient;
import com.example.mealmap.Models.RecipeDetailsResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmap.databinding.ActivityGroceryListBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class GroceryListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private GroceryAdapter adapter;
    private ProgressDialog progressDialog;
    private Map<String, ExtendedIngredient> combinedIngredients = new HashMap<>();
    private String UID;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

        recyclerView = findViewById(R.id.recycler_grocery);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroceryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading groceries...");
        progressDialog.show();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) UID = user.getUid();

//        fetchMealPlanRecipes();


        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.bottomNav_today) {
                startActivity(new Intent(GroceryListActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.bottomNav_mealPlan) {
                startActivity(new Intent(GroceryListActivity.this, MealPlanningActivity.class));
//                startActivity(new Intent(MainActivity.this, MealPlanningActivityTest.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.bottomNav_playlist) {
                startActivity(new Intent(GroceryListActivity.this, PlaylistActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            else {
                startActivity(new Intent(GroceryListActivity.this, GroceryListActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
//            return true;
        });

    }

//    private void fetchMealPlanRecipes() {
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
//                .child("users").child(UID).child("MealPlan");
//
//        ref.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//
//                Map<Integer, Integer> recipeCounts = new HashMap<>();
//
//                for (DataSnapshot daySnapshot : snapshot.getChildren()) {
//                    for (DataSnapshot recipeSnapshot : daySnapshot.getChildren()) {
//
//                        Integer id = recipeSnapshot.child("id").getValue(Integer.class);
//                        if (id != null) {
//
//                            int currentCount = recipeCounts.containsKey(id) ? recipeCounts.get(id) : 0;
//                            recipeCounts.put(id, currentCount + 1);
//                        }
//                    }
//                }
//
//                fetchRecipeDetails(recipeCounts);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//                progressDialog.dismiss();
//                Toast.makeText(GroceryListActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    private void fetchRecipeDetails(Map<Integer, Integer> recipeCounts) {
//        RequestManager manager = new RequestManager(GroceryListActivity.this);
//        AtomicInteger counter = new AtomicInteger(0);
//        int totalRecipes = recipeCounts.size();
//
//        if (totalRecipes == 0) {
//            progressDialog.dismiss();
//            updateUI();
//            return;
//        }
//
//        for (Map.Entry<Integer, Integer> entry : recipeCounts.entrySet()) {
//            Integer id = entry.getKey();
//            Integer count = entry.getValue();
//
//            manager.getRecipeDetails(new RecipeDetailsListener() {
//                @Override
//                public void didFetch(RecipeDetailsResponse response, String message) {
//                    // Pass the count to combineIngredients
//                    combineIngredients(response.extendedIngredients, count);
//                    if (counter.incrementAndGet() == totalRecipes) {
//                        progressDialog.dismiss();
//                        updateUI();
//                    }
//                }
//
//                @Override
//                public void didError(String message) {
//                    Toast.makeText(GroceryListActivity.this, "Failed to fetch recipe: " + message, Toast.LENGTH_SHORT).show();
//                    if (counter.incrementAndGet() == totalRecipes) {
//                        progressDialog.dismiss();
//                        updateUI();
//                    }
//                }
//            }, id);
//        }
//    }

//    private void combineIngredients(List<ExtendedIngredient> ingredients, int recipeCount) {
//        for (ExtendedIngredient ingredient : ingredients) {
//            String key = ingredient.name.toLowerCase() + "|" + ingredient.unit.toLowerCase();
//
//            if (combinedIngredients.containsKey(key)) {
//                ExtendedIngredient existing = combinedIngredients.get(key);
//                // Multiply the ingredient amount by the recipe count before adding
//                existing.amount += (ingredient.amount * recipeCount);
//            } else {
//                // Create a copy of the ingredient to avoid modifying the original
//                ExtendedIngredient newIngredient = new ExtendedIngredient();
//                newIngredient.id = ingredient.id;
//                newIngredient.aisle = ingredient.aisle;
//                newIngredient.image = ingredient.image;
//                newIngredient.consistency = ingredient.consistency;
//                newIngredient.name = ingredient.name;
//                newIngredient.nameClean = ingredient.nameClean;
//                newIngredient.original = ingredient.original;
//                newIngredient.originalName = ingredient.originalName;
//                // Multiply the amount by the recipe count
//                newIngredient.amount = ingredient.amount * recipeCount;
//                newIngredient.unit = ingredient.unit;
//                newIngredient.meta = ingredient.meta;
//                newIngredient.measures = ingredient.measures;
//
//                combinedIngredients.put(key, newIngredient);
//            }
//        }
//    }

//    private void updateUI() {
//        List<ExtendedIngredient> groceryList = new ArrayList<>(combinedIngredients.values());
//        adapter = new GroceryAdapter(groceryList);
//        recyclerView.setAdapter(adapter);
//    }
}