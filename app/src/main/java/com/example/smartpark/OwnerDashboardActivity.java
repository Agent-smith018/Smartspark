package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpark.R;
import com.example.smartpark.OwnerSpotsAdapter;
import com.example.smartpark.ParkingLot;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class OwnerDashboardActivity extends AppCompatActivity {

    private TextView tvWelcome, tvTotalSpots, tvAvailableNow, tvTodayBookings, tvRating;
    private RecyclerView rvSpots;
    private Button btnAddSpot;
    private LinearLayout navOverview, navSpots, navProfile;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private List<ParkingLot> lotList = new ArrayList<>();
    private OwnerSpotsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_dashboard);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        initViews();
        setupRecyclerView();
        loadOwnerData();
        setupNavigation();

        btnAddSpot.setOnClickListener(v ->
                startActivity(new Intent(this, AddSpotActivity.class)));
    }

    private void initViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        tvTotalSpots = findViewById(R.id.tvTotalSpots);
        tvAvailableNow = findViewById(R.id.tvAvailableNow);
        tvTodayBookings = findViewById(R.id.tvTodayBookings);
        tvRating = findViewById(R.id.tvRating);
        rvSpots = findViewById(R.id.rvSpots);
        btnAddSpot = findViewById(R.id.btnAddSpot);
        navOverview = findViewById(R.id.navOverview);
        navSpots = findViewById(R.id.navSpots);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupRecyclerView() {
        adapter = new OwnerSpotsAdapter(lotList, lot -> {
            // Navigate to spot list for this lot
            Intent intent = new Intent(this, SpotListActivity.class);
            intent.putExtra("lotId", lot.getId());
            intent.putExtra("lotName", lot.getName());
            startActivity(intent);
        });
        rvSpots.setLayoutManager(new LinearLayoutManager(this));
        rvSpots.setAdapter(adapter);
    }

    private void loadOwnerData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        // Load owner display name
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        tvWelcome.setText("Welcome back, " + (name != null ? name : "Owner") + "!");
                    }
                });

        // Load owner's parking lots
        db.collection("parkingLots")
                .whereEqualTo("ownerId", uid)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    lotList.clear();
                    int totalSpots = 0, availableSpots = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        ParkingLot lot = doc.toObject(ParkingLot.class);
                        lot.setId(doc.getId());
                        lotList.add(lot);
                        totalSpots += lot.getTotalSpots();
                        availableSpots += lot.getAvailableSpots();
                    }

                    tvTotalSpots.setText(String.valueOf(totalSpots));
                    tvAvailableNow.setText(String.valueOf(availableSpots));
                    int pct = totalSpots > 0 ? (availableSpots * 100 / totalSpots) : 0;
                    adapter.notifyDataSetChanged();
                });

        // Load today's bookings count
        db.collection("bookings")
                .whereEqualTo("ownerId", uid)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    tvTodayBookings.setText(String.valueOf(snapshots.size()));
                });
    }

    private void setupNavigation() {
        navOverview.setOnClickListener(v -> { /* Already here */ });
        navSpots.setOnClickListener(v ->
                startActivity(new Intent(this, SpotListActivity.class)));
        navProfile.setOnClickListener(v ->
                startActivity(new Intent(this, UserManagmentActivity.class)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOwnerData();
    }
}
