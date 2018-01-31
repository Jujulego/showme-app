package net.capellari.showme;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import net.capellari.showme.db.Type;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by julien on 27/01/18.
 *
 * Liste des types à ordonner
 */

public class AjoutTypesFragment extends Fragment {
    // Attributs
    private TypesAdapter m_adapter;
    private RecyclerView m_liste;

    // Events
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ajouttypes, container, false);

        // Récupération de la liste
        m_liste = view.findViewById(R.id.liste);

        if (m_adapter == null) m_adapter = new TypesAdapter();
        m_liste.setAdapter(m_adapter);

        return view;
    }


    // Méthodes
    public TypesAdapter getAdapter() {
        if (m_adapter == null) m_adapter = new TypesAdapter();
        return m_adapter;
    }


    // Classes
    class TypeViewHolder extends RecyclerView.ViewHolder {
        // Attributs
        private TextView nom;

        // Constructeur
        public TypeViewHolder(View itemView) {
            super(itemView);

            // Récupération des vues
            nom = itemView.findViewById(R.id.nom);
        }

        // Méthodes
        public void setType(Type type) {
            // Remplissage
            nom.setText(type.nom);
            nom.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    Type.getIconRessource((int) type._id),
                    0, 0, 0
            );
        }
    }
    class TypesAdapter extends RecyclerView.Adapter<TypeViewHolder> {
        // Attributs
        private LiveData<List<Type>> m_livedata;
        private List<Type> m_types = new LinkedList<>();

        // Constructeur
        public TypesAdapter() {}

        // Events
        @Override
        public TypeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_ajouttype, parent, false);
            return new TypeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TypeViewHolder holder, int position) {
            holder.setType(m_types.get(position));
        }

        @Override
        public int getItemCount() {
            return m_types.size();
        }

        public void setLiveData(LiveData<List<Type>> livedata) {
            if (m_livedata != null) m_livedata.removeObservers(AjoutTypesFragment.this);

            m_livedata = livedata;
            m_livedata.observe(AjoutTypesFragment.this, new Observer<List<Type>>() {
                @Override
                public void onChanged(@Nullable List<Type> types) {
                    m_types = types;
                    notifyDataSetChanged();
                }
            });
        }
    }
}
