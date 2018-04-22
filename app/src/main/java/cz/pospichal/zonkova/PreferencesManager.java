package cz.pospichal.zonkova;

import android.content.Context;
import android.content.SharedPreferences;

class PreferencesManager {
    private static final PreferencesManager ourInstance = new PreferencesManager();

    static PreferencesManager getInstance() {
        return ourInstance;
    }

    private PreferencesManager() {
    }

    public static void SaveCheckTime(Context context, int time) {
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("time", time);
        editor.apply();
    }
    public static int ReadCheckTime(Context context) {
        SharedPreferences settings = context.getSharedPreferences("settings", 0);
        return settings.getInt("time", 120);
    }
}
