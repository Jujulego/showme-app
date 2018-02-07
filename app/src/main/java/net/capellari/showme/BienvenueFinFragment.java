package net.capellari.showme;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import net.capellari.showme.MainActivity;
import net.capellari.showme.R;

/**
 * Created by julien on 07/02/18.
 *
 * Fin de l'activité bienvenue !
 */

public class BienvenueFinFragment extends Fragment {
    // Attributs
    private Button m_bouton;

    private SharedPreferences m_preferences;

    // Events
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ouverture des préférences
        m_preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bienvenue_fin, container, false);

        // Gestion du bouton
        m_bouton = view.findViewById(R.id.bouton);
        m_bouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = getActivity();

                if (activity != null) {
                    // Modification préférence
                    SharedPreferences.Editor editor = m_preferences.edit();
                    editor.putBoolean(getString(R.string.pref_bienvenue), false);
                    editor.apply();

                    // Lancement de MainActivity
                    Intent intent = new Intent(activity, MainActivity.class);
                    startActivity(intent);

                    activity.finish();
                }
            }
        });

        return view;
    }
}
