package com.example.smartpark;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FieldValue;
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
import java.io.IOException;

public class HomeActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private BottomSheetBehavior<androidx.cardview.widget.CardView> bottomSheetBehavior;
    private TextView tvSelectedTitle;
    private TextView tvDetailStatus;
    private TextView tvDetailAddedTime;
    private TextView tvDetailDistance;
    private TextView tvDetailUserId;
    private ImageButton btnFavoriteSpot;
    private Button btnNavigate;
    private Button btnReportSpot;
    private Button btnCloseSheet;
    private TextInputEditText etLocationSearch;
    private RecyclerView rvParkingSpots;
    private ParkingSpotListAdapter spotListAdapter;
    private List<ParkingSpotMapInfo> spotList;
    private BottomSheetBehavior<androidx.cardview.widget.CardView> listSheetBehavior;
    private final Map<Marker, ParkingSpotMapInfo> markerSpotMap = new HashMap<>();
    private ListenerRegistration parkingSpotsListener;
    private boolean showAvailableOnly;

        private boolean filterFree = true;
        private boolean filterPaid = true;
        private boolean filterStreet = true;
        private boolean filterPrivate = true;
        private boolean typeFiltersVisible = false;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final LatLng MONTREAL = new LatLng(45.5017, -73.5673);
    private static final long SPOT_EXPIRY_MILLIS = 24L * 60L * 60L * 1000L;
    private LatLng currentUserLatLng;

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
        SwitchMaterial switchAvailableOnly = findViewById(R.id.switch_available_only);
        Button btnSearchLocation = findViewById(R.id.btn_search_location);
        etLocationSearch = findViewById(R.id.et_location_search);

        androidx.cardview.widget.CardView bottomSheet = findViewById(R.id.bottom_sheet);
        tvSelectedTitle = findViewById(R.id.tv_selected_title);
        tvDetailStatus = findViewById(R.id.tv_detail_status);
        tvDetailAddedTime = findViewById(R.id.tv_detail_added_time);
        tvDetailDistance = findViewById(R.id.tv_detail_distance);
        tvDetailUserId = findViewById(R.id.tv_detail_user_id);
        btnFavoriteSpot = findViewById(R.id.btn_favorite_spot);
        btnNavigate = findViewById(R.id.btn_navigate);
        btnReportSpot = findViewById(R.id.btn_report_spot);
        btnCloseSheet = findViewById(R.id.btn_close_sheet);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setDraggable(true);
        bottomSheetBehavior.setSkipCollapsed(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        // Initialize List View Bottom Sheet
        androidx.cardview.widget.CardView listSheet = findViewById(R.id.bottom_sheet_list);
        rvParkingSpots = findViewById(R.id.rv_parking_spots);
        spotList = new ArrayList<>();
        spotListAdapter = new ParkingSpotListAdapter(this, spotList, spot -> {
            // Zoom to marker when clicked
            LatLng spotLatLng = new LatLng(spot.latitude, spot.longitude);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(spotLatLng, 16f));
            // Collapse the list sheet
            listSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });
        rvParkingSpots.setLayoutManager(new LinearLayoutManager(this));
        rvParkingSpots.setAdapter(spotListAdapter);
        
        listSheetBehavior = BottomSheetBehavior.from(listSheet);
        listSheetBehavior.setHideable(true);
        listSheetBehavior.setDraggable(true);
        listSheetBehavior.setSkipCollapsed(false);
        listSheetBehavior.setPeekHeight(60);

        btnCloseSheet.setOnClickListener(v -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN));

        switchAvailableOnly.setOnCheckedChangeListener((buttonView, isChecked) -> {
            showAvailableOnly = isChecked;
            startParkingSpotsRealtimeListener();
        });

        btnSearchLocation.setOnClickListener(v -> searchLocation());

            // Initialize parking type filters
            android.view.View layoutTypeFilters = findViewById(R.id.layout_type_filters);
            android.widget.Button btnToggleFilter = findViewById(R.id.btn_toggle_type_filter);
            android.widget.CheckBox cbFree = findViewById(R.id.cb_free);
            android.widget.CheckBox cbPaid = findViewById(R.id.cb_paid);
            android.widget.CheckBox cbStreet = findViewById(R.id.cb_street);
            android.widget.CheckBox cbPrivate = findViewById(R.id.cb_private);

            btnToggleFilter.setOnClickListener(v -> {
                typeFiltersVisible = !typeFiltersVisible;
                layoutTypeFilters.setVisibility(typeFiltersVisible ? android.view.View.VISIBLE : android.view.View.GONE);
                btnToggleFilter.setText(typeFiltersVisible ? "Hide Filter" : "Show Filter");
            });

            cbFree.setOnCheckedChangeListener((button, isChecked) -> {
                filterFree = isChecked;
                startParkingSpotsRealtimeListener();
            });
            cbPaid.setOnCheckedChangeListener((button, isChecked) -> {
                filterPaid = isChecked;
                startParkingSpotsRealtimeListener();
            });
            cbStreet.setOnCheckedChangeListener((button, isChecked) -> {
                filterStreet = isChecked;
                startParkingSpotsRealtimeListener();
            });
            cbPrivate.setOnCheckedChangeListener((button, isChecked) -> {
                filterPrivate = isChecked;
                startParkingSpotsRealtimeListener();
            });
        etLocationSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH) {
                searchLocation();
                return true;
            }
            return false;
        });

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
                        startActivity(new Intent(HomeActivity.this, OwnerDashboardActivity.class));
                        finish();
                    } else if ("admin".equalsIgnoreCase(role)) {
                        startActivity(new Intent(HomeActivity.this, AdminDashboardActivity.class));
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
            tvDetailAddedTime.setText("Last Updated: " + info.lastUpdatedTime);
            tvDetailDistance.setText("Distance: " + formatDistanceFromUser(info.latitude, info.longitude));
            tvDetailUserId.setText("User ID: " + info.userId);
            btnReportSpot.setOnClickListener(v -> reportSpot(info));
            bindFavoriteSpotButton(info);
            if ("available".equalsIgnoreCase(info.status)) {
                btnNavigate.setVisibility(View.VISIBLE);
                btnNavigate.setEnabled(true);
                btnNavigate.setOnClickListener(v -> openNavigation(info.latitude, info.longitude));
            } else {
                btnNavigate.setVisibility(View.GONE);
                btnNavigate.setEnabled(false);
                btnNavigate.setOnClickListener(null);
            }
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
                    spotList.clear();
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
                        Object updatedAt = doc.get("updatedAt");
                        String lastUpdatedTime = formatAddedTime(updatedAt != null ? updatedAt : doc.get("addedAt"));

                        boolean isAvailable = "available".equalsIgnoreCase(status);
                        float markerColor = isAvailable
                            ? BitmapDescriptorFactory.HUE_GREEN
                            : BitmapDescriptorFactory.HUE_RED;

                        String markerTitle = (name != null && !name.trim().isEmpty()) ? name : "Parking Spot";
                        String normalizedStatus = (status != null && !status.trim().isEmpty()) ? status : "occupied";

                        if (showAvailableOnly && !"available".equalsIgnoreCase(normalizedStatus)) {
                            continue;
                        }

                            // Get parking type and apply type filter
                            String parkingType = doc.getString("type");
                            if (parkingType == null || parkingType.trim().isEmpty()) {
                                parkingType = "free";
                            }
                            if (!shouldIncludeType(parkingType)) {
                                continue;
                            }

                        Marker marker = mMap.addMarker(new MarkerOptions()
                                .position(new LatLng(latitude, longitude))
                                .title(markerTitle)
                                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));

                        if (marker != null) {
                            ParkingSpotMapInfo spotInfo = new ParkingSpotMapInfo(
                                    doc.getId(),
                                    markerTitle,
                                    normalizedStatus,
                                    lastUpdatedTime,
                                    userId,
                                    latitude,
                                    longitude,
                                        parkingType
                            );
                            markerSpotMap.put(marker, spotInfo);
                            spotList.add(spotInfo);
                        }
                    }

                    // Sort by distance (requires currentUserLatLng)
                    if (currentUserLatLng != null) {
                        float[] results = new float[1];
                        for (ParkingSpotMapInfo spot : spotList) {
                            Location.distanceBetween(currentUserLatLng.latitude, currentUserLatLng.longitude,
                                    spot.latitude, spot.longitude, results);
                            spot.distance = results[0];
                        }
                        spotList.sort((a, b) -> Float.compare(a.distance, b.distance));
                    }
                    spotListAdapter.updateList(spotList);

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

        private boolean shouldIncludeType(String type) {
            if (type == null || type.trim().isEmpty()) {
                return filterFree;
            }
            String lowerType = type.toLowerCase();
            if (lowerType.contains("free")) return filterFree;
            if (lowerType.contains("paid")) return filterPaid;
            if (lowerType.contains("street")) return filterStreet;
            if (lowerType.contains("private")) return filterPrivate;
            return filterFree; // default to free if type not recognized
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
            updateCurrentLocationCache();
        }
    }

    private void getDeviceLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                        currentUserLatLng = currentLatLng;
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

    private void updateCurrentLocationCache() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                currentUserLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            }
        });
    }

    private String formatDistanceFromUser(double targetLatitude, double targetLongitude) {
        if (currentUserLatLng == null) {
            return "N/A";
        }

        float[] results = new float[1];
        Location.distanceBetween(
                currentUserLatLng.latitude,
                currentUserLatLng.longitude,
                targetLatitude,
                targetLongitude,
                results
        );

        float meters = results[0];
        if (meters < 1000f) {
            return Math.round(meters) + " m";
        }
        return String.format(Locale.getDefault(), "%.1f km", meters / 1000f);
    }

    private void searchLocation() {
        if (etLocationSearch == null || etLocationSearch.getText() == null) {
            return;
        }

        String query = etLocationSearch.getText().toString().trim();
        if (query.isEmpty()) {
            etLocationSearch.setError("Enter a location");
            return;
        }

        if (mMap == null) {
            Toast.makeText(this, "Map is not ready yet", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Geocoder.isPresent()) {
            Toast.makeText(this, "Location search is not available", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            List<Address> addresses = new ArrayList<>();
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                addresses = geocoder.getFromLocationName(query, 1);
            } catch (IOException ignored) {
            }

            List<Address> finalAddresses = addresses;
            runOnUiThread(() -> {
                if (finalAddresses == null || finalAddresses.isEmpty()) {
                    Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                Address address = finalAddresses.get(0);
                LatLng searchedLatLng = new LatLng(address.getLatitude(), address.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchedLatLng, 14f));
            });
        }).start();
    }

    private void reportSpot(ParkingSpotMapInfo info) {
        FirebaseUser reporter = mAuth.getCurrentUser();
        if (reporter == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> report = new HashMap<>();
        report.put("spotId", info.spotId);
        report.put("spotName", info.name);
        report.put("spotStatus", info.status);
        report.put("ownerId", info.userId);
        report.put("reporterId", reporter.getUid());
        report.put("reportedAt", FieldValue.serverTimestamp());
        report.put("reason", "incorrect_or_fake");
        report.put("state", "open");

        db.collection("spot_reports")
                .add(report)
                .addOnSuccessListener(documentReference -> Toast.makeText(this, "Report submitted", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to submit report", Toast.LENGTH_SHORT).show());
    }

    private void bindFavoriteSpotButton(ParkingSpotMapInfo info) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            btnFavoriteSpot.setVisibility(View.GONE);
            return;
        }

        btnFavoriteSpot.setVisibility(View.VISIBLE);
        String favoriteDocId = getFavoriteDocId(user.getUid(), info.spotId);

        db.collection("user_favorites").document(favoriteDocId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isFavorite = documentSnapshot.exists();
                    setFavoriteIcon(isFavorite);
                    btnFavoriteSpot.setOnClickListener(v -> toggleFavorite(info, favoriteDocId, isFavorite));
                })
                .addOnFailureListener(e -> {
                    setFavoriteIcon(false);
                    btnFavoriteSpot.setOnClickListener(v -> toggleFavorite(info, favoriteDocId, false));
                });
    }

    private void toggleFavorite(ParkingSpotMapInfo info, String favoriteDocId, boolean isCurrentlyFavorite) {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isCurrentlyFavorite) {
            db.collection("user_favorites").document(favoriteDocId)
                    .delete()
                    .addOnSuccessListener(unused -> {
                        setFavoriteIcon(false);
                        btnFavoriteSpot.setOnClickListener(v -> toggleFavorite(info, favoriteDocId, false));
                        Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to update favorite", Toast.LENGTH_SHORT).show());
            return;
        }

        Map<String, Object> favorite = new HashMap<>();
        favorite.put("userId", user.getUid());
        favorite.put("spotId", info.spotId);
        favorite.put("spotName", info.name);
        favorite.put("spotStatus", info.status);
        favorite.put("ownerId", info.userId);
        favorite.put("latitude", info.latitude);
        favorite.put("longitude", info.longitude);
        favorite.put("savedAt", FieldValue.serverTimestamp());

        db.collection("user_favorites").document(favoriteDocId)
                .set(favorite)
                .addOnSuccessListener(unused -> {
                    setFavoriteIcon(true);
                    btnFavoriteSpot.setOnClickListener(v -> toggleFavorite(info, favoriteDocId, true));
                    Toast.makeText(this, "Saved to favorites", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save favorite", Toast.LENGTH_SHORT).show());
    }

    private void setFavoriteIcon(boolean isFavorite) {
        btnFavoriteSpot.setImageResource(
                isFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off
        );
    }

    private String getFavoriteDocId(String userId, String spotId) {
        return userId + "_" + spotId;
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

}
