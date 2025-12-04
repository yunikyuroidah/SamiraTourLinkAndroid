package com.example.samiratravelmobile.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.samiratravelmobile.R;
import com.example.samiratravelmobile.models.Paket;
import java.util.List;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {
    private final List<Paket> paketList;
    private int fixedHeightPx = ViewGroup.LayoutParams.WRAP_CONTENT;

    public PackageAdapter(List<Paket> paketList) {
        this.paketList = paketList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_package, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Paket p = paketList.get(position);
        holder.nama.setText(p.getNama_paket());
        holder.deskripsi.setText(p.getDeskripsi());

        StringBuilder fasilitasText = new StringBuilder();
        if (p.getFasilitas() != null) {
            for (String f : p.getFasilitas()) fasilitasText.append("• ").append(f).append("\n");
        }
        String fasilitasDisplay = fasilitasText.length() > 0 ? fasilitasText.toString().trim() : "-";
        holder.fasilitas.setText(fasilitasDisplay);

        StringBuilder featuresText = new StringBuilder();
        if (p.getFeatures() != null) {
            for (String feature : p.getFeatures()) featuresText.append("• ").append(feature).append("\n");
        }
        String featuresDisplay = featuresText.length() > 0 ? featuresText.toString().trim() : "-";
        holder.features.setText(featuresDisplay);

        if (fixedHeightPx > 0) {
            ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
            if (params != null && params.height != fixedHeightPx) {
                params.height = fixedHeightPx;
                holder.itemView.setLayoutParams(params);
            }
        }
    }

    @Override
    public int getItemCount() {
        return paketList.size();
    }

    public void setFixedHeight(int heightPx) {
        if (heightPx <= 0 || heightPx == fixedHeightPx) {
            return;
        }
        fixedHeightPx = heightPx;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nama, deskripsi, fasilitas, features;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nama = itemView.findViewById(R.id.txtNamaPaket);
            deskripsi = itemView.findViewById(R.id.txtDeskripsi);
            fasilitas = itemView.findViewById(R.id.txtFasilitas);
            features = itemView.findViewById(R.id.txtFeatures);
        }
    }
}
