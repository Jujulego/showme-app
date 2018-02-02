package net.capellari.showme.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.support.annotation.NonNull;

import net.capellari.showme.R;

/**
 * Created by julien on 02/02/18.
 *
 * Base regroupant les paramètres (notamment autour des types !)
 */

@Database(entities = {
        TypeParam.class
}, version = 1, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class ParamDatabase extends RoomDatabase {
    // Attributs
    private static ParamDatabase m_instance;

    // DAOs
    public abstract TypeParam.TypeParamDAO getTypeDAO();

    // Méthodes
    @NonNull
    public static synchronized ParamDatabase getInstance(@NonNull Context context) {
        // (Re)ouverture de la base
        if (m_instance == null || !m_instance.isOpen()) {
            m_instance = Room.databaseBuilder(
                    context.getApplicationContext(), ParamDatabase.class,
                    context.getString(R.string.param_database)
            ).build();
        }

        return m_instance;
    }
}
