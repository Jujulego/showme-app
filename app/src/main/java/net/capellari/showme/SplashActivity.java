package net.capellari.showme;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

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

        // Lancement de MainActivity
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        finish();
    }
}
