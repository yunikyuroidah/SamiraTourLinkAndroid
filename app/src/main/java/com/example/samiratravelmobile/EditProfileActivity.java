package com.example.samiratravelmobile;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
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

public class EditProfileActivity extends AppCompatActivity {
    private static final String COLLECTION_NAME = "profil_travel";
    private static final String DOCUMENT_ID = "profil_id";

    private TextInputEditText alamatInput;
    private TextInputEditText emailInput;
    private ShapeableImageView profileImage;
    private MaterialButton btnChangePhoto;
    private MaterialButton btnSave;
    private MaterialButton btnEditMode;
    private MaterialButton btnCancel;
    private View actionButtonContainer;
    private CircularProgressIndicator progressIndicator;
    private FirebaseFirestore db;
    private String currentBase64Image = "";
    private String originalAlamat = "";
    private String originalEmail = "";
    private String originalBase64Image = "";
    private boolean isEditMode;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), this::onImagePicked);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        MaterialToolbar toolbar = findViewById(R.id.toolbarEditProfile);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Profil Perusahaan");
        toolbar.setSubtitle("Pastikan data travel selalu mutakhir");
        toolbar.setNavigationOnClickListener(v -> showExitConfirmation());

        alamatInput = findViewById(R.id.inputAlamat);
        emailInput = findViewById(R.id.inputEmail);
        profileImage = findViewById(R.id.imgProfilTravel);
        btnChangePhoto = findViewById(R.id.btnChangePhotoProfile);
        btnSave = findViewById(R.id.btnSaveProfile);
        btnEditMode = findViewById(R.id.btnEditModeProfile);
        btnCancel = findViewById(R.id.btnCancelProfile);
        actionButtonContainer = findViewById(R.id.layoutActionProfile);
        progressIndicator = findViewById(R.id.progressProfile);

        db = FirebaseFirestore.getInstance();

        btnChangePhoto.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));
        btnSave.setOnClickListener(v -> saveProfile());
        btnEditMode.setOnClickListener(v -> setEditMode(true));
        btnCancel.setOnClickListener(v -> cancelEdit());

        setEditMode(false);
        setupBottomNavigation();

        fetchProfile();
    }

    private void fetchProfile() {
        showLoading(true);
        DocumentReference docRef = db.collection(COLLECTION_NAME).document(DOCUMENT_ID);
        docRef.get().addOnSuccessListener(snapshot -> {
            if (snapshot != null && snapshot.exists()) {
                originalAlamat = snapshot.getString("alamat") != null ? snapshot.getString("alamat") : "";
                originalEmail = snapshot.getString("email") != null ? snapshot.getString("email") : "";
                originalBase64Image = snapshot.getString("gambar") != null ? snapshot.getString("gambar") : "";

                alamatInput.setText(originalAlamat);
                emailInput.setText(originalEmail);
                currentBase64Image = originalBase64Image;
                applyImage(currentBase64Image);
            }
            showLoading(false);
        }).addOnFailureListener(e -> {
            ToastUtils.showError(this, "Gagal memuat profil: " + e.getMessage(), ToastUtils.LENGTH_LONG);
            showLoading(false);
        });
    }

    private void saveProfile() {
        String alamat = alamatInput.getText() != null ? alamatInput.getText().toString().trim() : "";
        String email = emailInput.getText() != null ? emailInput.getText().toString().trim() : "";

        if (TextUtils.isEmpty(alamat)) {
            alamatInput.setError("Alamat tidak boleh kosong");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email tidak boleh kosong");
            return;
        }

        showSavingState(true);
        Map<String, Object> data = new HashMap<>();
        data.put("alamat", alamat);
        data.put("email", email);
        data.put("gambar", currentBase64Image != null ? currentBase64Image : "");

        db.collection(COLLECTION_NAME).document(DOCUMENT_ID)
                .set(data)
                .addOnSuccessListener(unused -> {
                    ToastUtils.showSuccess(this, "Profil disimpan");
                    originalAlamat = alamat;
                    originalEmail = email;
                    originalBase64Image = currentBase64Image != null ? currentBase64Image : "";
                    showSavingState(false);
                    setEditMode(false);
                })
                .addOnFailureListener(e -> {
                    ToastUtils.showError(this, "Gagal menyimpan: " + e.getMessage(), ToastUtils.LENGTH_LONG);
                    showSavingState(false);
                });
    }

    private void showLoading(boolean loading) {
        progressIndicator.setVisibility(loading ? View.VISIBLE : View.GONE);
        profileImage.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        btnEditMode.setEnabled(!loading && !isEditMode);
        btnChangePhoto.setEnabled(!loading && isEditMode);
        btnSave.setEnabled(!loading && isEditMode);
        btnCancel.setEnabled(!loading && isEditMode);
        actionButtonContainer.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
    }

    private void showSavingState(boolean saving) {
        btnSave.setEnabled(!saving);
        btnSave.setText(saving ? "Menyimpan..." : "Simpan Perubahan");
        btnSave.setAlpha(saving ? 0.7f : 1f);
        btnChangePhoto.setEnabled(!saving);
        btnChangePhoto.setAlpha(!saving ? 1f : 0.6f);
        btnCancel.setEnabled(!saving);
        btnCancel.setAlpha(!saving ? 1f : 0.7f);
    }

    private void setEditMode(boolean enabled) {
        boolean wasEditMode = isEditMode;
        isEditMode = enabled;
        if (enabled && !wasEditMode) {
            captureOriginalState();
        }
        alamatInput.setEnabled(enabled);
        alamatInput.setFocusableInTouchMode(enabled);
        emailInput.setEnabled(enabled);
        emailInput.setFocusableInTouchMode(enabled);
        btnChangePhoto.setEnabled(enabled);
        btnChangePhoto.setAlpha(enabled ? 1f : 0.6f);
        btnSave.setVisibility(enabled ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(enabled);
        btnSave.setAlpha(enabled ? 1f : 0.7f);
        btnSave.setText("Simpan Perubahan");
        btnEditMode.setVisibility(enabled ? View.GONE : View.VISIBLE);
        btnEditMode.setEnabled(!enabled);
        btnCancel.setEnabled(enabled);
        btnCancel.setAlpha(enabled ? 1f : 0.7f);
        btnCancel.setVisibility(enabled ? View.VISIBLE : View.GONE);
        actionButtonContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        if (!enabled) {
            alamatInput.clearFocus();
            emailInput.clearFocus();
        }
    }

    private void captureOriginalState() {
        originalAlamat = alamatInput.getText() != null ? alamatInput.getText().toString() : "";
        originalEmail = emailInput.getText() != null ? emailInput.getText().toString() : "";
        originalBase64Image = currentBase64Image != null ? currentBase64Image : "";
    }

    private void cancelEdit() {
        alamatInput.setText(originalAlamat);
        emailInput.setText(originalEmail);
        currentBase64Image = originalBase64Image != null ? originalBase64Image : "";
        applyImage(currentBase64Image);
        alamatInput.setError(null);
        emailInput.setError(null);
        setEditMode(false);
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomAdminNav);
        if (bottomNavigationView == null) {
            return;
        }
        bottomNavigationView.setSelectedItemId(R.id.action_profile);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_profile) {
                return true;
            } else if (itemId == R.id.action_packages) {
                navigateTo(EditPackageActivity.class);
                return true;
            } else if (itemId == R.id.action_documentation) {
                navigateTo(EditGalleryActivity.class);
                return true;
            } else if (itemId == R.id.action_leader) {
                navigateTo(EditTourLeaderActivity.class);
                return true;
            } else if (itemId == R.id.action_logout) {
                showLogoutConfirmation();
                return true;
            }
            return false;
        });
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

    private void onImagePicked(@Nullable Uri uri) {
        if (uri == null) {
            return;
        }
        try (InputStream inputStream = getContentResolver().openInputStream(uri)) {
            if (inputStream == null) {
                ToastUtils.showError(this, "Tidak dapat membaca gambar");
                return;
            }

            byte[] bytes = readBytes(inputStream, 1024 * 1024);
            if (bytes.length >= 1024 * 1024) {
                ToastUtils.showError(this, "Ukuran gambar melebihi 1MB", ToastUtils.LENGTH_LONG);
                return;
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            if (bitmap != null) {
                profileImage.setImageBitmap(bitmap);
                currentBase64Image = encodeBitmapToBase64(bitmap);
            }
        } catch (IOException e) {
            ToastUtils.showError(this, "Gagal memproses gambar");
        }
    }

    private void applyImage(String base64) {
        if (TextUtils.isEmpty(base64)) {
            profileImage.setImageResource(R.drawable.ic_admin_profile);
            return;
        }
        try {
            byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
            if (bitmap != null) {
                profileImage.setImageBitmap(bitmap);
            } else {
                profileImage.setImageResource(R.drawable.ic_admin_profile);
            }
        } catch (IllegalArgumentException e) {
            profileImage.setImageResource(R.drawable.ic_admin_profile);
        }
    }

    private String encodeBitmapToBase64(Bitmap bitmap) {
        int targetWidth = Math.min(1000, bitmap.getWidth());
        float ratio = bitmap.getWidth() == 0 ? 1f : (float) targetWidth / (float) bitmap.getWidth();
        int targetHeight = Math.max(1, Math.round(bitmap.getHeight() * ratio));
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, targetWidth, targetHeight, true);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.JPEG, 80, outputStream);
        byte[] bytes = outputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private byte[] readBytes(InputStream inputStream, int maxBytes) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[4096];
        int nRead;
        while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
            if (buffer.size() > maxBytes) {
                break;
            }
        }
        return buffer.toByteArray();
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
