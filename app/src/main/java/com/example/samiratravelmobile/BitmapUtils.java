package com.example.samiratravelmobile;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

public class BitmapUtils {
    public static void setBitmapToImageViewFromBytes(ImageView iv, byte[] bytes) {
        if (iv == null) return;
        try {
            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bmp != null) iv.setImageBitmap(bmp);
            else iv.setImageResource(android.R.drawable.ic_menu_report_image);
        } catch (Exception e) {
            iv.setImageResource(android.R.drawable.ic_menu_report_image);
        }
    }
}
