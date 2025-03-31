package com.example.mealmap;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.mealmap.ui.main.SectionsPagerAdapter;
import com.example.mealmap.databinding.ActivityMealPlanBinding;

public class MealPlanActivity extends AppCompatActivity {

    private ActivityMealPlanBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_meal_plan);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = binding.viewPager;
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = binding.tabs;
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = binding.fab;

        BottomNavigationView bottomNavigationView = findViewById(R.id.meal_plan_bottomNavigationView);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.bottomNav_today) {
                startActivity(new Intent(MealPlanActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
//            else if (itemId == R.id.bottomNav_mealPlan) {
//                startActivity(new Intent(MainActivity.this, MealPlanActivity.class));
//                overridePendingTransition(0, 0);
//                return true;
//            }
            else if (itemId == R.id.bottomNav_playlist) {
                startActivity(new Intent(MealPlanActivity.this, PlaylistActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
//            else if (itemId == R.id.bottomNav_groceryList) {
//                startActivity(new Intent(MainActivity.this, GroceryListActivity.class));
//                overridePendingTransition(0, 0);
//                return true;
//            }

            return true;
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .setAnchorView(R.id.fab).show();
            }
        });
    }
}