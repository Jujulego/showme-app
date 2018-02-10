package net.capellari.showme;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import net.capellari.showme.data.DiffLieu;
import net.capellari.showme.data.LieuxModel;
import net.capellari.showme.db.Lieu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by julien on 04/01/18.
 *
 * Présente les résultats
 */
public class ResultatFragment extends Fragment {
    // Constantes
    private static final String TAG = "ResultatFragment";

    // Attributs
    private TextView m_message;
    private RecyclerView m_liste;
    private SwipeRefreshLayout m_swipeRefresh;

    private boolean m_neverRefreshed = true;
    private int m_refreshMenuItem;
    private OnResultatListener m_listener;

    private LieuAdapter m_adapter = new LieuAdapter();

    private LieuxModel m_lieuxModel;
    private LiveData<List<Lieu>> m_live_lieux;

    // Events
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gestion du menu
        setHasOptionsMenu(true);

        // Récupération du model
        m_lieuxModel = ViewModelProviders.of(getActivity()).get(LieuxModel.class);

        m_live_lieux = m_lieuxModel.recupLieux();
        m_live_lieux.observe(this, new Observer<List<Lieu>>() {
            @Override
            public void onChanged(@Nullable List<Lieu> lieux) {
                m_adapter.setLieux(lieux);
            }
        });
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate !
        View view = inflater.inflate(R.layout.fragment_resultat, container, false);

        // Message
        m_message = view.findViewById(R.id.message);
        m_message.setText(getString(R.string.liste_rafraichir));
        m_message.setVisibility(View.VISIBLE);

        // Liste
        m_liste = view.findViewById(R.id.liste);
        m_liste.setAdapter(m_adapter);
        m_liste.setItemAnimator(new DefaultItemAnimator());

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
    public void onDestroyView() {
        super.onDestroyView();

        // Vidage
        m_liste = null;
        m_swipeRefresh = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Vidage
        m_listener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Arrêt réception maj
        m_live_lieux.removeObservers(this);
    }

    // Menu
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
    public void setRefreshMenuItem(@SuppressWarnings("SameParameterValue") int id) {
        m_refreshMenuItem = id;
    }
    public void setOnResultatListener(OnResultatListener listener) {
        m_listener = listener;
    }

    public void setRefreshing(boolean refresh) {
        m_neverRefreshed &= !refresh;
        m_swipeRefresh.setRefreshing(refresh);
    }
    public boolean isRefreshing() {
        return m_swipeRefresh.isRefreshing();
    }

    public void majDistances(Location location) {
        m_adapter.majDistances(location);
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
        private RatingBar m_note;
        private TextView m_status;
        private TextView m_distance;

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
            m_nom  = itemView.findViewById(R.id.nom);
            m_note = itemView.findViewById(R.id.note);
            m_status = itemView.findViewById(R.id.lieu_status);
            m_distance = itemView.findViewById(R.id.distance);
        }

        // Méthodes
        public void setLieu(Lieu lieu) {
            m_lieu = lieu;

            // Contenu !
            m_nom.setText(lieu.nom);

            if (lieu.note != null) {
                m_note.setVisibility(View.VISIBLE);
                m_note.setRating(lieu.note.floatValue());
            } else {
                m_note.setVisibility(View.GONE);
            }

            Boolean ouvert = Lieu.estOuvert(m_lieuxModel.recupHoraires(lieu._id));
            if (ouvert != null) {
                m_status.setVisibility(View.VISIBLE);
                m_status.setText(ouvert ? R.string.status_ouvert : R.string.status_ferme);
            } else {
                m_status.setVisibility(View.GONE);
            }
        }

        public void majDistance(@NonNull Location location) {
            m_distance.setText(getString(R.string.distance, Math.round(location.distanceTo(m_lieu.getLocation()))));
        }
    }
    private class LieuAdapter extends RecyclerView.Adapter<LieuViewHolder> {
        // Attributs
        private Location m_location;
        private ArrayList<Lieu> m_lieux = new ArrayList<>();
        private Set<LieuViewHolder> m_viewHolders = new HashSet<>();

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
            if (m_location != null) holder.majDistance(m_location);

            m_viewHolders.add(holder);
        }

        @Override
        public void onViewRecycled(LieuViewHolder holder) {
            m_viewHolders.remove(holder);
        }

        // Méthodes
        @Override
        public int getItemCount() {
            return m_lieux.size();
        }

        public void setLieux(List<Lieu> lieux) {
            // Calcul de la différence
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffLieu(m_lieux, lieux));
            m_lieux.clear();
            m_lieux.addAll(lieux);

            // Maj UI
            result.dispatchUpdatesTo(this);

            if (lieux.size() == 0) {
                int nb = m_lieuxModel.nbLieuxFiltres();

                // Message !
                if (nb == 0) {
                    m_message.setText(getString(m_neverRefreshed ? R.string.liste_rafraichir : R.string.liste_vide));
                } else {
                    m_message.setText(getResources().getQuantityString(R.plurals.liste_filtree, nb, nb));
                }

                m_message.setVisibility(View.VISIBLE);
            } else {
                m_message.setVisibility(View.GONE);
            }
        }

        public void majDistances(Location location) {
            m_location = location;

            // Tri
            List<Lieu> copie = new LinkedList<>(m_lieux);
            Collections.sort(copie, new TriDistance(location));

            // Application des changements
            setLieux(copie);

            // Calcul des distances
            for (LieuViewHolder viewHolder : m_viewHolders) {
                viewHolder.majDistance(location);
            }
        }
    }

    // Tri
    private class TriDistance implements Comparator<Lieu> {
        // Attributs
        private Location m_ref;

        // Constructeur
        public TriDistance(Location ref) {
            m_ref = ref;
        }

        // Méthodes
        @Override
        public int compare(Lieu l1, Lieu l2) {
            float d1 = m_ref.distanceTo(l1.getLocation());
            float d2 = m_ref.distanceTo(l2.getLocation());

            if (d1 < d2) {
                return -1;
            } else if (d1 == d2) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
