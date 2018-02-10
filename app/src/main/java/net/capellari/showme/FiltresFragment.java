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
    private static final LieuxModel.LieuStatus[] TABLE_STATUS = {LieuxModel.LieuStatus.TOUS, LieuxModel.LieuStatus.OUVERT, LieuxModel.LieuStatus.FERME};

    // Attributs
    private CheckBox m_filtrerTypes;
    private Spinner m_lieuStatus;

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

        // Préparation checkbox
        m_filtrerTypes = view.findViewById(R.id.filtrer_types);
        m_filtrerTypes.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                m_lieuxModel.setFiltreParam(isChecked);
            }
        });

        // Préparation spinner
        m_lieuStatus = view.findViewById(R.id.lieu_status);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(), R.array.status, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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

        return view;
    }
}
