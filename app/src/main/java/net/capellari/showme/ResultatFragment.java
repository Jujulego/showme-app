package net.capellari.showme;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.capellari.showme.db.Lieu;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julien on 04/01/18.
 *
 * Présente les résultats
 */

public class ResultatFragment extends Fragment {
    // Constantes
    private static final String TAG = "ResultatFragment";

    // Attributs
    private RecyclerView m_liste;
    private SwipeRefreshLayout m_swipeRefresh;

    private int m_refreshMenuItem;
    private OnResultatListener m_listener;

    private int m_compteur = 0; // inversé : mis au max puis réduit jusqu'à 0 => plein !
    private LieuAdapter m_adapter = new LieuAdapter();

    private FiltreModel m_filtreModel;

    // Events
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gestion du menu
        setHasOptionsMenu(true);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Listener !
        m_listener = null;
        if (context instanceof OnResultatListener) {
            m_listener = (OnResultatListener) context;
        }
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Infalte !
        View view = inflater.inflate(R.layout.fragment_resultat, container, false);

        // Liste
        m_liste = view.findViewById(R.id.liste);
        m_liste.setAdapter(m_adapter);

        // SwipeRefresh
        m_swipeRefresh = view.findViewById(R.id.swipe_refresh);

        m_swipeRefresh.setColorSchemeResources(R.color.colorAccent);
        m_swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (m_listener != null) m_listener.onRefresh();
            }
        });

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Refresh ?
        if (item.getItemId() == m_refreshMenuItem) {
            if (m_listener != null) m_listener.onRefresh();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Méthodes
    public void setRefreshMenuItem(int id) {
        m_refreshMenuItem = id;
    }

    public void setRefreshing(boolean refresh) {
        m_swipeRefresh.setRefreshing(refresh);
    }
    public boolean isRefreshing() {
        return m_swipeRefresh.isRefreshing();
    }

    public void initCompteur(int nb) {
        m_compteur += nb;

        // On commence !
        setRefreshing(true);
    }
    public void decrementer() {
        decrementer(1);

        // Fini !
        if (m_compteur == 0) setRefreshing(false);
    }
    public void decrementer(int v) {
        m_compteur -= v;

        if (m_compteur <= 0) {
            m_compteur = 0;
            setRefreshing(false);
        }
    }

    public void ajouterLieu(Lieu lieu) {
        m_adapter.ajouterLieu(lieu);
    }
    public void ajouterLieux(List<Lieu> lieux) {
        m_adapter.ajouterLieux(lieux);
    }
    public void vider() {
        m_adapter.vider();
    }

    // Listener
    public interface OnResultatListener {
        void onRefresh();
        void onLieuClick(Lieu lieu);
    }

    // Classe
    private class LieuViewHolder extends RecyclerView.ViewHolder {
        // Attributs
        private CardView m_card;
        private TextView m_nom;

        private Lieu m_lieu;

        // Constructeur
        public LieuViewHolder(View itemView) {
            super(itemView);

            // Carte
            m_card = itemView.findViewById(R.id.card);
            m_card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    m_listener.onLieuClick(m_lieu);
                }
            });

            // Récupération des vues
            m_nom = itemView.findViewById(R.id.nom);
        }

        // Méthodes
        public void setLieu(Lieu lieu) {
            m_lieu = lieu;

            // Contenu !
            m_nom.setText(lieu.nom);
        }
    }
    private class LieuAdapter extends RecyclerView.Adapter<LieuViewHolder> {
        // Attributs
        private ArrayList<Lieu> m_lieux = new ArrayList<>();

        // Events
        @Override
        public LieuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new LieuViewHolder(
                    getLayoutInflater().inflate(R.layout.card_lieu, parent, false)
            );
        }

        @Override
        public void onBindViewHolder(LieuViewHolder holder, int position) {
            holder.setLieu(m_lieux.get(position));
        }

        // Méthodes
        @Override
        public int getItemCount() {
            return m_lieux.size();
        }

        public void ajouterLieu(Lieu lieu) {
            Log.d(TAG, "ajouterLieu");
            m_lieux.add(lieu);
            notifyItemInserted(m_lieux.size() -1);
        }
        public void ajouterLieux(List<Lieu> lieux) {
            Log.d(TAG, "ajouterLieux");
            int deb = m_lieux.size();
            m_lieux.addAll(lieux);

            notifyItemRangeInserted(deb, m_lieux.size());
        }

        public void vider() {
            Log.d(TAG, "vider");
            int taille = m_lieux.size();
            m_lieux.clear();

            notifyItemRangeRemoved(0, taille);
        }
    }
}
