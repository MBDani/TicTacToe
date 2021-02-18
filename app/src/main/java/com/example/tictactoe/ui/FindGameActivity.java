package com.example.tictactoe.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tictactoe.R;
import com.example.tictactoe.app.Constantes;
import com.example.tictactoe.model.Jugada;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import javax.annotation.Nullable;

public class FindGameActivity extends AppCompatActivity {

    private TextView tvLoadingMessage, tvPuntuacion, tvNombre;
    private ProgressBar progressBar;
    private ScrollView layoutProgressBar, layoutMenuJuego;
    private Button btnJugar, btnRanking;
    private String uid; //el identificador de usuario
    private String jugadaId;
    private ListenerRegistration listenerRegistration = null; //en un principio es una variable para
    // estar continuamente registrando/revisando algo, lo dejamos como null para que no empiece activada (solo se activará si es host, si hace join a la partida nunca se llega a activar.
    private LottieAnimationView animationView;
    private MediaPlayer mp_empezar;
    private SharedPreferences preferences;
    private  boolean encontrado = false; //con esta variable resuelvo los casos de abandonos antes de empezar la partida
    private FloatingActionButton fab;

    //Firebase
    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser; //para almacenar el usuario actual con el que estamos trabajando

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_game);


        layoutProgressBar = findViewById(R.id.layoutProgressBar);
        layoutMenuJuego = findViewById(R.id.menuJuego);
        btnJugar = findViewById(R.id.buttonJugar);
        btnRanking = findViewById(R.id.buttonRanking);
        tvPuntuacion = findViewById(R.id.textViewPuntosCuenta);
        tvNombre = findViewById(R.id.textViewNombreUsuario);
        fab = findViewById(R.id.floatingActionButtonPararBusqueda);


        //Música

        //Efectos de sonido
        mp_empezar = MediaPlayer.create(this, R.raw.empezar_partida);



        initProgressBar();
        initFirebase();
        eventos();
        puntosCuenta();

    }



    private void initFirebase() {
        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser(); //con esto obtenemos el usuario actual
        uid = firebaseUser.getUid(); //guardamos el ID del usuario en una variable de tipo String

    }

    private void eventos() {
        btnJugar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeMenuVisibility(false);
                buscarJugadaLibre();
            }
        });

        btnRanking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(FindGameActivity.this, RankingActivity.class);
                startActivity(i);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              if (encontrado == false){
                  //borramos la partida que acabmos de crear
                  db.collection("jugadas").document(jugadaId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                      @Override
                      public void onSuccess(Void aVoid) {
                          listenerRegistration.remove();
                          changeMenuVisibility(true);


                      }
                  });
              }else {
                  Toast.makeText(FindGameActivity.this, "¡Ya se ha encontrado tu contrincante!", Toast.LENGTH_SHORT).show();
              }



            }
        });
    }

    private void buscarJugadaLibre() { //Join game
        tvLoadingMessage.setText("Buscando partidas...");
        animationView.playAnimation();
        db.collection("jugadas")
                .whereEqualTo("jugadorDosId" , "") //buscamos a alguien que haya creado la partida y tenga el campo de jugadorDosId vacío
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.getResult().size() == 0){ //esto quiere decir que no existe ninguna partida libre
                            //No existen partidas libres, crear una nueva
                            crearNuevaJugada();
                        }else{ //ha encontrado una partida


                            encontrado=true;

                            for ( DocumentSnapshot docJugada : task.getResult().getDocuments()) { //de toda la lista de partidas libres encontradas, las almacenamos en docJugada y las vamos a ir recorriendo una a una

                                if (!docJugada.get("jugadorUnoId").equals(uid)) { //con esto comprobamos que no tienen el mismo ID el jugador 1 y 2


                                    jugadaId = docJugada.getId(); //le transferimos el ID de la partida que hemos encontrado a la variable jugadaID
                                    Jugada jugada = docJugada.toObject(Jugada.class); //Jugada es la clase y el toObject() es para parsearlo a un objeto

                                    jugada.setJugadorDosId(uid); //al host le añadimos el ID del invitado llenando asi el ID del jugador dos

                                    db.collection("jugadas")
                                            .document(jugadaId)
                                            .set(jugada)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    tvLoadingMessage.setText("¡Partida encontrada! Comenzando juego...");
                                                    animationView.setRepeatCount(0); //solo se repita una vez
                                                    animationView.setAnimation("checked_animation.json"); //sustituir la animación por otra
                                                    mp_empezar.start();
                                                    animationView.playAnimation();

                                                    final Handler handler = new Handler();
                                                    final Runnable r = new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            startGame();
                                                        }
                                                    };

                                                    handler.postDelayed(r, 1500);

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            changeMenuVisibility(true);
                                            Toast.makeText(FindGameActivity.this, "Error al entrar en la partida", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                    break; //esto es para que se pare el for ya que ya tenemos lista la partida
                                }

                                encontrado = false;
                                if (encontrado == false){
                                    crearNuevaJugada();
                                }
                            }


                        }
                    }
                });
    }

    private void crearNuevaJugada() { //Host Game
        tvLoadingMessage.setText("Creando una nueva partida...");
        Jugada nuevaJugada = new Jugada(uid); //usamos el constructor de la clase Jugada y usamos el constructor que solo tiene como parámetro el ID del primer jugador (el nuestro)

        db.collection("jugadas")
                .add(nuevaJugada)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        jugadaId = documentReference.getId();
                        //Tenemos creada la jugada, debemos de esperar a otro jugador
                        esperarJugador();

                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                changeMenuVisibility(true);
                Toast.makeText(FindGameActivity.this, "Error al crear partida", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void esperarJugador() {
        tvLoadingMessage.setText("Esperando a otro jugador");
        listenerRegistration = db.collection("jugadas")
                .document(jugadaId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() { //esto es para saber cuando ocurre un cambio en el documento jugadas (con esto sabremos si se ha conectado el jugador 2)

                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        if (!documentSnapshot.get("jugadorDosId").equals("")){ //se ha unido el segundo jugador a la partida
                            encontrado = true; //ya ha encontrado contrincante
                            tvLoadingMessage.setText("¡Ya ha llegado tu contrincante! Comienza la partida...");
                            animationView.setRepeatCount(0); //solo se repita una vez
                            animationView.setAnimation("checked_animation.json"); //sustituir la animación por otra
                            animationView.playAnimation();
                            mp_empezar.start();

                            final Handler handler = new Handler();
                            final Runnable r = new Runnable() {
                                @Override
                                public void run() {
                                    startGame();
                                }
                            };

                            handler.postDelayed(r, 1500);

                        }
                    }
                });

    }

    private void startGame() {
        if (listenerRegistration != null){
            listenerRegistration.remove(); //cuando ya haya cumplido su función lo eliminamos para que no este "continuamente escuchando"
        }
        Intent i = new Intent(FindGameActivity.this, GameActivity.class);
        i.putExtra(Constantes.EXTRA_JUGADA_ID, jugadaId); //usamos la variable de la clase constantes para no equivocarnos
        startActivity(i);
        jugadaId="";
        finish();
    }

    private void initProgressBar() {
        tvLoadingMessage = findViewById(R.id.textViewLoading);
        progressBar = findViewById(R.id.progressBarJugadas);
        animationView = findViewById(R.id.animationView);

        progressBar.setIndeterminate(true); //va a estar yendo de izquierda a derecha
        tvLoadingMessage.setText("Cargando...");

        changeMenuVisibility(true);
    }

    private void changeMenuVisibility(boolean showMenu) {
        layoutProgressBar.setVisibility(showMenu ? View.GONE : View.VISIBLE);
        layoutMenuJuego.setVisibility(showMenu ? View.VISIBLE : View.GONE); //la diferencia entre gone e invisible es que gone no deja ni el hueco
        fab.setVisibility(showMenu ? View.GONE : View.VISIBLE);
    }

/* NO FUNCIONA DE MOMENTO
    @Override
    protected void onResume() {
        super.onResume();
        if (jugadaId != ""){
            changeMenuVisibility(false);
            esperarJugador();
        }else{
            changeMenuVisibility(true);
        }
    }
*/

    @Override
    protected void onStop() {
        if (listenerRegistration != null){ //si ha dejado de ser nulo, es decir, si lo hemos usado
            listenerRegistration.remove(); //lo borramos
        }


        super.onStop();
    }

    private void puntosCuenta() {
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(FindGameActivity.this, new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String puntos = documentSnapshot.get("points").toString();
                        String nombre = documentSnapshot.get("name").toString();
                        tvNombre.setText(nombre);
                        tvPuntuacion.setText("Puntos: " + puntos);
                    }
                });




    }

    //Método para mostrar los botones de acción
    public boolean onCreateOptionsMenu (Menu menu){
        getMenuInflater().inflate(R.menu.overflow_findgame, menu);
        return true;
    }

    //Método para agregar las acciones a nuestros botones
    public boolean onOptionsItemSelected (MenuItem item){
        int id = item.getItemId();
        if (id == R.id.itemCerrarSesión){
            preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("usuario_email", null);
            editor.putString("usuario_contraseña", null);
            editor.commit();
            Intent i = new Intent(FindGameActivity.this, LoginActivity.class);
            startActivity(i);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

    }
}
