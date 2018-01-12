package net.capellari.showme;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.TextView;

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

    // Events
    @Nullable
    @Override
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

                m_message.setText(R.string.chargement);
                m_message.setTextAlignment(View.TEXT_ALIGNMENT_INHERIT);

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

                m_message.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

                break;

            case VIDE:
                m_liste.setVisibility(View.GONE);
                m_waiter.setVisibility(View.GONE);
                m_message.setVisibility(View.GONE);
                break;
        }
    }
}
