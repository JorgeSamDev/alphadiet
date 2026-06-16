package com.example.alphadiet;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    private EditText etCorreo;
    private EditText etContrasena;
    private Button btnContinuar;
    private TextView tvOlvidaste;
    private TextView tvTerminos;
    private TextView tvPrivacidad;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_login);
        mAuth = FirebaseAuth.getInstance();
        initViews();
        setupListeners();
    }

    private void initViews() {
        etCorreo     = findViewById(R.id.et_correo);
        etContrasena = findViewById(R.id.et_contrasena);
        btnContinuar = findViewById(R.id.btn_continuar);
        tvOlvidaste  = findViewById(R.id.tv_olvidaste);
        tvTerminos   = findViewById(R.id.tv_terminos);
        tvPrivacidad = findViewById(R.id.tv_privacidad);
    }

    private void setupListeners() {
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
                    iniciarSesion();
                }
            }
        });

        tvOlvidaste.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recuperarContrasena();
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

    private void iniciarSesion() {
        String correo     = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        if (mAuth.getCurrentUser().isEmailVerified()) {
                            Toast.makeText(LoginActivity.this, "Sesión iniciada", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this,
                                    "Debes verificar tu correo antes de iniciar sesión",
                                    Toast.LENGTH_LONG).show();
                            mAuth.signOut();
                        }
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Correo o contraseña incorrectos", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void recuperarContrasena() {
        String correo = etCorreo.getText().toString().trim();

        if (TextUtils.isEmpty(correo)) {
            etCorreo.setError("Ingresa tu correo primero");
            etCorreo.requestFocus();
            return;
        }

        mAuth.sendPasswordResetEmail(correo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(LoginActivity.this,
                                "Se envió un correo de recuperación", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Error, verifica el correo ingresado", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validarCampos() {
        String correo     = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        if (TextUtils.isEmpty(correo)) {
            etCorreo.setError("Ingresa tu correo");
            etCorreo.requestFocus();
            return false;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Correo inválido");
            etCorreo.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(contrasena)) {
            etContrasena.setError("Ingresa tu contraseña");
            etContrasena.requestFocus();
            return false;
        }
        if (contrasena.length() < 6) {
            etContrasena.setError("Mínimo 6 caracteres");
            etContrasena.requestFocus();
            return false;
        }
        return true;
    }
}