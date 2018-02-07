package net.capellari.showme;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;

/**
 * Created by julien on 06/02/18.
 *
 * Splash !
 */

public class SplashActivity extends AppCompatActivity {
    // Events
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ouverture des préférences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Lancement de MainActivity ou de BienvenueActivity
        Intent intent;

        if (preferences.getBoolean(getString(R.string.pref_bienvenue), true)) {
            intent = new Intent(this, BienvenueActivity.class);
        } else {
            intent = new Intent(this, MainActivity.class);
        }

        startActivity(intent);

        finish();
    }
}
