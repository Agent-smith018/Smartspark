package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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
    private LinearLayout favoritesContainer;
    private TextView tvEmptyFavorites;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_favorites);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        favoritesContainer = findViewById(R.id.favorites_container);
        tvEmptyFavorites = findViewById(R.id.tv_empty_favorites);

        Button btnBackProfile = findViewById(R.id.btn_back_profile);
        btnBackProfile.setOnClickListener(v -> finish());

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
                .orderBy("savedAt", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    favoritesContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        tvEmptyFavorites.setVisibility(TextView.VISIBLE);
                        return;
                    }

                    tvEmptyFavorites.setVisibility(TextView.GONE);
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        String name = valueOrDefault(doc.getString("spotName"), "Parking Spot");
                        String status = valueOrDefault(doc.getString("spotStatus"), "N/A");
                        String ownerId = valueOrDefault(doc.getString("ownerId"), "N/A");
                        Timestamp savedAt = doc.getTimestamp("savedAt");
                        String savedAtText = savedAt == null
                                ? "N/A"
                                : new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(savedAt.toDate());

                        TextView tvItem = new TextView(this);
                        tvItem.setPadding(24, 20, 24, 20);
                        tvItem.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
                        tvItem.setText(
                                "Spot: " + name + "\n"
                                        + "Status: " + status + "\n"
                                        + "Owner: " + ownerId + "\n"
                                        + "Saved At: " + savedAtText
                        );

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, 18);
                        tvItem.setLayoutParams(params);
                        favoritesContainer.addView(tvItem);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load favorites", Toast.LENGTH_SHORT).show());
    }

    private String valueOrDefault(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }
}
