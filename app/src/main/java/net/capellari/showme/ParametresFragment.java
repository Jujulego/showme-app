package net.capellari.showme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;

/**
 * Created by julien on 01/01/18.
 *
 * Gestion des paramètres
 */

public class ParametresFragment extends PreferenceFragmentCompat {
    // Constantes
    private static final String TAG = "ParametresFragment";

    // Events
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Ajout des préférences
        addPreferencesFromResource(R.xml.preferences);

        // Réactions
        SwitchPreferenceCompat internetSwitch = (SwitchPreferenceCompat) findPreference(getString(R.string.pref_internet));
        internetSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(final Preference preference, Object newValue) {
                if (!((Boolean) newValue)) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.pref_dialog)
                            .setMessage(R.string.pref_dialog_internet)
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .create().show();
                }

                return true;
            }
        });
    }
}
