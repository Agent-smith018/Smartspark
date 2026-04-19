package com.example.smartpark;

import android.content.Intent;
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

public class OwnerHistoryActivity extends AppCompatActivity {

    private RecyclerView rvHistory;
    private ArrayList<OwnerHistory> historyList;
    private OwnerHistoryAdapter adapter;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_history);

        db = FirebaseFirestore.getInstance();

        rvHistory = findViewById(R.id.rv_owner_history);
        
        android.view.View btnBack = findViewById(R.id.btn_back);
        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        rvHistory.setLayoutManager(new LinearLayoutManager(this));

        historyList = new ArrayList<>();
        adapter = new OwnerHistoryAdapter(this, historyList);
        rvHistory.setAdapter(adapter);

        loadHistory();
    }

    private void loadHistory() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        db.collection("spot_history")
                .whereEqualTo("ownerId", user.getUid())
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) {
                        return;
                    }
                    historyList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        String id = doc.getId();
                        String spotName = doc.getString("spotName");
                        String status = doc.getString("status");
                        
                        Object timestampObj = doc.get("timestamp");
                        String timeString = "Unknown Time";
                        if (timestampObj instanceof Timestamp) {
                            Date date = ((Timestamp) timestampObj).toDate();
                            timeString = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(date);
                        }

                        historyList.add(new OwnerHistory(id, spotName, status, timeString));
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}
