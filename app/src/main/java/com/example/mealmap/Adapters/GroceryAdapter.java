package com.example.mealmap.Adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.mealmap.Models.ExtendedIngredient;
import com.example.mealmap.R;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class GroceryAdapter extends RecyclerView.Adapter<GroceryViewHolder> {
    private Context context;
    private List<ExtendedIngredient> groceries;
    private Set<String> checkedItems = new HashSet<>();
    private SharedPreferences sharedPreferences;
    public GroceryAdapter(List<ExtendedIngredient> groceries, Context context) {
        this.context = context;
        this.groceries = groceries;
        this.sharedPreferences = context.getSharedPreferences("grocery_checks", Context.MODE_PRIVATE);
        loadCheckedStates();
        setHasStableIds(true);
    }
    private void loadCheckedStates() {
        checkedItems.addAll(sharedPreferences.getStringSet("checked_items", new HashSet<>()));
    }

    private void saveCheckedStates() {
        sharedPreferences.edit()
                .putStringSet("checked_items", checkedItems)
                .apply();
    }

    @NonNull
    @Override
    public GroceryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroceryViewHolder(LayoutInflater.from(context).inflate(R.layout.list_grocery_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull GroceryViewHolder holder, int position) {
        ExtendedIngredient ingredient = groceries.get(position);

        String normalizedUnit = normalizeUnit(ingredient.unit);
        String uniqueKey = ingredient.name.toLowerCase() + "|" + normalizedUnit.toLowerCase();
        String quantity = formatQuantity(ingredient);

        holder.name.setText(ingredient.name);
        holder.amount.setText(quantity);

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(checkedItems.contains(uniqueKey));
        applyStrikeThrough(holder, checkedItems.contains(uniqueKey));

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                checkedItems.add(uniqueKey);
            } else {
                checkedItems.remove(uniqueKey);
            }
            applyStrikeThrough(holder, isChecked);
            saveCheckedStates();
        });
    }

    private String formatQuantity(ExtendedIngredient ingredient) {
        String displayUnit = normalizeUnit(ingredient.unit);

        // "2 cups" vs "2.0 cups"
        if (ingredient.amount == (int) ingredient.amount) {
            return String.format(Locale.getDefault(), "%d %s",
                    (int) ingredient.amount,
                    displayUnit);
        }
        return String.format(Locale.getDefault(), "%.2f %s",
                ingredient.amount,
                displayUnit);
    }

    private String normalizeUnit(String originalUnit) {
        if (originalUnit == null) return "";

        return originalUnit.toLowerCase()
                .replaceAll("s$", "")
                .replaceAll("\\.$", "")
                .replace("tbsp", "tablespoon")
                .replace("tsp", "teaspoon")
                .replace("servings", "serving")
                .replace("grams", "gram")
                .replace("ounces", "ounce")
                .trim();
    }

    private void applyStrikeThrough(GroceryViewHolder holder, boolean isChecked) {
        if (isChecked) {
            holder.name.setPaintFlags(holder.name.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.amount.setPaintFlags(holder.amount.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.name.setPaintFlags(holder.name.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.amount.setPaintFlags(holder.amount.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }



    @Override
    public int getItemCount() {
        return groceries.size();
    }
    @Override
    public long getItemId(int position) {
        return groceries.get(position).id;
    }
    public void updateList(List<ExtendedIngredient> newList) {
        groceries = new ArrayList<>(newList);
        notifyDataSetChanged();
    }
}
class GroceryViewHolder extends RecyclerView.ViewHolder {
    CheckBox checkBox;
    TextView name, amount;
    public GroceryViewHolder(@NonNull View itemView) {
        super(itemView);
        checkBox = itemView.findViewById(R.id.check_ingredient);
        name = itemView.findViewById(R.id.text_ingredient_name);
        amount = itemView.findViewById(R.id.text_ingredient_amount);
    }
}
