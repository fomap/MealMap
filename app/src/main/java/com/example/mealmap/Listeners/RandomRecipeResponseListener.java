package com.example.mealmap.Listeners;

import com.example.mealmap.Models.RandomRecipeApiResponse;

public interface RandomRecipeResponseListener {
     void didFetch(RandomRecipeApiResponse response, String message);
     void didError(String message);

}
