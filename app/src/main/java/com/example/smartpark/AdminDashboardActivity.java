package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpark.R;
import com.example.smartpark.RecentActivityAdapter;
import com.example.smartpark.ActivityItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {
    private TextView tvTotalUsers, tvActiveSpots, tvPendingOwners, tvOpenReports;
    private LinearLayout chartContainer;
    private RecyclerView rvRecentActivity;
    private LinearLayout navOverview, navUsers, navSpots, navReports;

    private FirebaseFirestore db;
    private List<ActivityItem> activityList = new ArrayList<>();
    private RecentActivityAdapter activityAdapter;

    // Sample weekly data (replace with real Firestore data)
    private final int[] weeklyData = {60, 40, 75, 55, 80, 45};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
        drawChart();
        loadDashboardData();
        setupNavigation();
    }

    private void initViews() {
        tvTotalUsers = findViewById(R.id.tvTotalUsers);
        tvActiveSpots = findViewById(R.id.tvActiveSpots);
        tvPendingOwners = findViewById(R.id.tvPendingOwners);
        tvOpenReports = findViewById(R.id.tvOpenReports);
        chartContainer = findViewById(R.id.chartContainer);
        rvRecentActivity = findViewById(R.id.rvRecentActivity);
        navOverview = findViewById(R.id.navOverview);
        navUsers = findViewById(R.id.navUsers);
        navSpots = findViewById(R.id.navSpots);
        navReports = findViewById(R.id.navReports);
    }

    private void setupRecyclerView() {
        activityAdapter = new RecentActivityAdapter(activityList);
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(this));
        rvRecentActivity.setAdapter(activityAdapter);
    }

    private void drawChart() {
        chartContainer.removeAllViews();
        int maxValue = 0;
        for (int v : weeklyData) if (v > maxValue) maxValue = v;

        int chartHeightDp = 80;
        float density = getResources().getDisplayMetrics().density;
        int chartHeightPx = (int)(chartHeightDp * density);

        for (int i = 0; i < weeklyData.length; i++) {
            LinearLayout barWrapper = new LinearLayout(this);
            LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            wrapperParams.setMarginStart((int)(2 * density));
            wrapperParams.setMarginEnd((int)(2 * density));
            barWrapper.setLayoutParams(wrapperParams);
            barWrapper.setOrientation(LinearLayout.VERTICAL);
            barWrapper.setGravity(Gravity.BOTTOM);

            View bar = new View(this);
            int barHeight = maxValue > 0 ? (int)((weeklyData[i] / (float) maxValue) * chartHeightPx) : 4;
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, barHeight);
            bar.setLayoutParams(barParams);
            bar.setBackgroundColor(0xFF1565C0); // Admin blue
            bar.setAlpha(0.7f + (0.3f * weeklyData[i] / maxValue));

            barWrapper.addView(bar);
            chartContainer.addView(barWrapper);
        }
    }

    private void loadDashboardData() {
        // Total users count
        db.collection("users")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    tvTotalUsers.setText(String.format("%,d", snapshots.size()));
                });

        // Active spots count
        db.collection("parkingLots")
                .whereEqualTo("status", "Open")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    tvActiveSpots.setText(String.valueOf(snapshots.size()));
                });

        // Pending owners
        db.collection("users")
                .whereEqualTo("role", "owner")
                .whereEqualTo("approved", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    tvPendingOwners.setText(String.valueOf(snapshots.size()));
                });

        // Open reports
        db.collection("reports")
                .whereEqualTo("resolved", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    int critical = 0;
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Boolean isCritical = doc.getBoolean("critical");
                        if (Boolean.TRUE.equals(isCritical)) critical++;
                    }
                    tvOpenReports.setText(String.valueOf(snapshots.size()));
                });

        // Recent activity
        db.collection("activityLog")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    activityList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        ActivityItem item = doc.toObject(ActivityItem.class);
                        activityList.add(item);
                    }
                    activityAdapter.notifyDataSetChanged();
                });
    }

    private void setupNavigation() {
        navOverview.setOnClickListener(v -> { /* Already here */ });
        navUsers.setOnClickListener(v ->
                startActivity(new Intent(this, UserManagmentActivity.class)));
        navSpots.setOnClickListener(v ->
                startActivity(new Intent(this, SpotOversightActivity.class)));
        navReports.setOnClickListener(v ->
                startActivity(new Intent(this, ReportFlagsActivity.class)));
    }
}
