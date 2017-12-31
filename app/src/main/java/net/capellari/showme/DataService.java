package net.capellari.showme;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * Created by julien on 30/12/17.
 *
 * Gestion des communications avec la bas de données distante
 */

public class DataService extends IntentService {
    // Constructeur
    public DataService() {
        super("BDService");
    }

    // Evenement
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // cool !
    }
}
