package net.capellari.showme.data;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

/**
 * Created by julien on 10/02/18.
 *
 * Gestion de l'accès aux suggestions
 */

public class LieuxSuggestions {
    // Constantes
    public static final String AUTORITE = "net.capellari.showme.data.LieuProvider";

    // - historique
    public static final class Historique {
        public static final String URI   = "historique";
        public static final String QUERY = "query";

        // Méthodes
        public static Uri getUri() {
            return Uri.parse("content://" + AUTORITE + "/" + URI);
        }
    }

    // Méthodes
    public static void ajouterEntree(@NonNull final Context context, final String query) {
        new Thread("ajout entree historique") {
            @Override
            public void run() {
                // Préparation des valeurs
                ContentValues values = new ContentValues();
                values.put(Historique.QUERY, query);

                // Ajout !
                context.getContentResolver().insert(Historique.getUri(), values);
            }
        }.start();
    }
    public static void viderHistorique(@NonNull final Context context) {
        new Thread("vider historique") {
            @Override
            public void run() {
                context.getContentResolver().delete(Historique.getUri(), null, null);
            }
        }.start();
    }
}
