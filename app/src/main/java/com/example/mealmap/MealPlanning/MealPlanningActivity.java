package com.example.mealmap.MealPlanning;
import android.content.Intent;
import android.os.Bundle;
import com.example.mealmap.Adapters.ViewPagerAdapter;
import com.example.mealmap.GroceryListActivity;
import com.example.mealmap.MainActivity;
import com.example.mealmap.PlaylistActivity;
import com.example.mealmap.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Arrays;


public class MealPlanningActivity extends AppCompatActivity {


    TabLayout tabLayout;
    ViewPager2 viewPager2;
    BottomNavigationView bottomNavigationView;

    private final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday",
            "Friday", "Saturday", "Sunday"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planning);

        viewPager2 = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tab_layout);

        viewPager2.setAdapter(new ViewPagerAdapter(this));


        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            String[] tabDays = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            tab.setText(tabDays[position]);
        }).attach();

        if (getIntent() != null && getIntent().hasExtra("selectedDay")) {
            String selectedDay = getIntent().getStringExtra("selectedDay");
            int position = Arrays.asList(days).indexOf(selectedDay);
            if (position >= 0) {
                viewPager2.setCurrentItem(position, false);
            }
        }


        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {


            int itemId = item.getItemId();

            if (itemId == R.id.bottomNav_today) {
                startActivity(new Intent(MealPlanningActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.bottomNav_groceryList) {
             //   startActivity(new Intent(MealPlanningActivity.this, GroceryListActivity.class));
//                startActivity(new Intent(MainActivity.this, MealPlanningActivityTest.class));
              //  overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.bottomNav_playlist) {
                startActivity(new Intent(MealPlanningActivity.this, PlaylistActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }
            return true;
        });
    }
}
