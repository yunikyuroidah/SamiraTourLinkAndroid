package com.example.samiratravelmobile;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.samiratravelmobile.adapters.AdminPackageAdapter;
import com.example.samiratravelmobile.utils.ToastUtils;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.example.samiratravelmobile.utils.AuthManager;
import java.util.*;

public class EditPackageActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ExtendedFloatingActionButton fabAdd;
    private final List<Map<String, Object>> paketList = new ArrayList<>();
    private AdminPackageAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_package);

        MaterialToolbar toolbar = findViewById(R.id.toolbarEditPackage);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Kelola Paket");
        toolbar.setSubtitle("Perbarui paket unggulan Anda");
        toolbar.setNavigationOnClickListener(v -> showExitConfirmation());

        recyclerView = findViewById(R.id.recyclerAdminPaket);
        fabAdd = findViewById(R.id.fabAddPaket);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new AdminPackageAdapter(this, paketList, this::refreshData);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();
        loadData();

        fabAdd.setOnClickListener(v -> adapter.addNewPaket());

        setupBottomNavigation();
    }

    private void loadData() {
        db.collection("paket").get()
                .addOnSuccessListener(snapshot -> {
                    paketList.clear();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : snapshot) {
                        Map<String, Object> data = doc.getData();
                        data.put("id", doc.getId());
                        paketList.add(data);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> ToastUtils.showError(this, "Gagal memuat: " + e.getMessage()));
    }

    private void refreshData() {
        loadData();
    }

    /** 🧭 Bottom Navigation Admin **/
    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomAdminNav);
        bottomNavigationView.setSelectedItemId(R.id.action_packages);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_packages) {
                return true;
            } else if (itemId == R.id.action_documentation) {
                navigateAndFinish(EditGalleryActivity.class);
                return true;
            } else if (itemId == R.id.action_leader) {
                navigateAndFinish(EditTourLeaderActivity.class);
                return true;
            } else if (itemId == R.id.action_profile) {
                navigateAndFinish(EditProfileActivity.class);
                return true;
            } else if (itemId == R.id.action_logout) {
                showLogoutConfirmation();
                return true;
            }
            return false;
        });
    }

    private void navigateAndFinish(Class<?> target) {
        Intent intent = new Intent(this, target);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
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
