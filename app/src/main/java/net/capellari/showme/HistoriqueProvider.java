package net.capellari.showme;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by julien on 27/01/18.
 *
 * Historique des recherches
 */

public class HistoriqueProvider extends SearchRecentSuggestionsProvider {
    // Constantes
    public final static String AUTORITE = "net.capellari.showme.historique";
    public final static int MODE        = DATABASE_MODE_QUERIES;

    // Constructeur
    public HistoriqueProvider() {
        setupSuggestions(AUTORITE, MODE);
    }
}
