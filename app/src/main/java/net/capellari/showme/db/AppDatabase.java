package net.capellari.showme.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;

import net.capellari.showme.R;

/**
 * Created by julien on 06/01/18.
 *
 * Définition de la base de données générale
 */

@Database(entities = {
        Lieu.class,
        Type.class,
        TypeLieu.class,
        Horaire.class
}, version = 1)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    // Attributs
    private static AppDatabase m_instance;

    // DAOs
    public abstract Type.TypeDAO getTypeDAO();
    public abstract Lieu.LieuDAO getLieuDAO();

    // Méthodes
    @NonNull
    public static synchronized AppDatabase getInstance(@NonNull Context context) {
        // (Re)ouverture de la base
        if (m_instance == null || !m_instance.isOpen()) {
            m_instance = Room.databaseBuilder(
                    context.getApplicationContext(), AppDatabase.class,
                    context.getString(R.string.database)
            ).build();
        }

        return m_instance;
    }
}