package com.example.alphadiet;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private EditText etNombre;
    private EditText etPrecio;
    private EditText etCategoria;
    private EditText etDescripcion;
    private EditText etImagen;
    private Button btnGuardar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Toolbar
        Toolbar toolbar = new Toolbar(this);
        toolbar.setTitle("Admin");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        initViews();
        setupListeners();
    }

    private void initViews() {
        etNombre      = findViewById(R.id.et_nombre);
        etPrecio      = findViewById(R.id.et_precio);
        etCategoria   = findViewById(R.id.et_categoria);
        etDescripcion = findViewById(R.id.et_descripcion);
        etImagen      = findViewById(R.id.et_imagen);
        btnGuardar    = findViewById(R.id.btn_guardar);
    }

    private void setupListeners() {
        btnGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validarCampos()) {
                    guardarProducto();
                }
            }
        });
    }

    private void guardarProducto() {
        String nombre      = etNombre.getText().toString().trim();
        double precio      = Double.parseDouble(etPrecio.getText().toString().trim());
        String categoria   = etCategoria.getText().toString().trim();
        String descripcion = etDescripcion.getText().toString().trim();
        String imagen      = etImagen.getText().toString().trim();

        Map<String, Object> producto = new HashMap<>();
        producto.put("nombre", nombre);
        producto.put("precio", precio);
        producto.put("categoria", categoria);
        producto.put("descripcion", descripcion);
        producto.put("imagen", imagen);

        db.collection("productos")
                .add(producto)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AdminActivity.this,
                            "Producto guardado exitosamente", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AdminActivity.this,
                            "Error al guardar el producto", Toast.LENGTH_SHORT).show();
                });
    }

    private void limpiarCampos() {
        etNombre.setText("");
        etPrecio.setText("");
        etCategoria.setText("");
        etDescripcion.setText("");
        etImagen.setText("");
    }

    private boolean validarCampos() {
        if (TextUtils.isEmpty(etNombre.getText().toString().trim())) {
            etNombre.setError("Ingresa el nombre");
            etNombre.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etPrecio.getText().toString().trim())) {
            etPrecio.setError("Ingresa el precio");
            etPrecio.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etCategoria.getText().toString().trim())) {
            etCategoria.setError("Ingresa la categoría");
            etCategoria.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etDescripcion.getText().toString().trim())) {
            etDescripcion.setError("Ingresa la descripción");
            etDescripcion.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(etImagen.getText().toString().trim())) {
            etImagen.setError("Ingresa la URL de la imagen");
            etImagen.requestFocus();
            return false;
        }
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}