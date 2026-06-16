package com.example.alphadiet;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DetalleProductoActivity extends AppCompatActivity {

    private ImageButton btnVolver;
    private ImageButton btnFavorito;
    private Button btnCarrito;
    private Button btnPagar;
    private boolean esFavorito = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_detalle_producto);
        initViews();
        setupListeners();
    }

    private void initViews() {
        btnVolver   = findViewById(R.id.btn_volver);
        btnFavorito = findViewById(R.id.btn_favorito);
        btnCarrito  = findViewById(R.id.btn_carrito);
        btnPagar    = findViewById(R.id.btn_pagar);
    }

    private void setupListeners() {
        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnFavorito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                esFavorito = !esFavorito;
                if (esFavorito) {
                    btnFavorito.setImageResource(android.R.drawable.btn_star_big_on);
                    Toast.makeText(DetalleProductoActivity.this,
                            "Agregado a favoritos", Toast.LENGTH_SHORT).show();
                } else {
                    btnFavorito.setImageResource(android.R.drawable.btn_star_big_off);
                    Toast.makeText(DetalleProductoActivity.this,
                            "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnCarrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DetalleProductoActivity.this,
                        "Producto agregado al carrito", Toast.LENGTH_SHORT).show();
            }
        });

        btnPagar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(DetalleProductoActivity.this,
                        "Procesando pago...", Toast.LENGTH_SHORT).show();
            }
        });
    }
}