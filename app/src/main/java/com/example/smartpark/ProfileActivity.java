package com.example.smartpark;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    private static final String PROFILE_PREFS = "profile_prefs";
    private static final String PROFESSIONAL_TYPE_PREFIX = "professional_type_";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView tvProfileEmail = findViewById(R.id.tv_profile_email);
        TextInputEditText etProfessionalType = findViewById(R.id.et_professional_type);
        android.widget.Button btnSaveProfile = findViewById(R.id.btn_save_profile);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String profileKey = PROFESSIONAL_TYPE_PREFIX + (currentUser != null ? currentUser.getUid() : "guest");

        if (currentUser != null && currentUser.getEmail() != null) {
            tvProfileEmail.setText(currentUser.getEmail());
        } else {
            tvProfileEmail.setText("Not signed in");
        }

        String savedProfessionalType = getSharedPreferences(PROFILE_PREFS, MODE_PRIVATE)
                .getString(profileKey, "");
        etProfessionalType.setText(savedProfessionalType);

        btnSaveProfile.setOnClickListener(v -> {
            String professionalType = "";
            if (etProfessionalType.getText() != null) {
                professionalType = etProfessionalType.getText().toString().trim();
            }

            getSharedPreferences(PROFILE_PREFS, MODE_PRIVATE)
                    .edit()
                    .putString(profileKey, professionalType)
                    .apply();

            Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show();
        });
    }
}
