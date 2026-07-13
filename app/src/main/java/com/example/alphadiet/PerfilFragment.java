package com.example.alphadiet;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PerfilFragment extends Fragment {

    private TextView tvNombre, tvCorreo, tvFavoritos;
    private ImageView ivAvatar;
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_perfil, container, false);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        tvNombre    = view.findViewById(R.id.tv_nombre);
        tvCorreo    = view.findViewById(R.id.tv_correo);
        tvFavoritos = view.findViewById(R.id.tv_favoritos);
        ivAvatar    = view.findViewById(R.id.iv_avatar);

        cargarDatosUsuario();
        cargarContadorFavoritos();

        view.findViewById(R.id.btn_editar_perfil).setOnClickListener(v -> mostrarDialogoEditarNombre());
        view.findViewById(R.id.btn_mis_compras).setOnClickListener(v ->
                Toast.makeText(getContext(), "Próximamente: historial de pedidos", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.btn_notificaciones).setOnClickListener(v ->
                Toast.makeText(getContext(), "Revisa la pestaña de Notificaciones", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.acceso_direccion).setOnClickListener(v ->
                Toast.makeText(getContext(), "Próximamente: gestión de direcciones", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.acceso_pedidos).setOnClickListener(v ->
                Toast.makeText(getContext(), "Próximamente: historial de pedidos", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.acceso_favoritos).setOnClickListener(v -> {
            // Navega el bottom nav a favoritos si tu MainActivity expone el ViewPager;
            // por ahora solo informa
            Toast.makeText(getContext(), "Revisa la pestaña de Favoritos", Toast.LENGTH_SHORT).show();
        });
        view.findViewById(R.id.acceso_avisos).setOnClickListener(v ->
                Toast.makeText(getContext(), "Revisa la pestaña de Notificaciones", Toast.LENGTH_SHORT).show());

        view.findViewById(R.id.btn_cerrar_sesion).setOnClickListener(v -> cerrarSesion());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarContadorFavoritos();
    }

    private void cargarDatosUsuario() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || getContext() == null) return;

        String nombre = user.getDisplayName();
        String correo = user.getEmail();

        tvNombre.setText(nombre != null && !nombre.isEmpty() ? nombre : "Usuario AlphaDiet");
        tvCorreo.setText(correo != null ? correo : "");

        String avatarUrl = obtenerUrlGravatar(correo);
        if (avatarUrl != null) {
            Glide.with(this)
                    .load(avatarUrl)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .placeholder(R.drawable.ic_person_nav)
                    .into(ivAvatar);
        }
    }

    /**
     * Genera la URL de Gravatar a partir del hash MD5 del correo.
     * Si el usuario nunca se registró en gravatar.com, devuelve un patrón
     * geométrico único (identicon) generado a partir de su correo, nunca queda vacío.
     */
    @Nullable
    private String obtenerUrlGravatar(String correo) {
        if (correo == null || correo.isEmpty()) return null;
        try {
            String correoNormalizado = correo.trim().toLowerCase();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(correoNormalizado.getBytes());

            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return "https://www.gravatar.com/avatar/" + hexString + "?d=identicon&s=200";
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private void cargarContadorFavoritos() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || getContext() == null) return;

        db.collection("favoritos")
                .document(user.getUid())
                .collection("items")
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (tvFavoritos != null) {
                        tvFavoritos.setText(String.valueOf(snapshot.size()));
                    }
                });
    }

    private void mostrarDialogoEditarNombre() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null || getContext() == null) return;

        EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(user.getDisplayName());
        input.setHint("Tu nombre");

        new AlertDialog.Builder(getContext())
                .setTitle("Editar perfil")
                .setView(input)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String nuevoNombre = input.getText().toString().trim();
                    if (nuevoNombre.isEmpty()) {
                        Toast.makeText(getContext(), "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                            .setDisplayName(nuevoNombre)
                            .build();

                    user.updateProfile(request)
                            .addOnSuccessListener(unused -> {
                                tvNombre.setText(nuevoNombre);
                                Toast.makeText(getContext(), "Perfil actualizado", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(), "Error al actualizar", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void cerrarSesion() {
        auth.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}