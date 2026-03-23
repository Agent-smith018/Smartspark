package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etConfirmPassword;
    private RadioGroup rgRole;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +
                    "(?=.*[a-z])" +
                    "(?=.*[A-Z])" +
                    "(?=.*[@#$%^&+=!])" +
                    "(?=\\S+$)" +
                    ".{8,}" +
                    "$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.et_signup_name);
        etEmail = findViewById(R.id.et_signup_email);
        etPassword = findViewById(R.id.et_signup_password);
        etConfirmPassword = findViewById(R.id.et_signup_confirm_password);
        rgRole = findViewById(R.id.rg_role);
        Button btnSignup = findViewById(R.id.btn_signup);
        TextView tvLoginLink = findViewById(R.id.tv_login_link);
        progressBar = findViewById(R.id.signup_progress);

        btnSignup.setOnClickListener(v -> registerUser());

        tvLoginLink.setOnClickListener(v -> {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
            }
        });
    }

    private void registerUser() {
        if (etName.getText() == null || etEmail.getText() == null || 
            etPassword.getText() == null || etConfirmPassword.getText() == null) return;

        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        int selectedRoleId = rgRole.getCheckedRadioButtonId();
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Please select a role", Toast.LENGTH_SHORT).show();
            return;
        }
        
        RadioButton rbRole = findViewById(selectedRoleId);
        final String role = rbRole.getText().toString().toLowerCase().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email required");
            return;
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            etPassword.setError("Password too weak");
            return;
        }

        if (!password.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords don't match");
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Account created successfully! Now save role and navigate immediately.
                        saveUserToFirestoreAndNavigate(name, email, role);
                    } else {
                        progressBar.setVisibility(View.GONE);
                        handleSignupError(task.getException());
                    }
                });
    }

    private void saveUserToFirestoreAndNavigate(String name, String email, String role) {
        String userId = mAuth.getCurrentUser().getUid();
        Map<String, Object> user = new HashMap<>();
        user.put("name", name);
        user.put("email", email);
        user.put("role", role);

        // We trigger the save, but we also navigate immediately to provide a fast experience.
        db.collection("users").document(userId).set(user);
        
        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
        progressBar.setVisibility(View.GONE);
        navigateToHome(role);
    }

    private void navigateToHome(String role) {
        Intent intent;
        if ("owner".equalsIgnoreCase(role)) {
            intent = new Intent(SignupActivity.this, OwnerHomeActivity.class);
        } else {
            intent = new Intent(SignupActivity.this, HomeActivity.class);
        }
        // These flags clear the signup page so you can't go back to it.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void handleSignupError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            etEmail.setError("Email already registered");
        } else {
            Toast.makeText(this, "Registration failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}