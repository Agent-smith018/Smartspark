package com.example.smartpark;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartpark.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AddSpotActivity extends AppCompatActivity {

    private EditText etSpotName, etAddress, etTotalSpots, etOpenTime, etCloseTime;
    private Spinner spinnerSpotType;
    private Button btnStatusOpen, btnStatusClosed, btnStatusOccupied;
    private Button btnPricingFree, btnPricingPaid;
    private Button btnAddSpot;
    private ImageButton btnBack;

    private String selectedStatus = "Open";
    private String selectedPricing = "Free";

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupSpotTypeSpinner();
        setupStatusButtons();
        setupPricingButtons();

        btnBack.setOnClickListener(v -> finish());
        btnAddSpot.setOnClickListener(v -> saveSpot());
    }

    private void initViews() {
        etSpotName = findViewById(R.id.etSpotName);
        etAddress = findViewById(R.id.etAddress);
        etTotalSpots = findViewById(R.id.etTotalSpots);
        etOpenTime = findViewById(R.id.etOpenTime);
        etCloseTime = findViewById(R.id.etCloseTime);
        spinnerSpotType = findViewById(R.id.spinnerSpotType);
        btnStatusOpen = findViewById(R.id.btnStatusOpen);
        btnStatusClosed = findViewById(R.id.btnStatusClosed);
        btnStatusOccupied = findViewById(R.id.btnStatusOccupied);
        btnPricingFree = findViewById(R.id.btnPricingFree);
        btnPricingPaid = findViewById(R.id.btnPricingPaid);
        btnAddSpot = findViewById(R.id.btnAddSpot);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupSpotTypeSpinner() {
        String[] types = {"Standard", "Accessible", "EV", "Motorcycle"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpotType.setAdapter(adapter);
    }

    private void setupStatusButtons() {
        View.OnClickListener listener = v -> {
            // Reset all to grey
            btnStatusOpen.setBackgroundResource(R.drawable.bg_button_grey);
            btnStatusClosed.setBackgroundResource(R.drawable.bg_button_grey);
            btnStatusOccupied.setBackgroundResource(R.drawable.bg_button_grey);

            // Highlight selected
            ((Button) v).setBackgroundResource(R.drawable.bg_button_green);

            if (v.getId() == R.id.btnStatusOpen) selectedStatus = "Open";
            else if (v.getId() == R.id.btnStatusClosed) selectedStatus = "Closed";
            else selectedStatus = "Occupied";
        };

        btnStatusOpen.setOnClickListener(listener);
        btnStatusClosed.setOnClickListener(listener);
        btnStatusOccupied.setOnClickListener(listener);
    }

    private void setupPricingButtons() {
        btnPricingFree.setOnClickListener(v -> {
            selectedPricing = "Free";
            btnPricingFree.setBackgroundResource(R.drawable.bg_button_green);
            btnPricingPaid.setBackgroundResource(R.drawable.bg_button_outline);
        });

        btnPricingPaid.setOnClickListener(v -> {
            selectedPricing = "Paid";
            btnPricingPaid.setBackgroundResource(R.drawable.bg_button_green);
            btnPricingFree.setBackgroundResource(R.drawable.bg_button_outline);
        });
    }

    private void saveSpot() {
        String name = etSpotName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String totalSpotsStr = etTotalSpots.getText().toString().trim();
        String openTime = etOpenTime.getText().toString().trim();
        String closeTime = etCloseTime.getText().toString().trim();
        String spotType = spinnerSpotType.getSelectedItem().toString();

        if (name.isEmpty() || address.isEmpty() || totalSpotsStr.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalSpots = Integer.parseInt(totalSpotsStr);
        String ownerId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        Map<String, Object> spotData = new HashMap<>();
        spotData.put("name", name);
        spotData.put("address", address);
        spotData.put("totalSpots", totalSpots);
        spotData.put("availableSpots", totalSpots);
        spotData.put("spotType", spotType);
        spotData.put("status", selectedStatus);
        spotData.put("pricing", selectedPricing);
        spotData.put("openTime", openTime);
        spotData.put("closeTime", closeTime);
        spotData.put("ownerId", ownerId);
        spotData.put("rating", 0.0);
        spotData.put("reviewCount", 0);

        btnAddSpot.setEnabled(false);
        db.collection("parkingLots")
                .add(spotData)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(this, "Spot added successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add spot: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    btnAddSpot.setEnabled(true);
                });
    }
}
