package com.sooguosheng.mathplayground;

import android.content.Context;
import android.content.SharedPreferences;

public class Prefs {
    private static final String FILE = "math_prefs";
    private static SharedPreferences p(Context c){ return c.getSharedPreferences(FILE, Context.MODE_PRIVATE); }

    public static void setBestStars(Context c, String mode, String difficulty, int stars){
        String k = "best_" + mode + "_" + difficulty;
        int cur = p(c).getInt(k, 0);
        if (stars > cur) p(c).edit().putInt(k, stars).apply();
    }
    public static int getBestStars(Context c, String mode, String difficulty){
        return p(c).getInt("best_" + mode + "_" + difficulty, 0);
    }
    public static void setSoundOn(Context c, boolean on){ p(c).edit().putBoolean("sound_on", on).apply(); }
    public static boolean isSoundOn(Context c){ return p(c).getBoolean("sound_on", true); }
}
