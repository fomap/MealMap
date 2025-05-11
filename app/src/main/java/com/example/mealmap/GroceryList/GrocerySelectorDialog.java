package com.example.mealmap.GroceryList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.icu.util.Measure;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.example.mealmap.Listeners.RecipeDetailsListener;
import com.example.mealmap.Models.Measures;
import com.example.mealmap.Models.Metric;
import com.example.mealmap.Models.RecipeDetailsResponse;
import com.example.mealmap.PreferenceManager;
import com.example.mealmap.R;
import com.example.mealmap.RequestManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import com.example.mealmap.Models.ExtendedIngredient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class GrocerySelectorDialog extends DialogFragment {
    private CheckBox[] dayCheckboxes;
    private LinearLayout playlistContainer;
    private GrocerySelectionListener listener;
    private String UID;
    private RequestManager requestManager;
    private ProgressDialog progressDialog;
    public interface GrocerySelectionListener {
        void onGroceryListGenerated(List<ExtendedIngredient> combinedIngredients);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof GrocerySelectionListener) {
            listener = (GrocerySelectionListener) context;
        }
    }
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.grocery_selection_popup, null);
        setupViews(view);
        loadPlaylists();
        setupGenerateButton(view);

        builder.setView(view)
                .setNegativeButton("close", (dialog, which) -> dismiss());

        return builder.create();
    }
    private void setupViews(View view) {
        dayCheckboxes = new CheckBox[]{
                view.findViewById(R.id.chk_mon),
                view.findViewById(R.id.chk_tue),
                view.findViewById(R.id.chk_wed),
                view.findViewById(R.id.chk_thu),
                view.findViewById(R.id.chk_fri),
                view.findViewById(R.id.chk_sat),
                view.findViewById(R.id.chk_sun)
        };

        playlistContainer = view.findViewById(R.id.linearLayout_playlist);
        requestManager = new RequestManager(requireContext());

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) UID = user.getUid();
    }
    private void loadPlaylists() {
        DatabaseReference playlistsRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(UID).child("playlists");
        playlistsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot playlistSnapshot : snapshot.getChildren()) {
                    String playlistName = playlistSnapshot.getKey();
                    addPlaylistCheckbox(playlistName);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(requireContext(), "Playlist load failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addPlaylistCheckbox(String playlistName) {
        CheckBox cb = new CheckBox(requireContext());
        cb.setText(playlistName);
        cb.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        cb.setPadding(0, 8, 0, 8);
        playlistContainer.addView(cb);
    }
    private void setupGenerateButton(View view) {
        view.findViewById(R.id.btn_generate).setOnClickListener(v -> {
            Set<Integer> uniqueRecipeIds = new HashSet<>();
            Map<Integer, Integer> recipePortions = new HashMap<>();
            Map<String, ExtendedIngredient> combinedIngredients = new HashMap<>();
            showProgressDialog();
            List<String> selectedDays = getSelectedDays();
            List<String> selectedPlaylists = getSelectedPlaylists();

            AtomicInteger pendingSources = new AtomicInteger(
                    selectedDays.size() + selectedPlaylists.size()
            );

            Runnable onAllSourcesProcessed = () -> {
                if (uniqueRecipeIds.isEmpty()) {
                    progressDialog.dismiss();
                    Toast.makeText(requireContext(), "No recipes selected", Toast.LENGTH_SHORT).show();
                    return;
                }

                AtomicInteger pendingRecipes = new AtomicInteger(uniqueRecipeIds.size());
                Runnable checkCompletion = () -> {
                    if (pendingRecipes.get() == 0) {
                        requireActivity().runOnUiThread(() -> {
                            progressDialog.dismiss();
                            dismiss();
                            listener.onGroceryListGenerated(
                                    new ArrayList<>(combinedIngredients.values())
                            );
                        });
                    }
                };

                for (Integer recipeId : uniqueRecipeIds) {
                    int portions = recipePortions.getOrDefault(recipeId, 1);
                    fetchRecipeIngredients(recipeId, portions, combinedIngredients, pendingRecipes, checkCompletion);
                }
            };

            // Process each source to collect recipes
            for (String day : selectedDays) {
                fetchRecipesFromPath("mealPlan/" + day, uniqueRecipeIds, recipePortions, pendingSources, onAllSourcesProcessed);
            }

            for (String playlist : selectedPlaylists) {
                fetchRecipesFromPath("playlists/" + playlist, uniqueRecipeIds, recipePortions, pendingSources, onAllSourcesProcessed);
            }
        });
    }








    private void fetchRecipesFromPath(String path,
                                      Set<Integer> uniqueRecipeIds,
                                      Map<Integer, Integer> recipePortions,
                                      AtomicInteger pendingSources,
                                      Runnable onAllSourcesProcessed) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users").child(UID).child(path);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    Map<String, Object> recipeData = (Map<String, Object>) recipeSnapshot.getValue();
                    Long idLong = (Long) recipeData.get("id");
                    Long portionsLong = (Long) recipeData.get("portions");
                    int portions = portionsLong != null ? portionsLong.intValue() : 1;

                    if (idLong != null) {
                        int recipeId = idLong.intValue();
                        uniqueRecipeIds.add(recipeId);
                        recipePortions.put(recipeId, recipePortions.getOrDefault(recipeId, 0) + portions);
                    }
                }

                pendingSources.decrementAndGet();
                if (pendingSources.get() == 0) {
                    onAllSourcesProcessed.run();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pendingSources.decrementAndGet();
                if (pendingSources.get() == 0) {
                    onAllSourcesProcessed.run();
                }
            }
        });
    }


    private void fetchRecipeIngredients(int recipeId,
                                        int portions,
                                        Map<String, ExtendedIngredient> combinedIngredients,
                                        AtomicInteger pendingRecipes,
                                        Runnable checkCompletion) {
        requestManager.getRecipeDetails(new RecipeDetailsListener() {
            @Override
            public void didFetch(RecipeDetailsResponse response, String message) {
                synchronized (combinedIngredients) {
                    combineIngredients(response.extendedIngredients, portions, combinedIngredients);
                }
                pendingRecipes.decrementAndGet();
                checkCompletion.run();
            }

            @Override
            public void didError(String message) {
                pendingRecipes.decrementAndGet();
                checkCompletion.run();
            }
        }, recipeId);
    }


    private void combineIngredients(List<ExtendedIngredient> ingredients,
                                    int portions,
                                    Map<String, ExtendedIngredient> combined) {
        Context context = getContext();
        if (context == null) return;

        boolean useMetric = PreferenceManager.isMetric(context);

        for (ExtendedIngredient ingredient : ingredients) {
            double finalAmount;
            String finalUnit;

            if (useMetric) {
                if (ingredient.measures != null && ingredient.measures.metric != null) {
                    finalAmount = ingredient.measures.metric.amount * portions;
                    finalUnit = normalizeUnit(ingredient.measures.metric.unitShort);
                } else {
                    finalAmount = ingredient.amount * portions;
                    finalUnit = normalizeUnit(ingredient.unit != null ? ingredient.unit : "");
                }
            } else { // US Standard
                if (ingredient.measures != null && ingredient.measures.us != null) {
                    finalAmount = ingredient.measures.us.amount * portions;
                    finalUnit = normalizeUnit(ingredient.measures.us.unitShort);
                } else {
                    finalAmount = ingredient.amount * portions;
                    finalUnit = normalizeUnit(ingredient.unit != null ? ingredient.unit : "");
                }
            }

            String key = (ingredient.name.toLowerCase().trim() + "|" + finalUnit.toLowerCase().trim()).trim();

            if (combined.containsKey(key)) {
                ExtendedIngredient existing = combined.get(key);
                existing.amount += finalAmount;
                existing.amount = Math.round(existing.amount * 100.0) / 100.0;
            } else {
                ExtendedIngredient clone = new ExtendedIngredient();
                clone.name = ingredient.name.trim();
                clone.amount = Math.round(finalAmount * 100.0) / 100.0;
                clone.unit = finalUnit;
                clone.image = ingredient.image;
                clone.original = ingredient.original;
                combined.put(key, clone);
            }
        }
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

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Combining ingredients...");
        progressDialog.setCancelable(false);
        progressDialog.show();
    }
    private List<String> getSelectedDays() {
        List<String> selected = new ArrayList<>();
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday",
                "Friday", "Saturday", "Sunday"};
        for (int i = 0; i < dayCheckboxes.length; i++) {
            if (dayCheckboxes[i].isChecked()) {
                selected.add(days[i]);
            }
        }
        return selected;
    }
    private List<String> getSelectedPlaylists() {
        List<String> selected = new ArrayList<>();
        for (int i = 0; i < playlistContainer.getChildCount(); i++) {
            CheckBox cb = (CheckBox) playlistContainer.getChildAt(i);
            if (cb.isChecked()) {
                selected.add(cb.getText().toString());
            }
        }
        return selected;
    }
    @Override
    public void onDestroyView() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        super.onDestroyView();
    }
}