package com.example.alphadiet;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetalleProductoActivity extends AppCompatActivity {

    private ImageButton btnFavorito, btnBack, btnMenos, btnMas;
    private Button btnCarrito, btnPagar;
    private ImageView imgProducto;
    private TextView tvNombreProducto, tvPrecio, tvCategoria, tvDescripcion, tvCantidad;
    private TextView tvCalorias, tvCarbohidratos, tvProteina, tvGrasas;
    private boolean esFavorito = false;
    private int cantidad = 1;

    private FirebaseFirestore db;
    private String productoId;
    private Producto productoActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_producto);

        db = FirebaseFirestore.getInstance();

        initViews();
        setupListeners();

        productoId = getIntent().getStringExtra("producto_id");
        if (productoId == null) {
            Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarProducto();
    }

    private void initViews() {
        btnFavorito      = findViewById(R.id.btn_favorito);
        btnBack          = findViewById(R.id.btn_back);
        btnCarrito       = findViewById(R.id.btn_carrito);
        btnPagar         = findViewById(R.id.btn_pagar);
        btnMenos         = findViewById(R.id.btn_menos);
        btnMas           = findViewById(R.id.btn_mas);
        imgProducto      = findViewById(R.id.img_producto);
        tvNombreProducto = findViewById(R.id.tv_nombre_producto);
        tvPrecio         = findViewById(R.id.tv_precio);
        tvCategoria      = findViewById(R.id.tv_categoria);
        tvDescripcion    = findViewById(R.id.tv_descripcion);
        tvCantidad       = findViewById(R.id.tv_cantidad);
        tvCalorias       = findViewById(R.id.tv_calorias);
        tvCarbohidratos  = findViewById(R.id.tv_carbohidratos);
        tvProteina       = findViewById(R.id.tv_proteina);
        tvGrasas         = findViewById(R.id.tv_grasas);
    }

    private void cargarProducto() {
        db.collection("productos")
                .document(productoId)
                .get()
                .addOnSuccessListener(this::mostrarProducto)
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar producto", Toast.LENGTH_SHORT).show()
                );
    }

    private void mostrarProducto(DocumentSnapshot doc) {
        if (!doc.exists()) {
            Toast.makeText(this, "Este producto ya no existe", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        productoActual = doc.toObject(Producto.class);
        if (productoActual == null) return;
        productoActual.setId(doc.getId());

        tvNombreProducto.setText(productoActual.getNombre());
        tvCategoria.setText(productoActual.getCategoria());
        tvDescripcion.setText(productoActual.getDescripcion());
        actualizarPrecio();

        tvCalorias.setText(String.valueOf(productoActual.getCalorias()));
        tvCarbohidratos.setText(productoActual.getCarbohidratos() + "g");
        tvProteina.setText(productoActual.getProteina() + "g");
        tvGrasas.setText(productoActual.getGrasas() + "g");

        Glide.with(this)
                .load(productoActual.getImagen())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(imgProducto);

        verificarSiEsFavorito();
    }
    private void actualizarPrecio() {
        if (productoActual == null) return;
        double total = productoActual.getPrecio() * cantidad;
        tvPrecio.setText(String.format("$%.2f", total));
    }

    private void setupListeners() {

        btnBack.setOnClickListener(v -> finish());

        btnFavorito.setOnClickListener(v -> toggleFavorito());

        btnMenos.setOnClickListener(v -> {
            if (cantidad > 1) {
                cantidad--;
                tvCantidad.setText(String.valueOf(cantidad));
                actualizarPrecio();
            }
        });

        btnMas.setOnClickListener(v -> {
            cantidad++;
            tvCantidad.setText(String.valueOf(cantidad));
            actualizarPrecio();
        });

        btnCarrito.setOnClickListener(v -> agregarAlCarrito());

        btnPagar.setOnClickListener(v ->
                Toast.makeText(this, "Procesando pago...", Toast.LENGTH_SHORT).show()
        );
    }

    private void agregarAlCarrito() {
        if (productoActual == null) {
            Toast.makeText(this, "Espera a que cargue el producto", Toast.LENGTH_SHORT).show();
            return;
        }

        com.google.firebase.auth.FirebaseUser user =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        java.util.Map<String, Object> item = new java.util.HashMap<>();
        item.put("nombre", productoActual.getNombre());
        item.put("precio", productoActual.getPrecio());
        item.put("cantidad", cantidad);
        item.put("imagen", productoActual.getImagen());
        item.put("usuarioId", user.getUid());

        db.collection("carritos")
                .document(user.getUid())
                .collection("items")
                .add(item)
                .addOnSuccessListener(ref ->
                        Toast.makeText(this, "Agregado al carrito", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al agregar", Toast.LENGTH_SHORT).show()
                );
    }

private void verificarSiEsFavorito() {
    com.google.firebase.auth.FirebaseUser user =
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
    if (user == null || productoId == null) return;

    db.collection("favoritos")
            .document(user.getUid())
            .collection("items")
            .document(productoId)
            .get()
            .addOnSuccessListener(doc -> {
                esFavorito = doc.exists();
                btnFavorito.setImageResource(esFavorito
                        ? android.R.drawable.btn_star_big_on
                        : R.drawable.ic_favorite_nav);
            });
}

private void toggleFavorito() {
    com.google.firebase.auth.FirebaseUser user =
            com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();

    if (user == null) {
        Toast.makeText(this, "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
        return;
    }
    if (productoActual == null) {
        Toast.makeText(this, "Espera a que cargue el producto", Toast.LENGTH_SHORT).show();
        return;
    }

    com.google.firebase.firestore.DocumentReference favRef = db.collection("favoritos")
            .document(user.getUid())
            .collection("items")
            .document(productoId);

    if (esFavorito) {
        favRef.delete().addOnSuccessListener(unused -> {
            esFavorito = false;
            btnFavorito.setImageResource(R.drawable.ic_favorite_nav);
            Toast.makeText(this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
        });
    } else {
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("nombre", productoActual.getNombre());
        data.put("categoria", productoActual.getCategoria());
        data.put("precio", productoActual.getPrecio());
        data.put("imagen", productoActual.getImagen());

        favRef.set(data).addOnSuccessListener(unused -> {
            esFavorito = true;
            btnFavorito.setImageResource(android.R.drawable.btn_star_big_on);
            Toast.makeText(this, "Agregado a favoritos", Toast.LENGTH_SHORT).show();
        });
    }
}
}