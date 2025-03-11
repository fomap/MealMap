package com.example.mealmap;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.Toast;

import com.example.mealmap.Adapters.RandomRecipeAdapter;
import com.example.mealmap.Listeners.RandomRecipeResponseListener;
import com.example.mealmap.Models.RandomRecipeApiResponse;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmap.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

     ActivityMainBinding binding;

    ProgressDialog dialog;
    RequestManager manager;
    RandomRecipeAdapter randomRecipeAdapter;
    RecyclerView recyclerView;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
//        setContentView(R.layout.activity_main);

        setContentView(binding.getRoot());

        //replaceFragment(new MainActivity());
        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.bottomNav_today) {
//                Toast.makeText(this, "Today", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.bottomNav_mealPlan) {
               // replaceFragment(new Meal_Plan_Fragment());
            } else if (itemId == R.id.bottomNav_groceryList) {
                Toast.makeText(this, "Grocery List", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.bottomNav_chat) {
                Toast.makeText(this, "Chat", Toast.LENGTH_SHORT).show();
            }

            return true; // Return true to indicate the item is selected
        });

     //   BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);


//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
//                .build();
//        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
//        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
//        NavigationUI.setupWithNavController(binding.navView, navController);

        dialog = new ProgressDialog(this);
        dialog.setTitle("Loading...");

        recyclerView = findViewById(R.id.recycler_random);
        manager = new RequestManager(this);
        manager.getRandomRecipes(randomRecipeResponseListener);
        dialog.show();

    }


    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout,fragment);
        fragmentTransaction.commit();
    }
    private final RandomRecipeResponseListener randomRecipeResponseListener = new RandomRecipeResponseListener() {
        @Override
        public void didFetch(RandomRecipeApiResponse response, String message) {

          dialog.dismiss();
          recyclerView.findViewById(R.id.recycler_random);
          recyclerView.setHasFixedSize(true);
          recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, 1));
          randomRecipeAdapter = new RandomRecipeAdapter(MainActivity.this, response.recipes);
          recyclerView.setAdapter(randomRecipeAdapter);
        }

        @Override
        public void didError(String message) {
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    };




}