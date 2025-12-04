package com.example.samiratravelmobile.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class IntentHelper {
    public static void openWhatsApp(Context context, String phone) {
        try {
            String cleanPhone = phone.replaceAll("[^0-9]", "");
            String url = "https://wa.me/" + cleanPhone;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            context.startActivity(i);
        } catch (Exception e) {
            ToastUtils.showError(context, "Tidak bisa membuka WhatsApp");
        }
    }
}
