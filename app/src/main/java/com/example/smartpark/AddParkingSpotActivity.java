package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

public class AddParkingSpotActivity extends AppCompatActivity {

    private EditText etSpotName, etAddress, etTotalSpots, etHourlyRate, etOpenTime, etCloseTime, etDescription;
    private Spinner spinnerSpotType, spinnerPricing;
    private RadioGroup rgStatus;
    private Button btnSave;
    private TextView tvTitle;

    private Spot existingSpot;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot); // Your XML layout with all fields

        // Initialize views
        tvTitle = findViewById(R.id.tv_title);
        etSpotName = findViewById(R.id.et_spot_name);
        etAddress = findViewById(R.id.et_address);
        etTotalSpots = findViewById(R.id.et_total_spots);
        spinnerSpotType = findViewById(R.id.spinner_spot_type);
        rgStatus = findViewById(R.id.rg_status);
        spinnerPricing = findViewById(R.id.spinner_pricing);
        etHourlyRate = findViewById(R.id.et_hourly_rate);
        etOpenTime = findViewById(R.id.et_open_time);
        etCloseTime = findViewById(R.id.et_close_time);
        etDescription = findViewById(R.id.et_description);
        btnSave = findViewById(R.id.btn_save);

        setupSpinners();

        // Check if we are in Edit Mode
        if (getIntent().hasExtra("spot")) {
            existingSpot = (Spot) getIntent().getSerializableExtra("spot");
            isEditMode = true;
            setupEditMode();
        }

        // Handle pricing spinner: enable/disable hourly rate field
        spinnerPricing.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                boolean isPaid = position == 1; // "Paid" is second item (index 1)
                etHourlyRate.setEnabled(isPaid);
                if (!isPaid) etHourlyRate.setText("0");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnSave.setOnClickListener(v -> saveSpot());
    }

    private void setupSpinners() {
        // Spot Type spinner
        ArrayAdapter<CharSequence> typeAdapter = ArrayAdapter.createFromResource(this,
                R.array.spot_types, android.R.layout.simple_spinner_item);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSpotType.setAdapter(typeAdapter);

        // Pricing spinner
        ArrayAdapter<CharSequence> pricingAdapter = ArrayAdapter.createFromResource(this,
                R.array.pricing_types, android.R.layout.simple_spinner_item);
        pricingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPricing.setAdapter(pricingAdapter);
    }

    private void setupEditMode() {
        tvTitle.setText("Edit Parking Spot");
        btnSave.setText("Update Spot");

        if (existingSpot != null) {
            etSpotName.setText(existingSpot.getName());
            etAddress.setText(existingSpot.getAddress());
            etTotalSpots.setText(String.valueOf(existingSpot.getTotalSpots()));

            // Set spinner values
            setSpinnerSelection(spinnerSpotType, existingSpot.getSpotType());
            setSpinnerSelection(spinnerPricing, existingSpot.getPricingType());

            // Set radio button for status
            if ("Open".equals(existingSpot.getStatus())) {
                rgStatus.check(R.id.radio_open);
            } else {
                rgStatus.check(R.id.radio_closed);
            }

            etHourlyRate.setText(String.valueOf(existingSpot.getHourlyRate()));
            etOpenTime.setText(existingSpot.getOpenTime());
            etCloseTime.setText(existingSpot.getCloseTime());
            etDescription.setText(existingSpot.getDescription());

            // Enable/disable hourly rate based on pricing type
            etHourlyRate.setEnabled("Paid".equals(existingSpot.getPricingType()));
        }
    }

    private void setSpinnerSelection(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void saveSpot() {
        String name = etSpotName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String totalSpotsStr = etTotalSpots.getText().toString().trim();
        String spotType = spinnerSpotType.getSelectedItem().toString();
        int selectedStatusId = rgStatus.getCheckedRadioButtonId();
        String status = (selectedStatusId == R.id.radio_open) ? "Open" : "Closed";
        String pricingType = spinnerPricing.getSelectedItem().toString();
        String hourlyRateStr = etHourlyRate.getText().toString().trim();
        String openTime = etOpenTime.getText().toString().trim();
        String closeTime = etCloseTime.getText().toString().trim();
        String description = etDescription.getText().toString().trim();

        // Validation
        if (name.isEmpty() || address.isEmpty() || totalSpotsStr.isEmpty() ||
                openTime.isEmpty() || closeTime.isEmpty()) {
            Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int totalSpots;
        try {
            totalSpots = Integer.parseInt(totalSpotsStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Total Spots must be a number", Toast.LENGTH_SHORT).show();
            return;
        }

        double hourlyRate = 0;
        if (pricingType.equals("Paid")) {
            try {
                hourlyRate = Double.parseDouble(hourlyRateStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid hourly rate", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        if (isEditMode) {
            // Update existing spot
            existingSpot.setName(name);
            existingSpot.setAddress(address);
            existingSpot.setTotalSpots(totalSpots);
            existingSpot.setSpotType(spotType);
            existingSpot.setStatus(status);
            existingSpot.setPricingType(pricingType);
            existingSpot.setHourlyRate(hourlyRate);
            existingSpot.setOpenTime(openTime);
            existingSpot.setCloseTime(closeTime);
            existingSpot.setDescription(description);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_spot", existingSpot);
            setResult(RESULT_OK, resultIntent);
            Toast.makeText(this, "Spot updated", Toast.LENGTH_SHORT).show();
        } else {
            // Create new spot
            String id = UUID.randomUUID().toString();
            Spot newSpot = new Spot(id, name, address, totalSpots, spotType, status,
                    pricingType, hourlyRate, openTime, closeTime, description);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("new_spot", newSpot);
            setResult(RESULT_OK, resultIntent);
            Toast.makeText(this, "Spot added", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}