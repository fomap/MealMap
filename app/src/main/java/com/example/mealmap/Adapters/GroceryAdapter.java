package com.example.mealmap.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mealmap.Models.ExtendedIngredient;
import com.example.mealmap.R;

import java.util.List;
import java.util.Locale;

public class GroceryAdapter extends RecyclerView.Adapter<GroceryViewHolder> {
    private List<ExtendedIngredient> groceries;

    public GroceryAdapter(List<ExtendedIngredient> groceries) {
        this.groceries = groceries;
    }

    @NonNull
    @Override
    public GroceryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroceryViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_grocery_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryViewHolder holder, int position) {
        ExtendedIngredient ingredient = groceries.get(position);
        holder.name.setText(ingredient.name);
        holder.quantity.setText(formatQuantity(ingredient));
    }

    private String formatQuantity(ExtendedIngredient ingredient) {
        return String.format(Locale.getDefault(), "%.2f %s", ingredient.amount, ingredient.unit);
    }

    @Override
    public int getItemCount() {
        return groceries.size();
    }
}

class GroceryViewHolder extends RecyclerView.ViewHolder {
    TextView name, quantity;

    public GroceryViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.text_grocery_name);
        quantity = itemView.findViewById(R.id.text_grocery_quantity);
    }
}