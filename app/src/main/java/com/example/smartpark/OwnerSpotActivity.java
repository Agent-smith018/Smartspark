package com.example.smartpark;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.smartpark.R;
import com.example.smartpark.adapters.SpotAdapter;
import com.parking.mr.data.Spot;
import com.parking.manager.data.SpotRepository;
import java.util.List;

public class OwnerSpotActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SpotAdapter adapter;
    private SpotRepository repository;
    private TextView tvActualValue, tvAccrued, tvTodayBooking, tvTodayReservation;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_spots, container, false);

        repository = SpotRepository.getInstance();

        // Other's Value Record stats
        tvActualValue = view.findViewById(R.id.tvActualValue);
        tvAccrued = view.findViewById(R.id.tvAccrued);
        tvTodayBooking = view.findViewById(R.id.tvTodayBooking);
        tvTodayReservation = view.findViewById(R.id.tvTodayReservation);

        tvActualValue.setText("45");
        tvAccrued.setText("2 late");
        tvTodayBooking.setText("214");
        tvTodayReservation.setText("10");

        // RecyclerView for spots
        recyclerView = view.findViewById(R.id.recyclerSpots);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        Button btnAddSpot = view.findViewById(R.id.btnAddParkingSpot);
        btnAddSpot.setOnClickListener(v -> {
            AddSpotFragment addFragment = new AddSpotFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, addFragment)
                    .addToBackStack(null)
                    .commit();
        });

        loadSpots();

        return view;
    }

    private void loadSpots() {
        List<Spot> spots = repository.getAllSpots();
        adapter = new SpotAdapter(spots, spot -> {
            EditSpotFragment editFragment = EditSpotFragment.newInstance(spot.getId());
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, editFragment)
                    .addToBackStack(null)
                    .commit();
        });
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSpots();
    }
}