package com.example.samiratravelmobile.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.fragment.app.Fragment;
import com.example.samiratravelmobile.R;
import com.example.samiratravelmobile.utils.IntentHelper;
import com.google.firebase.firestore.FirebaseFirestore;

public class AboutFragment extends Fragment implements ReloadableFragment {
    private VideoView aboutVideo;
    private TextView aboutText;
    private LinearLayout whatsappButton;
    private FirebaseFirestore db;
    private String phoneNumber = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_about, container, false);

        aboutVideo = v.findViewById(R.id.aboutVideo);
        aboutText = v.findViewById(R.id.aboutText);
        whatsappButton = v.findViewById(R.id.whatsappButton);

        // --- Firestore instance ---
        db = FirebaseFirestore.getInstance();

        // --- Deskripsi hardcoded seperti versi web ---
        aboutText.setText("Samira Travel hadir untuk melayani perjalanan ibadah Umrah dan Haji " +
                "Anda dengan pelayanan terpercaya, fasilitas lengkap, dan bimbingan profesional " +
                "yang berpengalaman.");

        reloadData();

        // --- Putar video lokal dari raw ---
        Uri videoUri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.profilvideo);
        aboutVideo.setVideoURI(videoUri);
        aboutVideo.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            aboutVideo.start();
        });

        // --- Tombol WhatsApp, ambil nomor dari Firestore ---
        whatsappButton.setOnClickListener(vw -> {
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                IntentHelper.openWhatsApp(getContext(), phoneNumber);
            }
        });

        return v;
    }

    @Override
    public void reloadData() {
        if (!isAdded() || whatsappButton == null) {
            return;
        }

        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        db.collection("tour_leader").document("tour_leader_id").get().addOnSuccessListener(doc -> {
            if (!isAdded() || doc == null || !doc.exists()) {
                phoneNumber = null;
                return;
            }
            phoneNumber = doc.getString("telepon");
        });
    }
}
