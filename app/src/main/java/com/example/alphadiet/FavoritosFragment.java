package com.example.alphadiet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class FavoritosFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout layoutFavoritos;
    private LinearLayout layoutVacio;
    private TextView tvContador;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favoritos, container, false);

        db = FirebaseFirestore.getInstance();
        layoutFavoritos = view.findViewById(R.id.layout_favoritos);
        layoutVacio = view.findViewById(R.id.layout_vacio);
        tvContador = view.findViewById(R.id.tv_contador);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recarga cada vez que el usuario vuelve a esta pestaña,
        // así refleja favoritos agregados/quitados desde el detalle del producto
        cargarFavoritos();
    }

    private void cargarFavoritos() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || getContext() == null) return;

        db.collection("favoritos")
                .document(user.getUid())
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (layoutFavoritos == null) return;
                    layoutFavoritos.removeAllViews();

                    int cantidad = queryDocumentSnapshots.size();
                    tvContador.setText(cantidad + (cantidad == 1 ? " producto" : " productos"));
                    layoutVacio.setVisibility(cantidad == 0 ? View.VISIBLE : View.GONE);

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String productoId = doc.getId();
                        String nombre     = doc.getString("nombre");
                        String categoria  = doc.getString("categoria");
                        String imagen     = doc.getString("imagen");
                        Double precio     = doc.getDouble("precio");

                        View itemView = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_favorito, layoutFavoritos, false);

                        TextView tvNombre = itemView.findViewById(R.id.tv_nombre);
                        TextView tvInfo   = itemView.findViewById(R.id.tv_info);
                        TextView tvPrecio = itemView.findViewById(R.id.tv_precio);
                        ImageView ivImagen = itemView.findViewById(R.id.iv_imagen);
                        ImageButton btnCompartir = itemView.findViewById(R.id.btn_compartir);
                        ImageButton btnFavorito  = itemView.findViewById(R.id.btn_favorito);

                        tvNombre.setText(nombre != null ? nombre : "");
                        tvInfo.setText(categoria != null ? categoria : "");
                        tvPrecio.setText(String.format("$%.2f", precio != null ? precio : 0.0));

                        if (imagen != null && !imagen.isEmpty()) {
                            Glide.with(getContext())
                                    .load(imagen)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .into(ivImagen);
                        }

                        btnCompartir.setOnClickListener(v -> {
                            Intent shareIntent = new Intent(Intent.ACTION_SEND);
                            shareIntent.setType("text/plain");
                            shareIntent.putExtra(Intent.EXTRA_TEXT,
                                    "Mira este producto en AlphaDiet: " + nombre);
                            startActivity(Intent.createChooser(shareIntent, "Compartir producto"));
                        });

                        btnFavorito.setOnClickListener(v -> {
                            db.collection("favoritos")
                                    .document(user.getUid())
                                    .collection("items")
                                    .document(productoId)
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(getContext(),
                                                "Eliminado de favoritos", Toast.LENGTH_SHORT).show();
                                        cargarFavoritos();
                                    });
                        });

                        itemView.setOnClickListener(v -> {
                            Intent intent = new Intent(getActivity(), DetalleProductoActivity.class);
                            intent.putExtra("producto_id", productoId);
                            startActivity(intent);
                        });

                        layoutFavoritos.addView(itemView);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al cargar favoritos", Toast.LENGTH_SHORT).show()
                );
    }
}