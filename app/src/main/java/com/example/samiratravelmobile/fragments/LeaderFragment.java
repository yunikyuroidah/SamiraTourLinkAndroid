package com.example.samiratravelmobile.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.samiratravelmobile.R;
import com.example.samiratravelmobile.utils.IntentHelper;
import com.google.firebase.firestore.FirebaseFirestore;

public class LeaderFragment extends Fragment implements ReloadableFragment {

    private TextView leaderName, leaderDesc;
    private ImageView leaderImg;
    private LinearLayout btnWhatsApp;
    private FirebaseFirestore db;
    private String phoneNumber = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_leader, container, false);

        leaderName = v.findViewById(R.id.leaderName);
        leaderDesc = v.findViewById(R.id.leaderDesc);
        leaderImg = v.findViewById(R.id.leaderImg);
        btnWhatsApp = v.findViewById(R.id.btnWhatsApp);

        int[] cardIds = {R.id.cardFeatureCommunity, R.id.cardFeatureCertified, R.id.cardFeatureRating, R.id.cardFeatureTrust};
        int[] iconRes = {R.drawable.ic_feature_people, R.drawable.ic_feature_certificate, R.drawable.ic_feature_star, R.drawable.ic_feature_shield};
        String[] titles = {"2000+ Jamaah", "Bersertifikat", "Rating 4.9/5", "Amanah"};
        String[] subtitles = {
                "Telah mendampingi lebih dari 2000 jamaah",
                "Memiliki sertifikat resmi dari Kemenag",
                "Mendapat rating tinggi dari jamaah",
                "Terpercaya dan bertanggung jawab penuh"
        };

        for (int i = 0; i < cardIds.length; i++) {
            View card = v.findViewById(cardIds[i]);
            if (card == null) continue;
            TextView titleView = card.findViewById(R.id.featureTitle);
            TextView subtitleView = card.findViewById(R.id.featureSubtitle);
            ImageView iconView = card.findViewById(R.id.featureIcon);

            if (titleView != null) {
                titleView.setText(titles[i]);
            }
            if (subtitleView != null) {
                subtitleView.setText(subtitles[i]);
            }
            if (iconView != null) {
                iconView.setImageResource(iconRes[i]);
                iconView.setContentDescription(titles[i]);
            }
        }

        db = FirebaseFirestore.getInstance();
        reloadData();

        btnWhatsApp.setOnClickListener(vw -> {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                IntentHelper.openWhatsApp(getContext(), phoneNumber);
            }
        });

        return v;
    }

    @Override
    public void reloadData() {
        if (!isAdded() || leaderName == null || leaderImg == null) {
            return;
        }

        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        db.collection("tour_leader")
                .document("tour_leader_id")
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || doc == null || !doc.exists()) {
                        phoneNumber = null;
                        return;
                    }

                    leaderName.setText(doc.getString("nama"));
                    phoneNumber = doc.getString("telepon");

                    String gambar = doc.getString("gambar");

                    if (gambar != null && !gambar.isEmpty()) {
                        try {
                            byte[] decoded = Base64.decode(gambar, Base64.DEFAULT);
                            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                            leaderImg.setImageBitmap(bitmap);
                        } catch (Exception err) {
                            leaderImg.setImageResource(R.drawable.ic_admin_leader);
                        }
                    }
                });
    }
}
