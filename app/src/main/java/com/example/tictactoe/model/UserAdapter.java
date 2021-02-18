package com.example.tictactoe.model;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tictactoe.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;

public class UserAdapter extends FirestoreRecyclerAdapter<User, UserAdapter.ViewHolder> {


    /**
     * Create a new RecyclerView adapter that listens to a Firestore Query.  See {@link
     * FirestoreRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public UserAdapter(@NonNull FirestoreRecyclerOptions<User> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int i, @NonNull User user) {
        holder.textViewNombre.setText(user.getName());
        holder.textViewJugadas.setText(String.valueOf(user.getPartidasJugadas()));
        holder.textViewPuntos.setText(String.valueOf(user.getPoints()));

        int pos = i+1;

        holder.textViewPuesto.setText(pos + "º");

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        //he cambiado el objeto ViewGroup
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.datos_ranking, viewGroup, false);
        return new ViewHolder(view);

    }

    //La clase que hemos creado
    public class ViewHolder extends RecyclerView.ViewHolder{
        //Aquí instanciamos las vistas
       TextView textViewNombre;
        TextView textViewJugadas;
        TextView textViewPuntos;
        TextView textViewPuesto;

        //Creamos el constructor
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewJugadas = itemView.findViewById(R.id.textViewJugadas);
            textViewPuntos = itemView.findViewById(R.id.textViewPuntos);
            textViewPuesto = itemView.findViewById(R.id.textViewPuesto);
        }
    }

}
