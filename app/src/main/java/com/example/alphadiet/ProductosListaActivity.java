package com.example.alphadiet;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ProductosListaActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private LinearLayout layoutLista;
    private LinearLayout layoutChips;
    private List<Producto> todosLosProductos = new ArrayList<>();
    private String categoriaSeleccionada = "Todos";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_productos_lista);

        Toolbar toolbar = findViewById(R.id.toolbar_lista);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        layoutLista = findViewById(R.id.layout_lista);
        layoutChips = findViewById(R.id.layout_chips);

        construirChips();
        cargarProductos();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarProductos();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void construirChips() {
        String[] categorias = getResources().getStringArray(R.array.categorias_producto);
        layoutChips.removeAllViews();

        agregarChip("Todos");
        for (String categoria : categorias) {
            if (categoria.equals("Selecciona una categoría")) continue;
            agregarChip(categoria);
        }
    }

    private void agregarChip(String nombre) {
        TextView chip = new TextView(this);
        chip.setText(nombre);
        chip.setTextSize(13);
        chip.setPadding(36, 20, 36, 20);
        chip.setTextColor(nombre.equals(categoriaSeleccionada) ? 0xFFFFFFFF : 0xFF8A93B8);
        chip.setBackgroundResource(nombre.equals(categoriaSeleccionada)
                ? R.drawable.bg_chip_selected
                : R.drawable.bg_chip_unselected);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMarginEnd(10);
        chip.setLayoutParams(params);

        chip.setOnClickListener(v -> {
            categoriaSeleccionada = nombre;
            construirChips();
            mostrarProductosFiltrados();
        });

        layoutChips.addView(chip);
    }

    private void cargarProductos() {
        db.collection("productos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    todosLosProductos.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Producto p = doc.toObject(Producto.class);
                        p.setId(doc.getId());
                        todosLosProductos.add(p);
                    }
                    mostrarProductosFiltrados();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al cargar productos", Toast.LENGTH_SHORT).show()
                );
    }

    private void mostrarProductosFiltrados() {
        layoutLista.removeAllViews();

        for (Producto producto : todosLosProductos) {
            if (!categoriaSeleccionada.equals("Todos")
                    && !categoriaSeleccionada.equals(producto.getCategoria())) {
                continue;
            }

            View itemView = LayoutInflater.from(this)
                    .inflate(R.layout.item_producto_admin, layoutLista, false);

            TextView tvNombre = itemView.findViewById(R.id.tv_nombre);
            TextView tvCategoria = itemView.findViewById(R.id.tv_categoria);
            TextView tvPrecio = itemView.findViewById(R.id.tv_precio);
            ImageView ivImagen = itemView.findViewById(R.id.iv_imagen);
            ImageButton btnEditar = itemView.findViewById(R.id.btn_editar);
            ImageButton btnEliminar = itemView.findViewById(R.id.btn_eliminar);

            tvNombre.setText(producto.getNombre());
            tvCategoria.setText(producto.getCategoria());
            tvPrecio.setText(String.format("$%.2f", producto.getPrecio()));

            if (producto.getImagen() != null && !producto.getImagen().isEmpty()) {
                Glide.with(this)
                        .load(producto.getImagen())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(ivImagen);
            }

            btnEditar.setOnClickListener(v -> {
                Intent intent = new Intent(this, AdminActivity.class);
                intent.putExtra("producto_id", producto.getId());
                startActivity(intent);
            });

            btnEliminar.setOnClickListener(v -> confirmarEliminar(producto));

            layoutLista.addView(itemView);
        }

        if (layoutLista.getChildCount() == 0) {
            TextView vacio = new TextView(this);
            vacio.setText("No hay productos en esta categoría");
            vacio.setTextColor(0xFF8A93B8);
            vacio.setPadding(0, 40, 0, 0);
            vacio.setGravity(android.view.Gravity.CENTER);
            layoutLista.addView(vacio);
        }
    }

    private void confirmarEliminar(Producto producto) {
        new AlertDialog.Builder(this)
                .setTitle("Eliminar producto")
                .setMessage("¿Seguro que quieres eliminar \"" + producto.getNombre() + "\"? Esta acción no se puede deshacer.")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarProducto(producto))
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void eliminarProducto(Producto producto) {
        db.collection("productos")
                .document(producto.getId())
                .delete()
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Producto eliminado", Toast.LENGTH_SHORT).show();
                    cargarProductos();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al eliminar", Toast.LENGTH_SHORT).show()
                );
    }
}