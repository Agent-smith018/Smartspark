package com.example.smartpark;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddParkingSpotActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private TextInputEditText etSpotName, etSpotAddress, etSpotPrice, etSpotCapacity, etSpotDescription, etSpotWorkingHours;
    private AutoCompleteTextView actvSpotStatus;
        private AutoCompleteTextView actvParkingType;
    private TextView tvSelectedLocation;
    private Button btnSaveSpot;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private LatLng selectedLatLng;
    private boolean isEditMode = false;
    private String editSpotId;
    private ParkingSpot editSpotData;

    private static final LatLng MONTREAL = new LatLng(45.5017, -73.5673);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_parking_spot);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        android.view.View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        enforceOwnerAccess();

        etSpotName = findViewById(R.id.et_spot_name);
        etSpotAddress = findViewById(R.id.et_spot_address);
        etSpotPrice = findViewById(R.id.et_spot_price);
        etSpotCapacity = findViewById(R.id.et_spot_capacity);
        etSpotDescription = findViewById(R.id.et_spot_description);
        etSpotWorkingHours = findViewById(R.id.et_spot_working_hours);
        actvSpotStatus = findViewById(R.id.actv_spot_status);
        actvParkingType = findViewById(R.id.actv_parking_type);
        tvSelectedLocation = findViewById(R.id.tv_selected_location);
        btnSaveSpot = findViewById(R.id.btn_save_spot);
        progressBar = findViewById(R.id.save_progress);

        setupStatusDropdown();
        setupTypeDropdown();
        
        editSpotData = (ParkingSpot) getIntent().getSerializableExtra("spot");
        if (editSpotData != null) {
            isEditMode = true;
            editSpotId = editSpotData.getId();
            
            etSpotName.setText(editSpotData.getName() != null ? editSpotData.getName() : "");
            etSpotAddress.setText(editSpotData.getAddress() != null ? editSpotData.getAddress() : "");
            etSpotPrice.setText(editSpotData.getPrice() != null ? editSpotData.getPrice().replace("$","").replace("/hr","").trim() : "");
            etSpotCapacity.setText(String.valueOf(editSpotData.getCapacity()));
            etSpotDescription.setText(editSpotData.getDescription() != null ? editSpotData.getDescription() : "");
            etSpotWorkingHours.setText(editSpotData.getWorkingHours() != null ? editSpotData.getWorkingHours() : "");
            
            if (editSpotData.getStatus() != null) {
                // Capitalize first letter logic for display is handled by dropdowns naturally but let's set raw
                actvSpotStatus.setText(editSpotData.getStatus(), false);
            }
            if (editSpotData.getType() != null) {
                // Ensure proper capitalization for display
                String typeStr = editSpotData.getType();
                if (typeStr.equalsIgnoreCase("free")) typeStr = "Free";
                else if (typeStr.equalsIgnoreCase("paid")) typeStr = "Paid";
                else if (typeStr.equalsIgnoreCase("street parking")) typeStr = "Street Parking";
                else if (typeStr.equalsIgnoreCase("private lot")) typeStr = "Private Lot";
                actvParkingType.setText(typeStr, false);
            }
            
            if (editSpotData.getDescription() != null) {
                etSpotDescription.setText(editSpotData.getDescription());
            }
            if (editSpotData.getWorkingHours() != null) {
                etSpotWorkingHours.setText(editSpotData.getWorkingHours());
            }
            
            selectedLatLng = new LatLng(editSpotData.getLatitude(), editSpotData.getLongitude());
            
            btnSaveSpot.setText("UPDATE PARKING SPOT");
        }

        setupMap();

        btnSaveSpot.setOnClickListener(v -> saveParkingSpot());
    }

    private void enforceOwnerAccess() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new android.content.Intent(AddParkingSpotActivity.this, MainActivity.class));
            finish();
            return;
        }

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if (!"owner".equalsIgnoreCase(role)) {
                        startActivity(new android.content.Intent(AddParkingSpotActivity.this, HomeActivity.class));
                        finish();
                    }
                });
    }

    private void setupStatusDropdown() {
        ArrayAdapter<CharSequence> statusAdapter = ArrayAdapter.createFromResource(
                this,
                R.array.spot_status_options,
                android.R.layout.simple_dropdown_item_1line
        );
        actvSpotStatus.setAdapter(statusAdapter);
        actvSpotStatus.setText(getString(R.string.status_available), false);
    }

    private void setupTypeDropdown() {
        String[] parkingTypes = {"Free", "Paid", "Street Parking", "Private Lot"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                parkingTypes
        );
        actvParkingType.setAdapter(typeAdapter);
        actvParkingType.setText("Free", false);
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_pick_spot);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        if (isEditMode && selectedLatLng != null && selectedLatLng.latitude != 0.0) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(selectedLatLng, 15f));
            mMap.addMarker(new MarkerOptions().position(selectedLatLng).title("Selected Spot"));
            tvSelectedLocation.setText(getString(
                    R.string.selected_location_format,
                    selectedLatLng.latitude,
                    selectedLatLng.longitude
            ));
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MONTREAL, 12f));
        }
        
        enableMyLocation();

        mMap.setOnMapClickListener(latLng -> {
            selectedLatLng = latLng;
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title("Selected Spot"));
            tvSelectedLocation.setText(getString(
                    R.string.selected_location_format,
                    latLng.latitude,
                    latLng.longitude
            ));
        });
    }

    private void enableMyLocation() {
        if (mMap == null) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            enableMyLocation();
        }
    }

    private void saveParkingSpot() {
        String name = etSpotName.getText() != null ? etSpotName.getText().toString().trim() : "";
        String address = etSpotAddress.getText() != null ? etSpotAddress.getText().toString().trim() : "";
        String price = etSpotPrice.getText() != null ? etSpotPrice.getText().toString().trim() : "";
        String capacityStr = etSpotCapacity.getText() != null ? etSpotCapacity.getText().toString().trim() : "";
        String description = etSpotDescription.getText() != null ? etSpotDescription.getText().toString().trim() : "";
        String workingHours = etSpotWorkingHours.getText() != null ? etSpotWorkingHours.getText().toString().trim() : "";
        String status = actvSpotStatus.getText() != null ? actvSpotStatus.getText().toString().trim() : "";
        String type = actvParkingType.getText() != null ? actvParkingType.getText().toString().trim() : "";

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

        if (TextUtils.isEmpty(capacityStr)) {
            etSpotCapacity.setError("Capacity is required");
            return;
        }

        int capacity = 1;
        try {
            capacity = Integer.parseInt(capacityStr);
        } catch (NumberFormatException e) {
            etSpotCapacity.setError("Invalid capacity");
            return;
        }

        if (selectedLatLng == null) {
            Toast.makeText(this, "Please select spot location on map", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(status)) {
            actvSpotStatus.setError("Status is required");
            return;
        }
        
        if (TextUtils.isEmpty(type)) {
            actvParkingType.setError("Parking type is required");
            return;
        }

        if (mAuth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String ownerId = mAuth.getCurrentUser().getUid();
        Map<String, Object> spot = new HashMap<>();
        spot.put("name", name);
        spot.put("address", address);
        spot.put("price", price);
        spot.put("capacity", capacity);
        spot.put("description", description);
        spot.put("workingHours", workingHours);
        spot.put("ownerId", ownerId);
        spot.put("latitude", selectedLatLng.latitude);
        spot.put("longitude", selectedLatLng.longitude);
        spot.put("status", status.toLowerCase());
        spot.put("type", type.toLowerCase());
        
        if (isEditMode) {
            spot.put("updatedAt", FieldValue.serverTimestamp());
            db.collection("parking_spots").document(editSpotId)
                    .update(spot)
                    .addOnSuccessListener(aVoid -> {
                        if (editSpotData != null && !editSpotData.getStatus().equalsIgnoreCase(status)) {
                            java.util.Map<String, Object> history = new java.util.HashMap<>();
                            history.put("spotId", editSpotId);
                            history.put("spotName", name);
                            history.put("ownerId", ownerId);
                            history.put("status", status.toLowerCase());
                            history.put("timestamp", FieldValue.serverTimestamp());
                            db.collection("spot_history").add(history);
                        }
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Parking Spot Updated Successfully!", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Error updating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            spot.put("addedAt", FieldValue.serverTimestamp());
            db.collection("parking_spots")
                    .add(spot)
                    .addOnSuccessListener(documentReference -> {
                        java.util.Map<String, Object> history = new java.util.HashMap<>();
                        history.put("spotId", documentReference.getId());
                        history.put("spotName", name);
                        history.put("ownerId", ownerId);
                        history.put("status", status.toLowerCase());
                        history.put("timestamp", FieldValue.serverTimestamp());
                        db.collection("spot_history").add(history);
                        
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
}
