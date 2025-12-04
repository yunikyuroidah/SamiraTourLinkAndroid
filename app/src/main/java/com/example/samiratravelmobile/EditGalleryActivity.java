package com.example.samiratravelmobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// PERBAIKAN 1: Tambahkan import untuk Glide
import com.bumptech.glide.Glide;

import com.example.samiratravelmobile.adapters.AdminGalleryAdapter;
import com.example.samiratravelmobile.models.Dokumentasi;
import com.example.samiratravelmobile.BitmapUtils;
import com.example.samiratravelmobile.utils.ToastUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.example.samiratravelmobile.utils.AuthManager;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class EditGalleryActivity extends AppCompatActivity implements AdminGalleryAdapter.OnActionListener {
    private RecyclerView recyclerView;
    private final List<Dokumentasi> list = new ArrayList<>();
    private AdminGalleryAdapter adapter;
    private FirebaseFirestore db;

    private ActivityResultLauncher<String> pickImageLauncher;
    private static final int MAX_BYTES = 1_000_000; // 1 MB

    // dialog temp state
    private String pickedBase64 = "";
    private ImageView currentPreviewImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_gallery);

        MaterialToolbar toolbar = findViewById(R.id.toolbarEditGallery);
        toolbar.setNavigationOnClickListener(v -> showExitConfirmation());

        recyclerView = findViewById(R.id.recyclerAdminGallery);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        db = FirebaseFirestore.getInstance();

        adapter = new AdminGalleryAdapter(this, list, this);
        recyclerView.setAdapter(adapter);

        // picker
        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
            if (uri != null) {
                try {
                    byte[] bytes = readBytesFromUri(uri);
                    if (bytes.length > MAX_BYTES) {
                        ToastUtils.showError(this, "Ukuran gambar lebih dari 1MB, pilih gambar lebih kecil.", ToastUtils.LENGTH_LONG);
                        return;
                    }
                    pickedBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP);
                    if (currentPreviewImage != null) {
                        BitmapUtils.setBitmapToImageViewFromBytes(currentPreviewImage, bytes);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    ToastUtils.showError(this, "Gagal membaca gambar: " + e.getMessage());
                }
            }
        });

        loadData();
        setupBottomNavigation();

        ExtendedFloatingActionButton fab = findViewById(R.id.fabAddGallery);
        fab.setOnClickListener(v -> startAddDialog());
    }

    private byte[] readBytesFromUri(Uri uri) throws Exception {
        InputStream is = getContentResolver().openInputStream(uri);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((is != null) && (nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        if (is != null) is.close();
        buffer.flush();
        return buffer.toByteArray();
    }

    private void loadData() {
        db.collection("dokumentasi").get().addOnSuccessListener(snapshot -> {
            list.clear();
            for (QueryDocumentSnapshot doc : snapshot) {
                Dokumentasi d = doc.toObject(Dokumentasi.class);
                d.setId(doc.getId());
                list.add(d);
            }
            adapter.notifyDataSetChanged();
        }).addOnFailureListener(e -> ToastUtils.showError(this, "Gagal memuat: " + e.getMessage()));
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottom = findViewById(R.id.bottomAdminNav);
        if (bottom == null) return;
        bottom.setSelectedItemId(R.id.action_documentation);
        bottom.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.action_packages) {
                startActivity(new Intent(this, EditPackageActivity.class));
                finish();
                return true;
            } else if (id == R.id.action_leader) {
                startActivity(new Intent(this, EditTourLeaderActivity.class));
                finish();
                return true;
            } else if (id == R.id.action_profile) {
                startActivity(new Intent(this, EditProfileActivity.class));
                finish();
                return true;
            } else if (id == R.id.action_logout) {
                showLogoutConfirmation();
                return true;
            }
            return false;
        });
    }

    // add dialog
    private void startAddDialog() {
        // limit 10
        if (list.size() >= 10) {
            ToastUtils.showInfo(this, "Maksimal 10 dokumentasi. Hapus beberapa item sebelum menambah.", ToastUtils.LENGTH_LONG);
            return;
        }
        showDialogForDokumentasi(null);
    }

    // edit dialog (if d != null)
    private void showDialogForDokumentasi(Dokumentasi d) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_gallery, null);
        EditText inputNama = dialogView.findViewById(R.id.inputNamaGallery);
        EditText inputDeskripsi = dialogView.findViewById(R.id.inputDeskripsiGallery);
        ImageView preview = dialogView.findViewById(R.id.previewImageGallery);
        Button btnPick = dialogView.findViewById(R.id.btnPickImage);

        pickedBase64 = "";
        currentPreviewImage = preview;

        // 🌟 Saat mode EDIT, isi field nama & deskripsi
        if (d != null) {
            inputNama.setText(d.getNama());
            inputDeskripsi.setText(d.getDeskripsi());

            String g = d.getGambar();
            if (g != null && !g.trim().isEmpty()) {
                try {
                    boolean isBase64 = !g.startsWith("http") && g.length() > 50;
                    if (isBase64) {
                        byte[] bytes = Base64.decode(g, Base64.DEFAULT);
                        BitmapUtils.setBitmapToImageViewFromBytes(preview, bytes);
                    } else {
                        Glide.with(this)
                                .load(g)
                                .placeholder(R.drawable.ic_broken_image)
                                .into(preview);
                    }
                } catch (Exception ex) {
                    preview.setImageResource(R.drawable.ic_broken_image);
                }
            }
        } else {
            // Mode tambah
            preview.setImageResource(R.drawable.ic_broken_image);
        }

        btnPick.setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.logo)
            .setTitle(d == null ? "Tambah Dokumentasi" : "Edit Dokumentasi")
                .setView(dialogView)
                .setPositiveButton("Simpan", (dlg, which) -> {
                    String nama = inputNama.getText().toString().trim();
                    String deskripsi = inputDeskripsi.getText().toString().trim();

                    if (nama.isEmpty()) {
                        ToastUtils.showError(this, "Nama harus diisi");
                        return;
                    }

                    String base64ToSave = pickedBase64;
                    if ((base64ToSave == null || base64ToSave.isEmpty()) && d != null) {
                        base64ToSave = d.getGambar();
                    }

                    if (d != null) {
                        d.setNama(nama);
                        d.setDeskripsi(deskripsi);
                        d.setGambar(base64ToSave);
                        db.collection("dokumentasi").document(d.getId()).set(d)
                                .addOnSuccessListener(a -> {
                                    ToastUtils.showSuccess(this, "Diperbarui");
                                    loadData();
                                })
                                .addOnFailureListener(e -> ToastUtils.showError(this, "Gagal: " + e.getMessage()));
                    } else {
                        Dokumentasi newD = new Dokumentasi();
                        newD.setNama(nama);
                        newD.setDeskripsi(deskripsi);
                        newD.setGambar(base64ToSave);
                        db.collection("dokumentasi").add(newD)
                                .addOnSuccessListener(ref -> {
                                    ToastUtils.showSuccess(this, "Dokumentasi ditambahkan");
                                    loadData();
                                })
                                .addOnFailureListener(e -> ToastUtils.showError(this, "Gagal menambah: " + e.getMessage()));
                    }

                    pickedBase64 = "";
                    currentPreviewImage = null;
                })
                .setNegativeButton("Batal", (dlg, which) -> {
                    pickedBase64 = "";
                    currentPreviewImage = null;
                    dlg.dismiss();
                })
                .create();

        dialog.show();
    }

    // adapter callbacks
    @Override
    public void onEditRequested(Dokumentasi d) {
        showDialogForDokumentasi(d);
    }

    @Override
    public void onDeleteRequested(Dokumentasi d) {
        new MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.logo)
            .setTitle("Hapus Dokumentasi?")
                .setMessage("Hapus: " + d.getNama() + " ?")
                .setPositiveButton("Hapus", (dialog, which) -> {
                    db.collection("dokumentasi").document(d.getId()).delete()
                            .addOnSuccessListener(v -> {
                                ToastUtils.showSuccess(this, "Dihapus");
                                loadData();
                            })
                            .addOnFailureListener(e -> ToastUtils.showError(this, "Gagal hapus: " + e.getMessage()));
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        showExitConfirmation();
    }

    private void showExitConfirmation() {
        new MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.logo)
            .setTitle("Keluar dari admin?")
                .setMessage("Apakah anda yakin keluar dari halaman admin?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    dialog.dismiss();
                    navigateToLoginAndClearStack();
                })
                .setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(this)
            .setIcon(R.drawable.logo)
            .setTitle("Keluar dari admin?")
                .setMessage("Apakah anda yakin keluar dari halaman admin?")
                .setPositiveButton("Ya", (dialog, which) -> {
                    dialog.dismiss();
                    navigateToLoginAndClearStack();
                })
                .setNegativeButton("Tidak", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void navigateToLoginAndClearStack() {
        FirebaseAuth.getInstance().signOut();
        AuthManager.setLoggedIn(this, false);
        Intent intent = new Intent(this, AdminLoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
