package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

public class OwnerHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_home);

        ExtendedFloatingActionButton btnAddSpot = findViewById(R.id.btn_add_spot_owner);
        Button btnLogout = findViewById(R.id.btn_logout_owner);

        btnAddSpot.setOnClickListener(v -> {
            Intent intent = new Intent(OwnerHomeActivity.this, AddParkingSpotActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(OwnerHomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}