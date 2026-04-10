package com.example.smartpark;

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

    private TextInputEditText etSpotName, etSpotAddress, etSpotPrice;
    private AutoCompleteTextView actvSpotStatus;
    private TextView tvSelectedLocation;
    private Button btnSaveSpot;
    private ProgressBar progressBar;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private GoogleMap mMap;
    private LatLng selectedLatLng;

    private static final LatLng MONTREAL = new LatLng(45.5017, -73.5673);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_parking_spot);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        enforceOwnerAccess();

        etSpotName = findViewById(R.id.et_spot_name);
        etSpotAddress = findViewById(R.id.et_spot_address);
        etSpotPrice = findViewById(R.id.et_spot_price);
        actvSpotStatus = findViewById(R.id.actv_spot_status);
        tvSelectedLocation = findViewById(R.id.tv_selected_location);
        btnSaveSpot = findViewById(R.id.btn_save_spot);
        progressBar = findViewById(R.id.save_progress);

        setupStatusDropdown();
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MONTREAL, 12f));

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

    private void saveParkingSpot() {
        String name = etSpotName.getText() != null ? etSpotName.getText().toString().trim() : "";
        String address = etSpotAddress.getText() != null ? etSpotAddress.getText().toString().trim() : "";
        String price = etSpotPrice.getText() != null ? etSpotPrice.getText().toString().trim() : "";
        String status = actvSpotStatus.getText() != null ? actvSpotStatus.getText().toString().trim() : "";

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

        if (selectedLatLng == null) {
            Toast.makeText(this, "Please select spot location on map", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(status)) {
            actvSpotStatus.setError("Status is required");
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
        spot.put("ownerId", ownerId);
        spot.put("latitude", selectedLatLng.latitude);
        spot.put("longitude", selectedLatLng.longitude);
        spot.put("status", status.toLowerCase());
        spot.put("addedAt", FieldValue.serverTimestamp());

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
