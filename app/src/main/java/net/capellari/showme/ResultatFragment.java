package net.capellari.showme;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.capellari.showme.db.Lieu;
import net.capellari.showme.db.Type;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by julien on 04/01/18.
 *
 * Présente les résultats
 */

public class ResultatFragment extends Fragment {
    // Enumération
    public enum Status {
        VIDE, CHARGEMENT, MESSAGE, LISTE
    }

    // Attributs
    private TextView m_message;
    private ExpandableListView m_liste;
    private ProgressBar m_waiter;

    private boolean m_init = false;
    private CharSequence m_messagePreinit = "";
    private Status m_status = Status.VIDE;

    private LinkedList<Lieu> m_lieux = new LinkedList<>();

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
        m_liste.setAdapter(new LieuxAdapter());
        m_liste.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView expandableListView, View view, int groupPosition, int childPosition, long id) {
                Intent intent = new Intent(getActivity(), LieuActivity.class);
                intent.putExtra(LieuActivity.INTENT_LIEU, id);

                startActivity(intent);
                return true;
            }
        });

        // Mise à l'etat
        m_init = true;
        setStatus(m_status);
        m_message.setText(m_messagePreinit);

        return view;
    }

    // Méthodes
    public void indetermine() {
        m_waiter.setIndeterminate(true);
        m_message.setText(R.string.chargement_indetermine);
    }
    public void initProgress(int max) {
        m_waiter.setMax(max);
        m_waiter.setProgress(0);
        m_waiter.setIndeterminate(false);

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

    @SuppressLint("StaticFieldLeak")
    public void majListe(final Type.TypeDAO typeDAO) {
        new AsyncTask<Void,Void,Type.TypeNb[]>() {
            @Override
            protected Type.TypeNb[] doInBackground(Void... voids) {
                LinkedList<Long> ids = new LinkedList<>();
                for (Lieu lieu : m_lieux) {
                    ids.add(lieu.id);
                }

                return typeDAO.recupTypes(ids);
            }

            @Override
            protected void onPostExecute(Type.TypeNb[] types) {
                m_liste.setAdapter(new LieuxAdapter(m_lieux, types));
            }
        }.execute();
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
                m_waiter.setVisibility(View.VISIBLE);
                m_message.setVisibility(View.VISIBLE);

                indetermine();

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

    // Classe
    class LieuxAdapter extends BaseExpandableListAdapter {
        // Attributs
        private List<Lieu> m_lieux;
        private Type.TypeNb[] m_types;

        // Constructeurs
        public LieuxAdapter() {
            m_lieux = new LinkedList<>();
            m_types = new Type.TypeNb[0];
        }
        public LieuxAdapter(List<Lieu> lieux, Type.TypeNb[] types) {
            m_lieux = lieux;
            m_types = types;
        }

        // Méthodes
        @Override public int getGroupCount() {
            return m_types.length;
        }
        @Override public int getChildrenCount(int groupPosition) {
            return m_types[groupPosition].nb_lieux;
        }

        @Override public Object getGroup(int groupPosition) {
            return m_types[groupPosition];
        }
        @Override public Object getChild(int groupPosition, int childPosition) {
            long typeId = getGroupId(groupPosition);

            for (Lieu lieu : m_lieux) {
                // Bon type ?
                boolean ok = false;
                for (Type type : lieu.types) {
                    if (type.id == typeId) {
                        ok = true;
                        break;
                    }
                }

                // Bon lieu ?
                if (ok) {
                    if (childPosition == 0) {
                        return lieu;
                    }

                    childPosition--;
                }
            }

            return null;
        }

        @Override public boolean hasStableIds() {
            return true;
        }
        @Override public long getGroupId(int groupPosition) {
            return m_types[groupPosition].id;
        }
        @Override public long getChildId(int groupPosition, int childPosition) {
            return ((Lieu) getChild(groupPosition, childPosition)).id;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
            if (view == null) view = getLayoutInflater().inflate(R.layout.categorie_type, parent, false);

            // Récupération du nom
            TextView  nom    = view.findViewById(R.id.nom);
            ImageView expand = view.findViewById(R.id.expand_icone);

            // Remplissage
            Type.TypeNb type = (Type.TypeNb) getGroup(groupPosition);

            if (type.nb_lieux > 1) {
                nom.setText(type.pluriel);
            } else {
                nom.setText(type.nom);
            }

            // Expand !
            expand.setSelected(isExpanded);

            return view;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
            if (view == null) view = getLayoutInflater().inflate(R.layout.item_lieu, parent, false);

            // Remplissage
            TextView nom = view.findViewById(R.id.nom);
            Lieu lieu = (Lieu) getChild(groupPosition, childPosition);

            if (lieu != null) {
                nom.setText(lieu.nom);
            }

            return view;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}
