package com.example.alphadiet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InicioFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout layoutProductos;
    private List<Producto> listaProductos = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        db = FirebaseFirestore.getInstance();
        layoutProductos = view.findViewById(R.id.layout_productos);

        cargarProductos();

        return view;
    }

    private void cargarProductos() {
        db.collection("productos")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    listaProductos.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Producto p = doc.toObject(Producto.class);
                        p.setId(doc.getId());
                        listaProductos.add(p);
                    }
                    mostrarPorCategoria();
                })
                .addOnFailureListener(e -> {
                });
    }

    private void mostrarPorCategoria() {
        if (layoutProductos == null || getContext() == null) return;
        layoutProductos.removeAllViews();

        // Agrupar por categoría
        Map<String, List<Producto>> porCategoria = new HashMap<>();
        for (Producto p : listaProductos) {
            String cat = p.getCategoria();
            if (!porCategoria.containsKey(cat)) {
                porCategoria.put(cat, new ArrayList<>());
            }
            porCategoria.get(cat).add(p);
        }

        // Mostrar cada categoría como fila horizontal deslizable
        for (Map.Entry<String, List<Producto>> entry : porCategoria.entrySet()) {

            // Título de categoría
            TextView tvCategoria = new TextView(getContext());
            tvCategoria.setText(entry.getKey());
            tvCategoria.setTextSize(18);
            tvCategoria.setTextColor(0xFFFFFFFF);
            tvCategoria.setTypeface(null, android.graphics.Typeface.BOLD);
            LinearLayout.LayoutParams paramsTitle = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            paramsTitle.setMargins(0, 28, 0, 12);
            tvCategoria.setLayoutParams(paramsTitle);
            layoutProductos.addView(tvCategoria);

            // Scroll horizontal con las cards de esta categoría
            HorizontalScrollView scroll = new HorizontalScrollView(getContext());
            scroll.setHorizontalScrollBarEnabled(false);
            scroll.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));

            LinearLayout filaProductos = new LinearLayout(getContext());
            filaProductos.setOrientation(LinearLayout.HORIZONTAL);

            for (Producto producto : entry.getValue()) {
                View cardView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_producto_grid, filaProductos, false);

                TextView tvNombre = cardView.findViewById(R.id.tv_nombre);
                TextView tvPrecio = cardView.findViewById(R.id.tv_precio);
                ImageView ivImagen = cardView.findViewById(R.id.iv_imagen);

                tvNombre.setText(producto.getNombre());
                tvPrecio.setText("$" + producto.getPrecio());

                Glide.with(getContext())
                        .load(producto.getImagen())
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .into(ivImagen);

                LinearLayout.LayoutParams cardParams =
                        (LinearLayout.LayoutParams) cardView.getLayoutParams();
                cardParams.setMarginEnd(12);
                cardView.setLayoutParams(cardParams);

                final Producto productoFinal = producto;
                cardView.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), DetalleProductoActivity.class);
                    intent.putExtra("producto_id", productoFinal.getId());
                    startActivity(intent);
                });

                filaProductos.addView(cardView);
            }

            scroll.addView(filaProductos);
            layoutProductos.addView(scroll);
        }
    }
}