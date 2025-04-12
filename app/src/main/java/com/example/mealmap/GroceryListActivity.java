package com.example.mealmap;

import android.app.ProgressDialog;
import android.os.Bundle;

import com.example.mealmap.Adapters.GroceryAdapter;
import com.example.mealmap.Listeners.RecipeDetailsListener;
import com.example.mealmap.Models.ExtendedIngredient;
import com.example.mealmap.Models.RecipeDetailsResponse;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class GroceryListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private GroceryAdapter adapter;
    private ProgressDialog progressDialog;
    private Map<String, ExtendedIngredient> combinedIngredients = new HashMap<>();
    private String UID;

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

        fetchMealPlanRecipes();
    }

    private void fetchMealPlanRecipes() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users").child(UID).child("MealPlan");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<Integer> uniqueRecipeIds = new HashSet<>();

                for (DataSnapshot daySnapshot : snapshot.getChildren()) {
                    for (DataSnapshot recipeSnapshot : daySnapshot.getChildren()) {
                        Integer id = recipeSnapshot.getValue(Integer.class);
                        if (id != null) uniqueRecipeIds.add(id);
                    }
                }

                fetchRecipeDetails(new ArrayList<>(uniqueRecipeIds));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
                Toast.makeText(GroceryListActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecipeDetails(List<Integer> recipeIds) {
        RequestManager manager = new RequestManager(GroceryListActivity.this);
        AtomicInteger counter = new AtomicInteger(0);

        for (Integer id : recipeIds) {
            manager.getRecipeDetails(new RecipeDetailsListener() {
                @Override
                public void didFetch(RecipeDetailsResponse response, String message) {
                    combineIngredients(response.extendedIngredients);
                    if (counter.incrementAndGet() == recipeIds.size()) {
                        progressDialog.dismiss();
                        updateUI();
                    }
                }

                @Override
                public void didError(String message) {
                    Toast.makeText(GroceryListActivity.this, "Failed to fetch recipe: " + message, Toast.LENGTH_SHORT).show();
                    if (counter.incrementAndGet() == recipeIds.size()) {
                        progressDialog.dismiss();
                        updateUI();
                    }
                }
            }, id);
        }
    }

    private void combineIngredients(List<ExtendedIngredient> ingredients) {
        for (ExtendedIngredient ingredient : ingredients) {
            String key = ingredient.name.toLowerCase() + "|" + ingredient.unit.toLowerCase();

            if (combinedIngredients.containsKey(key)) {
                ExtendedIngredient existing = combinedIngredients.get(key);
                existing.amount += ingredient.amount;
            } else {
                combinedIngredients.put(key, ingredient);
            }
        }
    }

    private void updateUI() {
        List<ExtendedIngredient> groceryList = new ArrayList<>(combinedIngredients.values());
        adapter = new GroceryAdapter(groceryList);
        recyclerView.setAdapter(adapter);
    }
}