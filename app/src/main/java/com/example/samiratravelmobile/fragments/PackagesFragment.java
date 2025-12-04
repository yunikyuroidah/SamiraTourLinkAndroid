package com.example.samiratravelmobile.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.samiratravelmobile.R;
import com.example.samiratravelmobile.adapters.PackageAdapter;
import com.example.samiratravelmobile.models.Paket;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class PackagesFragment extends Fragment implements ReloadableFragment {
    private RecyclerView recyclerView;
    private PackageAdapter adapter;
    private List<Paket> paketList = new ArrayList<>();
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_packages, container, false);
        recyclerView = v.findViewById(R.id.recyclerPackages);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        adapter = new PackageAdapter(paketList);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        reloadData();

        return v;
    }

    @Override
    public void reloadData() {
        if (!isAdded() || adapter == null) {
            return;
        }

        if (db == null) {
            db = FirebaseFirestore.getInstance();
        }

        db.collection("paket").get().addOnSuccessListener(snapshot -> {
            if (!isAdded()) {
                return;
            }
            paketList.clear();
            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshot) {
                Paket p = doc.toObject(Paket.class);
                if (p != null) paketList.add(p);
            }
            int maxHeight = calculateCardMaxHeight();
            adapter.setFixedHeight(maxHeight);
            adapter.notifyDataSetChanged();
        });
    }

    private int calculateCardMaxHeight() {
        Context context = getContext();
        if (context == null || paketList.isEmpty()) {
            return 0;
        }

        LayoutInflater inflater = LayoutInflater.from(context);
        FrameLayout parent = new FrameLayout(context);
        int widthPx = dpToPx(context, 280); // match item_package.xml width
        int widthSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY);
        int heightSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);

        int maxHeight = 0;
        for (Paket paket : paketList) {
            View temp = inflater.inflate(R.layout.item_package, parent, false);
            bindPackageView(temp, paket);
            temp.measure(widthSpec, heightSpec);
            int measured = temp.getMeasuredHeight();
            if (measured > maxHeight) {
                maxHeight = measured;
            }
        }
        return maxHeight;
    }

    private void bindPackageView(View view, Paket paket) {
        TextView nama = view.findViewById(R.id.txtNamaPaket);
        TextView deskripsi = view.findViewById(R.id.txtDeskripsi);
        TextView fasilitas = view.findViewById(R.id.txtFasilitas);
        TextView features = view.findViewById(R.id.txtFeatures);

        if (nama != null) nama.setText(paket.getNama_paket());
        if (deskripsi != null) deskripsi.setText(paket.getDeskripsi());

        if (fasilitas != null) fasilitas.setText(buildBulletedList(paket.getFasilitas()));
        if (features != null) features.setText(buildBulletedList(paket.getFeatures()));
    }

    private String buildBulletedList(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "-";
        }
        StringBuilder builder = new StringBuilder();
        for (String item : items) {
            builder.append("• ").append(item).append("\n");
        }
        return builder.toString().trim();
    }

    private int dpToPx(Context context, int dp) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics));
    }
}
