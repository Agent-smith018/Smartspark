package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class OwnerDashboardActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_owner_dashboard);

		enforceOwnerAccess();

		Button btnManageSpots = findViewById(R.id.btn_manage_spots);
		Button btnAddSpot = findViewById(R.id.btn_add_spot);
		Button btnOwnerLogout = findViewById(R.id.btn_owner_logout);

		btnManageSpots.setOnClickListener(v -> startActivity(new Intent(OwnerDashboardActivity.this, OwnerSpotActivity.class)));
		btnAddSpot.setOnClickListener(v -> startActivity(new Intent(OwnerDashboardActivity.this, AddParkingSpotActivity.class)));
		btnOwnerLogout.setOnClickListener(v -> {
			FirebaseAuth.getInstance().signOut();
			Intent intent = new Intent(OwnerDashboardActivity.this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			finish();
		});
	}

	private void enforceOwnerAccess() {
		FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
		if (currentUser == null) {
			startActivity(new Intent(OwnerDashboardActivity.this, MainActivity.class));
			finish();
			return;
		}

		FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
				.addOnSuccessListener(documentSnapshot -> {
					String role = documentSnapshot.getString("role");
					if (!"owner".equalsIgnoreCase(role)) {
						if ("admin".equalsIgnoreCase(role)) {
							startActivity(new Intent(OwnerDashboardActivity.this, AdminDashboardActivity.class));
						} else {
							startActivity(new Intent(OwnerDashboardActivity.this, HomeActivity.class));
						}
						finish();
					}
				});
	}
}
