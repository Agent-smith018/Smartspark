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
import com.example.smartpark.UserListAdapter;
import com.example.smartpark.AppUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;


public class UserManagmentActivity extends AppCompatActivity {

    private TextView tvUserCount, tabAll, tabDrivers, tabOwners, tabPending;
    private RecyclerView rvUsers;
    private ImageButton btnSearch;
    private LinearLayout navOverview, navUsers, navSpots, navReports;

    private FirebaseFirestore db;
    private List<AppUser> allUsers = new ArrayList<>();
    private List<AppUser> filteredUsers = new ArrayList<>();
    private UserListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_managment);

        db = FirebaseFirestore.getInstance();
        initViews();
        setupRecyclerView();
        setupTabs();
        loadUsers();
        setupNavigation();
    }

    private void initViews() {
        tvUserCount = findViewById(R.id.tvUserCount);
        tabAll = findViewById(R.id.tabAll);
        tabDrivers = findViewById(R.id.tabDrivers);
        tabOwners = findViewById(R.id.tabOwners);
        tabPending = findViewById(R.id.tabPending);
        rvUsers = findViewById(R.id.rvUsers);
        btnSearch = findViewById(R.id.btnSearch);
        navOverview = findViewById(R.id.navOverview);
        navUsers = findViewById(R.id.navUsers);
        navSpots = findViewById(R.id.navSpots);
        navReports = findViewById(R.id.navReports);
    }

    private void setupRecyclerView() {
        adapter = new UserListAdapter(filteredUsers, user -> {
            if ("owner".equals(user.getRole()) && !user.isApproved()) {
                Intent intent = new Intent(this, OwnerApprovalActivity.class);
                intent.putExtra("userId", user.getId());
                startActivity(intent);
            }
        });
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);
    }

    private void setupTabs() {
        tabAll.setOnClickListener(v -> filterUsers("all"));
        tabDrivers.setOnClickListener(v -> filterUsers("driver"));
        tabOwners.setOnClickListener(v -> filterUsers("owner"));
        tabPending.setOnClickListener(v -> filterUsers("pending"));
    }

    private void filterUsers(String filter) {
        filteredUsers.clear();
        for (AppUser user : allUsers) {
            boolean match = false;
            switch (filter) {
                case "all": match = true; break;
                case "driver": match = "driver".equals(user.getRole()); break;
                case "owner": match = "owner".equals(user.getRole()) && user.isApproved(); break;
                case "pending": match = "owner".equals(user.getRole()) && !user.isApproved(); break;
            }
            if (match) filteredUsers.add(user);
        }
        adapter.notifyDataSetChanged();
    }

    private void loadUsers() {
        db.collection("users")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    allUsers.clear();
                    int pending = 0;

                    for (QueryDocumentSnapshot doc : snapshots) {
                        AppUser user = doc.toObject(AppUser.class);
                        user.setId(doc.getId());
                        allUsers.add(user);
                        if ("owner".equals(user.getRole()) && !user.isApproved()) pending++;
                    }

                    tvUserCount.setText(allUsers.size() + " accounts");
                    tabAll.setText("All(" + allUsers.size() + ")");
                    tabPending.setText("Pending(" + pending + ")");

                    filteredUsers.clear();
                    filteredUsers.addAll(allUsers);
                    adapter.notifyDataSetChanged();
                });
    }

    private void setupNavigation() {
        navOverview.setOnClickListener(v ->
                startActivity(new Intent(this, AdminDashboardActivity.class)));
        navUsers.setOnClickListener(v -> { /* Already here */ });
        navSpots.setOnClickListener(v ->
                startActivity(new Intent(this, SpotOversightActivity.class)));
        navReports.setOnClickListener(v ->
                startActivity(new Intent(this, ReportFlagsActivity.class)));
    }
}
