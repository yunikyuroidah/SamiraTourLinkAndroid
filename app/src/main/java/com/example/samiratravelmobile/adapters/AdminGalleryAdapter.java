package com.example.samiratravelmobile.adapters;

import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.samiratravelmobile.R;
import com.example.samiratravelmobile.models.Dokumentasi;
import com.example.samiratravelmobile.BitmapUtils;
import java.util.List;
import com.bumptech.glide.Glide;

public class AdminGalleryAdapter extends RecyclerView.Adapter<AdminGalleryAdapter.ViewHolder> {

    private final Context context;
    private final List<Dokumentasi> list;
    private final OnActionListener listener;

    public interface OnActionListener {
        void onEditRequested(Dokumentasi d);
        void onDeleteRequested(Dokumentasi d);
    }

    public AdminGalleryAdapter(Context ctx, List<Dokumentasi> list, OnActionListener listener) {
        this.context = ctx;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AdminGalleryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery_admin, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dokumentasi d = list.get(position);
        holder.txtNama.setText(d.getNama());
        holder.txtDeskripsi.setText(d.getDeskripsi());

        String g = d.getGambar();
        if (g != null && !g.trim().isEmpty()) {
            try {
                boolean isBase64 = !g.startsWith("http") && g.length() > 50;
                if (isBase64) {
                    byte[] bytes = Base64.decode(g, Base64.DEFAULT);
                    BitmapUtils.setBitmapToImageViewFromBytes(holder.img, bytes);
                } else {
                    Glide.with(holder.itemView.getContext())
                            .load(g)
                            .placeholder(R.drawable.ic_broken_image)
                            .into(holder.img);
                }
            } catch (Exception e) {
                holder.img.setImageResource(R.drawable.ic_broken_image);
            }
        } else {
            holder.img.setImageResource(R.drawable.ic_broken_image);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEditRequested(d));
        holder.btnHapus.setOnClickListener(v -> listener.onDeleteRequested(d));
    }



    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView img;
        TextView txtNama, txtDeskripsi;
        Button btnEdit, btnHapus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.imgDokumentasi);
            txtNama = itemView.findViewById(R.id.txtNamaGalleryAdmin);
            txtDeskripsi = itemView.findViewById(R.id.txtDeskripsiGalleryAdmin);
            btnEdit = itemView.findViewById(R.id.btnEditGallery);
            btnHapus = itemView.findViewById(R.id.btnHapusGallery);
        }
    }
}
