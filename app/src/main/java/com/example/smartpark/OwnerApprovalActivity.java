package com.example.smartpark;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartpark.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class OwnerApprovalActivity extends AppCompatActivity {
    private TextView tvAvatar, tvOwnerName, tvOwnerEmail, tvDriverSince, tvSpotCount;
    private TextView tvDetailSpotName, tvDetailAddress, tvDetailCapacity, tvDetailType, tvViewFile;
    private EditText etAdminNote;
    private Button btnReject, btnApprove;
    private ImageButton btnBack;

    private String userId;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_approval);

        db = FirebaseFirestore.getInstance();
        userId = getIntent().getStringExtra("userId");

        initViews();
        loadOwnerData();

        btnBack.setOnClickListener(v -> finish());
        btnApprove.setOnClickListener(v -> approveOwner());
        btnReject.setOnClickListener(v -> rejectOwner());
    }

    private void initViews() {
        tvAvatar = findViewById(R.id.tvAvatar);
        tvOwnerName = findViewById(R.id.tvOwnerName);
        tvOwnerEmail = findViewById(R.id.tvOwnerEmail);
        tvDriverSince = findViewById(R.id.tvDriverSince);
        tvSpotCount = findViewById(R.id.tvSpotCount);
        tvDetailSpotName = findViewById(R.id.tvDetailSpotName);
        tvDetailAddress = findViewById(R.id.tvDetailAddress);
        tvDetailCapacity = findViewById(R.id.tvDetailCapacity);
        tvDetailType = findViewById(R.id.tvDetailType);
        tvViewFile = findViewById(R.id.tvViewFile);
        etAdminNote = findViewById(R.id.etAdminNote);
        btnReject = findViewById(R.id.btnReject);
        btnApprove = findViewById(R.id.btnApprove);
        btnBack = findViewById(R.id.btnBack);
    }

    private void loadOwnerData() {
        if (userId == null) return;

        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    String name = doc.getString("name");
                    String email = doc.getString("email");
                    String createdAt = doc.getString("createdAt");

                    tvOwnerName.setText(name != null ? name : "Unknown");
                    tvOwnerEmail.setText(email != null ? email : "");
                    tvDriverSince.setText("Driver since " + (createdAt != null ? createdAt : "N/A"));

                    // Set avatar initials
                    if (name != null && name.length() >= 2) {
                        String[] parts = name.trim().split(" ");
                        String initials = parts.length >= 2
                                ? "" + parts[0].charAt(0) + parts[1].charAt(0)
                                : name.substring(0, 2).toUpperCase();
                        tvAvatar.setText(initials.toUpperCase());
                    }

                    // Load submission details
                    Map<String, Object> submission = (Map<String, Object>) doc.get("ownerSubmission");
                    if (submission != null) {
                        tvDetailSpotName.setText(getString(submission, "spotName"));
                        tvDetailAddress.setText(getString(submission, "address"));
                        tvDetailCapacity.setText(getString(submission, "capacity") + " spots");
                        tvDetailType.setText(getString(submission, "type"));
                    }
                });

        // Count owner's existing spots
        db.collection("parkingLots")
                .whereEqualTo("ownerId", userId)
                .get()
                .addOnSuccessListener(snapshots ->
                        tvSpotCount.setText(snapshots.size() + " spot" + (snapshots.size() != 1 ? "s" : "")));
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    private void approveOwner() {
        if (userId == null) return;
        String note = etAdminNote.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("approved", true);
        updates.put("role", "owner");
        if (!note.isEmpty()) updates.put("adminNote", note);

        setButtonsEnabled(false);
        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    logActivity("Owner approved: " + userId);
                    Toast.makeText(this, "Owner approved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setButtonsEnabled(true);
                });
    }

    private void rejectOwner() {
        if (userId == null) return;
        String note = etAdminNote.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("approved", false);
        updates.put("rejected", true);
        if (!note.isEmpty()) updates.put("adminNote", note);

        setButtonsEnabled(false);
        db.collection("users").document(userId).update(updates)
                .addOnSuccessListener(aVoid -> {
                    logActivity("Owner rejected: " + userId);
                    Toast.makeText(this, "Owner rejected.", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    setButtonsEnabled(true);
                });
    }

    private void logActivity(String message) {
        Map<String, Object> log = new HashMap<>();
        log.put("message", message);
        log.put("timestamp", com.google.firebase.Timestamp.now());
        db.collection("activityLog").add(log);
    }

    private void setButtonsEnabled(boolean enabled) {
        btnApprove.setEnabled(enabled);
        btnReject.setEnabled(enabled);
    }
}
