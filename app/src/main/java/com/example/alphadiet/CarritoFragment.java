package com.example.alphadiet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class CarritoFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout layoutCarrito;
    private TextView tvTotal;
    private Button btnPagar;
    private double total = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_carrito, container, false);

        db = FirebaseFirestore.getInstance();
        layoutCarrito = view.findViewById(R.id.layout_carrito);
        tvTotal = view.findViewById(R.id.tv_total);
        btnPagar = view.findViewById(R.id.btn_pagar);

        cargarCarrito();

        btnPagar.setOnClickListener(v ->
                Toast.makeText(getContext(), "Procesando pago...", Toast.LENGTH_SHORT).show()
        );

        return view;
    }

    private void cargarCarrito() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("carritos")
                .document(user.getUid())
                .collection("items")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    layoutCarrito.removeAllViews();
                    total = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String nombre  = doc.getString("nombre");
                        double precio  = doc.getDouble("precio");
                        long cantidadLong = doc.getLong("cantidad");
                        String imagen  = doc.getString("imagen");
                        String itemId  = doc.getId();

                        total += precio * cantidadLong;

                        View itemView = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_carrito, layoutCarrito, false);

                        TextView tvNombre   = itemView.findViewById(R.id.tv_nombre);
                        TextView tvPrecio   = itemView.findViewById(R.id.tv_precio);
                        TextView tvCantidad = itemView.findViewById(R.id.tv_cantidad);
                        TextView btnMenos   = itemView.findViewById(R.id.btn_menos);
                        TextView btnMas     = itemView.findViewById(R.id.btn_mas);
                        ImageView ivImagen  = itemView.findViewById(R.id.iv_imagen);
                        ImageButton btnEliminar = itemView.findViewById(R.id.btn_eliminar);

                        tvNombre.setText(nombre);
                        tvPrecio.setText(String.format("$%.2f", precio));
                        tvCantidad.setText(String.valueOf(cantidadLong));

                        if (imagen != null && !imagen.isEmpty()) {
                            Glide.with(getContext())
                                    .load(imagen)
                                    .placeholder(android.R.drawable.ic_menu_gallery)
                                    .into(ivImagen);
                        }

                        // Contenedor mutable para poder modificar la cantidad desde los listeners
                        final long[] cantidadActual = { cantidadLong };
                        final double precioUnitario = precio;

                        btnMenos.setOnClickListener(v -> {
                            if (cantidadActual[0] <= 1) {
                                Toast.makeText(getContext(),
                                        "La cantidad mínima es 1", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            cantidadActual[0]--;
                            actualizarCantidadEnFirestore(user.getUid(), itemId, cantidadActual[0]);
                            tvCantidad.setText(String.valueOf(cantidadActual[0]));
                            recalcularTotalLocal();
                        });

                        btnMas.setOnClickListener(v -> {
                            cantidadActual[0]++;
                            actualizarCantidadEnFirestore(user.getUid(), itemId, cantidadActual[0]);
                            tvCantidad.setText(String.valueOf(cantidadActual[0]));
                            recalcularTotalLocal();
                        });

                        btnEliminar.setOnClickListener(v -> {
                            db.collection("carritos")
                                    .document(user.getUid())
                                    .collection("items")
                                    .document(itemId)
                                    .delete()
                                    .addOnSuccessListener(unused -> {
                                        Toast.makeText(getContext(),
                                                "Producto eliminado", Toast.LENGTH_SHORT).show();
                                        cargarCarrito();
                                    });
                        });

                        layoutCarrito.addView(itemView);
                    }

                    tvTotal.setText(String.format("$%.2f", total));
                });
    }

    private void actualizarCantidadEnFirestore(String uid, String itemId, long nuevaCantidad) {
        db.collection("carritos")
                .document(uid)
                .collection("items")
                .document(itemId)
                .update("cantidad", nuevaCantidad)
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al actualizar cantidad", Toast.LENGTH_SHORT).show()
                );
    }

    private void recalcularTotalLocal() {
        // Recalcula sumando lo que hay actualmente en pantalla, sin re-consultar Firestore
        total = 0;
        for (int i = 0; i < layoutCarrito.getChildCount(); i++) {
            View itemView = layoutCarrito.getChildAt(i);
            TextView tvPrecio = itemView.findViewById(R.id.tv_precio);
            TextView tvCantidad = itemView.findViewById(R.id.tv_cantidad);
            if (tvPrecio == null || tvCantidad == null) continue;

            String precioTexto = tvPrecio.getText().toString().replace("$", "");
            double precio = Double.parseDouble(precioTexto);
            int cantidad = Integer.parseInt(tvCantidad.getText().toString());
            total += precio * cantidad;
        }
        tvTotal.setText(String.format("$%.2f", total));
    }
}