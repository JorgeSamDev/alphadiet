package com.example.alphadiet;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class AdminActivity extends AppCompatActivity {

    private EditText etNombre;
    private EditText etPrecio;
    private Spinner spinnerCategoria;
    private EditText etDescripcion;
    private EditText etImagen;
    private EditText etCalorias;
    private EditText etProteina;
    private EditText etCarbohidratos;
    private EditText etGrasas;
    private EditText etStock;
    private Button btnGuardar;
    private Button btnVerProductos;
    private FirebaseFirestore db;

    private String productoIdEditando = null;
    private String[] categorias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Toolbar toolbar = new Toolbar(this);
        toolbar.setTitle("Admin");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        categorias = getResources().getStringArray(R.array.categorias_producto);

        initViews();
        setupCategoriaSpinner();
        setupListeners();

        productoIdEditando = getIntent().getStringExtra("producto_id");
        if (productoIdEditando != null) {
            cargarProductoParaEditar();
        }
    }

    private void initViews() {
        etNombre         = findViewById(R.id.et_nombre);
        etPrecio         = findViewById(R.id.et_precio);
        spinnerCategoria = findViewById(R.id.spinner_categoria);
        etDescripcion    = findViewById(R.id.et_descripcion);
        etImagen         = findViewById(R.id.et_imagen);
        etCalorias       = findViewById(R.id.et_calorias);
        etProteina       = findViewById(R.id.et_proteina);
        etCarbohidratos  = findViewById(R.id.et_carbohidratos);
        etGrasas         = findViewById(R.id.et_grasas);
        etStock          = findViewById(R.id.et_stock);
        btnGuardar       = findViewById(R.id.btn_guardar);
        btnVerProductos  = findViewById(R.id.btn_ver_productos);
    }

    private void setupCategoriaSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.categorias_producto,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategoria.setAdapter(adapter);
    }

    private void setupListeners() {
        btnGuardar.setOnClickListener(v -> {
            if (validarCampos()) {
                if (productoIdEditando != null) {
                    actualizarProducto();
                } else {
                    guardarProducto();
                }
            }
        });

        btnVerProductos.setOnClickListener(v ->
                startActivity(new Intent(this, ProductosListaActivity.class))
        );
    }

    private void cargarProductoParaEditar() {
        db.collection("productos")
                .document(productoIdEditando)
                .get()
                .addOnSuccessListener(this::precargarCampos)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar producto", Toast.LENGTH_SHORT).show()
                );
    }

    private void precargarCampos(DocumentSnapshot doc) {
        if (!doc.exists()) return;

        Producto p = doc.toObject(Producto.class);
        if (p == null) return;

        etNombre.setText(p.getNombre());
        etPrecio.setText(String.valueOf(p.getPrecio()));
        etDescripcion.setText(p.getDescripcion());
        etImagen.setText(p.getImagen());
        etCalorias.setText(String.valueOf(p.getCalorias()));
        etProteina.setText(String.valueOf(p.getProteina()));
        etCarbohidratos.setText(String.valueOf(p.getCarbohidratos()));
        etGrasas.setText(String.valueOf(p.getGrasas()));
        etStock.setText(String.valueOf(p.getStock()));

        for (int i = 0; i < categorias.length; i++) {
            if (categorias[i].equals(p.getCategoria())) {
                spinnerCategoria.setSelection(i);
                break;
            }
        }

        btnGuardar.setText("Actualizar Producto");
        btnVerProductos.setVisibility(View.GONE);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Editar producto");
        }
    }

    private void guardarProducto() {
        Map<String, Object> producto = construirMapaProducto();

        db.collection("productos")
                .add(producto)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AdminActivity.this,
                            "Producto guardado exitosamente", Toast.LENGTH_SHORT).show();
                    limpiarCampos();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(AdminActivity.this,
                                "Error al guardar el producto", Toast.LENGTH_SHORT).show()
                );
    }

    private void actualizarProducto() {
        Map<String, Object> producto = construirMapaProducto();

        db.collection("productos")
                .document(productoIdEditando)
                .set(producto)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Producto actualizado", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al actualizar", Toast.LENGTH_SHORT).show()
                );
    }

    private Map<String, Object> construirMapaProducto() {
        String nombre        = etNombre.getText().toString().trim();
        double precio         = Double.parseDouble(etPrecio.getText().toString().trim());
        String categoria     = spinnerCategoria.getSelectedItem().toString();
        String descripcion   = etDescripcion.getText().toString().trim();
        String imagen         = etImagen.getText().toString().trim();

        int calorias         = parseIntOrZero(etCalorias.getText().toString().trim());
        int proteina         = parseIntOrZero(etProteina.getText().toString().trim());
        int carbohidratos    = parseIntOrZero(etCarbohidratos.getText().toString().trim());
        int grasas             = parseIntOrZero(etGrasas.getText().toString().trim());
        int stock             = parseIntOrZero(etStock.getText().toString().trim());

        Map<String, Object> producto = new HashMap<>();
        producto.put("nombre", nombre);
        producto.put("precio", precio);
        producto.put("categoria", categoria);
        producto.put("descripcion", descripcion);
        producto.put("imagen", imagen);
        producto.put("calorias", calorias);
        producto.put("proteina", proteina);
        producto.put("carbohidratos", carbohidratos);
        producto.put("grasas", grasas);
        producto.put("stock", stock);

        return producto;
    }

    private int parseIntOrZero(String valor) {
        if (TextUtils.isEmpty(valor)) return 0;
        try {
            return Integer.parseInt(valor);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void limpiarCampos() {
        etNombre.setText("");
        etPrecio.setText("");
        spinnerCategoria.setSelection(0);
        etDescripcion.setText("");
        etImagen.setText("");
        etCalorias.setText("");
        etProteina.setText("");
        etCarbohidratos.setText("");
        etGrasas.setText("");
        etStock.setText("");
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
        if (spinnerCategoria.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecciona una categoría", Toast.LENGTH_SHORT).show();
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