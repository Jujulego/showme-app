package net.capellari.showme.data;

import android.app.SearchManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.media.Rating;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import net.capellari.showme.R;
import net.capellari.showme.db.AppDatabase;
import net.capellari.showme.db.Lieu;

import java.util.List;

/**
 * Created by julien on 10/02/18.
 *
 * Propose des noms de lieux connus
 */

public class LieuProvider extends ContentProvider {
    // Constantes
    public  static final String AUTORITE = "net.capellari.showme.data.LieuProvider";
    private static final String TAG      = "LieuProvider";

    private static final UriMatcher s_uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int SUGGESTION_URI = 1;

    // Initialisation uriMatcher
    static {
        // suggestions
        s_uriMatcher.addURI(AUTORITE, SearchManager.SUGGEST_URI_PATH_QUERY + "/*", SUGGESTION_URI);
    }

    // Méthodes
    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable @Override
    public String getType(@NonNull Uri uri) {
        if (s_uriMatcher.match(uri) == SUGGESTION_URI) {
            return SearchManager.SUGGEST_MIME_TYPE;
        }

        return null;
    }

    @Nullable @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Gardien
        if (s_uriMatcher.match(uri) != SUGGESTION_URI) {
            return null;
        }

        // Récupération de la recherche
        String query = '%' + Uri.decode(uri.getLastPathSegment()) + '%';

        // Récupération des suggestions
        AppDatabase db = AppDatabase.getInstance(getContext());
        List<Lieu> suggestions = db.getLieuDAO().suggestions(query);
        db.close();

        // Préparation du curseur
        MatrixCursor cursor = new MatrixCursor(new String[] {
                "_id",
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_QUERY,
                SearchManager.SUGGEST_COLUMN_ICON_1,
                SearchManager.SUGGEST_COLUMN_RATING_STYLE, SearchManager.SUGGEST_COLUMN_RATING_SCORE
        });

        for (Lieu lieu : suggestions) {
            cursor.addRow(new Object[] {
                    lieu._id,
                    lieu.nom,
                    lieu.nom,
                    R.drawable.suggestion,
                    Rating.RATING_5_STARS, lieu.note
            });
        }

        return cursor;
    }

    @Nullable @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
