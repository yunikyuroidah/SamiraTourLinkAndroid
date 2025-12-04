package com.example.samiratravelmobile.adapters;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.samiratravelmobile.BitmapUtils;
import com.example.samiratravelmobile.R;
import com.example.samiratravelmobile.models.Dokumentasi;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {

    private final List<Dokumentasi> list;

    public GalleryAdapter(List<Dokumentasi> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_gallery, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Dokumentasi item = list.get(position);

        holder.nama.setText(item.getNama());
        holder.deskripsi.setText(item.getDeskripsi());

        String g = item.getGambar();
        if (g != null && !g.trim().isEmpty()) {
            try {
                boolean isBase64 = !g.startsWith("http") && g.length() > 50;
                if (isBase64) {
                    byte[] bytes = Base64.decode(g, Base64.DEFAULT);
                    BitmapUtils.setBitmapToImageViewFromBytes(holder.gambar, bytes);
                } else {
                    Glide.with(holder.itemView.getContext())
                            .load(g)
                            .placeholder(R.drawable.placeholder_image)
                            .into(holder.gambar);
                }
            } catch (Exception e) {
                holder.gambar.setImageResource(R.drawable.placeholder_image);
            }
        } else {
            holder.gambar.setImageResource(R.drawable.placeholder_image);
        }

        // =============== CLICK UNTUK POP UP =============== //
        holder.itemView.setOnClickListener(v -> showPopupDialog(v, item));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    // ================= POP-UP DIALOG ================= //
    private void showPopupDialog(View v, Dokumentasi item) {
        Dialog dialog = new Dialog(v.getContext());
        dialog.setContentView(R.layout.dialog_gallery);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        ImageView img = dialog.findViewById(R.id.dialogImage);
        TextView title = dialog.findViewById(R.id.dialogTitle);
        TextView desc = dialog.findViewById(R.id.dialogDesc);
        TextView btnClose = dialog.findViewById(R.id.btnClose);

        title.setText(item.getNama());
        desc.setText(item.getDeskripsi());

        String g = item.getGambar();
        if (g != null && !g.trim().isEmpty()) {
            try {
                boolean isBase64 = !g.startsWith("http") && g.length() > 50;
                if (isBase64) {
                    byte[] bytes = Base64.decode(g, Base64.DEFAULT);
                    BitmapUtils.setBitmapToImageViewFromBytes(img, bytes);
                } else {
                    Glide.with(v.getContext()).load(g).into(img);
                }
            } catch (Exception ignored) {}
        }

        // Tombol close berfungsi
        btnClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView gambar;
        TextView nama, deskripsi;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            gambar = itemView.findViewById(R.id.imgGallery);
            nama = itemView.findViewById(R.id.txtNamaGallery);
            deskripsi = itemView.findViewById(R.id.txtDeskripsiGallery);
        }
    }
}