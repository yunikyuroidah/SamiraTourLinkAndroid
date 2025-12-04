package com.example.samiratravelmobile.fragments;

import android.os.Bundle;
import android.view.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.samiratravelmobile.R;
import com.example.samiratravelmobile.adapters.GalleryAdapter;
import com.example.samiratravelmobile.models.Dokumentasi;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment implements ReloadableFragment {
    private RecyclerView recyclerView;
    private List<Dokumentasi> list = new ArrayList<>();
    private GalleryAdapter adapter;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_gallery, container, false);
        recyclerView = v.findViewById(R.id.recyclerGallery);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new GalleryAdapter(list);
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

        db.collection("dokumentasi").get().addOnSuccessListener(snapshot -> {
            if (!isAdded()) {
                return;
            }
            list.clear();
            for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshot) {
                Dokumentasi d = doc.toObject(Dokumentasi.class);
                if (d != null) list.add(d);
            }
            adapter.notifyDataSetChanged();
        });
    }
}
