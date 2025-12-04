package com.example.samiratravelmobile.fragments;

import android.os.Bundle;
import android.view.*;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.samiratravelmobile.R;

import java.util.Arrays;
import java.util.List;

public class KeunggulanFragment extends Fragment {

    private LinearLayout container;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_keunggulan, parent, false);

        container = v.findViewById(R.id.containerKeunggulan);

        List<String[]> list = Arrays.asList(
                new String[]{"🏛️", "Legalitas resmi & terpercaya"},
                new String[]{"🧑‍🏫", "Pembimbing berpengalaman"},
                new String[]{"🏨", "Fasilitas hotel terbaik"},
                new String[]{"✈️", "Pesawat langsung tanpa transit"},
                new String[]{"💰", "Harga terjangkau"},
                new String[]{"📅", "Jadwal fleksibel"},
                new String[]{"📞", "Layanan 24/7"},
                new String[]{"⭐", "Ribuan jamaah puas"}
        );

        // Tambah card 2 per baris
        for (int i = 0; i < list.size(); i += 2) {

            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            row.setPadding(0, 8, 0, 8); // jarak antar baris

            // Card pertama
            View card1 = inflater.inflate(R.layout.item_keunggulan, row, false);
            ((TextView) card1.findViewById(R.id.txtIcon)).setText(list.get(i)[0]);
            ((TextView) card1.findViewById(R.id.txtTitle)).setText(list.get(i)[1]);
            row.addView(card1);

            // Card kedua (kalau ada)
            if (i + 1 < list.size()) {
                View card2 = inflater.inflate(R.layout.item_keunggulan, row, false);
                ((TextView) card2.findViewById(R.id.txtIcon)).setText(list.get(i+1)[0]);
                ((TextView) card2.findViewById(R.id.txtTitle)).setText(list.get(i+1)[1]);
                row.addView(card2);
            }

            container.addView(row);
        }

        return v;
    }
}
