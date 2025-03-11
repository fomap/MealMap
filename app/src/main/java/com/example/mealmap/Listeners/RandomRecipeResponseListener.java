package com.example.mealmap.Listeners;

import com.example.mealmap.Models.RandomRecipeApiResponse;

public abstract class RandomRecipeResponseListener {
     public void didFetch(RandomRecipeApiResponse response, String message){};
     public void didError(String message){};

}
