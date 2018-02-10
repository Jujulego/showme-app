package net.capellari.showme;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Spinner;

import net.capellari.showme.data.LieuxModel;

/**
 * Created by julien on 02/02/18.
 *
 * Gestion des filtres
 */

public class FiltresFragment extends Fragment {
    // Constantes
    private static final String TAG = "FiltresFragment";

    private static final LieuxModel.LieuStatus[] TABLE_STATUS = {
            LieuxModel.LieuStatus.TOUS, LieuxModel.LieuStatus.OUVERT, LieuxModel.LieuStatus.FERME
    };
    private static final LieuxModel.Comparateur[] TABLE_MODES = {
            LieuxModel.Comparateur.TOUT,
            LieuxModel.Comparateur.MINIMUM,
            LieuxModel.Comparateur.EGAL,
            LieuxModel.Comparateur.MAXIMUM
    };

    // Attributs
    private CheckBox m_filtrerTypes;
    private Spinner m_lieuStatus;
    private Spinner m_noteMode;
    private ImageButton m_moinsBouton;
    private RatingBar m_ratingbar;
    private ImageButton m_plusBouton;

    private LieuxModel m_lieuxModel;

    // Events
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Récupération du model
        m_lieuxModel = ViewModelProviders.of(getActivity()).get(LieuxModel.class);
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_filtres, container, false);

        // Préparation
        setupCheckBox(view);
        setupSpinner(view);
        setupNote(view);

        return view;
    }

    // Méthodes
    private void setupCheckBox(View view) {
        // Préparation checkbox
        m_filtrerTypes = view.findViewById(R.id.filtrer_types);
        m_filtrerTypes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                m_lieuxModel.setFiltreParam(isChecked);
            }
        });
    }
    private void setupSpinner(View view) {
        // Préparation spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.status,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        m_lieuStatus = view.findViewById(R.id.lieu_status);
        m_lieuStatus.setAdapter(adapter);
        m_lieuStatus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                m_lieuxModel.setLieuStatus(TABLE_STATUS[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                m_lieuxModel.setLieuStatus(LieuxModel.LieuStatus.TOUS);
            }
        });
    }

    private void setupNote(View view) {
        // Préparation note
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(), R.array.node_mode,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // mode
        m_noteMode = view.findViewById(R.id.note_mode);
        m_noteMode.setAdapter(adapter);
        m_noteMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                m_lieuxModel.setNoteComparateur(TABLE_MODES[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                m_lieuxModel.setNoteComparateur(LieuxModel.Comparateur.TOUT);
            }
        });

        // note
        m_ratingbar = view.findViewById(R.id.note);
        m_plusBouton = view.findViewById(R.id.plus_bouton);
        m_moinsBouton = view.findViewById(R.id.moins_bouton);
        chgNote(m_lieuxModel.getNote(), true);

        m_ratingbar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                if (fromUser) {
                    chgNote(rating, false);
                }
            }
        });
        m_plusBouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chgNote(m_lieuxModel.getNote() + 0.5f, true);
            }
        });
        m_moinsBouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chgNote(m_lieuxModel.getNote() - 0.5f, true);
            }
        });
    }
    private void chgNote(float note, boolean ratingbar) {
        // Affichage
        if (ratingbar) m_ratingbar.setRating(note);
        m_lieuxModel.setNote(note);

        // État des boutons
        if (note == 0) {
            m_plusBouton.setEnabled(true);
            m_moinsBouton.setEnabled(false);
        } else if (note == 5) {
            m_plusBouton.setEnabled(false);
            m_moinsBouton.setEnabled(true);
        } else {
            m_plusBouton.setEnabled(true);
            m_moinsBouton.setEnabled(true);
        }
    }
}
