package com.example.alphadiet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class InicioFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        // Conectar cards con DetalleProductoActivity
        View card1 = view.findViewById(R.id.card_producto_1);
        View card2 = view.findViewById(R.id.card_producto_2);
        View card3 = view.findViewById(R.id.card_producto_3);

        View.OnClickListener irADetalle = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), DetalleProductoActivity.class);
                startActivity(intent);
            }
        };

        if (card1 != null) card1.setOnClickListener(irADetalle);
        if (card2 != null) card2.setOnClickListener(irADetalle);
        if (card3 != null) card3.setOnClickListener(irADetalle);

        return view;
    }
}