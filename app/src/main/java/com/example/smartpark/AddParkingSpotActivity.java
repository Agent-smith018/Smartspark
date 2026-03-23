package com.example.smartpark;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddParkingSpotActivity extends AppCompatActivity {

    private TextInputEditText etSpotName, etSpotAddress, etSpotPrice;
    private Button btnSaveSpot;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_parking_spot);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        etSpotName = findViewById(R.id.et_spot_name);
        etSpotAddress = findViewById(R.id.et_spot_address);
        etSpotPrice = findViewById(R.id.et_spot_price);
        btnSaveSpot = findViewById(R.id.btn_save_spot);
        progressBar = findViewById(R.id.save_progress);

        btnSaveSpot.setOnClickListener(v -> saveParkingSpot());
    }

    private void saveParkingSpot() {
        String name = etSpotName.getText().toString().trim();
        String address = etSpotAddress.getText().toString().trim();
        String price = etSpotPrice.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etSpotName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(address)) {
            etSpotAddress.setError("Address is required");
            return;
        }

        if (TextUtils.isEmpty(price)) {
            etSpotPrice.setError("Price is required");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String ownerId = mAuth.getCurrentUser().getUid();
        Map<String, Object> spot = new HashMap<>();
        spot.put("name", name);
        spot.put("address", address);
        spot.put("price", price);
        spot.put("ownerId", ownerId);
        spot.put("status", "available"); // Default status

        db.collection("parking_spots")
                .add(spot)
                .addOnSuccessListener(documentReference -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Parking Spot Added Successfully!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}