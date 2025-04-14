package com.example.mealmap.Adapters;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.mealmap.MealPlanning.MealPlanFragment;
import com.example.mealmap.R;

public class FragmentHostActivity extends AppCompatActivity {

    public static final String COLLECTION_TYPE = "collectionType";
    public static final String COLLECTION_KEY = "collectionKey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment_host);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        String collectionType = getIntent().getStringExtra(COLLECTION_TYPE);
        String collectionKey = getIntent().getStringExtra(COLLECTION_KEY);

        if (savedInstanceState == null) {
            MealPlanFragment fragment = MealPlanFragment.newInstance(collectionType, collectionKey);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
