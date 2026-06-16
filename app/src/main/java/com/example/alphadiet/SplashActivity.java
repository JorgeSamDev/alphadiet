package com.example.alphadiet;

import android.content.Intent;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private TextView tvLogo;
    private TextView tvSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_splash);

        tvLogo     = findViewById(R.id.tv_splash_logo);
        tvSubtitle = findViewById(R.id.tv_splash_subtitle);

        // Animación fade in del logo
        tvLogo.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(300)
                .start();

        // Animación fade in del subtítulo
        tvSubtitle.animate()
                .alpha(1f)
                .setDuration(1000)
                .setStartDelay(800)
                .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        // Esperar 1 segundo más y pasar a Welcome
                        tvSubtitle.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(SplashActivity.this, WelcomeActivity.class));
                                finish();
                            }
                        }, 1000);
                    }
                })
                .start();
    }
}