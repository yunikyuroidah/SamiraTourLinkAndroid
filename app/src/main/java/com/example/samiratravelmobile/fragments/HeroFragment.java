package com.example.samiratravelmobile.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.example.samiratravelmobile.R;
import com.example.samiratravelmobile.utils.IntentHelper;
import com.google.firebase.firestore.FirebaseFirestore;

public class HeroFragment extends Fragment implements ReloadableFragment {

    private ImageView heroImage;
    private TextView heroTitle, heroSubtitle;
    private LinearLayout btnWhatsApp;
    private FirebaseFirestore db;
    private String phoneNumber = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_hero, container, false);

        heroImage = v.findViewById(R.id.heroImage);
        heroTitle = v.findViewById(R.id.heroTitle);
        heroSubtitle = v.findViewById(R.id.heroSubtitle);
        btnWhatsApp = v.findViewById(R.id.btnWhatsApp);

        db = FirebaseFirestore.getInstance();

        // --- Ambil data dari Firestore ---
        reloadData();


        // --- Tombol WhatsApp ---
        btnWhatsApp.setOnClickListener(vw -> {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                IntentHelper.openWhatsApp(getContext(), phoneNumber);
            }
        });

        return v;
    }

    @Override
    public void reloadData() {
        if (!isAdded() || heroImage == null || heroTitle == null || heroSubtitle == null) {
            return;
        }

        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        db.collection("profil_travel").document("profil_id")
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || doc == null || !doc.exists()) {
                        phoneNumber = null;
                        return;
                    }

                    heroTitle.setText("SAMIRA TRAVEL");
                    heroSubtitle.setText("Sahabat Umroh & Haji Keluarga Anda");

                    phoneNumber = doc.getString("telepon");
                    String gambar = doc.getString("gambar");

                    if (gambar != null && !gambar.isEmpty() && getContext() != null) {
                        try {
                            byte[] bytes = android.util.Base64.decode(gambar, android.util.Base64.DEFAULT);
                            Glide.with(getContext())
                                    .asBitmap()
                                    .load(bytes)
                                    .into(heroImage);
                        } catch (Exception e) {
                            heroImage.setImageResource(R.drawable.ic_broken_image);
                        }
                    }
                });
    }
}
