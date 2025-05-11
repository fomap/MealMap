package com.example.mealmap;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREFS_NAME = "unit_prefs";
    private static final String KEY_SYSTEM = "measurement_system";

    public static boolean isMetric(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getBoolean(KEY_SYSTEM, true);
    }

    public static void setMetric(Context context, boolean isMetric) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_SYSTEM, isMetric);
        editor.apply();
    }
}