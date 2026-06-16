package com.example.alphadiet;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnYaTengoCuenta;
    private Button btnRegistrarme;
    private Button btnGoogle;
    private Button btnInstagram;
    private Button btnFacebook;
    private TextView tvTerminos;
    private TextView tvPrivacidad;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_welcome);
        initViews();
        setupListeners();
    }

    @SuppressLint("WrongViewCast")
    private void initViews() {
        btnYaTengoCuenta = findViewById(R.id.btn_ya_tengo_cuenta);
        btnRegistrarme   = findViewById(R.id.btn_registrarme);
        btnGoogle        = findViewById(R.id.btn_google);
        btnInstagram     = findViewById(R.id.btn_instagram);
        btnFacebook      = findViewById(R.id.btn_facebook);
        tvTerminos       = findViewById(R.id.tv_terminos);
        tvPrivacidad     = findViewById(R.id.tv_privacidad);
    }

    private void setupListeners() {
        btnYaTengoCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            }
        });

        btnRegistrarme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, RegisterStep1Activity.class));
            }
        });

        btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Google Sign-In
            }
        });

        btnInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Instagram Login
            }
        });

        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Facebook Login
            }
        });

        tvTerminos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: abrir Términos de Uso
            }
        });

        tvPrivacidad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: abrir Política de Privacidad
            }
        });
    }
}