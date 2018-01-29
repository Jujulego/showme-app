package net.capellari.showme;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import net.capellari.showme.db.Lieu;

import java.util.ArrayList;

/**
 * Created by julien on 04/01/18.
 *
 * Présente les résultats
 */

public class ResultatFragment extends Fragment {
    // Attributs
    private RecyclerView m_liste;
    private SwipeRefreshLayout m_swipeRefresh;

    private int m_refreshMenuItem;
    private OnResultatListener m_listener;

    private int m_compteur = 0; // inversé : mis au max puis réduit jusqu'à 0 => plein !
    private LieuAdapter m_adapter = new LieuAdapter();

    // Events
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Cool !
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
        if (refresh) m_adapter.vider();
        m_swipeRefresh.setRefreshing(refresh);
    }
    public void initCompteur(int nb) {
        m_compteur += nb;

        // On commence !
        setRefreshing(true);
    }
    public void decrementer() {
        decrementer(1);
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

        // Fini !
        if (m_compteur == 0) setRefreshing(false);
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
            m_lieux.add(lieu);
            notifyItemInserted(m_lieux.size() -1);
        }

        public void vider() {
            int nb = m_lieux.size();
            m_lieux.clear();

            notifyItemRangeRemoved(0, nb-1);
        }
    }

    // Enumération
    /*public enum Status {
        VIDE, CHARGEMENT, MESSAGE, LISTE
    }

    // Attributs
    private TextView m_message;
    private SwipeRefreshLayout m_swipeRefresh;
    private RecyclerView m_liste;
    private ProgressBar m_waiter;

    private boolean m_init = false;
    private CharSequence m_messagePreinit = "";
    private Status m_status = Status.VIDE;
    private SwipeRefreshLayout.OnRefreshListener m_listener = null;

    private GoogleMap m_map = null;
    private LieuxAdapter m_adapter = new LieuxAdapter();
    private LinkedList<Lieu> m_lieux = new LinkedList<>();
    private LiveData<Type.TypeNb[]> m_livedata;

    private int m_groupeOuvert = -1;

    // Events
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Ajout du layout
        View view = inflater.inflate(R.layout.fragment_resultat, container, false);

        // Récupération des vues
        m_liste   = view.findViewById(R.id.liste);
        m_waiter  = view.findViewById(R.id.waiter);
        m_message = view.findViewById(R.id.message);

        // Préparation liste

        // Préparation refresh layout
        m_swipeRefresh = view.findViewById(R.id.swipe_refresh);
        if (m_listener != null) {
            m_swipeRefresh.setOnRefreshListener(m_listener);
            m_listener = null;
        }

        // Mise à l'etat
        m_init = true;
        setStatus(m_status);
        m_message.setText(m_messagePreinit);

        return view;
    }

    // Méthodes
    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        if (m_init) {
            m_swipeRefresh.setOnRefreshListener(listener);
        } else {
            m_listener = listener;
        }
    }
    public void refreshing() {
        m_swipeRefresh.setRefreshing(true);
        m_waiter.setVisibility(View.GONE);
        m_message.setText("");
    }

    public void initProgress(int max) {
        m_waiter.setMax(max);
        m_waiter.setProgress(0);
        m_waiter.setVisibility(View.VISIBLE);
        m_swipeRefresh.setRefreshing(false);

        m_message.setText(getString(R.string.chargement_determine, m_waiter.getProgress(), m_waiter.getMax()));
    }
    public void incrementProgress(int diff) {
        m_waiter.incrementProgressBy(diff);

        m_message.setText(getString(R.string.chargement_determine, m_waiter.getProgress(), m_waiter.getMax()));
    }

    public CharSequence getMessage() {
        if (m_init) {
            return m_message.getText();
        } else {
            return m_messagePreinit;
        }
    }
    public void setMessage(CharSequence msg) {
        if (m_init) {
            m_message.setText(msg);
        } else {
            m_messagePreinit = msg;
        }
    }

    public void clearLieux() {
        m_lieux.clear();
    }
    public void ajouterLieu(Lieu lieu) {
        m_lieux.add(lieu);
        incrementProgress(1);
    }
    public void ajouterLieux(Lieu... lieux) {
        m_lieux.addAll(Arrays.asList(lieux));
        incrementProgress(lieux.length);
    }
    public boolean plein() {
        return m_waiter.getMax() == m_lieux.size();
    }

    public Status getStatus() {
        return m_status;
    }
    public void setStatus(Status status) {
        m_status = status;

        // Maj vues
        if (!m_init) return;

        switch (status) {
            case CHARGEMENT:
                m_liste.setVisibility(View.GONE);
                m_message.setVisibility(View.VISIBLE);
                refreshing();

                break;

            case LISTE:
                m_liste.setVisibility(View.VISIBLE);
                m_waiter.setVisibility(View.GONE);
                m_message.setVisibility(View.GONE);
                break;

            case MESSAGE:
                m_liste.setVisibility(View.GONE);
                m_waiter.setVisibility(View.GONE);
                m_message.setVisibility(View.VISIBLE);

                break;

            case VIDE:
                m_liste.setVisibility(View.GONE);
                m_waiter.setVisibility(View.GONE);
                m_message.setVisibility(View.GONE);
                break;
        }
    }

    public void setMap(GoogleMap map) {
        m_map = map;
        ajouterMarqueurs();

        // Réactions
        m_map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Long lieu_id = (Long) marker.getTag();

                // Affichage du lieu
                Intent intent = new Intent(getActivity(), LieuActivity.class);
                intent.putExtra(LieuActivity.INTENT_LIEU, lieu_id);

                startActivity(intent);
            }
        });
    }
    public void ajouterMarqueurs() {
        // Gardiens
        if (m_map == null) return;
        if (m_groupeOuvert == -1) return;

        // Vidage
        m_map.clear();

        // Ajout des marqueurs
        /*List<Lieu> lieux = m_adapter.getLieux(m_groupeOuvert);
        for (Lieu lieu : lieux) {
            MarkerOptions opts = new MarkerOptions();
            opts.position(new LatLng(lieu.coordonnees.latitude, lieu.coordonnees.longitude));
            opts.title(lieu.nom);

            if (lieu.note != null) {
                opts.snippet(String.format(Locale.getDefault(), "Note : %.1f / 5", lieu.note));
            }

            // Ajout du marqueur
            m_map.addMarker(opts).setTag(lieu._id);
        }* /
    }*/
}
