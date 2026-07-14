package com.example.alphadiet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class NotificacionesFragment extends Fragment {

    private FirebaseFirestore db;
    private LinearLayout layoutNotificaciones;
    private LinearLayout layoutVacio;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notificaciones, container, false);

        db = FirebaseFirestore.getInstance();
        layoutNotificaciones = view.findViewById(R.id.layout_notificaciones);
        layoutVacio = view.findViewById(R.id.layout_vacio);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarNotificaciones();
    }

    private void cargarNotificaciones() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || getContext() == null) return;

        db.collection("notificaciones")
                .document(user.getUid())
                .collection("items")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (layoutNotificaciones == null) return;
                    layoutNotificaciones.removeAllViews();

                    layoutVacio.setVisibility(snapshot.isEmpty() ? View.VISIBLE : View.GONE);

                    SimpleDateFormat formato = new SimpleDateFormat("dd MMM, HH:mm", new Locale("es", "MX"));

                    for (QueryDocumentSnapshot doc : snapshot) {
                        String titulo = doc.getString("titulo");
                        String mensaje = doc.getString("mensaje");
                        Timestamp fecha = doc.getTimestamp("fecha");

                        View itemView = LayoutInflater.from(getContext())
                                .inflate(R.layout.item_notificacion, layoutNotificaciones, false);

                        TextView tvTitulo = itemView.findViewById(R.id.tv_titulo);
                        TextView tvMensaje = itemView.findViewById(R.id.tv_mensaje);
                        TextView tvFecha = itemView.findViewById(R.id.tv_fecha);

                        tvTitulo.setText(titulo != null ? titulo : "");
                        tvMensaje.setText(mensaje != null ? mensaje : "");
                        tvFecha.setText(fecha != null ? formato.format(fecha.toDate()) : "");

                        layoutNotificaciones.addView(itemView);
                    }
                });
    }
}