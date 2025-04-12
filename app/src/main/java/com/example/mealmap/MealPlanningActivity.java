package com.example.mealmap;
import android.content.Intent;
import android.os.Bundle;
import com.example.mealmap.Adapters.ViewPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayoutMediator;


public class MealPlanningActivity extends AppCompatActivity {


    TabLayout tabLayout;
    ViewPager2 viewPager2;
    BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meal_planning);

        viewPager2 = findViewById(R.id.viewpager);
        tabLayout = findViewById(R.id.tab_layout);

        viewPager2.setAdapter(new ViewPagerAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            String[] days = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
            tab.setText(days[position]);
        }).attach();

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {

            int itemId = item.getItemId();

            if (itemId == R.id.bottomNav_today) {
                startActivity(new Intent(MealPlanningActivity.this, MainActivity.class));
                overridePendingTransition(0, 0);
                return true;
            } else if (itemId == R.id.bottomNav_groceryList) {
//                startActivity(new Intent(MealPlanningActivity.this, MealPlanningActivity.class));
////                startActivity(new Intent(MainActivity.this, MealPlanningActivityTest.class));
//                overridePendingTransition(0, 0);
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
