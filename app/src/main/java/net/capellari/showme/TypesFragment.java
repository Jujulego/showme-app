package net.capellari.showme;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.capellari.showme.db.Type;
import net.capellari.showme.db.TypeBase;
import net.capellari.showme.db.TypeParam;
import net.capellari.showme.net.TypesModel;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by julien on 12/01/18.
 *
 * Gestion catégories
 */

public class TypesFragment extends Fragment {
    // Attributs
    private TypesAdapter m_adapter;
    private RecyclerView m_liste;

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
        View view = inflater.inflate(R.layout.fragment_types, container, false);

        // Préparation adapter
        if (m_adapter == null) m_adapter = new TypesAdapter();
        m_adapter.setLiveData(m_typesmodel.getTypes());

        // Gestion de la liste
        m_liste = view.findViewById(R.id.liste);
        m_liste.setAdapter(m_adapter);

        // dividers
        LinearLayoutManager layoutManager = (LinearLayoutManager) m_liste.getLayoutManager();
        m_liste.addItemDecoration(new DividerItemDecoration(
                m_liste.getContext(),
                layoutManager.getOrientation()
        ));

        // touch helper
        ItemTouchHelper.Callback callback = new TypesTouchCallback(m_adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(m_liste);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Méthodes
    public TypesAdapter getAdapter() {
        if (m_adapter == null) m_adapter = new TypesAdapter();
        return m_adapter;
    }

    // Classes
    class TypeViewHolder extends RecyclerView.ViewHolder {
        // Attributs
        private TextView m_nom;

        // Constructeur
        public TypeViewHolder(View itemView) {
            super(itemView);

            // Récupération des vues
            m_nom = itemView.findViewById(R.id.nom);
            m_nom.setSelected(true);
        }

        // Méthodes
        public void setType(TypeParam type) {
            m_nom.setText(type.nom);
            m_nom.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    TypeBase.getIconRessource((int) type._id),
                    0, 0, 0
            );
        }
    }
    class TypesAdapter extends RecyclerView.Adapter<TypeViewHolder> {
        // Attributs
        private LiveData<List<TypeParam>> m_livedata;
        private List<TypeParam> m_types = new LinkedList<>();

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

        // Méthodes
        @Override
        public int getItemCount() {
            return m_types.size();
        }

        public void remove(int pos) {
            m_typesmodel.enlever(pos);
        }

        public void setLiveData(LiveData<List<TypeParam>> livedata) {
            if (m_livedata != null) m_livedata.removeObservers(TypesFragment.this);

            m_livedata = livedata;
            m_livedata.observe(TypesFragment.this, new Observer<List<TypeParam>>() {
                @Override
                public void onChanged(@Nullable List<TypeParam> types) {
                    m_types = types;
                    notifyDataSetChanged();
                }
            });
        }
    }
    class TypesTouchCallback extends ItemTouchHelper.Callback {
        // Attributs
        private final TypesAdapter m_adapter;

        // Constructeur
        public TypesTouchCallback(TypesAdapter adapter) {
            m_adapter = adapter;
        }

        // Méthodes
        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return true;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags  = 0;
            int swipeFlags = ItemTouchHelper.END;

            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            m_adapter.remove(viewHolder.getAdapterPosition());
        }
    }
}