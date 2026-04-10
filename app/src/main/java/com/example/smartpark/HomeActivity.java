package com.example.smartpark;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private BottomSheetBehavior<androidx.cardview.widget.CardView> bottomSheetBehavior;
    private TextView tvSelectedTitle;
    private TextView tvDetailStatus;
    private TextView tvDetailAddedTime;
    private TextView tvDetailUserId;
    private Button btnNavigate;
    private Button btnCloseSheet;
    private final Map<Marker, ParkingSpotMapInfo> markerSpotMap = new HashMap<>();
    private ListenerRegistration parkingSpotsListener;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final LatLng MONTREAL = new LatLng(45.5017, -73.5673);
    private static final long SPOT_EXPIRY_MILLIS = 24L * 60L * 60L * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        enforceDriverAccess();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Button btnLogout = findViewById(R.id.btn_logout);
        FloatingActionButton fabMyLocation = findViewById(R.id.fabMyLocation);

        androidx.cardview.widget.CardView bottomSheet = findViewById(R.id.bottom_sheet);
        tvSelectedTitle = findViewById(R.id.tv_selected_title);
        tvDetailStatus = findViewById(R.id.tv_detail_status);
        tvDetailAddedTime = findViewById(R.id.tv_detail_added_time);
        tvDetailUserId = findViewById(R.id.tv_detail_user_id);
        btnNavigate = findViewById(R.id.btn_navigate);
        btnCloseSheet = findViewById(R.id.btn_close_sheet);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setDraggable(true);
        bottomSheetBehavior.setSkipCollapsed(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        btnCloseSheet.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(HomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        fabMyLocation.setOnClickListener(v -> getDeviceLocation());

        android.widget.ImageView ivProfile = findViewById(R.id.iv_profile);
        if (ivProfile != null) {
            ivProfile.setOnClickListener(v -> {
                Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }
    }

    private void enforceDriverAccess() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
            return;
        }

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if ("owner".equalsIgnoreCase(role)) {
                        startActivity(new Intent(HomeActivity.this, OwnerHomeActivity.class));
                        finish();
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(MONTREAL, 12));

        mMap.setOnMarkerClickListener(marker -> {
            ParkingSpotMapInfo info = markerSpotMap.get(marker);
            if (info == null) {
                return false;
            }

            tvSelectedTitle.setText(info.name);
            tvDetailStatus.setText("Status: " + info.status);
            tvDetailAddedTime.setText("Added Time: " + info.addedTime);
            tvDetailUserId.setText("User ID: " + info.userId);
            btnNavigate.setOnClickListener(v -> openNavigation(info.latitude, info.longitude));
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return true;
        });

        mMap.setOnMapClickListener(latLng -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));

        startParkingSpotsRealtimeListener();
        enableMyLocation();
    }

    private void startParkingSpotsRealtimeListener() {
        if (parkingSpotsListener != null) {
            parkingSpotsListener.remove();
        }

        parkingSpotsListener = db.collection("parking_spots")
                .addSnapshotListener((querySnapshot, error) -> {
                    if (error != null) {
                        Toast.makeText(this, "Failed to listen for spot updates", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (querySnapshot == null || mMap == null) {
                        return;
                    }

                    List<String> expiredSpotIds = new ArrayList<>();
                    mMap.clear();
                    markerSpotMap.clear();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Timestamp addedAtTimestamp = doc.getTimestamp("addedAt");
                        if (isExpired(addedAtTimestamp)) {
                            expiredSpotIds.add(doc.getId());
                            continue;
                        }

                        Double latitude = doc.getDouble("latitude");
                        Double longitude = doc.getDouble("longitude");
                        if (latitude == null || longitude == null) {
                            continue;
                        }

                        String name = doc.getString("name");
                        String status = doc.getString("status");
                        String userId = doc.getString("ownerId");
                        if (userId == null || userId.trim().isEmpty()) {
                            userId = doc.getString("userId");
                        }
                        if (userId == null || userId.trim().isEmpty()) {
                            userId = "N/A";
                        }
                        String addedTime = formatAddedTime(doc.get("addedAt"));

                        float markerColor = "occupied".equalsIgnoreCase(status)
                                ? BitmapDescriptorFactory.HUE_RED
                                : BitmapDescriptorFactory.HUE_GREEN;

                        String markerTitle = (name != null && !name.trim().isEmpty()) ? name : "Parking Spot";
                        String normalizedStatus = (status != null && !status.trim().isEmpty()) ? status : "available";

                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title(markerTitle)
                                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

                        if (marker != null) {
                            markerSpotMap.put(marker, new ParkingSpotMapInfo(
                                    markerTitle,
                                    normalizedStatus,
                                    addedTime,
                                    userId,
                                    latitude,
                                    longitude
                            ));
                        }
                    }

                    deleteExpiredSpots(expiredSpotIds);
                });
    }

    private boolean isExpired(Timestamp addedAt) {
        if (addedAt == null) {
            return false;
        }
        long age = System.currentTimeMillis() - addedAt.toDate().getTime();
        return age > SPOT_EXPIRY_MILLIS;
    }

    private void deleteExpiredSpots(List<String> expiredSpotIds) {
        for (String spotId : expiredSpotIds) {
            db.collection("parking_spots").document(spotId).delete();
        }
    }

    private String formatAddedTime(Object addedAt) {
        if (addedAt instanceof Timestamp) {
            Date date = ((Timestamp) addedAt).toDate();
            return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(date);
        }
        if (addedAt instanceof Long) {
            return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(new Date((Long) addedAt));
        }
        if (addedAt instanceof String && !((String) addedAt).trim().isEmpty()) {
            return (String) addedAt;
        }
        return "N/A";
    }

    private void openNavigation(double latitude, double longitude) {
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

    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else if (mMap != null) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    private void getDeviceLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15));
                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                enableMyLocation();
            }
        } catch (SecurityException e) {
            Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (parkingSpotsListener != null) {
            parkingSpotsListener.remove();
            parkingSpotsListener = null;
        }
    }

    private static class ParkingSpotMapInfo {
        final String name;
        final String status;
        final String addedTime;
        final String userId;
        final double latitude;
        final double longitude;

        ParkingSpotMapInfo(String name, String status, String addedTime, String userId, double latitude, double longitude) {
            this.name = name;
            this.status = status;
            this.addedTime = addedTime;
            this.userId = userId;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
