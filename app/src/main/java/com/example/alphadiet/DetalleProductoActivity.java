package com.example.alphadiet;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class DetalleProductoActivity extends AppCompatActivity {

    private ImageButton btnFavorito;
    private Button btnCarrito;
    private Button btnPagar;
    private boolean esFavorito = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_producto);

        Toolbar toolbar = findViewById(R.id.toolbar_detalle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnFavorito = findViewById(R.id.btn_favorito);
        btnCarrito  = findViewById(R.id.btn_carrito);
        btnPagar    = findViewById(R.id.btn_pagar);
    }

    private void setupListeners() {

        btnFavorito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                esFavorito = !esFavorito;
                if (esFavorito) {
                    btnFavorito.setImageResource(android.R.drawable.btn_star_big_on);
                    Toast.makeText(DetalleProductoActivity.this, "Agregado a favoritos", Toast.LENGTH_SHORT).show();
                } else {
                    btnFavorito.setImageResource(android.R.drawable.btn_star_big_off);
                    Toast.makeText(DetalleProductoActivity.this, "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCarrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DetalleProductoActivity.this, "Producto agregado al carrito", Toast.LENGTH_SHORT).show();
            }
        });

        btnPagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DetalleProductoActivity.this, "Procesando pago...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        if (item.getItemId() == R.id.action_search) {
            Toast.makeText(this, "Buscar...", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}