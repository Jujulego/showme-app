package net.capellari.showme;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.capellari.showme.db.Type;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by julien on 12/01/18.
 *
 * Gestion catégories
 */

public class OrdreTypesFragment extends Fragment {
    // Attributs
    private TypesAdapter m_adapter;
    private RecyclerView m_liste;

    // Events
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ordretypes, container, false);

        // Récupération des vues
        m_liste = view.findViewById(R.id.liste);

        if (m_adapter == null) m_adapter = new TypesAdapter();
        m_liste.setAdapter(m_adapter);

        ItemTouchHelper.Callback callback = new TypesTouchCallback(m_adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(m_liste);

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
        public TextView nom;

        // Constructeur
        public TypeViewHolder(View itemView) {
            super(itemView);

            // Récupération des vues
            nom = itemView.findViewById(R.id.nom);
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
            View view = getLayoutInflater().inflate(R.layout.item_ordretype, parent, false);
            return new TypeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TypeViewHolder holder, int position) {
            holder.nom.setText(m_types.get(position).nom);
        }

        public boolean onItemMove(int from, int to) {
            if (from < to) {
                for (int i = from; i < to; i++) {
                    Collections.swap(m_types, i, i + 1);
                }
            } else {
                for (int i = from; i > to; i--) {
                    Collections.swap(m_types, i, i - 1);
                }
            }

            notifyItemMoved(from, to);
            return true;
        }

        // Méthodes
        @Override
        public int getItemCount() {
            return m_types.size();
        }

        public void setLiveData(LiveData<List<Type>> livedata) {
            if (m_livedata != null) m_livedata.removeObservers(OrdreTypesFragment.this);

            m_livedata = livedata;
            m_livedata.observe(OrdreTypesFragment.this, new Observer<List<Type>>() {
                @Override
                public void onChanged(@Nullable List<Type> types) {
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
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags  = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = 0;

            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            m_adapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());

            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        }
    }
}