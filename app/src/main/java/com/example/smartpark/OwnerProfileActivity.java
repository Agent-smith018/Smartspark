package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class OwnerProfileActivity extends AppCompatActivity {

    private TextView tvProfileName;
    private TextView tvProfileEmail;
    private TextView tvProfileRole;
    private TextView tvTotalSpots;
    private TextView tvActiveSpots;
    private Button btnBackToDashboard;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        android.view.View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        tvProfileName = findViewById(R.id.tv_owner_profile_name);
        tvProfileEmail = findViewById(R.id.tv_owner_profile_email);
        tvProfileRole = findViewById(R.id.tv_owner_profile_role);
        tvTotalSpots = findViewById(R.id.tv_owner_total_spots);
        tvActiveSpots = findViewById(R.id.tv_owner_active_spots);
        btnBackToDashboard = findViewById(R.id.btn_back_to_dashboard);

        btnBackToDashboard.setOnClickListener(v -> finish());

        loadProfileData();
        loadSpotStats();
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
                    String role = documentSnapshot.getString("role");
                    
                    if (name != null && !name.trim().isEmpty()) {
                        tvProfileName.setText(name);
                    } else {
                        tvProfileName.setText("Owner");
                    }
                    
                    if (role != null) {
                        // Capitalize first letter
                        String displayRole = role.substring(0, 1).toUpperCase() + role.substring(1);
                        tvProfileRole.setText(displayRole);
                    }
                })
                .addOnFailureListener(e -> tvProfileName.setText("Owner"));
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
                .whereEqualTo("ownerId", uid)
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
}
