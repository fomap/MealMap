package com.example.mealmap;

import android.content.Context;
import com.example.mealmap.Listeners.InstructionsListener;
import com.example.mealmap.Listeners.RandomRecipeResponseListener;
import com.example.mealmap.Listeners.RecipeDetailsListener;
import com.example.mealmap.Models.InstructionsResponse;
import com.example.mealmap.Models.RandomRecipeApiResponse;
import com.example.mealmap.Models.RecipeDetailsResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class RequestManager {
    Context context;
    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("https://api.spoonacular.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build();
    public RequestManager(Context context) {
        this.context = context;
    }

    private String getCurrentApiKey() {
        return UserProfileActivity.getCurrentApiKey(context);
    }
    public void getRandomRecipes(RandomRecipeResponseListener listener, List<String> tags) {

        String units = PreferenceManager.isMetric(context) ? "metric" : "us";

        CaLLRandomRecipes caLLRandomRecipes = retrofit.create(CaLLRandomRecipes.class);
        Call<RandomRecipeApiResponse> request = caLLRandomRecipes.callRandomRecipe(getCurrentApiKey(), "1", tags, units);
        request.enqueue(new Callback<RandomRecipeApiResponse>() {
            @Override
            public void onResponse(Call<RandomRecipeApiResponse> call, Response<RandomRecipeApiResponse> response) {
                if (!response.isSuccessful())
                {
                    listener.didError(response.message());
                    return;
                }
                listener.didFetch(response.body(), response.message());
            }
            @Override
            public void onFailure(Call<RandomRecipeApiResponse> call, Throwable t) {
                listener.didError(t.getMessage());
            }
        });
    }
    public void getRecipeDetails(RecipeDetailsListener listener, int id) {

        String units = PreferenceManager.isMetric(context) ? "metric" : "us";

        CallRecipeDetails callRecipeDetails = retrofit.create(CallRecipeDetails.class);
        Call<RecipeDetailsResponse> call = callRecipeDetails.callRecipeDetails(id, getCurrentApiKey(), true, units);
        call.enqueue(new Callback<RecipeDetailsResponse>() {
            @Override
            public void onResponse(Call<RecipeDetailsResponse> call, Response<RecipeDetailsResponse> response) {
                if(!response.isSuccessful())
                {
                    listener.didError(response.message());
                    return ;
                }
                listener.didFetch(response.body(), response.message());
            }
            @Override
            public void onFailure(Call<RecipeDetailsResponse> call, Throwable t) {
                listener.didError(t.getMessage());
            }
        });
    }
    public void getInstructions(InstructionsListener listener, int id){
        CallInstructions callInstructions = retrofit.create(CallInstructions.class);
        Call<List<InstructionsResponse>> call = callInstructions.callInstructions(id, getCurrentApiKey());
        call.enqueue(new Callback<List<InstructionsResponse>>() {
            @Override
            public void onResponse(Call<List<InstructionsResponse>> call, Response<List<InstructionsResponse>> response) {
                if(!response.isSuccessful())
                {
                    listener.didError(response.message());
                    return;
                }
                listener.didFetch(response.body(), response.message());
            }
            @Override
            public void onFailure(Call<List<InstructionsResponse>> call, Throwable t) {
                listener.didError(t.getMessage());
            }
        });
    }

    private interface CaLLRandomRecipes{
        @GET("recipes/random")
        Call<RandomRecipeApiResponse> callRandomRecipe(
                @Query("apiKey") String apiKey,
                @Query("number") String number,
                @Query("tags") List<String> tags,
                @Query("units") String units
        );
    }
    private interface CallRecipeDetails{
        @GET("recipes/{id}/information")
        Call<RecipeDetailsResponse> callRecipeDetails(
                @Path("id") int id,
                @Query("apiKey") String apiKey,
                @Query("includeNutrition") boolean includeNutrition,
                @Query("units") String units
        );
    }
    private interface CallInstructions{
        @GET("recipes/{id}/analyzedInstructions")
        Call<List<InstructionsResponse>> callInstructions(
                @Path("id") int id,
                @Query("apiKey") String apiKey
        );
    }

}
