package com.example.mealmap.MealPlan;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MealPlanningAdapter extends FragmentStateAdapter {


    public MealPlanningAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position)
        {
            case 0:
                return new MonFragment();
            case 1:
                return new TueFragment();
            case 2:
                return new WedFragment();
            default:
                return new MonFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}
