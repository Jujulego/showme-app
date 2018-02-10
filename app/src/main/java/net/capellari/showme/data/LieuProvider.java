package net.capellari.showme.data;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import net.capellari.showme.R;
import net.capellari.showme.db.AppDatabase;
import net.capellari.showme.db.Historique;
import net.capellari.showme.db.Lieu;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by julien on 10/02/18.
 *
 * Propose des noms de lieux connus
 */

public class LieuProvider extends ContentProvider {
    // Constantes
    private static final String TAG = "LieuProvider";

    private static final UriMatcher s_uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int SUGGESTION_URI = 1;

    private static final int HISTORIQUE_URI = 10;
    private static final int HISTORIQUE_ENTREE_URI = 11;

    // Initialisation uriMatcher
    static {
        // suggestions
        s_uriMatcher.addURI(LieuxSuggestions.AUTORITE, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SUGGESTION_URI);

        // historique
        s_uriMatcher.addURI(LieuxSuggestions.AUTORITE, LieuxSuggestions.Historique.URI, HISTORIQUE_URI);
        s_uriMatcher.addURI(LieuxSuggestions.AUTORITE, LieuxSuggestions.Historique.URI + "/#", HISTORIQUE_ENTREE_URI);
    }

    // Events
    @Override
    public boolean onCreate() {
        return false;
    }

    // Méthodes
    // - interface
    @Nullable @Override
    public String getType(@NonNull Uri uri) {
        if (s_uriMatcher.match(uri) == SUGGESTION_URI) {
            return SearchManager.SUGGEST_MIME_TYPE;
        }

        return null;
    }

    @Nullable @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        switch (s_uriMatcher.match(uri)) {
            case HISTORIQUE_URI:
                return ajouterEntree(uri, values);

            default:
                return null;
        }
    }

    @Nullable @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Gardien
        switch (s_uriMatcher.match(uri)) {
            case SUGGESTION_URI:
                return getSuggestions(uri);

            default:
                return null;
        }
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        switch (s_uriMatcher.match(uri)) {
            case HISTORIQUE_URI:
                return viderHistorique();

            default:
                return 0;
        }
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    // - traitement interne
    private Cursor getSuggestions(@NonNull Uri uri) {
        // Récupération de la recherche
        String query = '%' + Uri.decode(uri.getLastPathSegment()) + '%';

        // Récupération des suggestions
        AppDatabase db = AppDatabase.getInstance(getContext());
        List<Historique> historique = db.getHistoriqueDao().suggestions(query);
        List<Lieu> suggestions = db.getLieuDAO().suggestions(query);
        db.close();

        // Préparation du curseur
        MatrixCursor cursor = new MatrixCursor(new String[]{
                "_id",
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_QUERY,
                SearchManager.SUGGEST_COLUMN_ICON_1
        });

        // Ajout de l'historique
        Set<String> queries = new HashSet<>();
        int id = 0;

        for (Historique entree : historique) {
            // Ajout au curseur
            cursor.addRow(new Object[]{
                    id++,
                    entree.query,
                    entree.query,
                    R.drawable.historique
            });

            // Ajout à la liste
            queries.add(entree.query);
        }

        // Ajout des lieux, si le texte n'est pas déjà proposé
        for (Lieu lieu : suggestions) {
            // Pas déjà ajouté
            if (queries.contains(lieu.nom)) {
                continue;
            }

            // Ajout au curseur
            cursor.addRow(new Object[] {
                    id++,
                    lieu.nom,
                    lieu.nom,
                    R.drawable.suggestion
            });
        }

        return cursor;
    }

    @Nullable
    private Uri ajouterEntree(@NonNull Uri uri, @Nullable ContentValues values) {
        // Gardien
        if (values == null) return null;

        // Contruction de l'objet
        Historique entree = new Historique();
        entree.query = values.getAsString(LieuxSuggestions.Historique.QUERY);

        // Ajout dans la base
        AppDatabase db = AppDatabase.getInstance(getContext());
        long id = db.getHistoriqueDao().ajouter(entree);
        db.close();

        return ContentUris.withAppendedId(uri, id);
    }
    private int viderHistorique() {
        AppDatabase db = AppDatabase.getInstance(getContext());
        int nb = db.getHistoriqueDao().vider();
        db.close();

        return nb;
    }
}
