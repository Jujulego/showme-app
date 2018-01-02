package net.capellari.showme;

import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SeekBarPreference;

import java.util.Locale;

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

        // Customisation
        RayonPreference prefRayonMax = (RayonPreference) getPreferenceManager()
                .findPreference(getString(R.string.pref_rayon_max));

        prefRayonMax.setFactor(100);
        prefRayonMax.setMax(1000);
        prefRayonMax.setMin(100);
        prefRayonMax.setDefaultValue(100);
    }
}
