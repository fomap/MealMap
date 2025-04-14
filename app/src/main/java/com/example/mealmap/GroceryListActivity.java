package com.example.mealmap;

import android.app.Dialog;
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

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class GroceryListActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private GroceryAdapter adapter;
    private ProgressBar progressBar;
    private TextView txtEmptyState;
    private Map<String, ExtendedIngredient> combinedIngredients = new HashMap<>();
    private String UID;
    private RequestManager requestManager;

    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

        initializeViews();
        checkUserAuthentication();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recycler_grocery);
        progressBar = findViewById(R.id.progress_bar);
        txtEmptyState = findViewById(R.id.txt_empty_state);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroceryAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);
    }


    private void checkUserAuthentication() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        UID = user.getUid();

        showLoading(true);
        fetchMealPlanRecipes();
    }


    private void fetchMealPlanRecipes() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users").child(UID).child("mealPlan");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<Integer> uniqueRecipeIds = new HashSet<>();
                for (DataSnapshot daySnapshot : snapshot.getChildren()) {
                    for (DataSnapshot recipeSnapshot : daySnapshot.getChildren()) {
                        Map<String, Object> recipeData = (Map<String, Object>) recipeSnapshot.getValue();
                        Long idLong = (Long) recipeData.get("id");
                        if (idLong != null) {
                            uniqueRecipeIds.add(idLong.intValue());
                        }
                    }
                }

                fetchRecipeDetails(new ArrayList<>(uniqueRecipeIds));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                showError("Database error: " + error.getMessage());
            }
        });
    }

    private void fetchRecipeDetails(List<Integer> recipeIds) {
        if (recipeIds.isEmpty()) {
            updateUI();
            return;
        }

        requestManager = new RequestManager(this);
        AtomicInteger counter = new AtomicInteger(0);

        for (Integer id : recipeIds) {
            requestManager.getRecipeDetails(new RecipeDetailsListener() {
                @Override
                public void didFetch(RecipeDetailsResponse response, String message) {
                    combineIngredients(response.extendedIngredients);
                    checkCompletion(counter, recipeIds.size());
                }

                @Override
                public void didError(String message) {
                    Log.e("GroceryList", "Error fetching recipe: " + message);
                    checkCompletion(counter, recipeIds.size());
                }
            }, id);
        }
    }

    private void checkCompletion(AtomicInteger counter, int total) {
        if (counter.incrementAndGet() == total) {
            runOnUiThread(this::updateUI);
        }
    }

    private void combineIngredients(List<ExtendedIngredient> ingredients) {
        for (ExtendedIngredient ingredient : ingredients) {
            String key = ingredient.name.toLowerCase().replaceAll("s$", "") + "|" +
                    ingredient.unit.toLowerCase();

            if (combinedIngredients.containsKey(key)) {
                ExtendedIngredient existing = combinedIngredients.get(key);
                existing.amount += ingredient.amount;
            } else {
                combinedIngredients.put(key, ingredient);
            }
        }
    }

    private void updateUI() {
        showLoading(false);

        List<ExtendedIngredient> groceryList = new ArrayList<>(combinedIngredients.values());
        if (groceryList.isEmpty()) {
            txtEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            txtEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updateList(groceryList);
        }
    }


    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    private void showErrorAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finish();
    }

    private void showError(String message) {
        showLoading(false);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

}

