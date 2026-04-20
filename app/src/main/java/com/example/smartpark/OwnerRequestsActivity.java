package com.example.smartpark;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class OwnerRequestsActivity extends AppCompatActivity {

    private RecyclerView rvRequests;
    private ArrayList<ParkingRequest> requestList;
    private OwnerRequestAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_requests);

        db = FirebaseFirestore.getInstance();

        android.view.View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvRequests = findViewById(R.id.rv_owner_requests);
        rvRequests.setLayoutManager(new LinearLayoutManager(this));

        requestList = new ArrayList<>();
        adapter = new OwnerRequestAdapter(this, requestList);
        rvRequests.setAdapter(adapter);

        loadRequests();
    }

    private void loadRequests() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        db.collection("parking_requests")
                .whereEqualTo("ownerId", user.getUid())
                .whereEqualTo("status", "pending")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        return;
                    }
                    requestList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        String id = doc.getId();
                        String spotId = doc.getString("spotId");
                        String spotName = doc.getString("spotName");
                        String driverId = doc.getString("driverId");
                        String ownerId = doc.getString("ownerId");
                        String status = doc.getString("status");
                        
                        Object timestampObj = doc.get("timestamp");
                        String timeString = "Unknown Time";
                        if (timestampObj instanceof Timestamp) {
                            Date date = ((Timestamp) timestampObj).toDate();
                            timeString = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(date);
                        }

                        requestList.add(new ParkingRequest(id, spotId, spotName, driverId, ownerId, status, timeString));
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
