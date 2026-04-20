package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpark.R;
import com.example.smartpark.RecentActivityAdapter;
import com.example.smartpark.ActivityItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AdminDashboardActivity extends AppCompatActivity {
    private TextView tvTotalUsers, tvActiveSpots, tvPendingOwners, tvOpenReports;
    private LinearLayout chartContainer;
    private RecyclerView rvRecentActivity;
    private LinearLayout navOverview, navUsers, navSpots, navReports;
    private ImageButton btnLogout;

    private FirebaseFirestore db;
    private List<ActivityItem> activityList = new ArrayList<>();
    private RecentActivityAdapter activityAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupRecyclerView();
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
        btnLogout = findViewById(R.id.btn_admin_logout);
    }

    private void setupRecyclerView() {
        activityAdapter = new RecentActivityAdapter(activityList);
        rvRecentActivity.setLayoutManager(new LinearLayoutManager(this));
        rvRecentActivity.setAdapter(activityAdapter);
    }

    private void drawChart(int[] dailyActivityCounts) {
        chartContainer.removeAllViews();
        int maxValue = 0;
        for (int v : dailyActivityCounts) if (v > maxValue) maxValue = v;

        int chartHeightDp = 80;
        float density = getResources().getDisplayMetrics().density;
        int chartHeightPx = (int)(chartHeightDp * density);

        for (int i = 0; i < dailyActivityCounts.length; i++) {
            LinearLayout barWrapper = new LinearLayout(this);
            LinearLayout.LayoutParams wrapperParams = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            wrapperParams.setMarginStart((int)(2 * density));
            wrapperParams.setMarginEnd((int)(2 * density));
            barWrapper.setLayoutParams(wrapperParams);
            barWrapper.setOrientation(LinearLayout.VERTICAL);
            barWrapper.setGravity(Gravity.BOTTOM);

            View bar = new View(this);
            int barHeight = maxValue > 0 ? (int)((dailyActivityCounts[i] / (float) maxValue) * chartHeightPx) : (int)(4 * density);
            if (barHeight < (int)(4 * density)) barHeight = (int)(4 * density);
            
            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, barHeight);
            bar.setLayoutParams(barParams);
            bar.setBackgroundColor(0xFF1565C0); // Admin blue
            bar.setAlpha(maxValue > 0 ? (0.6f + (0.4f * dailyActivityCounts[i] / maxValue)) : 0.4f);

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
        db.collection("parking_spots")
                .whereEqualTo("status", "available")
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
        db.collection("spot_reports")
                .whereEqualTo("state", "open")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    tvOpenReports.setText(String.valueOf(snapshots.size()));
                });

        // Recent activity and Chart Data
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -6);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date sevenDaysAgo = cal.getTime();

        db.collection("activityLog")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    
                    activityList.clear();
                    int[] dailyCounts = new int[7];
                    
                    for (QueryDocumentSnapshot doc : snapshots) {
                        ActivityItem item = doc.toObject(ActivityItem.class);
                        
                        if (activityList.size() < 10) {
                            activityList.add(item);
                        }

                        if (item.getTimestamp() != null) {
                            Date date = item.getTimestamp().toDate();
                            if (date.after(sevenDaysAgo)) {
                                long diff = date.getTime() - sevenDaysAgo.getTime();
                                int dayIndex = (int) (diff / (1000 * 60 * 60 * 24));
                                if (dayIndex >= 0 && dayIndex < 7) {
                                    dailyCounts[dayIndex]++;
                                }
                            }
                        }
                    }
                    activityAdapter.notifyDataSetChanged();
                    drawChart(dailyCounts);
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

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
