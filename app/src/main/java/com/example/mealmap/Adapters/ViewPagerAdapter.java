package com.example.mealmap.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import com.example.mealmap.MealPlanning.MealPlanFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private final String[] items;
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity, String[] items) {
        super(fragmentActivity);
        this.items = items;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return MealPlanFragment.newInstance("mealPlan", items[position]);
    }

    @Override
    public int getItemCount() {return items.length;
    }

}