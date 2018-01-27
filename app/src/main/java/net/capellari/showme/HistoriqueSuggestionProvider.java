package net.capellari.showme;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by julien on 27/01/18.
 *
 * Historique des recherches
 */

public class HistoriqueSuggestionProvider extends SearchRecentSuggestionsProvider {
    // Constantes
    public final static String AUTORITE = "net.capellari.showme.HistoriqueSuggestionProvider";
    public final static int MODE        = DATABASE_MODE_QUERIES;

    // Constructeur
    public HistoriqueSuggestionProvider() {
        setupSuggestions(AUTORITE, MODE);
    }
}
