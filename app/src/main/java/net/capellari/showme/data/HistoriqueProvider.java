package net.capellari.showme.data;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by julien on 27/01/18.
 *
 * Historique des recherches
 */

public class HistoriqueProvider extends SearchRecentSuggestionsProvider {
    // Constantes
    public static final String AUTORITE = "net.capellari.showme.data.HistoriqueProvider";
    public static final int MODE        = DATABASE_MODE_QUERIES;

    // Constructeur
    public HistoriqueProvider() {
        setupSuggestions(AUTORITE, MODE);
    }
}
