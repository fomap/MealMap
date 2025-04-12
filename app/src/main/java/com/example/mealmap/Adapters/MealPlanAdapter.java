package com.example.mealmap.Adapters;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmap.Models.RecipeDetailsResponse;
import com.example.mealmap.R;
import com.example.mealmap.RecipeDetailsActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.widget.Toast;

public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanViewHolder> {

    Context context;
    List<RecipeDetailsResponse> list;

    List<String> firebaseKeys;
    String currentDay;
    String UID;

    DatabaseReference rootDatabaseref;

    public MealPlanAdapter(Context context, List<RecipeDetailsResponse> list, List<String> firebaseKeys, String currentDay, String UID) {
        this.context = context;
        this.list = list;
        this.firebaseKeys = firebaseKeys;
        this.currentDay = currentDay;
        this.UID = UID;
    }

//    public MealPlanAdapter(List<RecipeDetailsResponse> list, List<String> firebaseKeys, String currentDay, String UID) {
//        this.list = list;
//        this.firebaseKeys = firebaseKeys;
//        this.currentDay = currentDay;
//        this.UID = UID;
//    }

    @NonNull
    @Override
    public MealPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MealPlanViewHolder(
                LayoutInflater.from(context).inflate(R.layout.item_meal_plan, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MealPlanViewHolder holder, int position) {
        RecipeDetailsResponse recipe = list.get(position);
        holder.mealName.setText(recipe.title);
        Picasso.get().load(recipe.image).into(holder.mealImage);


        holder.btnDelete.setOnClickListener(view -> {
            Log.d("MealPlanAdapter", "btnDelete clicked");

            int currentPosition = holder.getAdapterPosition();
            Log.d("MealPlanAdapter", "Current position: " + currentPosition);
            if(currentPosition != RecyclerView.NO_POSITION) {
                removeRecipe(currentPosition);
            } else {
                Log.w("MealPlanAdapter", "Invalid position: NO_POSITION"); // Log if position is invalid
            }

            rootDatabaseref = FirebaseDatabase.getInstance().getReference().child("Users");
            rootDatabaseref.child(UID).child("MealPlan").child(currentDay).child(firebaseKeys.get(position)).setValue(null);
        });

//        holder.btnDelete.setOnClickListener(v -> {

//            rootDatabaseref = FirebaseDatabase.getInstance().getReference().child("Users");
//            rootDatabaseref.child(UID).child("MealPlan").child(currentDay).child(firebaseKeys.get(position)).setValue(null);


//            int currentPosition = holder.getAdapterPosition();
//            removeRecipe(currentPosition);
//            if(currentPosition != RecyclerView.NO_POSITION) {
//                removeRecipe(currentPosition);
//            }
//        });

        holder.btnDetails.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if(currentPosition != RecyclerView.NO_POSITION) {
                RecipeDetailsResponse selectedRecipe = list.get(currentPosition);
                Intent intent = new Intent(context, RecipeDetailsActivity.class);
                intent.putExtra("id", String.valueOf(selectedRecipe.id));
                context.startActivity(intent);
            }
        });
    }

    private void removeRecipe(int position) {

        Log.d("MealPlanAdapter", "removeRecipe called for position: " + position);
        if (position < 0 || position >= firebaseKeys.size()) {
            Log.w("MealPlanAdapter", "Invalid position. Returning.");
            return;
        }


        if (!isFragmentActive()) return;

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(UID)
                .child("MealPlan")
                .child(currentDay)
                .child(firebaseKeys.get(position));

        ref.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful() && isFragmentActive()) {
                Log.d("MealPlanAdapter", "Firebase remove successful");

                list.remove(position);
                firebaseKeys.remove(position);

                Toast.makeText(context, "Recipe removed", Toast.LENGTH_SHORT).show();

        
                ((AppCompatActivity) context).runOnUiThread(() -> {
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, getItemCount());
                });
            }
            else {
                Toast.makeText(context, "Recipe removed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isFragmentActive() {
        return context instanceof AppCompatActivity &&
                !((AppCompatActivity) context).isFinishing();
    }

    private boolean isInvalidPosition(int position) {
        return position < 0 || position >= firebaseKeys.size() || position >= list.size();
    }
    public void clearAllRecipes() {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(UID)
                .child("MealPlan")
                .child(currentDay);

        ref.removeValue().addOnCompleteListener(task -> {
            if(task.isSuccessful()) {
                int size = list.size();
                list.clear();
                firebaseKeys.clear();
                notifyItemRangeRemoved(0, size);
                Toast.makeText(context, "Meal plan cleared", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
//    public MealPlanAdapter(Context context, List<RecipeDetailsResponse> list) {
//        this.context = context;
//        this.list = list;
//    }
//
//    @NonNull
//    @Override
//    public MealPlanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        return new MealPlanViewHolder(
//                LayoutInflater.from(context).inflate(R.layout.item_meal_plan, parent, false)
//        );
//    }

//    @Override
//    public void onBindViewHolder(@NonNull MealPlanViewHolder holder, int position) {
//        RecipeDetailsResponse recipe = list.get(position);
//        holder.mealName.setText(recipe.title);
//        Picasso.get().load(recipe.image).into(holder.mealImage);
//    }


}

class MealPlanViewHolder extends RecyclerView.ViewHolder {
    ImageView mealImage;
    TextView mealName;
    Button btnDelete, btnDetails;

    public MealPlanViewHolder(@NonNull View itemView) {
        super(itemView);
        mealImage = itemView.findViewById(R.id.imageView_meal);
        mealName = itemView.findViewById(R.id.textView_meal_name);
        btnDelete = itemView.findViewById(R.id.btn_delete);
        btnDetails = itemView.findViewById(R.id.btn_details);
    }
}

