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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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


    public MealPlanAdapter(List<RecipeDetailsResponse> list, List<String> firebaseKeys, Context context, String UID, String currentDay) {
        this.list = list;
        this.firebaseKeys = firebaseKeys;
        this.context = context;
        this.UID = UID;
        this.currentDay = currentDay;
    }
//    public MealPlanAdapter(List<RecipeDetailsResponse> list, List<String> firebaseKeys, Context context) {
//        this.list = list;
//        this.firebaseKeys = firebaseKeys;
//        this.context = context;
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

        holder.btnDelete.setOnClickListener(v -> {
            int currentPosition = holder.getBindingAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                removeRecipeNew(currentPosition);
            }
        });
    }

    //holder.btnDelete.setOnClickListener(v -> removeRecipeNew(position));

//        holder.btnDelete.setOnClickListener(view -> {
           // Log.d("MealPlanAdapter", "btnDelete clicked");
//
//            int currentPosition = holder.getAdapterPosition();
//            if(currentPosition != RecyclerView.NO_POSITION) {
//
//                removeRecipeNew(position);
//
//            }
//        });

//        holder.btnDetails.setOnClickListener(v -> {
//            int currentPosition = holder.getAdapterPosition();
//            if(currentPosition != RecyclerView.NO_POSITION) {
//                RecipeDetailsResponse selectedRecipe = list.get(currentPosition);
//                Intent intent = new Intent(context, RecipeDetailsActivity.class);
//                intent.putExtra("id", String.valueOf(selectedRecipe.id));
//               // mealplanmadapter.notifyDataSetChanged();
//                context.startActivity(intent);
//
//            }
//        });

//    private void removeRecipeNew(int position) {
//        // 1. Validate position
//        if (position < 0 || position >= firebaseKeys.size()) {
//            Log.e("DELETE", "Invalid position: " + position);
//            return;
//        }
//
//        // 2. Get fresh authentication
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//        if (user == null || user.getUid() == null) {
//            Toast.makeText(context, "Not authenticated!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        String currentUid = user.getUid();
//
//        // 3. Get the push ID/key that was generated when saving
//        String pushKey = firebaseKeys.get(position);
//        if (pushKey == null || pushKey.isEmpty()) {
//            Toast.makeText(context, "Invalid recipe key", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
//                .child("users")
//                .child(currentUid)
//                .child("MealPlan")
//                .child(currentDay)
//                .child(pushKey);
//
//        // 5. Add debug logging
//        Log.d("DELETE_DEBUG", "Attempting to delete at path: users/" + currentUid
//                + "/MealPlan/" + currentDay + "/" + pushKey);
//
//        // 6. Execute deletion
//        ref.removeValue()
//                .addOnSuccessListener(aVoid -> {
//                    // Only modify local lists after successful deletion
//                    list.remove(position);
//                    firebaseKeys.remove(position);
//                    notifyItemRemoved(position);
//                    Toast.makeText(context, "Recipe removed successfully", Toast.LENGTH_SHORT).show();
//                })
//                .addOnFailureListener(e -> {
////                    Log.e("DELETE_ERROR", " + e.getMessage());
//                            Toast.makeText(context, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
//                });
//    }

private void removeRecipeNew3(int position) {
    // Validate position
    if (position < 0 || position >= firebaseKeys.size() || position >= list.size()) {
        Toast.makeText(context, "Invalid position", Toast.LENGTH_SHORT).show();
        return;
    }

    // Get fresh authentication
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    if (user == null || user.getUid() == null) {
        Toast.makeText(context, "Not authenticated!", Toast.LENGTH_SHORT).show();
        return;
    }

    String pushKey = firebaseKeys.get(position);
    if (pushKey == null || pushKey.isEmpty()) {
        Toast.makeText(context, "Invalid recipe key", Toast.LENGTH_SHORT).show();
        return;
    }

    // Debug logging
    Log.d("DELETE_DEBUG", "Deleting at: users/" + user.getUid() +
            "/MealPlan/" + currentDay + "/" + pushKey);

    // Execute deletion
    FirebaseDatabase.getInstance().getReference()
            .child("users")
            .child(user.getUid())
            .child("MealPlan")
            .child(currentDay)
            .child(pushKey)
            .removeValue()
            .addOnSuccessListener(aVoid -> {
                list.remove(position);
                firebaseKeys.remove(position);
                notifyItemRemoved(position);
                Toast.makeText(context, "Recipe removed", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(context, "Delete failed: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
                Log.e("DELETE_ERROR", e.getMessage(), e);
            });
}

    private void removeRecipeNew2(int position) {
        // Validate position
        if (position < 0 || position >= firebaseKeys.size()) {
            return;
        }

        // Get Firebase reference to the specific recipe
        String pushKey = firebaseKeys.get(position);
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(UID)
                .child("MealPlan")
                .child(currentDay)
                .child(pushKey);

        // Perform the delete operation
        ref.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Remove from local data
                list.remove(position);
                firebaseKeys.remove(position);

                // Notify RecyclerView adapter of the removed item
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, getItemCount());
                this.notifyDataSetChanged();
                Toast.makeText(context, "Recipe removed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to remove recipe", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void removeRecipeNew(int position) {
//
//      //  Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show();
//
//        if (position < 0 || position >= firebaseKeys.size()) { return;}
//
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
//                .child("users")
//                .child(UID)
//                .child("MealPlan")
//                .child(currentDay)
//                .child(firebaseKeys.get(position));
//
//        ref.removeValue().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                list.remove(position);
//                firebaseKeys.remove(position);
//                notifyItemRemoved(position);
//                Toast.makeText(context, "Recipe removed", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(context, "Failed to remove recipe", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

//    private void removeRecipeNew(int position) {
//        if (position < 0 || position >= list.size() || position >= firebaseKeys.size()) {
//            Toast.makeText(context, "Invalid position", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        String firebaseKey = firebaseKeys.get(position);
//        if (firebaseKey == null || firebaseKey.isEmpty()) {
//            Toast.makeText(context, "Invalid Firebase key", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
//                .child("users")
//                .child(UID)
//                .child("MealPlan")
//                .child(currentDay)
//                .child(firebaseKey);
//
//        ref.removeValue().addOnCompleteListener(task -> {
//            if (task.isSuccessful()) {
//                // Remove from both lists
//                list.remove(position);
//                firebaseKeys.remove(position);
//                // Notify adapter
//                notifyItemRemoved(position);
//                notifyItemRangeChanged(position, getItemCount());
//                Toast.makeText(context, "Recipe removed", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(context, "Failed to remove recipe: " + task.getException(),
//                        Toast.LENGTH_SHORT).show();
//            }
//        });
//    }

    private void removeRecipeNew(int position) {
        // Validate position
        if (position < 0 || position >= firebaseKeys.size()) {
            return;
        }

        // Store the key before removal
        String keyToRemove = firebaseKeys.get(position);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(UID)
                .child("MealPlan")
                .child(currentDay)
                .child(keyToRemove);

        ref.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Remove from both lists
                list.remove(position);
                firebaseKeys.remove(position);


                notifyItemRemoved(position);
           

                Toast.makeText(context, "Recipe removed", Toast.LENGTH_SHORT).show();
                Log.d("MealPlanAdapter", "Recipe removed");
            } else {
                Toast.makeText(context, "Failed to remove recipe: " +
                        task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void removeRecipeNewLowkeyWorking(int position) {

        Toast.makeText(context, "Hello", Toast.LENGTH_SHORT).show();

        if (position < 0 || position >= firebaseKeys.size()) { return;}

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(UID)
                .child("MealPlan")
                .child(currentDay)
                .child(firebaseKeys.get(position));


        ref.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                list.remove(position);
                firebaseKeys.remove(position);
                notifyItemRemoved(position);
              //  this.notifyDataSetChanged();
                Toast.makeText(context, "Recipe removed", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to remove recipe", Toast.LENGTH_SHORT).show();
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

