package com.example.samiratravelmobile.utils;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;

import com.example.samiratravelmobile.R;

/**
 * Helper for branded toast messages so we can avoid the default Android robot icon.
 */
public final class ToastUtils {
    public static final int LENGTH_SHORT = Toast.LENGTH_SHORT;
    public static final int LENGTH_LONG = Toast.LENGTH_LONG;

    private enum ToastType {
        SUCCESS,
        ERROR,
        INFO
    }

    private ToastUtils() {
    }

    public static void showSuccess(@NonNull Context context, @NonNull String message) {
        showSuccess(context, message, LENGTH_SHORT);
    }

    public static void showSuccess(@NonNull Context context, @NonNull String message, int duration) {
        show(context, message, duration, ToastType.SUCCESS);
    }

    public static void showError(@NonNull Context context, @NonNull String message) {
        showError(context, message, LENGTH_SHORT);
    }

    public static void showError(@NonNull Context context, @NonNull String message, int duration) {
        show(context, message, duration, ToastType.ERROR);
    }

    public static void showInfo(@NonNull Context context, @NonNull String message) {
        showInfo(context, message, LENGTH_SHORT);
    }

    public static void showInfo(@NonNull Context context, @NonNull String message, int duration) {
        show(context, message, duration, ToastType.INFO);
    }

    private static void show(@NonNull Context context, @NonNull String message, int duration, @NonNull ToastType type) {
        Context appContext = context.getApplicationContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View toastView = inflater.inflate(R.layout.view_toast, null);

        LinearLayout container = toastView.findViewById(R.id.toastContainer);
        ImageView iconView = toastView.findViewById(R.id.toastIcon);
        TextView messageView = toastView.findViewById(R.id.toastMessage);

        if (container != null) {
            container.setBackground(AppCompatResources.getDrawable(context, R.drawable.bg_toast));
        }

        iconView.setImageResource(R.drawable.logo);
        messageView.setText(message);

        Toast toast = new Toast(appContext);
        toast.setDuration(duration);
        toast.setView(toastView);
        toast.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, dpToPx(context, 80));
        toast.show();
    }

    private static int dpToPx(@NonNull Context context, int dp) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
