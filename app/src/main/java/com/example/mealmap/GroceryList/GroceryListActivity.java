package com.example.mealmap.GroceryList;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import com.example.mealmap.Adapters.GroceryAdapter;
import com.example.mealmap.MainActivity;
import com.example.mealmap.MealPlanning.MealPlanningActivity;
import com.example.mealmap.Models.ExtendedIngredient;
import com.example.mealmap.Playlist.PlaylistActivity;
import com.example.mealmap.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GroceryListActivity extends AppCompatActivity implements GrocerySelectorDialog.GrocerySelectionListener {
    private RecyclerView recyclerView;
    private GroceryAdapter adapter;
    private ProgressBar progressBar;
    private TextView txtEmptyState;
    private Button btnReselect;
    private ImageButton btn_Share;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grocery_list);

        btnReselect = findViewById(R.id.btn_reselect);
        btnReselect.setOnClickListener(v -> showSelectionDialog());

        btn_Share = findViewById(R.id.btn_share);
        btn_Share.setOnClickListener(v -> shareGroceryList());

        recyclerView = findViewById(R.id.recycler_grocery);
        progressBar = findViewById(R.id.progress_bar);
        txtEmptyState = findViewById(R.id.txt_empty_state);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<ExtendedIngredient> savedIngredients = loadGroceriesFromPrefs();
        adapter = new GroceryAdapter(savedIngredients, this);
        recyclerView.setAdapter(adapter);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setupBottomNavigation();

        if (savedIngredients.isEmpty()) {
            showSelectionDialog();
        }
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
            if (targetActivity != null) {
                Intent intent = new Intent(getApplicationContext(), targetActivity);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT |
                                Intent.FLAG_ACTIVITY_SINGLE_TOP |
                                Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0);
            }
            return true;
        });

    }
    private void showSelectionDialog() {
        GrocerySelectorDialog dialog = new GrocerySelectorDialog();
        dialog.show(getSupportFragmentManager(), "grocery_selector");
    }
    private List<ExtendedIngredient> loadGroceriesFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("grocery_data", MODE_PRIVATE);
        String json = prefs.getString("saved_ingredients", null);
        if (json != null) {
            Type type = new TypeToken<List<ExtendedIngredient>>() {}.getType();
            return new Gson().fromJson(json, type);
        }
        return new ArrayList<>();
    }
    @Override
    public void onGroceryListGenerated(List<ExtendedIngredient> ingredients) {
        saveGroceriesToPrefs(new ArrayList<>());
        saveGroceriesToPrefs(ingredients);
        updateUI(ingredients);
    }
    private void saveGroceriesToPrefs(List<ExtendedIngredient> list) {
        SharedPreferences prefs = getSharedPreferences("grocery_data", MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(list);
        prefs.edit().putString("saved_ingredients", json).apply();
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

    @Override
    protected void onResume() {
        super.onResume();
        List<ExtendedIngredient> savedIngredients = loadGroceriesFromPrefs();
        updateUI(savedIngredients);
    }

    private void shareGroceryList() {
        List<ExtendedIngredient> ingredients = loadGroceriesFromPrefs();
        if (ingredients.isEmpty()) {
            Toast.makeText(this, "No ingredients to share!", Toast.LENGTH_SHORT).show();
            return;
        }
        String shareText = generateShareableText(ingredients);
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "My Grocery List");
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(shareIntent, "Share Grocery List"));
    }
    private String generateShareableText(List<ExtendedIngredient> ingredients) {
        StringBuilder sb = new StringBuilder();
        sb.append("Grocery list:\n\n");
        for (ExtendedIngredient ingredient : ingredients) {
            sb.append("- ")
                    .append(String.format("%.2f", ingredient.amount)) // 2 decimal places
                    .append(" ")
                    .append(ingredient.unit)
                    .append(" ")
                    .append(ingredient.name)
                    .append("\n");
        }
        sb.append("\nGenerated via MealMap ♥");
        return sb.toString();
    }


}

