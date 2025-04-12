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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class MealPlanFragment extends Fragment {

    private String day;
    private RecyclerView recyclerView;
    private MealPlanAdapter adapter;
    private List<RecipeDetailsResponse> recipes = new ArrayList<>();
    private List<String> firebaseKeys = new ArrayList<>();
    private String UID;

    Button btnClear;
    DatabaseReference rootDatabaseref;

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

       // rootDatabaseref = FirebaseDatabase.getInstance().getReference().child("Users");



    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal_plan, container, false);
        recyclerView = view.findViewById(R.id.recycler_meal_plan);
        btnClear = view.findViewById(R.id.btn_clear);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MealPlanAdapter(getContext(), recipes, firebaseKeys, day, UID);
        recyclerView.setAdapter(adapter);

        btnClear.setOnClickListener(v -> {
            adapter.clearAllRecipes();
        });

        btnClear.setOnClickListener(v -> clearAllRecipes());
//
//
//
//        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
//                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
//
//            @Override
//            public boolean onMove(@NonNull RecyclerView recyclerView,
//                                  @NonNull RecyclerView.ViewHolder viewHolder,
//                                  @NonNull RecyclerView.ViewHolder target) {
//                return false; // We don't need drag & drop
//            }
//
//            @Override
//            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
//                int position = viewHolder.getAdapterPosition();
//
//                if (direction == ItemTouchHelper.LEFT) {
//                    adapter.removeRecipe(position);
//                } else if (direction == ItemTouchHelper.RIGHT) {
//                    RecipeDetailsResponse recipe = recipes.get(position);
//                    Intent intent = new Intent(getContext(), RecipeDetailsActivity.class);
//                    intent.putExtra("id", String.valueOf(recipe.id));
//                    startActivity(intent);
//                    adapter.notifyItemChanged(position);
//                }
//            }
//
//        }).attachToRecyclerView(recyclerView);
          loadMeals();
          return view;
    }

    private void clearAllRecipes() {
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

    private void loadMeals() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users").child(UID).child("MealPlan").child(day);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Integer> ids = new ArrayList<>();
                firebaseKeys.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    Integer id = ds.getValue(Integer.class);
                    if (id != null)
                    {
                        ids.add(id);
                        firebaseKeys.add(ds.getKey());
                    }
                }
                fetchRecipes(ids);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void fetchRecipes(List<Integer> ids) {
//        RequestManager manager = new RequestManager(getContext());
//        recipes.clear();
//
//        // Create a temporary list to preserve order
//        List<RecipeDetailsResponse> orderedRecipes = new ArrayList<>();
//        for (int i = 0; i < ids.size(); i++) {
//            orderedRecipes.add(null);
//        }
//
//        AtomicInteger counter = new AtomicInteger(0);
//
//        for (int i = 0; i < ids.size(); i++) {
//            int index = i;
//            int id = ids.get(i);
//
//            manager.getRecipeDetails(new RecipeDetailsListener() {
//                @Override
//                public void didFetch(RecipeDetailsResponse response, String message) {
//                    orderedRecipes.set(index, response);
//                    if (counter.incrementAndGet() == ids.size()) {
//                        recipes.clear();
//                        recipes.addAll(orderedRecipes);
//                        adapter.notifyDataSetChanged();
//                    }
//                }
//
//                @Override
//                public void didError(String message) {
//                    if (counter.incrementAndGet() == ids.size()) {
//                        recipes.clear();
//                        recipes.addAll(orderedRecipes);
//                        adapter.notifyDataSetChanged();
//                    }
//                    Toast.makeText(getContext(), "Failed to fetch recipe", Toast.LENGTH_SHORT).show();
//                }
//            }, id);
//        }
//    }

    private void fetchRecipes(List<Integer> ids) {
        RequestManager manager = new RequestManager(requireContext());
        recipes.clear();

        List<RecipeDetailsResponse> orderedRecipes = new ArrayList<>(Collections.nCopies(ids.size(), null));
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < ids.size(); i++) {
            int finalI = i;
            manager.getRecipeDetails(new RecipeDetailsListener() {
                @Override
                public void didFetch(RecipeDetailsResponse response, String message) {
                    if (!isAdded()) return; // Check fragment attachment

                    orderedRecipes.set(finalI, response);

                    if (counter.incrementAndGet() == ids.size()) {
                        requireActivity().runOnUiThread(() -> {
                            recipes.clear();
                            recipes.addAll(orderedRecipes);
                            adapter.notifyDataSetChanged();
                        });
                    }
                }

                @Override
                public void didError(String message) {
                    if (!isAdded()) return;

                    if (counter.incrementAndGet() == ids.size()) {
                        requireActivity().runOnUiThread(() -> {
                            recipes.clear();
                            recipes.addAll(orderedRecipes);
                            adapter.notifyDataSetChanged();
                        });
                    }
                }
            }, ids.get(finalI));
        }
    }
}