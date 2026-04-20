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
import com.example.smartpark.OversightSpotAdapter;
import com.example.smartpark.ParkingLot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class SpotOversightActivity extends AppCompatActivity {

    private TextView tvOversightCount, tabAll, tabActive, tabSuspended;
    private RecyclerView rvOversightSpots;
    private ImageButton btnSearch;
    private LinearLayout navOverview, navUsers, navSpots, navReports;

    private FirebaseFirestore db;
    private List<ParkingLot> allLots = new ArrayList<>();
    private List<ParkingLot> filteredLots = new ArrayList<>();
    private OversightSpotAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spot_oversight);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupRecyclerView();
        setupTabs();
        loadSpots();
        setupNavigation();
    }

    private void initViews() {
        tvOversightCount = findViewById(R.id.tvOversightCount);
        tabAll = findViewById(R.id.tabAll);
        tabActive = findViewById(R.id.tabActive);
        tabSuspended = findViewById(R.id.tabSuspended);
        rvOversightSpots = findViewById(R.id.rvOversightSpots);
        btnSearch = findViewById(R.id.btnSearch);
        navOverview = findViewById(R.id.navOverview);
        navUsers = findViewById(R.id.navUsers);
        navSpots = findViewById(R.id.navSpots);
        navReports = findViewById(R.id.navReports);
    }

    private void setupRecyclerView() {
        adapter = new OversightSpotAdapter(filteredLots, lot -> {
            // Handle spot tap - could show detail / toggle suspend
        });
        rvOversightSpots.setLayoutManager(new LinearLayoutManager(this));
        rvOversightSpots.setAdapter(adapter);
    }

    private void setupTabs() {
        tabAll.setOnClickListener(v -> filterSpots("all"));
        tabActive.setOnClickListener(v -> filterSpots("active"));
        tabSuspended.setOnClickListener(v -> filterSpots("suspended"));
    }

    private void filterSpots(String filter) {
        filteredLots.clear();
        for (ParkingLot lot : allLots) {
            boolean match;
            switch (filter) {
                case "active": match = !lot.isSuspended(); break;
                case "suspended": match = lot.isSuspended(); break;
                default: match = true; break;
            }
            if (match) filteredLots.add(lot);
        }
        adapter.notifyDataSetChanged();
    }

    private void loadSpots() {
        db.collection("parkingLots")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    allLots.clear();
                    int suspended = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        ParkingLot lot = doc.toObject(ParkingLot.class);
                        lot.setId(doc.getId());
                        allLots.add(lot);
                        if (lot.isSuspended()) suspended++;
                    }

                    int active = allLots.size() - suspended;
                    tvOversightCount.setText(active + " active . " + suspended + " suspended");
                    tabAll.setText("All(" + allLots.size() + ")");
                    tabActive.setText("Active(" + active + ")");
                    tabSuspended.setText("Suspended(" + suspended + ")");

                    filteredLots.clear();
                    filteredLots.addAll(allLots);
                    adapter.notifyDataSetChanged();
                });
    }

    private void setupNavigation() {
        navOverview.setOnClickListener(v ->
                startActivity(new Intent(this, AdminDashboardActivity.class)));
        navUsers.setOnClickListener(v ->
                startActivity(new Intent(this, UserManagmentActivity.class)));
        navSpots.setOnClickListener(v -> { /* Already here */ });
        navReports.setOnClickListener(v ->
                startActivity(new Intent(this, ReportFlagsActivity.class)));
    }
}
