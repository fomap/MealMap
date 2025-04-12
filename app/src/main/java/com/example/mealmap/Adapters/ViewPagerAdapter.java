package com.example.mealmap.Adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mealmap.MealPlanFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    private final String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

    public ViewPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return MealPlanFragment.newInstance(days[position]);
    }

    @Override
    public int getItemCount() {
        return days.length;
    }
}