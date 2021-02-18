package com.example.tictactoe.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;
import com.example.tictactoe.R;
import com.example.tictactoe.app.Constantes;
import com.example.tictactoe.model.Jugada;
import com.example.tictactoe.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

public class GameActivity extends AppCompatActivity {

    List<ImageView> casillas;
    TextView tvPlayer1, tvPlayer2;
    FirebaseAuth firebaseAuth;
    FirebaseFirestore db;
    String uid, jugadaId, playerOneName = "", playerTwoName = "", ganadorId = "", playerOneId = "", playerTwoId = "";
    Jugada jugada;
    ListenerRegistration listenerJugada = null;
    FirebaseUser firebaseUser;
    String nombreJugador;
    User userplayer1, userplayer2;
    boolean jugador1 = true, rendirse=false;
    MediaPlayer mp_winner, mp_empate, mp_looser, mp_boton_ficha, mp_escapar;
    FloatingActionButton fab;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        //Música

        //Efectos de sonido
        mp_winner = MediaPlayer.create(this, R.raw.winner);
        mp_empate = MediaPlayer.create(this, R.raw.empate);
        mp_looser = MediaPlayer.create(this, R.raw.looser);
        mp_boton_ficha = MediaPlayer.create(this, R.raw.boton_ficha);
        mp_escapar = MediaPlayer.create(this, R.raw.escapar);


        initViews();
        initGame();

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jugada.getJugadorUnoId().equals(uid)) {
                    jugada.setAbandonoId(uid);
                    jugada.setGanadorId(jugada.getJugadorDosId());



                }else if (jugada.getJugadorDosId().equals(uid)){
                    jugada.setAbandonoId(uid);
                    jugada.setGanadorId(jugada.getJugadorUnoId());



                }

                db.collection("jugadas")
                        .document(jugadaId)
                        .set(jugada)
                        .addOnSuccessListener(GameActivity.this, new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                            }
                        }).addOnFailureListener(GameActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("Error", "Error al guardar la jugada");
                    }
                });


                rendirse=true;
                mostrarDialogoGameOver();




            }
        });

    }

    private void initGame() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        uid = firebaseUser.getUid();

        Bundle extras = getIntent().getExtras();
        jugadaId = extras.getString(Constantes.EXTRA_JUGADA_ID);

    }

    private void initViews() {
        tvPlayer1 = findViewById(R.id.textViewPlayer1);
        tvPlayer2 = findViewById(R.id.textViewPlayer2);

        casillas = new ArrayList<>();
        casillas.add((ImageView) findViewById(R.id.imageView0));
        casillas.add((ImageView) findViewById(R.id.imageView1));
        casillas.add((ImageView) findViewById(R.id.imageView2));
        casillas.add((ImageView) findViewById(R.id.imageView3));
        casillas.add((ImageView) findViewById(R.id.imageView4));
        casillas.add((ImageView) findViewById(R.id.imageView5));
        casillas.add((ImageView) findViewById(R.id.imageView6));
        casillas.add((ImageView) findViewById(R.id.imageView7));
        casillas.add((ImageView) findViewById(R.id.imageView8));

    }

    @Override
    protected void onStart() {
        super.onStart();
        jugadaListener();
    }

    private void jugadaListener() { //obtención de los datos de la jugada
        //CL-24
        listenerJugada = db.collection("jugadas")
                .document(jugadaId)
                .addSnapshotListener(GameActivity.this, new EventListener<DocumentSnapshot>() { //para poder identificar cambios en tiempo real
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                        if (e != null){
                            Toast.makeText(GameActivity.this, "Error al obtener los datos de la jugada", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        String source = snapshot != null
                                & snapshot.getMetadata().hasPendingWrites() ? "Local" : "Server"; //para saber si el cambio es a nivel local o de server

                        if (snapshot.exists() && source.equals("Server")){
                            //Parseando DocumentSnapshot -> Jugada
                            jugada = snapshot.toObject(Jugada.class);
                            if (playerOneName.isEmpty() || playerTwoName.isEmpty()){

                                //Obtener los nombres de usuario de la jugada
                                getPlayerNames();
                            }

                            updateUI();


                        }

                        updatePlayersUI();
                    }


                });
    }

    private void updatePlayersUI() {

        if (jugada.isTurnoJugadorUno()){
            tvPlayer1.setTextColor(getResources().getColor(R.color.colorPrimary));
            tvPlayer2.setTextColor(getResources().getColor(R.color.colorGris));
        }else{
            tvPlayer1.setTextColor(getResources().getColor(R.color.colorGris));
            tvPlayer2.setTextColor(getResources().getColor(R.color.colorAccent));
        }

        if (!jugada.getGanadorId().isEmpty() && rendirse==false){ //quiere decir que tenemos un ganador
            ganadorId = jugada.getGanadorId();
            mostrarDialogoGameOver();
        }







    }

    private void updateUI() {
        for (int i=0; i<9; i++){
            int casilla = jugada.getCeldasSeleccionadas().get(i); //obtenemos la selección del jugador
            ImageView ivCasillaActual = casillas.get(i);

            if(casilla == 0){
                ivCasillaActual.setImageResource(R.drawable.ic_empty_square);
            }else if (casilla == 1){
                ivCasillaActual.setImageResource(R.drawable.ic_player_one);
            }else{
                ivCasillaActual.setImageResource(R.drawable.ic_player_two);
            }
        }


    }

    private void getPlayerNames() {
        //Obtener el nombre del player 1
        db.collection("users")
                .document(jugada.getJugadorUnoId()) //este es el identificador del documento
                .get()
                .addOnSuccessListener(GameActivity.this, new OnSuccessListener<DocumentSnapshot>() { //no es obligatorio poner el contexto
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        userplayer1 = documentSnapshot.toObject(User.class); //con esto conseguimos la información del usuario 1
                        playerOneName = documentSnapshot.get("name").toString();
                        playerOneId = jugada.getJugadorUnoId(); //guardamos su ID
                        tvPlayer1.setText(playerOneName);



                        if (jugada.getJugadorUnoId().equals(uid)){ //con esto sabemos si es el jugador 1
                            nombreJugador = playerOneName;
                        }
                    }
                });

        //Obtener el nombre del player 2
        db.collection("users")
                .document(jugada.getJugadorDosId()) //jugada es una clase donde se encuentran todos los campos que hemos añadido al firebase
                .get()
                .addOnSuccessListener(GameActivity.this, new OnSuccessListener<DocumentSnapshot>() { //no es obligatorio poner el contexto
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        userplayer2 = documentSnapshot.toObject(User.class); //con esto conseguimos la información del usuario 2
                        playerTwoName = documentSnapshot.get("name").toString();
                        playerTwoId = jugada.getJugadorUnoId(); //guardamos su ID
                        tvPlayer2.setText(playerTwoName);

                        if (jugada.getJugadorDosId().equals(uid)){ //con esto sabemos si es el jugador 2
                            nombreJugador = playerTwoName;
                            jugador1 = false;
                        }
                    }
                });
    }

    @Override
    protected void onStop() {
        if (rendirse==false && jugada.getGanadorId().isEmpty()){
            if (jugada.getJugadorUnoId().equals(uid)) {
                jugada.setAbandonoId(uid);
                jugada.setGanadorId(jugada.getJugadorDosId());



            }else if (jugada.getJugadorDosId().equals(uid)){
                jugada.setAbandonoId(uid);
                jugada.setGanadorId(jugada.getJugadorUnoId());



            }

            db.collection("jugadas")
                    .document(jugadaId)
                    .set(jugada)
                    .addOnSuccessListener(GameActivity.this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    }).addOnFailureListener(GameActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("Error", "Error al guardar la jugada");
                }
            });


            rendirse=true;
            mostrarDialogoGameOver();


        }





        super.onStop();

    }

    public void casillaSeleccionada(View view) {
        if (!jugada.getGanadorId().isEmpty()){ //si ya existe un ganador
            Toast.makeText(this, "La partida ha terminado", Toast.LENGTH_SHORT).show();
        }else{
            if (jugada.isTurnoJugadorUno() && jugada.getJugadorUnoId().equals(uid)){
                //Está jugando el jugador 1
                actualizarJugada(view.getTag().toString()); //el tag está asignado en las imágenes de los cuadrados
                mp_boton_ficha.start();
            }else if (!jugada.isTurnoJugadorUno() && jugada.getJugadorDosId().equals(uid)){
                //Está jugando el jugador 2
                actualizarJugada(view.getTag().toString());
                mp_boton_ficha.start();
            }else{
                Toast.makeText(this, "Es el turno del otro jugador", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void actualizarJugada(String numeroCasilla) {
        int posicionCasilla = Integer.parseInt(numeroCasilla);

        if (jugada.getCeldasSeleccionadas().get(posicionCasilla) != 0) { //Si es distinto de cero es que contiene X u O
            Toast.makeText(this, "Seleccione una casilla libre", Toast.LENGTH_SHORT).show();
        } else {
            if (jugada.isTurnoJugadorUno()) {

                casillas.get(posicionCasilla).setImageResource(R.drawable.ic_player_one);
                jugada.getCeldasSeleccionadas().set(posicionCasilla, 1);
                mp_boton_ficha.start();

            } else {
                casillas.get(posicionCasilla).setImageResource(R.drawable.ic_player_two);
                jugada.getCeldasSeleccionadas().set(posicionCasilla, 2);
                mp_boton_ficha.start();
            }


            if (existeSolucion() && rendirse==false){
                jugada.setGanadorId(uid); //la última jugada es la que decide si ha ganado o hay empate

            }else if (existeEmpate()){
                jugada.setGanadorId("EMPATE");

            }else {
                cambioTurno();

            }


            //Actualizar en Firestore los datos de la jugada
            db.collection("jugadas")
                    .document(jugadaId)
                    .set(jugada)
                    .addOnSuccessListener(GameActivity.this, new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                        }
                    }).addOnFailureListener(GameActivity.this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("Error", "Error al guardar la jugada");
                }
            });
        }
    }



    private void cambioTurno() {
        //Cambio de turno
        jugada.setTurnoJugadorUno(!jugada.isTurnoJugadorUno());
    }

    private boolean existeEmpate() {
        boolean existe = false;

        //Empate
        boolean hayCasillaLibre = false;
        for (int i = 0; i < 9; i++) {
            if (jugada.getCeldasSeleccionadas().get(i) == 0) {
                hayCasillaLibre = true;
                break;
            }
        }
        if (hayCasillaLibre == false) { //Empate
            existe = true;
        }
        return existe;
    }

    private boolean existeSolucion() {
        boolean existe = false;


        List<Integer> selectedCells = jugada.getCeldasSeleccionadas();
        if (selectedCells.get(0) == selectedCells.get(1)
                && selectedCells.get(1) == selectedCells.get(2)
                && selectedCells.get(2) != 0) { // 0 - 1 - 2
            existe = true;
        } else if (selectedCells.get(3) == selectedCells.get(4)
                && selectedCells.get(4) == selectedCells.get(5)
                && selectedCells.get(5) != 0) { // 3 - 4 - 5
            existe = true;
        } else if (selectedCells.get(6) == selectedCells.get(7)
                && selectedCells.get(7) == selectedCells.get(8)
                && selectedCells.get(8) != 0) { // 6 - 7 - 8
            existe = true;
        } else if (selectedCells.get(0) == selectedCells.get(3)
                && selectedCells.get(3) == selectedCells.get(6)
                && selectedCells.get(6) != 0) { // 0 - 3 - 6
            existe = true;
        } else if (selectedCells.get(1) == selectedCells.get(4)
                && selectedCells.get(4) == selectedCells.get(7)
                && selectedCells.get(7) != 0) { // 1 - 4 - 7
            existe = true;
        } else if (selectedCells.get(2) == selectedCells.get(5)
                && selectedCells.get(5) == selectedCells.get(8)
                && selectedCells.get(8) != 0) { // 2 - 5 - 8
            existe = true;
        } else if (selectedCells.get(0) == selectedCells.get(4)
                && selectedCells.get(4) == selectedCells.get(8)
                && selectedCells.get(8) != 0) { // 0 - 4 - 8
            existe = true;
        } else if (selectedCells.get(2) == selectedCells.get(4)
                && selectedCells.get(4) == selectedCells.get(6)
                && selectedCells.get(6) != 0) { // 2 - 4 - 6
            existe = true;
        }



        return existe;
    }

    public void mostrarDialogoGameOver(){
// 1. Instantiate an <code><a href="/reference/android/app/AlertDialog.Builder.html">AlertDialog.Builder</a></code> with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View v = getLayoutInflater().inflate(R.layout.dialogo_game_over, null); //recogemos la vista en un objeto de tipo view
        //Obtenemos las referencias a los View components de nuestro layout
        TextView tvPuntos = v.findViewById(R.id.textViewPuntos);
        TextView tvInformacion = v.findViewById(R.id.textViewInformacion);
        LottieAnimationView gameOverAnimation = v.findViewById(R.id.animationView);

// 2. Chain together various setter methods to set the dialog characteristics
        builder.setTitle("Game Over");
        builder.setCancelable(false); //solo si se pulsa en el botón se puede cerrar el cuadro de diálogo
        builder.setView(v); //para añadirle la vista del layout


        if (jugador1 == true){
            nombreJugador = playerTwoName;
        } else {
            nombreJugador = playerOneName;
        }

        if (ganadorId.equals("EMPATE")){
            actualizarPuntuacion(1);
            tvInformacion.setText("¡Has empatado con " + nombreJugador + "!");
            tvPuntos.setText("+1 punto");
            gameOverAnimation.setAnimation("empate.json");
            mp_empate.start();

        } else if (ganadorId.equals(uid)) { //el usuario en el que nos encontramos es el ganador
            actualizarPuntuacion(3);
            tvInformacion.setText("¡Has ganado a " + nombreJugador + "!");
            tvPuntos.setText("+3 puntos");
            mp_winner.start();

        }else if(rendirse==true){
            actualizarPuntuacion(-2);
            tvInformacion.setText("¡Te has rendido contra " + nombreJugador + "!");
            tvPuntos.setText("-2 puntos");
            gameOverAnimation.setAnimation("rendirse.json");
            mp_escapar.start();



        }else{
            actualizarPuntuacion(0);
            tvInformacion.setText("¡Has perdido contra " + nombreJugador + "!");
            tvPuntos.setText("+0 puntos");
            gameOverAnimation.setAnimation("derrota.json");
            mp_looser.start();
        }

        gameOverAnimation.playAnimation();



        builder.setPositiveButton("Salir", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent i = new Intent(GameActivity.this, FindGameActivity.class);
                listenerJugada.remove();
                startActivity(i);
                finish();
            }
        });


// 3. Get the <code><a href="/reference/android/app/AlertDialog.html">AlertDialog</a></code> from <code><a href="/reference/android/app/AlertDialog.Builder.html#create()">create()</a></code>
        AlertDialog dialog = builder.create();
        dialog.show(); //para poder mostrar el diálogo en la pantalla IMPRESCINDIBLE


    }

    private void actualizarPuntuacion(int puntosConseguidos) {
        User jugadorActualizar = null;
        if (uid.equals(playerOneId)){
            userplayer1.setPoints(userplayer1.getPoints() + puntosConseguidos);
            userplayer1.setPartidasJugadas(userplayer1.getPartidasJugadas() + 1);
            jugadorActualizar = userplayer1;
        }else{
            userplayer2.setPoints(userplayer2.getPoints() + puntosConseguidos);
            userplayer2.setPartidasJugadas(userplayer2.getPartidasJugadas() + 1);
            jugadorActualizar = userplayer2;
        }

        db.collection("users")
                .document(uid)
                .set(jugadorActualizar)
                .addOnSuccessListener(GameActivity.this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                    }
                })
                .addOnFailureListener(GameActivity.this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }


}

