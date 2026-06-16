package com.example.alphadiet;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterStep1Activity extends AppCompatActivity {

    private EditText etNombre;
    private EditText etApellidos;
    private EditText etUsuario;
    private Button btnContinuar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_register_step1);
        initViews();
        setupListeners();
    }

    private void initViews() {
        etNombre    = findViewById(R.id.et_nombre);
        etApellidos = findViewById(R.id.et_apellidos);
        etUsuario   = findViewById(R.id.et_usuario);
        btnContinuar = findViewById(R.id.btn_continuar);
    }

    private void setupListeners() {
        btnContinuar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
                    startActivity(new Intent(RegisterStep1Activity.this, RegisterStep2Activity.class));
                }
            }
        });
    }

    private boolean validarCampos() {
        if (TextUtils.isEmpty(etNombre.getText().toString().trim())) {
            etNombre.setError("Ingresa tu nombre");
            etNombre.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etApellidos.getText().toString().trim())) {
            etApellidos.setError("Ingresa tus apellidos");
            etApellidos.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etUsuario.getText().toString().trim())) {
            etUsuario.setError("Ingresa tu usuario");
            etUsuario.requestFocus();
            return false;
        }
        return true;
    }
}