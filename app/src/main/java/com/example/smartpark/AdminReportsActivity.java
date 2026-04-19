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

public class AdminReportsActivity extends AppCompatActivity {

    private LinearLayout reportsContainer;
    private TextView tvEmptyReports;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_reports);

        db = FirebaseFirestore.getInstance();
        reportsContainer = findViewById(R.id.reports_container);
        tvEmptyReports = findViewById(R.id.tv_empty_reports);

        Button btnBack = findViewById(R.id.btn_back_admin_dashboard);
        Button btnRefresh = findViewById(R.id.btn_refresh_reports);

        btnBack.setOnClickListener(v -> finish());
        btnRefresh.setOnClickListener(v -> loadReports());

        enforceAdminAccess();
        loadReports();
    }

    private void enforceAdminAccess() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(AdminReportsActivity.this, MainActivity.class));
            finish();
            return;
        }

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if (!"admin".equalsIgnoreCase(role)) {
                        startActivity(new Intent(AdminReportsActivity.this, HomeActivity.class));
                        finish();
                    }
                });
    }

    private void loadReports() {
        db.collection("spot_reports")
                .orderBy("reportedAt", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    reportsContainer.removeAllViews();

                    if (querySnapshot.isEmpty()) {
                        tvEmptyReports.setText("No reports yet");
                        tvEmptyReports.setVisibility(TextView.VISIBLE);
                        return;
                    }

                    tvEmptyReports.setVisibility(TextView.GONE);
                    for (com.google.firebase.firestore.QueryDocumentSnapshot doc : querySnapshot) {
                        String spotName = valueOrDefault(doc.getString("spotName"), "Parking Spot");
                        String spotId = valueOrDefault(doc.getString("spotId"), doc.getId());
                        String reason = valueOrDefault(doc.getString("reason"), "N/A");
                        String state = valueOrDefault(doc.getString("state"), "open");
                        String reporterId = valueOrDefault(doc.getString("reporterId"), "N/A");
                        String ownerId = valueOrDefault(doc.getString("ownerId"), "N/A");
                        Timestamp reportedAt = doc.getTimestamp("reportedAt");
                        String reportedAtText = reportedAt == null
                                ? "N/A"
                                : new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(reportedAt.toDate());

                        TextView tvItem = new TextView(this);
                        tvItem.setPadding(24, 20, 24, 20);
                        tvItem.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);
                        tvItem.setText(
                                "Spot: " + spotName + "\n"
                                        + "Spot ID: " + spotId + "\n"
                                        + "Reason: " + reason + "\n"
                                        + "State: " + state + "\n"
                                        + "Reporter: " + reporterId + "\n"
                                        + "Owner: " + ownerId + "\n"
                                        + "Reported At: " + reportedAtText
                        );

                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        params.setMargins(0, 0, 0, 18);
                        tvItem.setLayoutParams(params);
                        reportsContainer.addView(tvItem);
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load reports", Toast.LENGTH_SHORT).show());
    }

    private String valueOrDefault(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value;
    }
}
