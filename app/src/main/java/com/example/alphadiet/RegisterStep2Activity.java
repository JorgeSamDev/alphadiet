package com.example.alphadiet;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterStep2Activity extends AppCompatActivity {

    private EditText etCorreo;
    private EditText etContrasena;
    private EditText etContrasenaa;
    private Button btnContinuar;
    private ProgressBar pbSeguridad;
    private TextView tvSeguridad;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_register_step2);
        mAuth = FirebaseAuth.getInstance();
        initViews();
        setupListeners();
    }

    private void initViews() {
        etCorreo      = findViewById(R.id.et_correo);
        etContrasena  = findViewById(R.id.et_contrasena);
        etContrasenaa = findViewById(R.id.et_contrasenaa);
        btnContinuar  = findViewById(R.id.btn_continuar);
        pbSeguridad   = findViewById(R.id.pb_seguridad);
        tvSeguridad   = findViewById(R.id.tv_seguridad);
    }

    private void setupListeners() {
        // Barra de seguridad en tiempo real
        etContrasena.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                actualizarSeguridad(s.toString());
            }
        });

        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
                    registrarUsuario();
                }
            }
        });
    }

    private void actualizarSeguridad(String contrasena) {
        int nivel = 0;

        if (contrasena.length() >= 6) nivel += 25;
        if (contrasena.length() >= 10) nivel += 25;
        if (contrasena.matches(".*[A-Z].*")) nivel += 25;
        if (contrasena.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}].*")) nivel += 25;

        pbSeguridad.setProgress(nivel);

        if (nivel <= 25) {
            tvSeguridad.setText("Seguridad: Débil");
            tvSeguridad.setTextColor(0xFFFF5252);
            pbSeguridad.getProgressDrawable().setColorFilter(android.graphics.Color.parseColor("#FF5252"), android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (nivel <= 50) {
            tvSeguridad.setText("Seguridad: Regular");
            tvSeguridad.setTextColor(0xFFFF9800);
            pbSeguridad.getProgressDrawable().setColorFilter(android.graphics.Color.parseColor("#FF9800"), android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (nivel <= 75) {
            tvSeguridad.setText("Seguridad: Buena");
            tvSeguridad.setTextColor(0xFF8BC34A);
            pbSeguridad.getProgressDrawable().setColorFilter(android.graphics.Color.parseColor("#8BC34A"), android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            tvSeguridad.setText("Seguridad: Fuerte");
            tvSeguridad.setTextColor(0xFF4CAF50);
            pbSeguridad.getProgressDrawable().setColorFilter(android.graphics.Color.parseColor("#4CAF50"), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    private void registrarUsuario() {
        String correo     = etCorreo.getText().toString().trim();
        String contrasena = etContrasena.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        mAuth.getCurrentUser().sendEmailVerification()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        Toast.makeText(RegisterStep2Activity.this,
                                                "¡Registro exitoso! Revisa tu correo para verificar tu cuenta.",
                                                Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(RegisterStep2Activity.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(RegisterStep2Activity.this,
                                                "Error al enviar correo de verificación",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(RegisterStep2Activity.this,
                                "Error al registrar, intenta de nuevo", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private boolean validarCampos() {
        String correo      = etCorreo.getText().toString().trim();
        String contrasena  = etContrasena.getText().toString().trim();
        String contrasenaa = etContrasenaa.getText().toString().trim();

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
        if (!contrasena.equals(contrasenaa)) {
            etContrasenaa.setError("Las contraseñas no coinciden");
            etContrasenaa.requestFocus();
            return false;
        }
        return true;
    }
}