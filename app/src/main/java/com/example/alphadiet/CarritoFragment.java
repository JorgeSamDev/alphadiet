package com.example.alphadiet;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class CarritoFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout layoutCarrito;
    private TextView tvTotal;
    private Button btnPagar;
    private double total = 0;

    private List<ItemCarrito> carritoActual = new ArrayList<>();

    private static class ItemCarrito {
        String itemId;
        String productoId;
        String nombre;
        double precio;
        long cantidad;
    }

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

        btnPagar.setOnClickListener(v -> iniciarFlujoPago());

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
                    carritoActual.clear();
                    total = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String nombre  = doc.getString("nombre");
                        double precio  = doc.getDouble("precio");
                        long cantidadLong = doc.getLong("cantidad");
                        String imagen  = doc.getString("imagen");
                        String productoId = doc.getString("productoId");
                        String itemId  = doc.getId();

                        total += precio * cantidadLong;

                        ItemCarrito item = new ItemCarrito();
                        item.itemId = itemId;
                        item.productoId = productoId;
                        item.nombre = nombre;
                        item.precio = precio;
                        item.cantidad = cantidadLong;
                        carritoActual.add(item);

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

                        final long[] cantidadActual = { cantidadLong };

                        btnMenos.setOnClickListener(v -> {
                            if (cantidadActual[0] <= 1) {
                                Toast.makeText(getContext(),
                                        "La cantidad mínima es 1", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            cantidadActual[0]--;
                            actualizarCantidadEnFirestore(user.getUid(), itemId, cantidadActual[0]);
                            tvCantidad.setText(String.valueOf(cantidadActual[0]));
                            item.cantidad = cantidadActual[0];
                            recalcularTotalLocal();
                        });

                        btnMas.setOnClickListener(v -> {
                            cantidadActual[0]++;
                            actualizarCantidadEnFirestore(user.getUid(), itemId, cantidadActual[0]);
                            tvCantidad.setText(String.valueOf(cantidadActual[0]));
                            item.cantidad = cantidadActual[0];
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

    // ---------- FLUJO DE PAGO ----------

    private void iniciarFlujoPago() {
        if (carritoActual.isEmpty()) {
            Toast.makeText(getContext(), "Tu carrito está vacío", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(), "Debes iniciar sesión", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("tarjetas")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        procesarPago(user.getUid());
                    } else {
                        mostrarDialogoTarjeta(user.getUid());
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al verificar tarjeta", Toast.LENGTH_SHORT).show()
                );
    }

    private void mostrarDialogoTarjeta(String uid) {
        if (getContext() == null) return;

        View dialogView = LayoutInflater.from(getContext())
                .inflate(R.layout.dialog_tarjeta, null);

        LinearLayout layoutFormulario = dialogView.findViewById(R.id.layout_formulario_tarjeta);
        LinearLayout layoutVerificando = dialogView.findViewById(R.id.layout_verificando);
        EditText etNumero = dialogView.findViewById(R.id.et_numero_tarjeta);
        EditText etVencimiento = dialogView.findViewById(R.id.et_vencimiento);
        EditText etCvv = dialogView.findViewById(R.id.et_cvv);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("Agregar tarjeta")
                .setView(dialogView)
                .setPositiveButton("Guardar y pagar", null)
                .setNegativeButton("Cancelar", null)
                .setCancelable(false)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            Button btnGuardar = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            btnGuardar.setOnClickListener(v -> {
                String numero = etNumero.getText().toString().trim();
                String vencimiento = etVencimiento.getText().toString().trim();
                String cvv = etCvv.getText().toString().trim();

                if (numero.length() < 12 || vencimiento.isEmpty() || cvv.length() < 3) {
                    Toast.makeText(getContext(), "Completa los datos de la tarjeta", Toast.LENGTH_SHORT).show();
                    return;
                }

                layoutFormulario.setVisibility(View.GONE);
                layoutVerificando.setVisibility(View.VISIBLE);
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);

                String ultimosDigitos = numero.substring(numero.length() - 4);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    guardarTarjeta(uid, ultimosDigitos, () -> {
                        dialog.dismiss();
                        procesarPago(uid);
                    });
                }, 2000);
            });
        });

        dialog.show();
    }

    private void guardarTarjeta(String uid, String ultimosDigitos, Runnable alTerminar) {
        java.util.Map<String, Object> tarjeta = new java.util.HashMap<>();
        tarjeta.put("ultimosDigitos", ultimosDigitos);
        tarjeta.put("fechaAgregada", FieldValue.serverTimestamp());

        db.collection("tarjetas")
                .document(uid)
                .set(tarjeta)
                .addOnSuccessListener(unused -> alTerminar.run())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error al guardar tarjeta", Toast.LENGTH_SHORT).show()
                );
    }

    private void procesarPago(String uid) {
        btnPagar.setEnabled(false);
        btnPagar.setText("Procesando...");

        WriteBatch batch = db.batch();

        // 1. Descontar stock de cada producto (si tiene productoId válido)
        for (ItemCarrito item : carritoActual) {
            if (item.productoId != null && !item.productoId.isEmpty()) {
                DocumentReference productoRef = db.collection("productos").document(item.productoId);
                batch.update(productoRef, "stock", FieldValue.increment(-item.cantidad));
            }
        }

        // 2. Crear el pedido
        List<java.util.Map<String, Object>> productosComprados = new ArrayList<>();
        for (ItemCarrito item : carritoActual) {
            java.util.Map<String, Object> p = new java.util.HashMap<>();
            p.put("nombre", item.nombre);
            p.put("precio", item.precio);
            p.put("cantidad", item.cantidad);
            productosComprados.add(p);
        }

        java.util.Map<String, Object> pedido = new java.util.HashMap<>();
        pedido.put("productos", productosComprados);
        pedido.put("total", total);
        pedido.put("fecha", FieldValue.serverTimestamp());

        DocumentReference pedidoRef = db.collection("pedidos")
                .document(uid)
                .collection("items")
                .document();
        batch.set(pedidoRef, pedido);

        // 3. Crear la notificación
        java.util.Map<String, Object> notificacion = new java.util.HashMap<>();
        notificacion.put("titulo", "Pedido confirmado");
        notificacion.put("mensaje", String.format("Tu pedido por $%.2f fue confirmado con éxito.", total));
        notificacion.put("fecha", FieldValue.serverTimestamp());
        notificacion.put("tipo", "pedido");

        DocumentReference notifRef = db.collection("notificaciones")
                .document(uid)
                .collection("items")
                .document();
        batch.set(notifRef, notificacion);

        // 4. Vaciar el carrito
        for (ItemCarrito item : carritoActual) {
            DocumentReference itemRef = db.collection("carritos")
                    .document(uid)
                    .collection("items")
                    .document(item.itemId);
            batch.delete(itemRef);
        }

        batch.commit()
                .addOnSuccessListener(unused -> {
                    btnPagar.setEnabled(true);
                    btnPagar.setText("Proceder al pago");
                    mostrarExito();
                    cargarCarrito();
                })
                .addOnFailureListener(e -> {
                    btnPagar.setEnabled(true);
                    btnPagar.setText("Proceder al pago");
                    Toast.makeText(getContext(), "Error al procesar el pago", Toast.LENGTH_SHORT).show();
                });
    }

    private void mostrarExito() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("¡Pago realizado con éxito!")
                .setMessage("Tu pedido fue confirmado. Puedes ver el aviso en la pestaña de Notificaciones.")
                .setPositiveButton("Entendido", null)
                .show();
    }
}