package net.capellari.showme;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import net.capellari.showme.data.StringUtils;
import net.capellari.showme.db.Horaire;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by julien on 06/02/18.
 *
 * Affichage des horaires
 */

public class HoraireFragment extends Fragment {
    // Constantes
    static final int[] JOURS = {Calendar.SUNDAY, Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY};

    // Attributs
    private TextView m_aujourdhui;
    private ImageButton m_bouton;
    private GridLayout m_joursLayout;
    private TextView[] m_jours = new TextView[7];
    private HoraireView[] m_horaires = new HoraireView[7];

    private List<Horaire> m_liste;

    // Events
    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_horaires, container, false);

        // Textes
        m_aujourdhui = view.findViewById(R.id.aujourdhui);

        m_joursLayout = view.findViewById(R.id.jours);
        m_jours[0] = view.findViewById(R.id.jour1);
        m_jours[1] = view.findViewById(R.id.jour2);
        m_jours[2] = view.findViewById(R.id.jour3);
        m_jours[3] = view.findViewById(R.id.jour4);
        m_jours[4] = view.findViewById(R.id.jour5);
        m_jours[5] = view.findViewById(R.id.jour6);
        m_jours[6] = view.findViewById(R.id.jour7);

        m_horaires[0] = new HoraireView(view.findViewById(R.id.jour1_horaire));
        m_horaires[1] = new HoraireView(view.findViewById(R.id.jour2_horaire));
        m_horaires[2] = new HoraireView(view.findViewById(R.id.jour3_horaire));
        m_horaires[3] = new HoraireView(view.findViewById(R.id.jour4_horaire));
        m_horaires[4] = new HoraireView(view.findViewById(R.id.jour5_horaire));
        m_horaires[5] = new HoraireView(view.findViewById(R.id.jour6_horaire));
        m_horaires[6] = new HoraireView(view.findViewById(R.id.jour7_horaire));

        // Initialisation
        DateFormatSymbols dateSymbols = DateFormatSymbols.getInstance();
        Calendar ajd = Calendar.getInstance();

        for (int jour : JOURS) {
            getJourView(ajd, jour).setText(StringUtils.toTitle(dateSymbols.getWeekdays()[jour]));
        }

        // Gestion du bouton
        m_bouton = view.findViewById(R.id.bouton);
        m_bouton.setEnabled(false);

        m_bouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_bouton.isSelected()) {
                    m_bouton.setSelected(false);
                    m_joursLayout.setVisibility(View.GONE);
                } else {
                    m_bouton.setSelected(true);
                    m_joursLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Vidage
        m_aujourdhui = null;
        m_joursLayout = null;
        m_bouton = null;
    }

    // Méthodes
    private boolean vide() {
        return m_liste == null || m_liste.size() == 0;
    }
    private TextView getJourView(Calendar calendar, int jour) {
        // petit calcul !
        jour -= calendar.getFirstDayOfWeek(); // -> le 1er jour de la semaine == 0
        if (jour < 0) jour += 7; // modulo 7 !

        return m_jours[jour];
    }
    private HoraireView getHoraireView(Calendar calendar, int jour) {
        // petit calcul !
        jour -= calendar.getFirstDayOfWeek(); // -> le 1er jour de la semaine == 0
        if (jour < 0) jour += 7; // modulo 7 !

        return m_horaires[jour];
    }
    private void majUI() {
        if (vide()) {
            // On cache !
            m_aujourdhui.setEnabled(false);
            m_aujourdhui.setText(R.string.inconnus);

            m_bouton.setEnabled(false);
            m_bouton.setSelected(false);

            m_joursLayout.setVisibility(View.GONE);
        } else {
            // On active !
            m_aujourdhui.setEnabled(true);
            m_aujourdhui.setText(estOuvert() ? R.string.ouvert : R.string.ferme);

            m_bouton.setEnabled(true);

            // Remplissage !
            Calendar ajd = Calendar.getInstance();

            for (Horaire horaire : m_liste) {
                getHoraireView(ajd, horaire.getCalendarDay()).ajouter(horaire);
            }
        }
    }

    public void setHoraires(List<Horaire> horaires) {
        m_liste = horaires;
        majUI();
    }
    public Boolean estOuvert() {
        // Pas d'horaires => null
        if (vide()) return null;

        // Test !
        Calendar ajd = Calendar.getInstance();
        boolean ouvert = false;

        for (Horaire horaire : m_liste) {
            // Récupération des horaires
            Calendar ouv = horaire.getOuverture(ajd);
            Calendar fer = horaire.getFermeture(ajd);

            // Test
            if (ouv.before(ajd) && ajd.before(fer)) {
                ouvert = true;
                break;
            }
        }

        return ouvert;
    }

    // Classe
    private class HoraireView {
        // Attributs
        private TextView m_horaire;
        private TextView m_horaire1;
        private TextView m_horaire2;

        private List<Horaire> m_horaires = new LinkedList<>();

        // Constructeur
        public HoraireView(View view) {
            // Récupération des vues
            m_horaire  = view.findViewById(R.id.horaire);
            m_horaire1 = view.findViewById(R.id.horaire1);
            m_horaire2 = view.findViewById(R.id.horaire2);
        }

        // Méthodes
        public void ajouter(Horaire horaire) {
            // Ajout à la liste
            m_horaires.add(horaire);
            Collections.sort(m_horaires);

            // Affichage
            if (m_horaires.size() == 1) {
                m_horaire.setText(horaire.toString());
            } else {
                m_horaire1.setText(m_horaires.get(0).toString());
                m_horaire2.setText(m_horaires.get(1).toString());
            }
        }
    }
}