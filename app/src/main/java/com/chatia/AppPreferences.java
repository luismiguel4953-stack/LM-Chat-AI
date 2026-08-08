package com.chatia;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    private static final String PREFS = "chatia_preferences";
    private static final String KEY_DARK_MODE = "dark_mode";
    private static final String KEY_USER_NAME = "user_name";
    private final SharedPreferences preferences;

    public AppPreferences(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public boolean isDarkMode() {
        return preferences.getBoolean(KEY_DARK_MODE, false);
    }

    public void setDarkMode(boolean enabled) {
        preferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply();
    }

    public String getUserName() {
        return preferences.getString(KEY_USER_NAME, "Usuario ChatIA");
    }

    public void setUserName(String name) {
        preferences.edit().putString(KEY_USER_NAME, name.trim().isEmpty() ? "Usuario ChatIA" : name.trim()).apply();
    }
}
