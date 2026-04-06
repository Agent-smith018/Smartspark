package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

public class OwnerSpotActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private RecyclerView rvSpots;
    private ArrayList<ParkingSpot> spotList;
    private ParkingSpotAdapter adapter;
    private Button btnAddSpotLarge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_spot);

        rvSpots = findViewById(R.id.rv_spots);
        btnAddSpotLarge = findViewById(R.id.btn_add_spot_large);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // Setup RecyclerView
        rvSpots.setLayoutManager(new LinearLayoutManager(this));

        // Initial Data
        spotList = new ArrayList<>();
        spotList.add(new ParkingSpot("1", "City Center", "Downtown, NY", "$5/hr"));
        spotList.add(new ParkingSpot("2", "Mall Parking", "Near Central Mall", "$3/hr"));

        // Adapter
        adapter = new ParkingSpotAdapter(this, spotList);
        rvSpots.setAdapter(adapter);

        // Add Button Logic
        btnAddSpotLarge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OwnerSpotActivity.this, AddParkingSpotActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
            }
        });

        // Bottom Navigation (Icons)
        bottomNav.setSelectedItemId(R.id.nav_spots);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            // Check for new spot
            if (data.hasExtra("new_spot")) {
                ParkingSpot newSpot = (ParkingSpot) data.getSerializableExtra("new_spot");
                if (newSpot != null) {
                    spotList.add(newSpot);
                    adapter.notifyItemInserted(spotList.size() - 1);
                    rvSpots.scrollToPosition(spotList.size() - 1);
                }
            } 
            // Check for updated spot
            else if (data.hasExtra("updated_spot")) {
                ParkingSpot updatedSpot = (ParkingSpot) data.getSerializableExtra("updated_spot");
                if (updatedSpot != null) {
                    for (int i = 0; i < spotList.size(); i++) {
                        if (spotList.get(i).getId().equals(updatedSpot.getId())) {
                            spotList.set(i, updatedSpot);
                            adapter.notifyItemChanged(i);
                            break;
                        }
                    }
                }
            }
        }
    }
}