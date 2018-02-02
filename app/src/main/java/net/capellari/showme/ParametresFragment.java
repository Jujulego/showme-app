package net.capellari.showme;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import net.capellari.showme.db.AppDatabase;

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

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        // Récupération du dialog
        DialogFragment fragment = null;
        if (preference instanceof NettoyagePreference) {
            if (preference.getKey().equals(getString(R.string.pref_cache))) {
                // Cache
                fragment = NettoyagePreferenceDialog.newInstance(
                        preference.getKey(), new NettoyagePreferenceDialog.OnDialogClosed() {
                            @Override
                            public void onDialogClosed(boolean positiveResult) {
                                if (positiveResult) {
                                    // Nettoyage !
                                    new ViderTask().execute();
                                }
                            }
                        }
                );
            } else if (preference.getKey().equals(getString(R.string.pref_historique))) {
                // Historique
                fragment = NettoyagePreferenceDialog.newInstance(
                        preference.getKey(), new NettoyagePreferenceDialog.OnDialogClosed() {
                            @Override
                            public void onDialogClosed(boolean positiveResult) {
                                if (positiveResult) {
                                    // Nettoyage !
                                    SearchRecentSuggestions suggestions = new SearchRecentSuggestions(
                                            getContext(),
                                            HistoriqueProvider.AUTORITE, HistoriqueProvider.MODE
                                    );

                                    suggestions.clearHistory();
                                }
                            }
                        }
                );
            }
        }

        if (fragment != null) {
            // Dialog custom
            fragment.setTargetFragment(this, 0);
            fragment.show(this.getFragmentManager(),
                    "android.support.v7.preference.PreferenceFragment.DIALOG");
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    // Taches
    private class ViderTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            // Vidage ... :(
            AppDatabase.getInstance(getContext()).getLieuDAO().viderLieux();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            View view = getView();

            if (view != null) {
                Snackbar snackbar = Snackbar.make(getView(), R.string.pref_snackbar_cache, Snackbar.LENGTH_LONG);

                // Chg de couleur (noir par défaut => blanc)
                TextView texte = (TextView) snackbar.getView();
                texte.setTextColor(ContextCompat.getColor(snackbar.getContext(), android.R.color.white));

                snackbar.show();
            } else {
                Log.i(TAG, getString(R.string.pref_snackbar_cache));
            }
        }
    }
}
