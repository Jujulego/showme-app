package net.capellari.showme;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import net.capellari.showme.data.LieuxModel;

/**
 * Created by julien on 02/02/18.
 *
 * Gestion des filtres
 */

public class FiltresFragment extends Fragment {
    // Constantes
    private static final String TAG = "FiltresFragment";

    // Attributs
    private CheckBox m_filtrerTypes;

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

        return view;
    }
}
