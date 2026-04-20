package com.example.smartpark;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartpark.R;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class EditSpotActivity extends AppCompatActivity {

    private EditText etSpotName, etOpenTime, etCloseTime, etCapacity, etDescription;
    private Button btnStatusOpen, btnStatusClosed, btnStatusOccupied;
    private Button btnDiscard, btnUpdate;
    private ImageButton btnBack;

    private String selectedStatus = "Open";
    private String spotId, lotId;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_spot);

        db = FirebaseFirestore.getInstance();
        spotId = getIntent().getStringExtra("spotId");
        lotId = getIntent().getStringExtra("lotId");

        initViews();
        setupStatusButtons();
        loadSpotData();

        btnBack.setOnClickListener(v -> finish());
        btnDiscard.setOnClickListener(v -> finish());
        btnUpdate.setOnClickListener(v -> updateSpot());
    }

    private void initViews() {
        etSpotName = findViewById(R.id.etSpotName);
        etOpenTime = findViewById(R.id.etOpenTime);
        etCloseTime = findViewById(R.id.etCloseTime);
        etCapacity = findViewById(R.id.etCapacity);
        etDescription = findViewById(R.id.etDescription);
        btnStatusOpen = findViewById(R.id.btnStatusOpen);
        btnStatusClosed = findViewById(R.id.btnStatusClosed);
        btnStatusOccupied = findViewById(R.id.btnStatusOccupied);
        btnDiscard = findViewById(R.id.btnDiscard);
        btnUpdate = findViewById(R.id.btnUpdate);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupStatusButtons() {
        View.OnClickListener listener = v -> {
            btnStatusOpen.setBackgroundResource(R.drawable.bg_button_grey);
            btnStatusClosed.setBackgroundResource(R.drawable.bg_button_grey);
            btnStatusOccupied.setBackgroundResource(R.drawable.bg_button_grey);
            ((Button) v).setBackgroundResource(R.drawable.bg_button_green);

            if (v.getId() == R.id.btnStatusOpen) selectedStatus = "Open";
            else if (v.getId() == R.id.btnStatusClosed) selectedStatus = "Closed";
            else selectedStatus = "Occupied";
        };
        btnStatusOpen.setOnClickListener(listener);
        btnStatusClosed.setOnClickListener(listener);
        btnStatusOccupied.setOnClickListener(listener);
    }

    private void loadSpotData() {
        if (spotId == null || lotId == null) return;

        db.collection("parkingLots").document(lotId)
                .collection("spots").document(spotId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    etSpotName.setText(doc.getString("name"));
                    etOpenTime.setText(doc.getString("openTime"));
                    etCloseTime.setText(doc.getString("closeTime"));
                    etDescription.setText(doc.getString("description"));

                    Long cap = doc.getLong("capacity");
                    if (cap != null) etCapacity.setText(String.valueOf(cap));

                    String status = doc.getString("status");
                    if (status != null) {
                        selectedStatus = status;
                        // Reflect status on buttons
                        btnStatusOpen.setBackgroundResource(R.drawable.bg_button_grey);
                        btnStatusClosed.setBackgroundResource(R.drawable.bg_button_grey);
                        btnStatusOccupied.setBackgroundResource(R.drawable.bg_button_grey);
                        switch (status) {
                            case "Open":
                                btnStatusOpen.setBackgroundResource(R.drawable.bg_button_green); break;
                            case "Closed":
                                btnStatusClosed.setBackgroundResource(R.drawable.bg_button_green); break;
                            case "Occupied":
                                btnStatusOccupied.setBackgroundResource(R.drawable.bg_button_green); break;
                        }
                    }
                });
    }

    private void updateSpot() {
        String name = etSpotName.getText().toString().trim();
        String openTime = etOpenTime.getText().toString().trim();
        String closeTime = etCloseTime.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String capacityStr = etCapacity.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Spot name cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        updates.put("status", selectedStatus);
        updates.put("openTime", openTime);
        updates.put("closeTime", closeTime);
        updates.put("description", description);
        if (!capacityStr.isEmpty()) {
            updates.put("capacity", Integer.parseInt(capacityStr));
        }

        btnUpdate.setEnabled(false);
        db.collection("parkingLots").document(lotId)
                .collection("spots").document(spotId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Spot updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnUpdate.setEnabled(true);
                });
    }
}
