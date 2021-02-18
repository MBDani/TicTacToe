package com.example.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.tictactoe.R;
import com.example.tictactoe.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RegistroActivity extends AppCompatActivity {

    private EditText etName, etEmail, etPass;
    private Button btRegistro;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private String name, email, password;
    private ProgressBar pbRegistro;
    private ScrollView formRegistro;
    private SharedPreferences preferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        etName = findViewById(R.id.editTextName);
        etEmail = findViewById(R.id.editTextEmail);
        etPass = findViewById(R.id.editTextPassword);
        btRegistro= findViewById(R.id.buttonRegistro);
        pbRegistro = findViewById(R.id.progressBarRegistro);
        formRegistro = findViewById(R.id.formRegistro);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);

        changeRegistroFromVisibility(true);
        eventos();
    }

    private void eventos() {
        btRegistro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 name = etName.getText().toString();
                 email = etEmail.getText().toString();
                 password = etPass.getText().toString();

                if (name.isEmpty()){
                    etName.setError("El nombre es obligatorio");
                }else if (email.isEmpty()){
                    etEmail.setError("El email es obligatorio");
                }else if (password.isEmpty()){
                    etPass.setError("La contraseña es obligatoria");
                }else{
                    //Realizamos el resgitro a Firebase

                    createUser();

                }
            }
        });
    }

    private void createUser() {
        changeRegistroFromVisibility(false);
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() { //para añadir el método cuando sea completado
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){ //task es la respuesta
                            FirebaseUser user = firebaseAuth.getCurrentUser(); //obtiene la información del usuario registrado
                            updateUI(user);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString("usuario_email", email);
                            editor.putString("usuario_contraseña", password);
                            editor.commit();

                        }else{
                            changeRegistroFromVisibility(true);
                            Toast.makeText(RegistroActivity.this, "Error en el registro", Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });

    }

    private void updateUI(FirebaseUser user) {
        if (user != null){
            //Almacenar la información del usuario en FireStore
            User nuevoUsuario = new User (name, 0, 0);

            db.collection("users") //el nombre que tendrá la colección
                    .document(user.getUid()) //creamos el identificador único (user.getUid se supone que es para coger el identificador que se le creo a ese user en la autentificación pero no parece funcionar de primneras)
                    .set(nuevoUsuario) //le añadimos los datos, en este caso le hemos metido los parámetros que tiene un objeto de otra clase
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            //Navegar hacia la siguiente pantalla de la aplicación
                            finish(); //terminamos la activity
                            Intent i = new Intent(RegistroActivity.this, FindGameActivity.class);
                            startActivity(i);
                        }
                    });

        }else{
            changeRegistroFromVisibility(true);
            Toast.makeText(this, "Alguno de los campos es incorrecto", Toast.LENGTH_SHORT).show();
        }
    }

    private void changeRegistroFromVisibility(boolean showForm) { //Método para ocultar el scrollView
        // (formLogin) y mostrar el pbLogin (progress bar login)

        pbRegistro.setVisibility(showForm ? View.GONE : View.VISIBLE);
        formRegistro.setVisibility(showForm ? View.VISIBLE : View.GONE);
    }
}
