package com.example.mealmap.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmap.Models.RecipeDetailsResponse;
import com.example.mealmap.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;

public class MealPlanAdapter extends RecyclerView.Adapter<MealPlanViewHolder> {

    Context context;
    List<RecipeDetailsResponse> list;

    public MealPlanAdapter(Context context, List<RecipeDetailsResponse> list) {
        this.context = context;
        this.list = list;
    }

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
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}

class MealPlanViewHolder extends RecyclerView.ViewHolder {
    ImageView mealImage;
    TextView mealName;

    public MealPlanViewHolder(@NonNull View itemView) {
        super(itemView);
        mealImage = itemView.findViewById(R.id.imageView_meal);
        mealName = itemView.findViewById(R.id.textView_meal_name);
    }
}


