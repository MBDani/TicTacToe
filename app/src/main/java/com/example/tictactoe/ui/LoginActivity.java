package com.example.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.tictactoe.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btn_login, btn_registro;
    private ScrollView formLogin;
    private ProgressBar pbLogin;
    private FirebaseAuth firebaseAuth;
    private String email, password;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.editTextEmail);
        etPassword = findViewById(R.id.editTextPassword);
        btn_login = findViewById(R.id.buttonLogin);
        formLogin = findViewById(R.id.formLogin);
        pbLogin = findViewById(R.id.progressBarLogin);
        btn_registro = findViewById(R.id.buttonRegistrarme);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);

        firebaseAuth = FirebaseAuth.getInstance();




        eventos();
        validarSesion();

    }

    private void validarSesion() {
        String usuario_email = preferences.getString("usuario_email", null);
        String usuario_contraseña = preferences.getString("usuario_contraseña", null);

        if (usuario_email != null && usuario_contraseña != null){
            firebaseAuth.signInWithEmailAndPassword(usuario_email, usuario_contraseña)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()){
                                FirebaseUser user = firebaseAuth.getCurrentUser(); //Obtenemos el nombre de usuario
                                updateUI(user);


                            }else{
                                Log.w("TAG", "signInError: " , task.getException());
                                updateUI(null); //esto realmente nos lo podemos ahorrar con toast
                            }
                        }
                    });
        }



    }

    private void eventos() {
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 email = etEmail.getText().toString();
                 password = etPassword.getText().toString();

                 if (email.isEmpty()){
                    etEmail.setError("El email es obligatorio");
                }else if (password.isEmpty()){
                    etPassword.setError("La contraseña es obligatoria");
                }else{
                    changeLoginFromVisibility(false); //ocultamos el formulario

                     //Realizamos el login a Firebase
                     loginUser();

                }
            }
        });

        btn_registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegistroActivity.class);
                startActivity(i);
            }
        });

    }

    private void loginUser() {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            FirebaseUser user = firebaseAuth.getCurrentUser(); //Obtenemos el id del usuario
                            updateUI(user);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("usuario_email", email);
                            editor.putString("usuario_contraseña", password);
                            editor.commit(); //es lo mejor para guardar cuando hay pocos datos

                        }else{
                            Log.w("TAG", "signInError: " , task.getException());
                            updateUI(null); //esto realmente nos lo podemos ahorrar con toast
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null){
            //Almacenar la información del usuario en FireStore

            //Navegar hacia la siguiente pantalla de la aplicación
            Intent i = new Intent(LoginActivity.this, FindGameActivity.class);
            startActivity(i);
        }else{
            changeLoginFromVisibility(true);
            Toast.makeText(this, "Alguno de los campos es incorrecto", Toast.LENGTH_SHORT).show();
        }
    }

    private void changeLoginFromVisibility(boolean showForm) { //Método para ocultar el scrollView
        // (formLogin) y mostrar el pbLogin (progress bar login)

        pbLogin.setVisibility(showForm ? View.GONE : View.VISIBLE); //showForm es la variable booleana, en el true hace el primer caso y con un false hace el segundo (el de detrás de los puntos)
        formLogin.setVisibility(showForm ? View.VISIBLE : View.GONE);
    }




}
