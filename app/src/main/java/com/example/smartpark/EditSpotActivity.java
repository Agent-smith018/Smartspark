package com.example.smartpark;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import java.sql.CallableStatement;
//import com.parking.manager.data.Spot;
//import com.parking.manager.data.SpotRepository;

public class EditSpotActivity extends AppCompatActivity {

    private static final String ARG_SPOT_ID = "spot_id";
    private String spotId;
    private EditText etSpotName, etAddress, etTotalSpots, etHourlyRate, etOpenTime, etCloseTime, etDescription;
    private Spinner spinnerSpotType, spinnerPricing;
    private RadioGroup rgStatus;
    private Button btnUpdate, btnDiscard;

    public static EditSpotActivity newInstance(String spotId) {
        EditSpotActivity activity = new EditSpotActivity();
        Bundle args = new Bundle();
        args.putString(ARG_SPOT_ID, spotId);
        activity.setArguments(args);
        return activity;
    }

    private void setArguments(Bundle args) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            spotId = getArguments().getString(ARG_SPOT_ID);
        }
    }

    private CallableStatement getArguments() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_edit_spot, container, false);

        etSpotName = view.findViewById(R.id.etSpotName);
        etAddress = view.findViewById(R.id.etAddress);
        etTotalSpots = view.findViewById(R.id.etTotalSpots);
        spinnerSpotType = view.findViewById(R.id.spinnerSpotType);
        rgStatus = view.findViewById(R.id.rgStatus);
        spinnerPricing = view.findViewById(R.id.spinnerPricing);
        etHourlyRate = view.findViewById(R.id.etHourlyRate);
        etOpenTime = view.findViewById(R.id.etOpenTime);
        etCloseTime = view.findViewById(R.id.etCloseTime);
        etDescription = view.findViewById(R.id.etDescription);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        btnDiscard = view.findViewById(R.id.btnDiscard);

        setupSpinners();
        loadSpotData();

        btnUpdate.setOnClickListener(v -> updateSpot());
        btnDiscard.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.spot_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpotType.setAdapter(typeAdapter);

        ArrayAdapter<CharSequence> pricingAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.pricing_types, android.R.layout.simple_spinner_item);
        pricingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPricing.setAdapter(pricingAdapter);
    }

    private void loadSpotData() {
        Spot spot = SpotRepository.getInstance().getSpotById(spotId);
        if (spot != null) {
            etSpotName.setText(spot.getName());
            etAddress.setText(spot.getAddress());
            etTotalSpots.setText(String.valueOf(spot.getTotalSpots()));
            setSpinnerValue(spinnerSpotType, spot.getSpotType());
            if (spot.getStatus().equals("Open")) {
                rgStatus.check(R.id.radioOpen);
            } else {
                rgStatus.check(R.id.radioClosed);
            }
            setSpinnerValue(spinnerPricing, spot.getPricingType());
            etHourlyRate.setText(String.valueOf(spot.getHourlyRate()));
            etOpenTime.setText(spot.getOpenTime());
            etCloseTime.setText(spot.getCloseTime());
            etDescription.setText(spot.getDescription());
        }
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void updateSpot() {
        String name = etSpotName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String totalSpotsStr = etTotalSpots.getText().toString().trim();
        String spotType = spinnerSpotType.getSelectedItem().toString();
        int selectedStatusId = rgStatus.getCheckedRadioButtonId();
        String status = selectedStatusId == R.id.radioOpen ? "Open" : "Closed";
        String pricingType = spinnerPricing.getSelectedItem().toString();
        double hourlyRate = Double.parseDouble(etHourlyRate.getText().toString());
        String openTime = etOpenTime.getText().toString().trim();
        String closeTime = etCloseTime.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        Spot updatedSpot = new Spot(spotId, name, address, Integer.parseInt(totalSpotsStr),
                spotType, status, pricingType, hourlyRate, openTime, closeTime, description);

        SpotRepository.getInstance().updateSpot(updatedSpot);
        Toast.makeText(getContext(), "Spot updated", Toast.LENGTH_SHORT).show();
        getParentFragmentManager().popBackStack();
    }
}