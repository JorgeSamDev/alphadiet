package com.example.alphadiet;

import android.content.Intent;
import androidx.annotation.Nullable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager2.widget.ViewPager2;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        setupBottomNav();
        cargarDatosDrawer();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        getWindow().setStatusBarColor(getResources().getColor(R.color.navy_dark));
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Por si el nombre se editó en PerfilFragment, refresca al volver
        cargarDatosDrawer();
    }

    private void init() {
        drawerLayout   = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar        = findViewById(R.id.toolbar);
        viewPager      = findViewById(R.id.view_pager);
        bottomNav      = findViewById(R.id.bottom_nav);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        // Mostrar Admin solo si es el correo admin
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null && user.getEmail() != null
                && user.getEmail().equals("jorgesam2302@gmail.com")) {
            navigationView.getMenu().findItem(R.id.nav_admin).setVisible(true);
        } else {
            navigationView.getMenu().findItem(R.id.nav_admin).setVisible(false);
        }
    }

    private void cargarDatosDrawer() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || navigationView == null) return;

        View headerView = navigationView.getHeaderView(0);
        if (headerView == null) return;

        TextView tvNombre = headerView.findViewById(R.id.tv_nombre_drawer);
        ImageView ivAvatar = headerView.findViewById(R.id.iv_avatar_drawer);

        String nombre = user.getDisplayName();
        tvNombre.setText(nombre != null && !nombre.isEmpty() ? nombre : "Usuario AlphaDiet");

        String avatarUrl = obtenerUrlGravatar(user.getEmail());
        if (avatarUrl != null) {
            Glide.with(this)
                    .load(avatarUrl)
                    .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                    .placeholder(R.drawable.ic_person_nav)
                    .into(ivAvatar);
        }
    }

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

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_inicio) {
                viewPager.setCurrentItem(0);
            } else if (itemId == R.id.nav_favoritos) {
                viewPager.setCurrentItem(1);
            } else if (itemId == R.id.nav_carrito) {
                viewPager.setCurrentItem(2);
            } else if (itemId == R.id.nav_notificaciones) {
                viewPager.setCurrentItem(3);
            } else if (itemId == R.id.nav_perfil) {
                viewPager.setCurrentItem(4);
            }
            return true;
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int itemId = menuItem.getItemId();

        if (itemId == R.id.nav_inicio) {
            viewPager.setCurrentItem(0);
            bottomNav.setSelectedItemId(R.id.nav_inicio);
        } else if (itemId == R.id.nav_perfil) {
            viewPager.setCurrentItem(4);
            bottomNav.setSelectedItemId(R.id.nav_perfil);
        } else if (itemId == R.id.nav_configuracion) {
            // TODO: no existe ConfiguracionFragment en el ViewPager todavía
            android.widget.Toast.makeText(this, "Próximamente", android.widget.Toast.LENGTH_SHORT).show();
        } else if (itemId == R.id.nav_nosotros) {
            // TODO: abrir fragment nosotros
        } else if (itemId == R.id.nav_compartir) {
            compartirApp();
        } else if (itemId == R.id.nav_sitio_web) {
            abrirSitioWeb();
        } else if (itemId == R.id.nav_admin) {
            startActivity(new Intent(MainActivity.this, AdminActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_search) {
            android.widget.Toast.makeText(this, "Buscar...", android.widget.Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void compartirApp() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, "¡Descarga AlphaDiet!");
        startActivity(Intent.createChooser(intent, "Compartir con:"));
    }

    private void abrirSitioWeb() {
        Uri uri = Uri.parse("https://www.tusitio.com");
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        startActivity(intent);
    }
}