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

