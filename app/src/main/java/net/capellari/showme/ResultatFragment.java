package net.capellari.showme;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.capellari.showme.db.Lieu;

import java.util.Arrays;
import java.util.LinkedList;

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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        // Ajout du layout
        View view = inflater.inflate(R.layout.fragment_resultat, container, false);

        // Récupération des vues
        m_liste   = view.findViewById(R.id.liste);
        m_waiter  = view.findViewById(R.id.waiter);
        m_message = view.findViewById(R.id.message);

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
    public void incrementProgressMax(int diff) {
        m_waiter.setMax(m_waiter.getMax() + diff);

        m_message.setText(getString(R.string.chargement_determine, m_waiter.getProgress(), m_waiter.getMax()));
    }

    @SuppressWarnings("unused")
    public CharSequence getMessage() {
        if (m_init) {
            return m_message.getText();
        } else {
            return m_messagePreinit;
        }
    }
    @SuppressWarnings("unused")
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

    @SuppressWarnings("unused")
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
}
