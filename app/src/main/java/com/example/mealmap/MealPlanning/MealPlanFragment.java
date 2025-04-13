package com.example.mealmap.MealPlanning;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mealmap.Adapters.MealPlanAdapter;
import com.example.mealmap.Listeners.RecipeDetailsListener;
import com.example.mealmap.Models.RecipeDetailsResponse;
import com.example.mealmap.R;
import com.example.mealmap.RecipeDetailsActivity;
import com.example.mealmap.RequestManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


public class MealPlanFragment extends Fragment {


    private RecyclerView recyclerView;
    private MealPlanAdapter adapter;
    private List<RecipeDetailsResponse> recipes = new ArrayList<>();
    private List<String> firebaseKeys = new ArrayList<>();
    Button btnClear;


    private String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();

    private LinearLayout mealsContainer;
    private String day;
    private DatabaseReference mealsRef;

    public static MealPlanFragment newInstance(String day) {
        MealPlanFragment fragment = new MealPlanFragment();
        Bundle args = new Bundle();
        args.putString("day", day);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            day = getArguments().getString("day");
        }
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) UID = user.getUid();

        mealsRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(UID).child("MealPlan").child(day);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_meal_plan, container, false);
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mealsContainer = view.findViewById(R.id.mealsContainer);
        Button btnClear = view.findViewById(R.id.btn_clear);

        btnClear.setOnClickListener(v -> clearAllRecipes());
        setupMealsListener();
    }


    private void clearAllRecipes() {
        mealsRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                mealsContainer.removeAllViews();
                Toast.makeText(getContext(), "All meals cleared", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupMealsListener() {
        mealsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!isAdded() || getContext() == null) return;

                mealsContainer.removeAllViews();
                for (DataSnapshot mealSnapshot : snapshot.getChildren()) {
                    addMealView(mealSnapshot);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMealView(DataSnapshot mealSnapshot) {

        if (!isAdded() || getContext() == null) {
            return;
        }

        View mealView = LayoutInflater.from(getContext())
                .inflate(R.layout.item_meal_simple, mealsContainer, false);

        TextView title = mealView.findViewById(R.id.mealTitle);
        ImageView image = mealView.findViewById(R.id.imageView_meal);
        Button btnDelete = mealView.findViewById(R.id.btn_delete);
        Button btnDetails = mealView.findViewById(R.id.btn_details);

        // Get values from snapshot
        String mealTitle = mealSnapshot.child("title").getValue(String.class);
        String imageUrl = mealSnapshot.child("image").getValue(String.class);
        String pushKey = mealSnapshot.getKey();

        title.setText(mealTitle);
        Picasso.get().load(imageUrl).into(image);

        // Set delete click listener
        btnDelete.setOnClickListener(v -> {
            mealsRef.child(pushKey).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        mealsContainer.removeView(mealView);
                        Toast.makeText(getContext(), "Meal removed", Toast.LENGTH_SHORT).show();
                    });
        });


        btnDetails.setOnClickListener(view -> {
            if (!isAdded() || getContext() == null) return;

            Long recipeId = mealSnapshot.child("id").getValue(Long.class);
            if (recipeId != null) {
                Intent intent = new Intent(requireContext(), RecipeDetailsActivity.class);

                intent.putExtra("id", recipeId.toString());
                intent.putExtra("source", "mealPlan");
                intent.putExtra("day", day);

                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Error: Recipe data not available", Toast.LENGTH_SHORT).show();
            }
        });


        mealsContainer.addView(mealView);
    }



    private void clearAllTodayRecipes() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(UID)
                .child("MealPlan")
                .child(day);

        ref.removeValue().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                recipes.clear();
                firebaseKeys.clear();
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), "Meal plan cleared", Toast.LENGTH_SHORT).show();
            }
        });
    }


//    private void clearAllRecipes() {
//        mDatabase.child("users").child(UID).child("MealPlan").child(currentDay).removeValue()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        recipes.clear();
//                        firebaseKeys.clear();
//                        adapter.notifyDataSetChanged();
//                        Toast.makeText(getContext(), "All meals removed.", Toast.LENGTH_SHORT).show();
//                    } else {
//                        Toast.makeText(getContext(), "Failed to clear meals.", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }


    private void loadMeals() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users").child(UID).child("MealPlan").child(day);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipes.clear();
                firebaseKeys.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    firebaseKeys.add(ds.getKey());

                    Long id = ds.child("id").getValue(Long.class);
                    String title = ds.child("title").getValue(String.class);
                    String image = ds.child("image").getValue(String.class);

                    if (id != null && title != null && image != null) {
                        RecipeDetailsResponse recipe = new RecipeDetailsResponse();
                        recipe.id = id.intValue();
                        recipe.title = title;
                        recipe.image = image;

                        recipes.add(recipe);
                    }
                }

                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

