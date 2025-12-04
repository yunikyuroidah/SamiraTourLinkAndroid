package com.example.samiratravelmobile.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import com.example.samiratravelmobile.AdminLoginActivity;

public class AuthManager {
    private static final String PREF_NAME = "auth_session";
    private static final String KEY_LOGGED_IN = "logged_in";

    public static boolean isLoggedIn(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return pref.getBoolean(KEY_LOGGED_IN, false);
    }

    public static void setLoggedIn(Context context, boolean status) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().putBoolean(KEY_LOGGED_IN, status).apply();
    }

    public static void logout(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        pref.edit().clear().apply();
        DeviceBlocker.clear(context);
        Intent i = new Intent(context, AdminLoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }
}
