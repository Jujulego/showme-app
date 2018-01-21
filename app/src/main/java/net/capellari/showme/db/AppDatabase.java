package net.capellari.showme.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;

/**
 * Created by julien on 06/01/18.
 *
 * Définition de la base de données
 */

@Database(entities = {
        Lieu.class,
        Type.class,
        TypeLieu.class,
        Horaire.class
}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    // DAOs
    public abstract Type.TypeDAO getTypeDAO();
    public abstract Lieu.LieuDAO getLieuDAO();
    public abstract Horaire.HoraireDAO getHoraireDAO();
}