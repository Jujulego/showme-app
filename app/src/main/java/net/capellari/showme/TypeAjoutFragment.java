package net.capellari.showme;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import net.capellari.showme.db.AppDatabase;
import net.capellari.showme.db.Type;
import net.capellari.showme.db.TypeBase;
import net.capellari.showme.db.TypeParam;
import net.capellari.showme.net.TypesModel;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by julien on 27/01/18.
 *
 * Liste des types à ordonner
 */

public class TypeAjoutFragment extends Fragment {
    // Constantes
    private static final String TAG = "TypeAjoutFragment";

    // Attributs
    private TypesAdapter m_adapter;
    private RecyclerView m_liste;

    private AppDatabase m_db;
    private TypesModel m_typesmodel;

    // Events
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Base de données
        m_db = AppDatabase.getInstance(getContext());

        // Récupération du model
        m_typesmodel = ViewModelProviders.of(getActivity()).get(TypesModel.class);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ajouttypes, container, false);

        // Préparation adapter
        if (m_adapter == null) m_adapter = new TypesAdapter();
        m_adapter.setLiveData(m_db.getTypeDAO().recup());
        m_adapter.setParamLiveData(m_typesmodel.getTypes());

        // Gestion de la liste
        m_liste = view.findViewById(R.id.liste);
        m_liste.setAdapter(m_adapter);

        // dividers
        LinearLayoutManager layoutManager = (LinearLayoutManager) m_liste.getLayoutManager();
        m_liste.addItemDecoration(new DividerItemDecoration(
                m_liste.getContext(),
                layoutManager.getOrientation()
        ));
        m_liste.setItemAnimator(new DefaultItemAnimator());

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
        private Type m_type;
        private TextView m_nom;

        // Constructeur
        public TypeViewHolder(View itemView) {
            super(itemView);

            // Récupération des vues
            m_nom = itemView.findViewById(R.id.nom);
            m_nom.setSelected(true);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Gardien
                    if (m_type == null) return;

                    // Ajout !
                    m_typesmodel.ajouter(m_type);
                }
            });
        }

        // Méthodes
        public void setType(Type type) {
            m_type = type;

            // Remplissage
            m_nom.setText(type.nom);
            m_nom.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    TypeBase.getIconRessource((int) type._id),
                    0, R.drawable.add_gris,0
            );
        }
    }
    class TypesAdapter extends RecyclerView.Adapter<TypeViewHolder> {
        // Attributs
        private LiveData<List<Type>> m_livedata;
        private LiveData<List<TypeParam>> m_param_livedata;

        private List<Type> m_types       = new LinkedList<>();
        private List<TypeParam> m_params = new LinkedList<>();

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
            holder.setType(m_types.get(position));
        }

        @Override
        public int getItemCount() {
            return m_types.size();
        }

        public void setLiveData(LiveData<List<Type>> livedata) {
            if (m_livedata != null) m_livedata.removeObservers(TypeAjoutFragment.this);

            m_livedata = livedata;
            m_livedata.observe(TypeAjoutFragment.this, new Observer<List<Type>>() {
                @Override
                public void onChanged(@Nullable List<Type> types) {
                    m_types = types;
                    notifyDataSetChanged();

                    filtrage();
                }
            });
        }

        public void setParamLiveData(LiveData<List<TypeParam>> livedata) {
            if (m_param_livedata != null) m_param_livedata.removeObservers(TypeAjoutFragment.this);

            m_param_livedata = livedata;
            m_param_livedata.observe(TypeAjoutFragment.this, new Observer<List<TypeParam>>() {
                @Override
                public void onChanged(@Nullable List<TypeParam> types) {
                    m_params = types;
                    filtrage();
                }
            });
        }

        private void filtrage() {
            // filtrage des types !
            for (TypeParam param : m_params) {

                // Parcous des types
                for (int i = 0; i < m_types.size(); ++i) {
                    Type t = m_types.get(i);

                    if (t._id == param._id) {
                        m_types.remove(i);
                        notifyItemRemoved(i);

                        break;
                    }
                }
            }
        }
    }
}
