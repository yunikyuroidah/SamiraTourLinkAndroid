package com.example.samiratravelmobile.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class DeviceBlocker {
    private static final String PREF_NAME = "login_protection";
    private static final String KEY_FAIL_COUNT = "fail_count";
    private static final String KEY_BLOCK_TIME = "block_time";
    private static final long BLOCK_DURATION = 7L * 24 * 60 * 60 * 1000; // 7 hari (ms)

    public static boolean isBlocked(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        long blockedAt = pref.getLong(KEY_BLOCK_TIME, 0);
        if (blockedAt == 0) return false;

        long now = System.currentTimeMillis();
        if (now - blockedAt >= BLOCK_DURATION) {
            clear(context);
            return false;
        }
        return true;
    }

    public static void registerFailure(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        int count = pref.getInt(KEY_FAIL_COUNT, 0) + 1;
        if (count >= 3) {
            pref.edit().putLong(KEY_BLOCK_TIME, System.currentTimeMillis()).apply();
        } else {
            pref.edit().putInt(KEY_FAIL_COUNT, count).apply();
        }
    }

    public static void clear(Context context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
