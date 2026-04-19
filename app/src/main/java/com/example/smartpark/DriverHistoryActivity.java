package com.example.smartpark;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DriverHistoryActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView rvHistory;
    private TextView tvEmptyHistory;
    private ParkingHistoryAdapter adapter;
    private List<ParkingHistory> historyList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_history);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rvHistory = findViewById(R.id.rv_history);
        tvEmptyHistory = findViewById(R.id.tv_empty_history);
        BottomNavigationView bottomNavDriver = findViewById(R.id.bottom_nav_driver);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        historyList = new ArrayList<>();

        ImageButton btnBackProfile = findViewById(R.id.btn_back_profile_history);
        btnBackProfile.setOnClickListener(v -> finish());

        if (bottomNavDriver != null) {
            // we keep profile selected visually since we enter from profile
            bottomNavDriver.setSelectedItemId(R.id.nav_driver_profile);
            bottomNavDriver.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_driver_profile) {
                    return true;
                }
                if (itemId == R.id.nav_driver_home) {
                    Intent intent = new Intent(DriverHistoryActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                if (itemId == R.id.nav_driver_favorites) {
                    Intent intent = new Intent(DriverHistoryActivity.this, DriverFavoritesActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                return false;
            });
        }

        enforceDriverAccess();
        loadHistory();
    }

    private void enforceDriverAccess() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(DriverHistoryActivity.this, MainActivity.class));
            finish();
            return;
        }

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if ("owner".equalsIgnoreCase(role)) {
                        startActivity(new Intent(DriverHistoryActivity.this, OwnerDashboardActivity.class));
                        finish();
                    } else if ("admin".equalsIgnoreCase(role)) {
                        startActivity(new Intent(DriverHistoryActivity.this, AdminDashboardActivity.class));
                        finish();
                    }
                });
    }

    private void loadHistory() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        db.collection("parking_history")
                .whereEqualTo("userId", currentUser.getUid())
                .limit(100)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    historyList.clear();

                    if (querySnapshot.isEmpty()) {
                        tvEmptyHistory.setVisibility(TextView.VISIBLE);
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        return;
                    }

                    tvEmptyHistory.setVisibility(TextView.GONE);
                    
                    List<QueryDocumentSnapshot> sortedDocs = new java.util.ArrayList<>();
                    for (QueryDocumentSnapshot d : querySnapshot) {
                        sortedDocs.add(d);
                    }
                    java.util.Collections.sort(sortedDocs, (d1, d2) -> {
                        Timestamp t1 = d1.getTimestamp("parkedAt");
                        Timestamp t2 = d2.getTimestamp("parkedAt");
                        if (t1 == null && t2 == null) return 0;
                        if (t1 == null) return 1;
                        if (t2 == null) return -1;
                        return t2.compareTo(t1);
                    });

                    for (QueryDocumentSnapshot doc : sortedDocs) {
                        String id = doc.getId();
                        String spotId = doc.getString("spotId");
                        String name = doc.getString("spotName");
                        if (name == null || name.trim().isEmpty()) {
                            name = "Parking Spot";
                        }
                        
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        double latitude = lat != null ? lat : 0.0;
                        double longitude = lng != null ? lng : 0.0;
                        
                        Timestamp parkedAt = doc.getTimestamp("parkedAt");
                        String parkedAtText = parkedAt == null
                                ? "N/A"
                                : new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(parkedAt.toDate());

                        historyList.add(new ParkingHistory(id, spotId, name, latitude, longitude, parkedAtText));
                    }

                    if (adapter == null) {
                        adapter = new ParkingHistoryAdapter(this, historyList, new ParkingHistoryAdapter.OnHistoryClickListener() {
                            @Override
                            public void onNavigateClick(ParkingHistory history) {
                                openNavigation(history.latitude, history.longitude);
                            }
                        });
                        rvHistory.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show());
    }

    private void openNavigation(double latitude, double longitude) {
        if (latitude == 0.0 && longitude == 0.0) {
            Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            Uri browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + latitude + "," + longitude);
            startActivity(new Intent(Intent.ACTION_VIEW, browserUri));
        }
    }
}
