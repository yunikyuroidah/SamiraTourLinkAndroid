package com.example.samiratravelmobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.samiratravelmobile.models.TourLeader;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.samiratravelmobile.utils.AuthManager;
import com.example.samiratravelmobile.utils.ToastUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class EditTourLeaderActivity extends AppCompatActivity {

    private static final String COLLECTION_NAME = "tour_leader";
    private static final String DOCUMENT_ID = "tour_leader_id";

    private MaterialCardView contentCard;
    private CircularProgressIndicator progressIndicator;
    private ShapeableImageView imgTourLeader;
    private TextInputEditText inputNama;
    private TextInputEditText inputTelepon;
    private MaterialButton btnChangePhoto;
    private MaterialButton btnSave;
    private MaterialButton btnEditMode;
    private MaterialButton btnCancel;
    private View actionButtonContainer;
    private BottomNavigationView bottomNavigationView;
    private TextView titleTextView;

    private FirebaseFirestore db;
    private String currentBase64Image = "";
    private String originalNama = "";
    private String originalTelepon = "";
    private String originalBase64Image = "";
    private boolean isEditMode;
    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_tour_leader);

        MaterialToolbar toolbar = findViewById(R.id.toolbarEditTourLeader);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> showExitConfirmation());
        toolbar.setTitle("Manajemen Tour Leader");
        toolbar.setSubtitle("Kelola pemandu wisata profesional");

        initViews();
        setupBottomNavigation();
        applyGradientToTitle(titleTextView);

        db = FirebaseFirestore.getInstance();
        fetchTourLeaderData();

        btnChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> saveTourLeaderData());
        btnEditMode.setOnClickListener(v -> setEditMode(true));
        btnCancel.setOnClickListener(v -> cancelEdit());

        setEditMode(false);
    }

    private void initViews() {
        contentCard = findViewById(R.id.cardTourLeader);
        progressIndicator = findViewById(R.id.progressTourLeader);
        imgTourLeader = findViewById(R.id.imgTourLeader);
        inputNama = findViewById(R.id.inputNamaTourLeader);
        inputTelepon = findViewById(R.id.inputTeleponTourLeader);
        btnChangePhoto = findViewById(R.id.btnChangePhoto);
        btnSave = findViewById(R.id.btnSaveTourLeader);
        btnEditMode = findViewById(R.id.btnEditModeTourLeader);
        btnCancel = findViewById(R.id.btnCancelTourLeader);
        actionButtonContainer = findViewById(R.id.layoutActionTourLeader);
        bottomNavigationView = findViewById(R.id.bottomAdminNav);
        titleTextView = findViewById(R.id.txtTitleTourLeader);
    }

    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_leader) {
                return true;
            } else if (itemId == R.id.action_packages) {
                navigateTo(EditPackageActivity.class);
                return true;
            } else if (itemId == R.id.action_documentation) {
                navigateTo(EditGalleryActivity.class);
                return true;
            } else if (itemId == R.id.action_profile) {
                navigateTo(EditProfileActivity.class);
                return true;
            } else if (itemId == R.id.action_logout) {
                showLogoutConfirmation();
                return true;
            }
            return false;
        });
        bottomNavigationView.setSelectedItemId(R.id.action_leader);
    }

    private void navigateTo(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        showExitConfirmation();
    }

    private void fetchTourLeaderData() {
        showLoading(true);
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(DOCUMENT_ID);
        docRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot != null && snapshot.exists()) {
                TourLeader leader = snapshot.toObject(TourLeader.class);
                if (leader != null) {
                    originalNama = leader.getNama() != null ? leader.getNama() : "";
                    originalTelepon = leader.getTelepon() != null ? leader.getTelepon() : "";
                    originalBase64Image = leader.getGambar() != null ? leader.getGambar() : "";
                    inputNama.setText(originalNama);
                    inputTelepon.setText(originalTelepon);
                    currentBase64Image = originalBase64Image;
                    applyImage(currentBase64Image);
                    setEditMode(false);
                }
            } else {
                ToastUtils.showInfo(this, "Data tour leader belum tersedia");
            }
            showLoading(false);
        }).addOnFailureListener(e -> {
            ToastUtils.showError(this, "Gagal memuat: " + e.getMessage(), ToastUtils.LENGTH_LONG);
            showLoading(false);
        });
    }

    private void saveTourLeaderData() {
        String nama = inputNama.getText() != null ? inputNama.getText().toString().trim() : "";
        String telepon = inputTelepon.getText() != null ? inputTelepon.getText().toString().trim() : "";

        if (TextUtils.isEmpty(nama)) {
            inputNama.setError("Nama tidak boleh kosong");
            return;
        }

        if (TextUtils.isEmpty(telepon)) {
            inputTelepon.setError("Nomor telepon tidak boleh kosong");
            return;
        }

        showSavingState(true);

        Map<String, Object> data = new HashMap<>();
        data.put("nama", nama);
        data.put("telepon", telepon);
        data.put("gambar", currentBase64Image != null ? currentBase64Image : "");

        db.collection(COLLECTION_NAME)
                .document(DOCUMENT_ID)
                .set(data)
                .addOnSuccessListener(unused -> {
                    ToastUtils.showSuccess(this, "Tour leader diperbarui");
                    showSavingState(false);
                    originalNama = nama;
                    originalTelepon = telepon;
                    originalBase64Image = currentBase64Image;
                    setEditMode(false);
                })
                .addOnFailureListener(e -> {
                    ToastUtils.showError(this, "Gagal menyimpan: " + e.getMessage(), ToastUtils.LENGTH_LONG);
                    showSavingState(false);
                });
    }

    private void cancelEdit() {
        restoreOriginalData();
        setEditMode(false);
    }

    private void restoreOriginalData() {
        inputNama.setText(originalNama);
        inputTelepon.setText(originalTelepon);
        inputNama.setError(null);
        inputTelepon.setError(null);
        currentBase64Image = originalBase64Image != null ? originalBase64Image : "";
        applyImage(currentBase64Image);
    }


    private void showLoading(boolean loading) {
        progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        int visibility = loading ? View.GONE : View.VISIBLE;
        contentCard.setVisibility(visibility);
        btnEditMode.setEnabled(!loading && !isEditMode);
        btnChangePhoto.setEnabled(!loading && isEditMode);
        btnChangePhoto.setAlpha(!loading && isEditMode ? 1f : 0.6f);
        btnSave.setEnabled(!loading && isEditMode);
        btnCancel.setEnabled(!loading && isEditMode);
        actionButtonContainer.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
    }

    private void showSavingState(boolean saving) {
        btnSave.setEnabled(!saving);
        btnSave.setText(saving ? "Menyimpan..." : "Simpan Perubahan");
        btnSave.setAlpha(saving ? 0.7f : 1f);
        btnCancel.setEnabled(!saving);
        btnCancel.setAlpha(saving ? 0.7f : 1f);
        btnChangePhoto.setEnabled(!saving);
        btnChangePhoto.setAlpha(!saving ? 1f : 0.6f);
    }

    private void setEditMode(boolean enabled) {
        boolean wasEditMode = isEditMode;
        isEditMode = enabled;
        if (enabled && !wasEditMode) {
            captureOriginalState();
        }
        inputNama.setEnabled(enabled);
        inputNama.setFocusableInTouchMode(enabled);
        inputTelepon.setEnabled(enabled);
        inputTelepon.setFocusableInTouchMode(enabled);
        btnChangePhoto.setEnabled(enabled);
        btnChangePhoto.setAlpha(enabled ? 1f : 0.6f);
        btnSave.setEnabled(enabled);
        btnSave.setAlpha(enabled ? 1f : 0.7f);
        btnSave.setText("Simpan Perubahan");
        btnSave.setVisibility(enabled ? View.VISIBLE : View.GONE);
        btnCancel.setEnabled(enabled);
        btnCancel.setAlpha(enabled ? 1f : 0.7f);
        btnCancel.setVisibility(enabled ? View.VISIBLE : View.GONE);
        actionButtonContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        btnEditMode.setVisibility(enabled ? View.GONE : View.VISIBLE);
        btnEditMode.setEnabled(!enabled);
        if (!enabled) {
            inputNama.clearFocus();
            inputTelepon.clearFocus();
        }
    }

    private void captureOriginalState() {
        originalNama = inputNama.getText() != null ? inputNama.getText().toString() : "";
        originalTelepon = inputTelepon.getText() != null ? inputTelepon.getText().toString() : "";
        originalBase64Image = currentBase64Image != null ? currentBase64Image : "";
    }

    private void onImagePicked(@Nullable Uri uri) {
        if (uri == null) {
            return;
        }
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                ToastUtils.showError(this, "Tidak dapat membaca gambar");
                return;
            }
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap != null) {
                imgTourLeader.setImageBitmap(bitmap);
                currentBase64Image = encodeBitmapToBase64(bitmap);
            }
        } catch (IOException e) {
            ToastUtils.showError(this, "Gagal memproses gambar");
        }
    }

    private void applyImage(String base64) {
        if (TextUtils.isEmpty(base64)) {
            imgTourLeader.setImageResource(R.drawable.ic_admin_leader);
            return;
        }
        try {
            byte[] decodedBytes = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            if (bitmap != null) {
                imgTourLeader.setImageBitmap(bitmap);
            } else {
                imgTourLeader.setImageResource(R.drawable.ic_admin_leader);
            }
        } catch (IllegalArgumentException e) {
            imgTourLeader.setImageResource(R.drawable.ic_admin_leader);
        }
    }

    private String encodeBitmapToBase64(Bitmap bitmap) {
        int targetWidth = Math.min(800, bitmap.getWidth());
        float ratio = bitmap.getWidth() == 0 ? 1f : (float) targetWidth / (float) bitmap.getWidth();
        int targetHeight = Math.max(1, Math.round(bitmap.getHeight() * ratio));
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] bytes = outputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private void applyGradientToTitle(TextView textView) {
        textView.post(() -> {
            TextPaint paint = textView.getPaint();
            float width = paint.measureText(textView.getText().toString());
            Shader shader = new LinearGradient(0, 0, width, textView.getTextSize(),
                    new int[]{ContextCompat.getColor(this, R.color.brand_gold), ContextCompat.getColor(this, R.color.brand_navy)},
                    null, Shader.TileMode.CLAMP);
            paint.setShader(shader);
            textView.invalidate();
        });
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
