package net.capellari.showme;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

/**
 * Created by julien on 01/01/18.
 *
 * Gestion des paramètres
 */

public class ParametresFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Ajout des préférences
        addPreferencesFromResource(R.xml.preferences);
    }
}
