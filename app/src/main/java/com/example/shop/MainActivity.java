package com.example.shop;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String> {

    EditText userNameET;
    EditText passwordET;
    EditText emailET;
    private static final String LOG_TAG = MainActivity.class.getName();
    private static final String PREF_KEY = Objects.requireNonNull(MainActivity.class.getPackage()).toString();
    private static final int RC_SIGN_IN = 123;

    private static final int SECRET_KEY = 17;
    private SharedPreferences preferences;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSingInClient;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailET = findViewById(R.id.editTextEmail);
        passwordET = findViewById(R.id.editTextPassword);

        preferences = getSharedPreferences(PREF_KEY, MODE_PRIVATE);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();
        mGoogleSingInClient = GoogleSignIn.getClient(this, gso);

        getSupportLoaderManager().restartLoader(0, null, this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission("android.permission.POST_NOTIFICATIONS") != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{"android.permission.POST_NOTIFICATIONS"}, 1001);
            }
        }

        Log.i(LOG_TAG, "onCreate");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN){
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d(LOG_TAG, "firebaseAuthWithGoogle: " + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e){
                Log.w(LOG_TAG, "Google bejelentkezés sikertelen", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this,task -> {
            if (task.isSuccessful()) {
                Log.d(LOG_TAG, "Sikeres bejelentkezés!");
                startShopping();
            } else {
                Log.d(LOG_TAG, "Sikertelen bejelentkezés!");
                Toast.makeText(MainActivity.this, "Sikertelen bejelentkezés: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void login(View view) {
        String password = passwordET.getText().toString();
        String email = emailET.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Kérlek, töltsd ki az emailt és a jelszót!", Toast.LENGTH_SHORT).show();
            Log.e(LOG_TAG, "Üres mezők!");
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                Log.d(LOG_TAG, "Sikeres bejelentkezés!");
                startShopping();
            } else {
                Log.d(LOG_TAG, "Sikertelen bejelentkezés!");
                Toast.makeText(MainActivity.this, "Sikertelen bejelentkezés: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void startShopping(){
        Intent intent = new Intent(this, ShopListActivity.class);
        startActivity(intent);
    }

    public void loginWithGoogle(View view){
        Intent signInIntent = mGoogleSingInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    public void loginAsGuest(View view){
        mAuth.signInAnonymously().addOnCompleteListener(this,  task -> {
            if (task.isSuccessful()) {
                Log.d(LOG_TAG, "Sikeres bejelentkezés!");
                startShopping();
            } else {
                Log.d(LOG_TAG, "Sikertelen bejelentkezés!");
                Toast.makeText(MainActivity.this, "Sikertelen bejelentkezés: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public void register(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        intent.putExtra("SECRET_KEY", SECRET_KEY);
        startActivity(intent);
    }



    @Override
    protected void onStart() {
        super.onStart();
        Log.i(LOG_TAG, "onStart");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(LOG_TAG, "onStop");

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(LOG_TAG, "onDestroy");

    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", emailET.getText().toString());
        editor.putString("password", passwordET.getText().toString());
        editor.apply();

        Log.i(LOG_TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_TAG, "onResume");

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(LOG_TAG, "onRestart");

    }
    @NonNull
    @Override
    public Loader<String> onCreateLoader(int id, @Nullable Bundle args) {
        return new AsyncLoader(this);
    }
    @Override
    public void onLoadFinished(@NonNull Loader<String> loader, String data) {
        Button anonym = findViewById(R.id.loginGuestButton);
        anonym.setText(data);
    }
    @Override
    public void onLoaderReset(@NonNull Loader<String> loader) {}
}