package com.example.mealmap.MealPlanning;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.mealmap.Adapters.MealPlanAdapter;
import com.example.mealmap.Listeners.RecipeDetailsListener;
import com.example.mealmap.Models.RecipeDetailsResponse;
import com.example.mealmap.R;
import com.example.mealmap.RequestManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;


public class MealPlanFragment extends Fragment {

    private String day;
    private RecyclerView recyclerView;
    private MealPlanAdapter adapter;
    private List<RecipeDetailsResponse> recipes = new ArrayList<>();
    private String UID;

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meal_plan, container, false);
        recyclerView = view.findViewById(R.id.recycler_meal_plan);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MealPlanAdapter(getContext(), recipes);
        recyclerView.setAdapter(adapter);
        loadMeals();
        return view;
    }

    private void loadMeals() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users").child(UID).child("MealPlan").child(day);

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Integer> ids = new ArrayList<>();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    Integer id = ds.getValue(Integer.class);
                    if (id != null) ids.add(id);
                }
                fetchRecipes(ids);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecipes(List<Integer> ids) {
        RequestManager manager = new RequestManager(getContext());
        recipes.clear();
        for (int id : ids) {
            manager.getRecipeDetails(new RecipeDetailsListener() {
                @Override
                public void didFetch(RecipeDetailsResponse response, String message) {
                    recipes.add(response);
                    if (recipes.size() == ids.size()) adapter.notifyDataSetChanged();
                }

                @Override
                public void didError(String message) {
                    Toast.makeText(getContext(), "Failed to fetch recipe", Toast.LENGTH_SHORT).show();
                }
            }, id);
        }
    }
}