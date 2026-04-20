package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpark.R;
import com.example.smartpark.IndividualSpotAdapter;
import com.example.smartpark.ParkingSpot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class SpotListActivity extends AppCompatActivity {

    private TextView tvLotName, tvSpotsCount, tabAll, tabFree, tabTaken;
    private RecyclerView rvIndividualSpots;
    private ImageButton btnBack, btnSearch;
    private LinearLayout navOverview, navSpots, navProfile;

    private FirebaseFirestore db;
    private List<ParkingSpot> allSpots = new ArrayList<>();
    private List<ParkingSpot> filteredSpots = new ArrayList<>();
    private com.example.smartpark.IndividualSpotAdapter adapter;
    private String lotId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_list);

        db = FirebaseFirestore.getInstance();
        lotId = getIntent().getStringExtra("lotId");
        String lotName = getIntent().getStringExtra("lotName");

        initViews();
        if (lotName != null) tvLotName.setText(lotName);
        setupRecyclerView();
        setupTabs();
        loadSpots();

        btnBack.setOnClickListener(v -> finish());
        navOverview.setOnClickListener(v -> startActivity(new Intent(this, OwnerDashboardActivity.class)));
        navProfile.setOnClickListener(v -> startActivity(new Intent(this, UserManagmentActivity.class)));
    }

    private void initViews() {
        tvLotName = findViewById(R.id.tvLotName);
        tvSpotsCount = findViewById(R.id.tvSpotsCount);
        tabAll = findViewById(R.id.tabAll);
        tabFree = findViewById(R.id.tabFree);
        tabTaken = findViewById(R.id.tabTaken);
        rvIndividualSpots = findViewById(R.id.rvIndividualSpots);
        btnBack = findViewById(R.id.btnBack);
        btnSearch = findViewById(R.id.btnSearch);
        navOverview = findViewById(R.id.navOverview);
        navSpots = findViewById(R.id.navSpots);
        navProfile = findViewById(R.id.navProfile);
    }

    private void setupRecyclerView() {
        adapter = new com.example.smartpark.IndividualSpotAdapter(filteredSpots, spot -> {
            Intent intent = new Intent(this, EditSpotActivity.class);
            intent.putExtra("spotId", spot.getId());
            intent.putExtra("lotId", lotId);
            startActivity(intent);
        });
        rvIndividualSpots.setLayoutManager(new LinearLayoutManager(this));
        rvIndividualSpots.setAdapter(adapter);
    }

    private void setupTabs() {
        tabAll.setOnClickListener(v -> filterSpots("all"));
        tabFree.setOnClickListener(v -> filterSpots("Available"));
        tabTaken.setOnClickListener(v -> filterSpots("Occupied"));
    }

    private void filterSpots(String filter) {
        filteredSpots.clear();
        for (ParkingSpot spot : allSpots) {
            if (filter.equals("all") || filter.equals(spot.getStatus())) {
                filteredSpots.add(spot);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void loadSpots() {
        if (lotId == null) return;

        db.collection("parkingLots").document(lotId)
                .collection("spots")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    allSpots.clear();
                    int available = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        ParkingSpot spot = doc.toObject(ParkingSpot.class);
                        spot.setId(doc.getId());
                        allSpots.add(spot);
                        if ("Available".equals(spot.getStatus())) available++;
                    }

                    tvSpotsCount.setText(allSpots.size() + " Spots . " + available + " available");
                    tabAll.setText("All(" + allSpots.size() + ")");
                    tabFree.setText("Free(" + available + ")");
                    tabTaken.setText("Taken(" + (allSpots.size() - available) + ")");

                    filteredSpots.clear();
                    filteredSpots.addAll(allSpots);
                    adapter.notifyDataSetChanged();
                });
    }
}
