package com.example.smartpark;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        View logo = findViewById(R.id.iv_logo);
        View title = findViewById(R.id.tv_app_name);
        View slogan = findViewById(R.id.tv_slogan);
        View progress = findViewById(R.id.splash_progress);

        // Initial state for animations
        logo.setAlpha(0f);
        logo.setScaleX(0.2f);
        logo.setScaleY(0.2f);
        
        title.setAlpha(0f);
        title.setTranslationY(100f);

        slogan.setAlpha(0f);
        
        progress.setAlpha(0f);

        // 1. Logo Animation: Scale up with a "pop" effect (Overshoot)
        logo.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .setDuration(1000)
                .setInterpolator(new AnticipateOvershootInterpolator())
                .start();

        // 2. Title Animation: Slide up and fade in
        title.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(800)
                .setStartDelay(400)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // 3. Slogan Animation: Simple fade in
        slogan.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(1000)
                .start();

        // 4. Progress Bar Animation: Fade in
        progress.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(1500)
                .start();

        // Navigate to MainActivity after 3.5 seconds
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            // Add a smooth fade-out transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finish();
        }, 3500);
    }
}