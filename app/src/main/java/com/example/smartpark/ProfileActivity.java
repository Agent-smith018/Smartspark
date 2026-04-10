package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvTotalSpots;
    private TextView tvActiveSpots;
    private TextInputEditText etEditName;
    private Button btnUpdateName;
    private Button btnLogout;
    private Button btnViewFavorites;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        enforceDriverAccess();

        tvProfileName = findViewById(R.id.tv_profile_name);
        tvProfileEmail = findViewById(R.id.tv_profile_email);
        tvTotalSpots = findViewById(R.id.tv_total_spots);
        tvActiveSpots = findViewById(R.id.tv_active_spots);
        etEditName = findViewById(R.id.et_edit_name);
        btnUpdateName = findViewById(R.id.btn_update_name);
        btnLogout = findViewById(R.id.btn_logout);
        btnViewFavorites = findViewById(R.id.btn_view_favorites);

        loadProfileData();
        loadSpotStats();

        btnUpdateName.setOnClickListener(v -> updateName());
        btnLogout.setOnClickListener(v -> logout());
        btnViewFavorites.setOnClickListener(v ->
            startActivity(new Intent(ProfileActivity.this, DriverFavoritesActivity.class)));
    }

    private void loadProfileData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            tvProfileName.setText("Guest");
            tvProfileEmail.setText("Not signed in");
            return;
        }

        String uid = currentUser.getUid();
        tvProfileEmail.setText(currentUser.getEmail() != null ? currentUser.getEmail() : "No email");

        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String name = documentSnapshot.getString("name");
                    if (name != null && !name.trim().isEmpty()) {
                        tvProfileName.setText(name);
                    } else {
                        tvProfileName.setText("Driver");
                    }
                })
                .addOnFailureListener(e -> tvProfileName.setText("Driver"));
    }

    private void loadSpotStats() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            tvTotalSpots.setText("0");
            tvActiveSpots.setText("0");
            return;
        }

        String uid = currentUser.getUid();

        db.collection("parking_spots")
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int total = querySnapshot.size();
                    int active = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String status = doc.getString("status");
                        if (status != null && status.equalsIgnoreCase("available")) {
                            active++;
                        }
                    }

                    tvTotalSpots.setText(String.valueOf(total));
                    tvActiveSpots.setText(String.valueOf(active));
                })
                .addOnFailureListener(e -> {
                    tvTotalSpots.setText("0");
                    tvActiveSpots.setText("0");
                });
    }

    private void enforceDriverAccess() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(ProfileActivity.this, MainActivity.class));
            finish();
            return;
        }

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if ("owner".equalsIgnoreCase(role)) {
                        startActivity(new Intent(ProfileActivity.this, OwnerDashboardActivity.class));
                        finish();
                    } else if ("admin".equalsIgnoreCase(role)) {
                        startActivity(new Intent(ProfileActivity.this, AdminDashboardActivity.class));
                        finish();
                    }
                });
    }

    private void updateName() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        String newName = etEditName.getText() != null ? etEditName.getText().toString().trim() : "";
        if (TextUtils.isEmpty(newName)) {
            etEditName.setError("Name is required");
            return;
        }

        db.collection("users")
                .document(currentUser.getUid())
                .update("name", newName)
                .addOnSuccessListener(unused -> {
                    tvProfileName.setText(newName);
                    etEditName.setText("");
                    Toast.makeText(this, "Name updated", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to update name", Toast.LENGTH_SHORT).show());
    }

    private void logout() {
        mAuth.signOut();
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
