package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class OwnerHomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_owner_home);

        enforceOwnerAccess();

        Button btnLogout = findViewById(R.id.btn_logout_owner);

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(OwnerHomeActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void enforceOwnerAccess() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(OwnerHomeActivity.this, MainActivity.class));
            finish();
            return;
        }

        FirebaseFirestore.getInstance().collection("users").document(currentUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    String role = documentSnapshot.getString("role");
                    if (!"owner".equalsIgnoreCase(role)) {
                        startActivity(new Intent(OwnerHomeActivity.this, HomeActivity.class));
                        finish();
                    }
                });
    }
}