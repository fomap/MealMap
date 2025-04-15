package com.example.mealmap.GroceryList;

import android.content.Intent;
import android.os.Bundle;

import com.example.mealmap.Adapters.GroceryAdapter;
import com.example.mealmap.MainActivity;
import com.example.mealmap.MealPlanning.MealPlanningActivity;
import com.example.mealmap.Models.ExtendedIngredient;
import com.example.mealmap.Playlist.PlaylistActivity;
import com.example.mealmap.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GroceryListActivity extends AppCompatActivity implements GrocerySelectorDialog.GrocerySelectionListener {
    private RecyclerView recyclerView;
    private GroceryAdapter adapter;
    private ProgressBar progressBar;
    private TextView txtEmptyState;
    private Button btnReselect;
    BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

        btnReselect = findViewById(R.id.btn_reselect);
        btnReselect.setOnClickListener(v -> showSelectionDialog());

        recyclerView = findViewById(R.id.recycler_grocery);
        progressBar = findViewById(R.id.progress_bar);
        txtEmptyState = findViewById(R.id.txt_empty_state);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GroceryAdapter(new ArrayList<>(), this);
        recyclerView.setAdapter(adapter);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigation();

//        showSelectionDialog();

    }

    private void setupBottomNavigation() {
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

            if (targetActivity != null && !this.getClass().equals(targetActivity)) {
                navigateToActivity(targetActivity);
            }
            return true;
        });

    }

    private void navigateToActivity(Class<?> targetActivity) {
        Intent intent = new Intent(this, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private void showSelectionDialog() {
        GrocerySelectorDialog dialog = new GrocerySelectorDialog();
        dialog.show(getSupportFragmentManager(), "grocery_selector");
    }

    @Override
    public void onGroceryListGenerated(List<ExtendedIngredient> ingredients) {
        adapter.updateList(new ArrayList<>());
        updateUI(ingredients);
    }

    private void updateUI(List<ExtendedIngredient> ingredients) {
        if (ingredients.isEmpty()) {
            txtEmptyState.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            txtEmptyState.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updateList(ingredients);
        }
    }
    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }



}

