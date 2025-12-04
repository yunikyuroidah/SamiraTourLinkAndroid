package com.example.samiratravelmobile.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.samiratravelmobile.R;
import com.example.samiratravelmobile.utils.ToastUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminPackageAdapter extends RecyclerView.Adapter<AdminPackageAdapter.PackageViewHolder> {
    private final Context context;
    private final List<Map<String, Object>> paketList;
    private final FirebaseFirestore db;
    private final Runnable refreshCallback;

    public AdminPackageAdapter(Context context, List<Map<String, Object>> paketList, Runnable refreshCallback) {
        this.context = context;
        this.paketList = paketList;
        this.db = FirebaseFirestore.getInstance();
        this.refreshCallback = refreshCallback;
    }

    @NonNull
    @Override
    public PackageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_package_admin, parent, false);
        return new PackageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PackageViewHolder holder, int position) {
        Map<String, Object> paket = paketList.get(position);
        String id = (String) paket.get("id");
        String nama = (String) paket.get("nama_paket");
        String deskripsi = (String) paket.get("deskripsi");

        holder.txtNama.setText(nama);
        holder.txtDeskripsi.setText(deskripsi);

        // Fasilitas (pakai bullet)
        Object fasilitasObj = paket.get("fasilitas");
        if (fasilitasObj instanceof List) {
            List<String> fasilitas = (List<String>) fasilitasObj;
            holder.txtFasilitas.setText("• " + TextUtils.join("\n• ", fasilitas));
        } else {
            holder.txtFasilitas.setText("-");
        }

        // Features (pakai bullet)
        Object fiturObj = paket.get("features");
        if (fiturObj instanceof List) {
            List<String> fitur = (List<String>) fiturObj;
            holder.txtFitur.setText("• " + TextUtils.join("\n• ", fitur));
        } else {
            holder.txtFitur.setText("-");
        }

        holder.btnEdit.setOnClickListener(v -> showEditDialog(id, paket));
        holder.btnHapus.setOnClickListener(v -> confirmDelete(id, nama));
    }

    @Override
    public int getItemCount() {
        return paketList.size();
    }

    /** ==================== EDIT PAKET ==================== */
    private void showEditDialog(String id, Map<String, Object> paket) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_package, null);

        TextView lblNama = new TextView(context);
        lblNama.setText("Nama Paket");
        lblNama.setPadding(0, 0, 0, 8);
        lblNama.setTextSize(14);

        EditText nama = dialogView.findViewById(R.id.inputNamaPaketDialog);
        EditText deskripsi = dialogView.findViewById(R.id.inputDeskripsiPaketDialog);
        EditText fasilitas = dialogView.findViewById(R.id.inputFasilitasPaketDialog);
        EditText fitur = dialogView.findViewById(R.id.inputFeaturesPaketDialog);

        nama.setText((String) paket.get("nama_paket"));
        deskripsi.setText((String) paket.get("deskripsi"));

        Object fasilitasObj = paket.get("fasilitas");
        if (fasilitasObj instanceof List)
            fasilitas.setText(TextUtils.join("\n", (List<String>) fasilitasObj));

        Object fiturObj = paket.get("features");
        if (fiturObj instanceof List)
            fitur.setText(TextUtils.join("\n", (List<String>) fiturObj));

        new MaterialAlertDialogBuilder(context)
            .setIcon(R.drawable.logo)
            .setTitle("Edit Paket")
                .setView(dialogView)
                .setPositiveButton("Simpan", (d, w) -> {
                    Map<String, Object> update = new HashMap<>();
                    update.put("nama_paket", nama.getText().toString().trim());
                    update.put("deskripsi", deskripsi.getText().toString().trim());
                    update.put("fasilitas", Arrays.asList(fasilitas.getText().toString().split("\\s*\\n\\s*")));
                    update.put("features", Arrays.asList(fitur.getText().toString().split("\\s*\\n\\s*")));

                    db.collection("paket").document(id).update(update)
                            .addOnSuccessListener(v -> {
                                ToastUtils.showSuccess(context, "Paket berhasil diperbarui");
                                if (refreshCallback != null) refreshCallback.run();
                            })
                            .addOnFailureListener(e -> ToastUtils.showError(context, "Gagal: " + e.getMessage()));
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    /** ==================== HAPUS PAKET ==================== */
    private void confirmDelete(String id, String nama) {
        new MaterialAlertDialogBuilder(context)
            .setIcon(R.drawable.logo)
            .setTitle("Hapus Paket")
                .setMessage("Yakin ingin menghapus paket '" + nama + "'?")
                .setPositiveButton("Hapus", (d, w) -> db.collection("paket").document(id).delete()
                        .addOnSuccessListener(v -> {
                            ToastUtils.showSuccess(context, "Paket dihapus");
                            if (refreshCallback != null) refreshCallback.run();
                        })
                        .addOnFailureListener(e -> ToastUtils.showError(context, "Gagal: " + e.getMessage())))
                .setNegativeButton("Batal", null)
                .show();
    }

    /** ==================== TAMBAH PAKET ==================== */
    public void addNewPaket() {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_package, null);
        EditText nama = dialogView.findViewById(R.id.inputNamaPaketDialog);
        EditText deskripsi = dialogView.findViewById(R.id.inputDeskripsiPaketDialog);
        EditText fasilitas = dialogView.findViewById(R.id.inputFasilitasPaketDialog);
        EditText fitur = dialogView.findViewById(R.id.inputFeaturesPaketDialog);

        new MaterialAlertDialogBuilder(context)
            .setIcon(R.drawable.logo)
            .setTitle("Tambah Paket Baru")
                .setView(dialogView)
                .setPositiveButton("Simpan", (d, w) -> {
                    String namaStr = nama.getText().toString().trim();
                    String deskripsiStr = deskripsi.getText().toString().trim();

                    if (TextUtils.isEmpty(namaStr) || TextUtils.isEmpty(deskripsiStr)) {
                        ToastUtils.showError(context, "Nama dan deskripsi wajib diisi");
                        return;
                    }

                    Map<String, Object> newData = new HashMap<>();
                    newData.put("nama_paket", namaStr);
                    newData.put("deskripsi", deskripsiStr);
                    newData.put("fasilitas", Arrays.asList(fasilitas.getText().toString().split("\\s*\\n\\s*")));
                    newData.put("features", Arrays.asList(fitur.getText().toString().split("\\s*\\n\\s*")));

                    db.collection("paket").add(newData)
                            .addOnSuccessListener(v -> {
                                ToastUtils.showSuccess(context, "Paket berhasil ditambahkan");
                                if (refreshCallback != null) refreshCallback.run();
                            })
                            .addOnFailureListener(e -> ToastUtils.showError(context, "Gagal: " + e.getMessage()));
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    /** ==================== HOLDER ==================== */
    static class PackageViewHolder extends RecyclerView.ViewHolder {
        TextView txtNama, txtDeskripsi, txtFasilitas, txtFitur;
        Button btnEdit, btnHapus;

        public PackageViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNama = itemView.findViewById(R.id.txtNamaPaket);
            txtDeskripsi = itemView.findViewById(R.id.txtDeskripsiPaket);
            txtFasilitas = itemView.findViewById(R.id.txtFasilitasPaket);
            txtFitur = itemView.findViewById(R.id.txtFiturPaket);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnHapus = itemView.findViewById(R.id.btnHapus);
        }
    }
}
