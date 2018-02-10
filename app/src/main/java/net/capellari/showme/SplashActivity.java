package net.capellari.showme;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;

import java.util.LinkedList;

/**
 * Created by julien on 06/02/18.
 *
 * Splash !
 */

public class SplashActivity extends AppCompatActivity {
    // Constantes
    private static final int PERMISSION_REQUEST = 1;

    // Attributs
    private SharedPreferences m_preferences;

    // Events
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ouverture des préférences
        m_preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Test permissions
        LinkedList<String> permissions = new LinkedList<>();

        // - position
        boolean gps = m_preferences.getBoolean(getString(R.string.pref_gps), true);
        String permission = gps ? Manifest.permission.ACCESS_FINE_LOCATION : Manifest.permission.ACCESS_COARSE_LOCATION;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(permission);
        }

        // Lancement sauf en cas de demande ...
        if (permissions.size() == 0) {
            lancerApp();
        } else {
            ActivityCompat.requestPermissions(this,
                    permissions.toArray(new String[permissions.size()]),
                    PERMISSION_REQUEST
            );
        }
    }

    // Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Si tout à été permis on lance l'appli !
        boolean ok = true;

        for (int result : grantResults) {
            ok = (result == PackageManager.PERMISSION_GRANTED);
            if (!ok) break;
        }

        if (ok) { // on lance l'appli ...
            lancerApp();
        } else {  // ou on se casse
            finish();
        }
    }

    // Méthodes
    private void lancerApp() {
        // Lancement de MainActivity ou de BienvenueActivity
        Intent intent;

        if (m_preferences.getBoolean(getString(R.string.pref_bienvenue), true)) {
            intent = new Intent(this, BienvenueActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
