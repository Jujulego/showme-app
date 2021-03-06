package net.capellari.showme.db;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.arch.persistence.room.migration.Migration;
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
        Horaire.class,
        Historique.class
}, version = 3)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    // Attributs
    private static AppDatabase s_instance;
    private static int s_instances = 0;

    // DAOs
    public abstract Type.TypeDAO getTypeDAO();
    public abstract Lieu.LieuDAO getLieuDAO();
    public abstract Historique.HistoriqueDao getHistoriqueDao();

    // Méthodes statiques
    @NonNull
    public static synchronized AppDatabase getInstance(@NonNull Context context) {
        // (Ré)ouverture de la base
        if (s_instance == null || !s_instance.isOpen()) {
            s_instance = getNewInstance(context);
        }

        ++s_instances;
        return s_instance;
    }
    public static AppDatabase getNewInstance(@NonNull Context context) {
        return Room.databaseBuilder(
                context.getApplicationContext(), AppDatabase.class,
                context.getString(R.string.database)
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build();
    }

    // Méthodes
    @Override
    public synchronized void close() {
        --s_instances;

        if (s_instances == 0) {
            super.close();
        } else if (s_instances < 0) {
            s_instances = 0;
        }
    }

    // Migrations
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE `Type` ADD COLUMN `blacklist` INTEGER NOT NULL DEFAULT 0");
        }
    };
    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `Historique` (`_id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `query` TEXT NOT NULL, `date` INTEGER NOT NULL)");
            database.execSQL("CREATE UNIQUE INDEX `index_Historique_query` ON `Historique` (`query`)");
        }
    };
}