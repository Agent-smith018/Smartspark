package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.net.Uri;
import java.util.List;
import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class DriverFavoritesActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private RecyclerView rvFavorites;
    private TextView tvEmptyFavorites;
    private FavoriteSpotAdapter adapter;
    private List<FavoriteSpot> favoriteSpotList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_favorites);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        rvFavorites = findViewById(R.id.rv_favorites);
        tvEmptyFavorites = findViewById(R.id.tv_empty_favorites);
        BottomNavigationView bottomNavDriver = findViewById(R.id.bottom_nav_driver);

        rvFavorites.setLayoutManager(new LinearLayoutManager(this));
        favoriteSpotList = new ArrayList<>();

        Button btnBackProfile = findViewById(R.id.btn_back_profile);
        btnBackProfile.setOnClickListener(v -> finish());

        if (bottomNavDriver != null) {
            bottomNavDriver.setSelectedItemId(R.id.nav_driver_favorites);
            bottomNavDriver.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_driver_favorites) {
                    return true;
                }
                if (itemId == R.id.nav_driver_home) {
                    Intent intent = new Intent(DriverFavoritesActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                if (itemId == R.id.nav_driver_profile) {
                    Intent intent = new Intent(DriverFavoritesActivity.this, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                }
                return false;
            });
        }

        enforceDriverAccess();
        loadFavorites();
    }

    private void enforceDriverAccess() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(DriverFavoritesActivity.this, MainActivity.class));
            finish();
            return;
        }

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if ("owner".equalsIgnoreCase(role)) {
                        startActivity(new Intent(DriverFavoritesActivity.this, OwnerDashboardActivity.class));
                        finish();
                    } else if ("admin".equalsIgnoreCase(role)) {
                        startActivity(new Intent(DriverFavoritesActivity.this, AdminDashboardActivity.class));
                        finish();
                    }
                });
    }

    private void loadFavorites() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            return;
        }

        db.collection("user_favorites")
                .whereEqualTo("userId", currentUser.getUid())
                .limit(100)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    favoriteSpotList.clear();

                    if (querySnapshot.isEmpty()) {
                        tvEmptyFavorites.setVisibility(TextView.VISIBLE);
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        return;
                    }

                    tvEmptyFavorites.setVisibility(TextView.GONE);
                    
                    List<com.google.firebase.firestore.QueryDocumentSnapshot> sortedDocs = new java.util.ArrayList<>();
                    for (com.google.firebase.firestore.QueryDocumentSnapshot d : querySnapshot) {
                        sortedDocs.add(d);
                    }
                    java.util.Collections.sort(sortedDocs, (d1, d2) -> {
                        Timestamp t1 = d1.getTimestamp("savedAt");
                        Timestamp t2 = d2.getTimestamp("savedAt");
                        if (t1 == null && t2 == null) return 0;
                        if (t1 == null) return 1;
                        if (t2 == null) return -1;
                        return t2.compareTo(t1);
                    });

                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : sortedDocs) {
                        String id = doc.getId();
                        String spotId = doc.getString("spotId");
                        String name = valueOrDefault(doc.getString("spotName"), "Parking Spot");
                        String status = valueOrDefault(doc.getString("spotStatus"), "N/A");
                        String ownerId = valueOrDefault(doc.getString("ownerId"), "N/A");
                        
                        Double lat = doc.getDouble("latitude");
                        Double lng = doc.getDouble("longitude");
                        double latitude = lat != null ? lat : 0.0;
                        double longitude = lng != null ? lng : 0.0;
                        
                        Timestamp savedAt = doc.getTimestamp("savedAt");
                        String savedAtText = savedAt == null
                                ? "N/A"
                                : new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(savedAt.toDate());

                        favoriteSpotList.add(new FavoriteSpot(id, spotId, name, status, ownerId, latitude, longitude, savedAtText));
                    }

                    if (adapter == null) {
                        adapter = new FavoriteSpotAdapter(this, favoriteSpotList, new FavoriteSpotAdapter.OnFavoriteClickListener() {
                            @Override
                            public void onRemoveFavorite(FavoriteSpot spot, int position) {
                                removeFavorite(spot, position);
                            }

                            @Override
                            public void onNavigateClick(FavoriteSpot spot) {
                                openNavigation(spot.latitude, spot.longitude);
                            }
                        });
                        rvFavorites.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load favorites", Toast.LENGTH_SHORT).show());
    }

    private void removeFavorite(FavoriteSpot spot, int position) {
        db.collection("user_favorites").document(spot.id)
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    favoriteSpotList.remove(position);
                    adapter.notifyItemRemoved(position);
                    if (favoriteSpotList.isEmpty()) {
                        tvEmptyFavorites.setVisibility(TextView.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to remove favorite", Toast.LENGTH_SHORT).show());
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

    private String valueOrDefault(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }
}
