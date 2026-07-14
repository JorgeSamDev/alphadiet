package com.example.alphadiet;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.gms.tasks.Task;

public class WelcomeActivity extends AppCompatActivity {

    private Button btnYaTengoCuenta;
    private Button btnRegistrarme;
    private ImageButton btnGoogle;
    private ImageButton btnInstagram;
    private ImageButton btnFacebook;
    private TextView tvTerminos;
    private TextView tvPrivacidad;

    private GoogleSignInClient googleSignInClient;
    private FirebaseAuth firebaseAuth;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_welcome);

        firebaseAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Intent data = result.getData();
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                    manejarResultadoGoogle(task);
                }
        );

        initViews();
        setupListeners();
    }

    private void initViews() {
        btnYaTengoCuenta = findViewById(R.id.btn_ya_tengo_cuenta);
        btnRegistrarme   = findViewById(R.id.btn_registrarme);
        btnGoogle        = findViewById(R.id.btn_google);
        btnInstagram     = findViewById(R.id.btn_instagram);
        btnFacebook      = findViewById(R.id.btn_facebook);
        tvTerminos       = findViewById(R.id.tv_terminos);
        tvPrivacidad     = findViewById(R.id.tv_privacidad);
    }

    private void setupListeners() {
        btnYaTengoCuenta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            }
        });

        btnRegistrarme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(WelcomeActivity.this, RegisterStep1Activity.class));
            }
        });

        btnGoogle.setOnClickListener(v -> iniciarSesionConGoogle());

        btnInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Instagram Login
            }
        });

        btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Facebook Login
            }
        });

        tvTerminos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: abrir Términos de Uso
            }
        });

        tvPrivacidad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: abrir Política de Privacidad
            }
        });
    }

    private void iniciarSesionConGoogle() {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        googleSignInLauncher.launch(signInIntent);
    }

    private void manejarResultadoGoogle(Task<GoogleSignInAccount> task) {
        try {
            GoogleSignInAccount account = task.getResult(ApiException.class);
            autenticarConFirebase(account);
        } catch (ApiException e) {
            Toast.makeText(this, "No se pudo iniciar sesión con Google", Toast.LENGTH_SHORT).show();
        }
    }

    private void autenticarConFirebase(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        firebaseAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    Toast.makeText(this, "¡Bienvenido!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error al iniciar sesión", Toast.LENGTH_SHORT).show()
                );
    }
}