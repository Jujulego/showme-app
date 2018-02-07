package net.capellari.showme;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.capellari.showme.data.StringUtils;
import net.capellari.showme.data.TypesModel;
import net.capellari.showme.db.Type;
import net.capellari.showme.db.TypeBase;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by julien on 07/02/18.
 *
 * Choix des types dans l'écran de bienvenue
 */

public class BienvenueTypesFragment extends Fragment {
    // Constantes
    private static final String TAG = "BienvenueTypesFragment";

    // Attributs
    private RecyclerView m_liste;
    private TypesAdapter m_adapter;

    private TypesModel m_typesmodel;

    // Events
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Récupération du model
        m_typesmodel = ViewModelProviders.of(getActivity()).get(TypesModel.class);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bienvenue_types, container, false);

        // Préparation adapter
        if (m_adapter == null) m_adapter = new TypesAdapter();
        m_adapter.setTypes(m_typesmodel.getTypes());
        m_adapter.setLiveData(m_typesmodel.getTypesNonSelect());

        // Gestion de la liste
        m_liste = view.findViewById(R.id.liste);
        m_liste.setAdapter(m_adapter);

        return view;
    }

    // Classes
    class TypeViewHolder extends RecyclerView.ViewHolder {
        // Attributs
        private Type m_type;
        private TextView m_nom;

        // Constructeur
        public TypeViewHolder(View itemView) {
            super(itemView);

            // Récupération des vues
            m_nom = itemView.findViewById(R.id.nom);
            m_nom.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Gardien
                    if (m_type == null) return;

                    // Ajouter / enlever
                    if (m_nom.isSelected()) {
                        m_typesmodel.enlever(m_type);
                    } else {
                        m_typesmodel.ajouter(m_type);
                    }
                }
            });
        }

        // Méthodes
        public void setType(Type type, boolean select) {
            // Type
            m_type = type;

            // UI
            m_nom.setText(StringUtils.toTitle(type.nom));
            m_nom.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    TypeBase.getIconRessource((int) type._id),
                    0, 0, 0
            );

            m_nom.setSelected(select);
        }
    }
    class TypesAdapter extends RecyclerView.Adapter<TypeViewHolder> {
        // Attributs
        private LiveData<List<Type>> m_livedata;
        private List<Type> m_types = new LinkedList<>();
        private List<Type> m_typesNonSelect = new LinkedList<>();

        // Constructeur
        public TypesAdapter() {}

        // Events
        @Override
        public TypeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_type, parent, false);
            return new TypeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TypeViewHolder holder, int position) {
            Type type = m_types.get(position);
            holder.setType(type, !m_typesNonSelect.contains(type));
        }

        // Méthodes
        @Override
        public int getItemCount() {
            return m_types.size();
        }

        public void setTypes(List<Type> types) {
            m_types = types;
        }

        public void setLiveData(LiveData<List<Type>> livedata) {
            if (m_livedata != null) m_livedata.removeObservers(BienvenueTypesFragment.this);

            m_livedata = livedata;
            m_livedata.observe(BienvenueTypesFragment.this, new Observer<List<Type>>() {
                @Override
                public void onChanged(@Nullable List<Type> types) {
                    // Gardien
                    if (types == null) return;

                    // Type changés
                    List<Type> chg = new ArrayList<>(m_typesNonSelect);

                    for (Type type : types) {
                        int index = chg.indexOf(type);

                        if (index == -1) {
                            chg.add(type);
                        } else {
                            chg.remove(index);
                        }
                    }

                    // Types non selectionnés
                    m_typesNonSelect.clear();
                    m_typesNonSelect.addAll(types);

                    // Changements
                    for (Type type : chg) {
                        notifyItemChanged(m_types.indexOf(type));
                    }
                }
            });
        }
    }
}
