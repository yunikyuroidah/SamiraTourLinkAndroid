package com.example.samiratravelmobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.samiratravelmobile.utils.AuthManager;
import com.example.samiratravelmobile.utils.DeviceBlocker;
import com.example.samiratravelmobile.utils.ToastUtils;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.*;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FieldValue;
import java.util.HashMap;
import java.util.Map;

public class AdminLoginActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private Button btnGoogle;
    private TextView txtWelcome, txtDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_login);

        auth = FirebaseAuth.getInstance();
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        btnGoogle = findViewById(R.id.btnGoogle);
        txtWelcome = findViewById(R.id.txtWelcome);
        txtDesc = findViewById(R.id.txtDesc);

        if (topAppBar != null) {
            topAppBar.setNavigationOnClickListener(v -> finish());
        }

        // Jika sudah login
        if (AuthManager.isLoggedIn(this)) {
            startActivity(new Intent(this, EditPackageActivity.class));
            finish();
            return;
        }

        // Jika device diblokir
        if (DeviceBlocker.isBlocked(this)) {
            ToastUtils.showError(this, "Login diblokir 7 hari karena 3x gagal", ToastUtils.LENGTH_LONG);
            btnGoogle.setEnabled(false);
            return;
        }

        // Konfigurasi Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        btnGoogle.setOnClickListener(v -> signInWithGoogle());
    }

    private void signInWithGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                DeviceBlocker.registerFailure(this);
                ToastUtils.showError(this, "Login gagal: " + e.getMessage());
                if (DeviceBlocker.isBlocked(this)) {
                    ToastUtils.showError(this, "Device diblokir 7 hari", ToastUtils.LENGTH_LONG);
                    btnGoogle.setEnabled(false);
                }
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    validateAdminAccess();
                })
                .addOnFailureListener(e -> {
                    DeviceBlocker.registerFailure(this);
                    ToastUtils.showError(this, "Autentikasi gagal: " + e.getMessage());
                });
    }

    private void validateAdminAccess() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            handleUnauthorized("Akun tidak ditemukan");
            return;
        }

        btnGoogle.setEnabled(false);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("admin_access_validation")
                .document("access_" + currentUser.getUid());

        Map<String, Object> payload = new HashMap<>();
        payload.put("checkedAt", FieldValue.serverTimestamp());
        payload.put("uid", currentUser.getUid());

        docRef.set(payload)
                .addOnSuccessListener(unused ->
                        docRef.delete().addOnCompleteListener(task -> completeLogin()))
                .addOnFailureListener(e -> {
                    if (e instanceof FirebaseFirestoreException &&
                            ((FirebaseFirestoreException) e).getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        handleUnauthorized("Email tidak terdaftar sebagai admin");
                    } else {
                        ToastUtils.showError(this, "Verifikasi admin gagal: " + e.getMessage(), ToastUtils.LENGTH_LONG);
                        btnGoogle.setEnabled(true);
                    }
                });
    }

    private void completeLogin() {
        DeviceBlocker.clear(this);
        AuthManager.setLoggedIn(this, true);
        startActivity(new Intent(this, EditPackageActivity.class));
        finish();
    }

    private void handleUnauthorized(String message) {
        DeviceBlocker.registerFailure(this);
        ToastUtils.showError(this, message, ToastUtils.LENGTH_LONG);
        AuthManager.setLoggedIn(this, false);
        if (googleSignInClient != null) {
            googleSignInClient.signOut();
        }
        auth.signOut();
        if (DeviceBlocker.isBlocked(this)) {
            ToastUtils.showError(this, "Device diblokir 7 hari", ToastUtils.LENGTH_LONG);
            btnGoogle.setEnabled(false);
        } else {
            btnGoogle.setEnabled(true);
        }
    }
}
