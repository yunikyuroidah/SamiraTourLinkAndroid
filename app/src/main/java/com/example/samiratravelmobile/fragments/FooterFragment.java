package com.example.samiratravelmobile.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import com.example.samiratravelmobile.AdminLoginActivity;
import com.example.samiratravelmobile.R;
import com.example.samiratravelmobile.utils.IntentHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Calendar;

public class FooterFragment extends Fragment implements ReloadableFragment {
    private TextView alamat, email, phone, tahun, igLink, tiktokLink;
    private Button btnWhatsApp;
    private FirebaseFirestore db;
    private String tourLeaderPhone;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_footer, container, false);

        alamat = v.findViewById(R.id.txtAlamat);
        email = v.findViewById(R.id.txtEmail);
        phone = v.findViewById(R.id.txtPhone);
        tahun = v.findViewById(R.id.txtTahun);
        igLink = v.findViewById(R.id.txtInstagram);
        tiktokLink = v.findViewById(R.id.txtTiktok);
        btnWhatsApp = v.findViewById(R.id.btnWhatsApp);

        db = FirebaseFirestore.getInstance();

        // Ambil data profil dari Firestore
        reloadData();

        // Tombol WhatsApp
        btnWhatsApp.setOnClickListener(vw -> {
            if (tourLeaderPhone != null && !tourLeaderPhone.isEmpty()) {
                IntentHelper.openWhatsApp(getContext(), tourLeaderPhone);
            }
        });

        // Klik link IG
        igLink.setOnClickListener(vw -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.instagram.com/samira_travel"));
            startActivity(intent);
        });

        // Klik link TikTok
        tiktokLink.setOnClickListener(vw -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com/@samiratravel"));
            startActivity(intent);
        });

        // --- Bagian Tahun & "Samira Travel" Klik ke Login ---
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String html = "© " + currentYear +
                " <font color='#4FC3F7'><u>Samira Travel</u></font>. Semua hak dilindungi.";
        tahun.setText(Html.fromHtml(html));
        tahun.setMovementMethod(LinkMovementMethod.getInstance());

        tahun.setOnClickListener(vw -> {
            Intent intent = new Intent(getContext(), AdminLoginActivity.class);
            startActivity(intent);
        });

        return v;
    }

    @Override
    public void reloadData() {
        if (!isAdded() || alamat == null || email == null || phone == null) {
            return;
        }

        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        db.collection("profil_travel").document("profil_id").get().addOnSuccessListener(doc -> {
            if (!isAdded() || doc == null || !doc.exists()) {
                return;
            }
            alamat.setText(doc.getString("alamat"));
            email.setText(doc.getString("email"));
        });

        db.collection("tour_leader").document("tour_leader_id").get().addOnSuccessListener(doc -> {
            if (!isAdded()) {
                return;
            }

            tourLeaderPhone = null;
            phone.setText("-");

            if (doc != null && doc.exists()) {
                tourLeaderPhone = doc.getString("telepon");
                if (tourLeaderPhone != null && !tourLeaderPhone.isEmpty()) {
                    phone.setText(tourLeaderPhone);
                }
            }
        });
    }
}
