package com.example.mealmap.GroceryList;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.mealmap.Listeners.RecipeDetailsListener;
import com.example.mealmap.Models.RecipeDetailsResponse;
import com.example.mealmap.R;
import com.example.mealmap.RequestManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
            Set<Integer> recipeIds = new HashSet<>();
            Map<String, ExtendedIngredient> combinedIngredients = new HashMap<>();

            showProgressDialog();

            List<String> selectedDays = getSelectedDays();
            List<String> selectedPlaylists = getSelectedPlaylists();

            AtomicInteger pendingSources = new AtomicInteger(
                    selectedDays.size() + selectedPlaylists.size()
            );
            AtomicInteger pendingRecipes = new AtomicInteger(0);

            Runnable checkCompletion = () -> {
                if (pendingSources.get() == 0 && pendingRecipes.get() == 0) {
                    requireActivity().runOnUiThread(() -> {
                        progressDialog.dismiss();
                        if (combinedIngredients.isEmpty()) {
                            Toast.makeText(requireContext(),
                                    "No recipes found in selected sources",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            listener.onGroceryListGenerated(
                                    new ArrayList<>(combinedIngredients.values())
                            );
                        }
                    });
                }
            };


            for (String day : selectedDays) {
                fetchRecipesFromPath("mealPlan/" + day, recipeIds,
                        pendingSources, pendingRecipes, combinedIngredients, checkCompletion);
            }

            for (String playlist : selectedPlaylists) {
                fetchRecipesFromPath("playlists/" + playlist, recipeIds,
                        pendingSources, pendingRecipes, combinedIngredients, checkCompletion);
            }
        });
    }

    private void fetchRecipesFromPath(String path,
                                      Set<Integer> recipeIds,
                                      AtomicInteger pendingSources,
                                      AtomicInteger pendingRecipes,
                                      Map<String, ExtendedIngredient> combinedIngredients,
                                      Runnable checkCompletion) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference()
                .child("users").child(UID).child(path);

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Integer> batchIds = new ArrayList<>();
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    Map<String, Object> recipeData = (Map<String, Object>) recipeSnapshot.getValue();
                    Long idLong = (Long) recipeData.get("id");
                    if (idLong != null) {
                        batchIds.add(idLong.intValue());
                    }
                }

                synchronized (recipeIds) {
                    recipeIds.addAll(batchIds);
                }

                // Track recipes from source
                pendingRecipes.addAndGet(batchIds.size());

                // Process recipes even if empty list
                fetchIngredientsForBatch(batchIds, combinedIngredients,
                        pendingRecipes, checkCompletion);

                // Mark source as processed
                pendingSources.decrementAndGet();
                checkCompletion.run();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                pendingSources.decrementAndGet();
                checkCompletion.run();
            }
        });
    }

    private void fetchIngredientsForBatch(List<Integer> recipeIds,
                                          Map<String, ExtendedIngredient> combinedIngredients,
                                          AtomicInteger pendingRecipes,
                                          Runnable checkCompletion) {
        if (recipeIds.isEmpty()) {
            checkCompletion.run();
            return;
        }

        for (Integer id : recipeIds) {
            requestManager.getRecipeDetails(new RecipeDetailsListener() {
                @Override
                public void didFetch(RecipeDetailsResponse response, String message) {
                    combineIngredients(response.extendedIngredients, combinedIngredients);
                    pendingRecipes.decrementAndGet();
                    checkCompletion.run();
                }

                @Override
                public void didError(String message) {
                    pendingRecipes.decrementAndGet();
                    checkCompletion.run();
                }
            }, id);
        }
    }

    private void combineIngredients(List<ExtendedIngredient> ingredients,
                                    Map<String, ExtendedIngredient> combined) {
        for (ExtendedIngredient ingredient : ingredients) {
            String key = ingredient.name.toLowerCase() + "|" + ingredient.unit.toLowerCase();

            if (combined.containsKey(key)) {
                ExtendedIngredient existing = combined.get(key);
                existing.amount += ingredient.amount;
            } else {
                combined.put(key, ingredient);
            }
        }
    }

//    private void checkCompletion(AtomicInteger counter,
//                                 Map<String, ExtendedIngredient> ingredients) {
//        if (counter.get() == 0) {
//            requireActivity().runOnUiThread(() -> {
//                progressDialog.dismiss();
//                if (listener != null) {
//                    listener.onGroceryListGenerated(new ArrayList<>(ingredients.values()));
//                }
//                dismiss();
//            });
//        }
//    }

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