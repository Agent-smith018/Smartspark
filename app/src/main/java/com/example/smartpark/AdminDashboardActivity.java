package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_admin_dashboard);

		enforceAdminAccess();

		Button btnViewDriverDashboard = findViewById(R.id.btn_view_driver_dashboard);
		Button btnViewOwnerDashboard = findViewById(R.id.btn_view_owner_dashboard);
		Button btnReviewReportedSpots = findViewById(R.id.btn_review_reported_spots);
		Button btnAdminLogout = findViewById(R.id.btn_admin_logout);

		btnViewDriverDashboard.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, HomeActivity.class)));
		btnViewOwnerDashboard.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, OwnerDashboardActivity.class)));
		btnReviewReportedSpots.setOnClickListener(v -> startActivity(new Intent(AdminDashboardActivity.this, AdminReportsActivity.class)));
		btnAdminLogout.setOnClickListener(v -> {
			FirebaseAuth.getInstance().signOut();
			Intent intent = new Intent(AdminDashboardActivity.this, MainActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			finish();
		});
	}

	private void enforceAdminAccess() {
		FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
		if (currentUser == null) {
			startActivity(new Intent(AdminDashboardActivity.this, MainActivity.class));
			finish();
			return;
		}

		FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
				.addOnSuccessListener(documentSnapshot -> {
					String role = documentSnapshot.getString("role");
					if (!"admin".equalsIgnoreCase(role)) {
						if ("owner".equalsIgnoreCase(role)) {
							startActivity(new Intent(AdminDashboardActivity.this, OwnerDashboardActivity.class));
						} else {
							startActivity(new Intent(AdminDashboardActivity.this, HomeActivity.class));
						}
						finish();
					}
				});
	}
}
