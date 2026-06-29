package com.example.alphadiet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;

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
                    mostrarProductos();
                })
                .addOnFailureListener(e -> {
                    // Error al cargar
                });
    }

    private void mostrarProductos() {
        if (layoutProductos == null || getContext() == null) return;
        layoutProductos.removeAllViews();

        for (Producto producto : listaProductos) {
            View cardView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_producto, layoutProductos, false);

            TextView tvNombre = cardView.findViewById(R.id.tv_nombre);
            TextView tvPrecio = cardView.findViewById(R.id.tv_precio);
            TextView tvCategoria = cardView.findViewById(R.id.tv_categoria);
            ImageView ivImagen = cardView.findViewById(R.id.iv_imagen);

            tvNombre.setText(producto.getNombre());
            tvPrecio.setText("$" + producto.getPrecio());
            tvCategoria.setText(producto.getCategoria());

            Glide.with(getContext())
                    .load(producto.getImagen())
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .into(ivImagen);

            layoutProductos.addView(cardView);
        }
    }
}