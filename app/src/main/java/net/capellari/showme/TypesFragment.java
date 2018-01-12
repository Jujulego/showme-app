package net.capellari.showme;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by julien on 12/01/18.
 *
 * Gestion catégories
 */

public class TypesFragment extends Fragment {
    // Attributs
    private RecyclerView m_recycler;

    // Events
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_types, container, false);

        // Test data
        ArrayList<String> types = new ArrayList<>();
        types.add("item 1");
        types.add("item 2");
        types.add("item 3");
        types.add("item 4");
        types.add("item 5");
        types.add("item 6");

        // Récupération des vues
        m_recycler = view.findViewById(R.id.recycler);

        TypesAdapter adapter = new TypesAdapter(types);
        m_recycler.setAdapter(adapter);

        ItemTouchHelper.Callback callback = new TypesTouchCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(m_recycler);

        return view;
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
        private ArrayList<String> m_types = new ArrayList<>();

        // Constructeur
        public TypesAdapter(Collection<String> types) {
            m_types.addAll(types);
        }

        // Events
        @Override
        public TypeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_type, parent, false);
            return new TypeViewHolder(view);
        }

        @Override
        public void onBindViewHolder(TypeViewHolder holder, int position) {
            holder.nom.setText(m_types.get(position));
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
            return;
        }
    }
}
