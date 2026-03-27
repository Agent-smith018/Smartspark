package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.UUID;

public class AddParkingSpotActivity extends AppCompatActivity {

    private EditText etSpotName, etAddress, etPrice;
    private Button btnSave;
    private TextView tvTitle;
    private ParkingSpot existingSpot;
    private boolean isEditMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_parking_spot);

        etSpotName = findViewById(R.id.et_spot_name);
        etAddress = findViewById(R.id.et_address);
        etPrice = findViewById(R.id.et_price);
        btnSave = findViewById(R.id.btn_save);
        tvTitle = findViewById(R.id.tv_title);

        // Check if we are in Edit Mode
        if (getIntent().hasExtra("spot")) {
            existingSpot = (ParkingSpot) getIntent().getSerializableExtra("spot");
            isEditMode = true;
            setupEditMode();
        }

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSpot();
            }
        });
    }

    private void setupEditMode() {
        tvTitle.setText("Edit Parking Spot");
        btnSave.setText("Update Spot");
        
        if (existingSpot != null) {
            etSpotName.setText(existingSpot.getName());
            etAddress.setText(existingSpot.getAddress());
            etPrice.setText(existingSpot.getPrice());
        }
    }

    private void saveSpot() {
        String name = etSpotName.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String price = etPrice.getText().toString().trim();

        if (name.isEmpty() || address.isEmpty() || price.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isEditMode) {
            // Update existing object
            existingSpot.setName(name);
            existingSpot.setAddress(address);
            existingSpot.setPrice(price);
            
            // Pass back the updated object to the previous activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra("updated_spot", existingSpot);
            setResult(RESULT_OK, resultIntent);
            
            Toast.makeText(this, "Spot updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            // Create a new object and send it back
            String id = UUID.randomUUID().toString();
            ParkingSpot newSpot = new ParkingSpot(id, name, address, price);
            
            Intent resultIntent = new Intent();
            resultIntent.putExtra("new_spot", newSpot);
            setResult(RESULT_OK, resultIntent);

            Toast.makeText(this, "Spot added successfully", Toast.LENGTH_SHORT).show();
        }
        
        finish();
    }
}