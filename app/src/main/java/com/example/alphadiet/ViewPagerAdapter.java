package com.example.alphadiet;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new InicioFragment();
            case 1: return new FavoritosFragment();
            case 2: return new CarritoFragment();
            case 3: return new NotificacionesFragment();
            case 4: return new PerfilFragment();
            default: return new InicioFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}