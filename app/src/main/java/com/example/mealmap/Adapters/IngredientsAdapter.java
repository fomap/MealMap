package com.example.mealmap.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.mealmap.Models.ExtendedIngredient;
import com.example.mealmap.PreferenceManager;
import com.example.mealmap.R;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class IngredientsAdapter extends RecyclerView.Adapter<IngredientsViewHolder>{
    Context context;
    List<ExtendedIngredient> list;
    public IngredientsAdapter(Context context, List<ExtendedIngredient> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public IngredientsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new IngredientsViewHolder(LayoutInflater.from(context).inflate(R.layout.list_meal_ingredients, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientsViewHolder holder, int position) {
        ExtendedIngredient ingredient = list.get(position);
        holder.textView_ingredients_name.setText(ingredient.name);
        holder.textView_ingredients_name.setSelected(true);


        if (ingredient.measures != null) {
            boolean isMetric = PreferenceManager.isMetric(context);
            String quantity;
            if (isMetric && ingredient.measures.metric != null) {
                quantity = ingredient.measures.metric.amount + " " + ingredient.measures.metric.unitShort;
            } else if (!isMetric && ingredient.measures.us != null) {
                quantity = ingredient.measures.us.amount + " " + ingredient.measures.us.unitShort;
            } else {
                quantity = ingredient.original;
            }
            holder.textView_ingredients_quantity.setText(quantity);
        } else {
            holder.textView_ingredients_quantity.setText(ingredient.original);
        }


        holder.textView_ingredients_quantity.setSelected(true);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }
}


class IngredientsViewHolder extends RecyclerView.ViewHolder {
    TextView textView_ingredients_name, textView_ingredients_quantity;
    public IngredientsViewHolder(@NonNull View itemView) {
        super(itemView);
        textView_ingredients_name = itemView.findViewById(R.id.textView_ingredients_name);
        textView_ingredients_quantity = itemView.findViewById(R.id.textView_ingredients_quantity);

    }
}
