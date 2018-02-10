package net.capellari.showme;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;

import net.capellari.showme.data.HistoriqueProvider;
import net.capellari.showme.data.RequeteManager;
import net.capellari.showme.db.AppDatabase;

/**
 * Created by julien on 01/01/18.
 *
 * Gestion des paramètres
 */

public class ParametresFragment extends PreferenceFragmentCompat {
    // Constantes
    private static final String TAG = "ParametresFragment";

    private static final int REQUEST_LOCATION_PERMISSION = 1;

    // Attributs
    private SharedPreferences m_preferences;

    // Events
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ouverture des préférences
        m_preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
    }

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

        SwitchPreferenceCompat gpsSwitch = (SwitchPreferenceCompat) findPreference(getString(R.string.pref_gps));
        gpsSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean gps = (Boolean) newValue;
                String permission = gps ? Manifest.permission.ACCESS_FINE_LOCATION : Manifest.permission.ACCESS_COARSE_LOCATION;

                if (ContextCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[] {permission}, REQUEST_LOCATION_PERMISSION);

                    return false;
                } else {
                    return true;
                }
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

    // Permission
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    boolean gps = (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION));
                    m_preferences.edit().putBoolean(getString(R.string.pref_gps), gps).apply();
                }

                break;
        }
    }

    // Taches
    private class ViderTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            // Vidage ... :(
            AppDatabase db = AppDatabase.getInstance(getContext());
            db.getLieuDAO().viderLieux();
            db.close();

            RequeteManager.getInstance(getContext()).getRequestQueue().getCache().clear();

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            View view = getView();

            if (view != null) {
                // Préparation texte
                String texte = getString(R.string.pref_snackbar_cache);
                SpannableStringBuilder ssb = new SpannableStringBuilder();
                ssb.append(texte);
                ssb.setSpan(new ForegroundColorSpan(Color.WHITE), 0,
                        texte.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                // Snack !
                Snackbar snackbar = Snackbar.make(getView(), ssb, Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                Log.i(TAG, getString(R.string.pref_snackbar_cache));
            }
        }
    }
}
