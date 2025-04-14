package com.example.mealmap.MealPlanning;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.mealmap.R;
import com.example.mealmap.RecipeDetailsActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;


public class MealPlanFragment extends Fragment {
    Button btnClear;
    private LinearLayout mealsContainer;
    private String day;
    private DatabaseReference mealsRef;

    private static final String ARG_COLLECTION_TYPE = "collectionType";
    private static final String ARG_COLLECTION_KEY = "collectionKey";

    private String collectionType;
    private String collectionKey;



    public static MealPlanFragment newInstance(String collectionType, String collectionKey) {
        MealPlanFragment fragment = new MealPlanFragment();
        Bundle args = new Bundle();
        args.putString(ARG_COLLECTION_TYPE, collectionType);
        args.putString(ARG_COLLECTION_KEY, collectionKey);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            collectionType = getArguments().getString(ARG_COLLECTION_TYPE);
            collectionKey = getArguments().getString(ARG_COLLECTION_KEY);
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mealsRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(user.getUid())
                    .child(collectionType)
                    .child(collectionKey);
        }
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


        btnClear = view.findViewById(R.id.btn_clear);
        mealsContainer = view.findViewById(R.id.mealsContainer);

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

        View mealView = LayoutInflater.from(requireContext())
                .inflate(R.layout.item_meal_simple, mealsContainer, false);

        TextView title = mealView.findViewById(R.id.mealTitle);
        ImageView image = mealView.findViewById(R.id.imageView_meal);
        Button btnDelete = mealView.findViewById(R.id.btn_delete);
        Button btnDetails = mealView.findViewById(R.id.btn_details);

        String mealTitle = mealSnapshot.child("title").getValue(String.class);
        String imageUrl = mealSnapshot.child("image").getValue(String.class);
        String pushKey = mealSnapshot.getKey();

        title.setText(mealTitle);
        Picasso.get().load(imageUrl).into(image);

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
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
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


    public void refreshReference(String newCollectionKey) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            mealsRef = FirebaseDatabase.getInstance().getReference()
                    .child("users")
                    .child(user.getUid())
                    .child(collectionType)
                    .child(newCollectionKey);

            setupMealsListener();
        }
    }

}

