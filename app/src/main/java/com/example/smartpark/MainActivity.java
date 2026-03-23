package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignup, tvForgotPassword;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            checkUserRoleAndNavigate(currentUser.getUid());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvSignup = findViewById(R.id.tv_signup);
        tvForgotPassword = findViewById(R.id.tv_forgot_password);
        progressBar = findViewById(R.id.progressBar);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnLogin.setOnClickListener(v -> loginUser());

        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        tvForgotPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter your email to reset password");
            etEmail.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(MainActivity.this, "Reset link sent to your email!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void loginUser() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

        etEmail.setError(null);
        etPassword.setError(null);

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        checkUserRoleAndNavigate(mAuth.getCurrentUser().getUid());
                    } else {
                        progressBar.setVisibility(View.GONE);
                        handleLoginError(task.getException());
                    }
                });
    }

    private void checkUserRoleAndNavigate(String uid) {
        progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            progressBar.setVisibility(View.GONE);
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String role = document.getString("role");
                    if ("owner".equals(role)) {
                        startActivity(new Intent(MainActivity.this, OwnerHomeActivity.class));
                    } else {
                        startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    }
                    finish();
                } else {
                    // Fallback if document doesn't exist
                    startActivity(new Intent(MainActivity.this, HomeActivity.class));
                    finish();
                }
            } else {
                Toast.makeText(MainActivity.this, "Error fetching user role", Toast.LENGTH_SHORT).show();
                // Fallback
                startActivity(new Intent(MainActivity.this, HomeActivity.class));
                finish();
            }
        });
    }

    private void handleLoginError(Exception exception) {
        if (exception instanceof FirebaseAuthInvalidUserException) {
            etEmail.setError("No account found with this email");
            etEmail.requestFocus();
        } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
            etPassword.setError("Incorrect password");
            etPassword.requestFocus();
        } else {
            Toast.makeText(MainActivity.this, "Login failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}