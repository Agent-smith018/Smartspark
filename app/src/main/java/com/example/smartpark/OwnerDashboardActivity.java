package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class OwnerDashboardActivity extends AppCompatActivity {

    private android.widget.TextView tvStatTotal;
    private android.widget.TextView tvStatAvailable;
    private android.widget.TextView tvStatOccupied;
    private com.google.firebase.firestore.ListenerRegistration statsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_dashboard);

        enforceOwnerAccess();

        tvStatTotal = findViewById(R.id.tv_stat_total);
        tvStatAvailable = findViewById(R.id.tv_stat_available);
        tvStatOccupied = findViewById(R.id.tv_stat_occupied);

        android.view.View btnManageSpots = findViewById(R.id.btn_manage_spots);
        android.view.View btnAddSpot = findViewById(R.id.btn_add_spot);
        android.view.View btnOwnerProfile = findViewById(R.id.btn_owner_profile);
        android.view.View btnHistory = findViewById(R.id.btn_history);
        android.view.View btnParkingRequests = findViewById(R.id.btn_parking_requests);
        android.view.View btnOwnerLogout = findViewById(R.id.btn_owner_logout);

        btnManageSpots.setOnClickListener(v -> startActivity(new Intent(OwnerDashboardActivity.this, OwnerSpotActivity.class)));
        btnAddSpot.setOnClickListener(v -> startActivity(new Intent(OwnerDashboardActivity.this, AddParkingSpotActivity.class)));
        btnOwnerProfile.setOnClickListener(v -> startActivity(new Intent(OwnerDashboardActivity.this, OwnerProfileActivity.class)));
        btnHistory.setOnClickListener(v -> startActivity(new Intent(OwnerDashboardActivity.this, OwnerHistoryActivity.class)));
        btnParkingRequests.setOnClickListener(v -> startActivity(new Intent(OwnerDashboardActivity.this, OwnerRequestsActivity.class)));
        btnOwnerLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(OwnerDashboardActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
        
        loadStatistics();
    }
    
    private void loadStatistics() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;
        
        statsListener = FirebaseFirestore.getInstance().collection("parking_spots")
                .whereEqualTo("ownerId", currentUser.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        return;
                    }
                    int total = 0;
                    int available = 0;
                    int occupied = 0;
                    
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : value) {
                        total++;
                        String status = doc.getString("status");
                        if ("available".equalsIgnoreCase(status)) {
                            available++;
                        } else {
                            occupied++;
                        }
                    }
                    
                    tvStatTotal.setText(String.valueOf(total));
                    tvStatAvailable.setText(String.valueOf(available));
                    tvStatOccupied.setText(String.valueOf(occupied));
                });
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (statsListener != null) {
            statsListener.remove();
        }
    }

    private void enforceOwnerAccess() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(OwnerDashboardActivity.this, MainActivity.class));
            finish();
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if (!"owner".equalsIgnoreCase(role)) {
                        if ("admin".equalsIgnoreCase(role)) {
                            startActivity(new Intent(OwnerDashboardActivity.this, AdminDashboardActivity.class));
                        } else {
                            startActivity(new Intent(OwnerDashboardActivity.this, HomeActivity.class));
                        }
                        finish();
                    }
                });
    }
}
