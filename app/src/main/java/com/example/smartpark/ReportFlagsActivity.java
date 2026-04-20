package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpark.R;
import com.example.smartpark.ReportAdapter;
import com.example.smartpark.Report;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class ReportFlagsActivity extends AppCompatActivity {

    private TextView tvCriticalCount;
    private RecyclerView rvCriticalReports, rvAllReports;
    private LinearLayout navOverview, navUsers, navSpots, navReports;

    private FirebaseFirestore db;
    private List<Report> criticalReports = new ArrayList<>();
    private List<Report> allReportsList = new ArrayList<>();
    private ReportAdapter criticalAdapter, allAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports_flags);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupRecyclerViews();
        loadReports();
        setupNavigation();
    }

    private void initViews() {
        tvCriticalCount = findViewById(R.id.tvCriticalCount);
        rvCriticalReports = findViewById(R.id.rvCriticalReports);
        rvAllReports = findViewById(R.id.rvAllReports);
        navOverview = findViewById(R.id.navOverview);
        navUsers = findViewById(R.id.navUsers);
        navSpots = findViewById(R.id.navSpots);
        navReports = findViewById(R.id.navReports);
    }

    private void setupRecyclerViews() {
        criticalAdapter = new ReportAdapter(criticalReports, this::handleReport);
        rvCriticalReports.setLayoutManager(new LinearLayoutManager(this));
        rvCriticalReports.setAdapter(criticalAdapter);

        allAdapter = new ReportAdapter(allReportsList, this::handleReport);
        rvAllReports.setLayoutManager(new LinearLayoutManager(this));
        rvAllReports.setAdapter(allAdapter);
    }

    private void handleReport(Report report) {
        // Navigate to user profile or spot detail
        if ("user".equals(report.getType())) {
            Intent intent = new Intent(this, UserManagmentActivity.class);
            startActivity(intent);
        } else if ("spot".equals(report.getType())) {
            Intent intent = new Intent(this, SpotOversightActivity.class);
            startActivity(intent);
        }
    }

    private void loadReports() {
        db.collection("reports")
                .whereEqualTo("resolved", false)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    criticalReports.clear();
                    allReportsList.clear();
                    int criticalCount = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        Report report = doc.toObject(Report.class);
                        report.setId(doc.getId());
                        allReportsList.add(report);

                        if (Boolean.TRUE.equals(doc.getBoolean("critical"))) {
                            criticalReports.add(report);
                            criticalCount++;
                        }
                    }

                    tvCriticalCount.setText(criticalCount + " critical");
                    criticalAdapter.notifyDataSetChanged();
                    allAdapter.notifyDataSetChanged();
                });
    }

    private void setupNavigation() {
        navOverview.setOnClickListener(v ->
                startActivity(new Intent(this, AdminDashboardActivity.class)));
        navUsers.setOnClickListener(v ->
                startActivity(new Intent(this, UserManagmentActivity.class)));
        navSpots.setOnClickListener(v ->
                startActivity(new Intent(this, SpotOversightActivity.class)));
        navReports.setOnClickListener(v -> { /* Already here */ });
    }
}
