package com.example.tictactoe.ui;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;


import com.example.tictactoe.R;
import com.example.tictactoe.model.User;
import com.example.tictactoe.model.UserAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


public class RankingActivity extends AppCompatActivity {

    private RecyclerView recyclerViewRanking;
    private UserAdapter mAdapter;
    private FirebaseFirestore mFirestore;
    private SharedPreferences preferences;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);


        recyclerViewRanking = findViewById(R.id.recyclerViewRanking);
        recyclerViewRanking.setLayoutManager(new LinearLayoutManager(this));

        mFirestore = FirebaseFirestore.getInstance();
        Query query = mFirestore.collection("users").orderBy("points", Query.Direction.DESCENDING)
                .limit(10); //Añadir que si hay empate por puntos, gana el que menos partidas haya jugado

        FirestoreRecyclerOptions<User> firestoreRecyclerOptions = new FirestoreRecyclerOptions.Builder
                <User>().setQuery(query, User.class).build();

        mAdapter = new UserAdapter(firestoreRecyclerOptions);
        mAdapter.notifyDataSetChanged();//para que todos los cambios se vean reflejados en tiempo real

        recyclerViewRanking.setAdapter(mAdapter);



    }

    @Override
    protected void onStart() {
        super.onStart();
        mAdapter.startListening(); //para que empiece a mostrar los artículos
    }


    @Override
    protected void onStop() {
        super.onStop();
        mAdapter.stopListening(); //Para que no este actualizando en segundo plano
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_ranking, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.itemCerrarSesion){
            preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("usuario_email", null);
            editor.putString("usuario_contraseña", null);
            editor.commit();
            startActivity(new Intent(RankingActivity.this, LoginActivity.class));
            finish();
            return true;

        }else if (id == R.id.itemMenu){
            startActivity(new Intent(RankingActivity.this, FindGameActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
