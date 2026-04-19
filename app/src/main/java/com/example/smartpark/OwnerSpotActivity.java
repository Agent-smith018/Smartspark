package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class OwnerSpotActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private RecyclerView rvSpots;
    private ArrayList<ParkingSpot> spotList;
    private ParkingSpotAdapter adapter;
    private FirebaseFirestore db;
    private ListenerRegistration spotsListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_spot);

        db = FirebaseFirestore.getInstance();
        enforceOwnerAccess();

        android.view.View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvSpots = findViewById(R.id.rv_spots);

        // Setup RecyclerView
        rvSpots.setLayoutManager(new LinearLayoutManager(this));

        // Initial Data
        spotList = new ArrayList<>();
        adapter = new ParkingSpotAdapter(this, spotList);
        rvSpots.setAdapter(adapter);
        
        loadMySpots();
    }
    
    private void loadMySpots() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        
        if (spotsListener != null) {
            spotsListener.remove();
        }
        
        spotsListener = db.collection("parking_spots")
                .whereEqualTo("ownerId", user.getUid())
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        return;
                    }
                    
                    spotList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        String id = doc.getId();
                        String name = doc.getString("name");
                        String address = doc.getString("address");
                        String priceRaw = doc.getString("price");
                        
                        String price = "";
                        if (priceRaw != null && !priceRaw.isEmpty()) {
                            price = "$" + priceRaw + "/hr";
                        }
                        
                        String status = doc.getString("status");
                        if (status == null) status = "available";
                        
                        ParkingSpot spot = new ParkingSpot(id, name, address, price, status);
                        spot.setDescription(doc.getString("description"));
                        spot.setWorkingHours(doc.getString("workingHours"));
                        spot.setType(doc.getString("type"));
                        Long capObj = doc.getLong("capacity");
                        spot.setCapacity(capObj != null ? capObj.intValue() : 1);
                        Double latObj = doc.getDouble("latitude");
                        Double lngObj = doc.getDouble("longitude");
                        spot.setLatitude(latObj != null ? latObj : 0.0);
                        spot.setLongitude(lngObj != null ? lngObj : 0.0);
                        
                        // Fix for raw price value when editing edit
                        spot.setPrice(priceRaw != null ? priceRaw : "");

                        spotList.add(spot);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void enforceOwnerAccess() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(OwnerSpotActivity.this, MainActivity.class));
            finish();
            return;
        }

        db.collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if (!"owner".equalsIgnoreCase(role)) {
                        startActivity(new Intent(OwnerSpotActivity.this, HomeActivity.class));
                        finish();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (spotsListener != null) {
            spotsListener.remove();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Leaving this in case some other activity passes back data, though the snapshot listener handles updates natively.
    }
}