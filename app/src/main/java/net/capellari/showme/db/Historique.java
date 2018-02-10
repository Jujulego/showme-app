package net.capellari.showme.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.content.ContentValues;
import android.support.annotation.NonNull;

import java.util.Date;
import java.util.List;

/**
 * Created by julien on 10/02/18.
 *
 * Historique des recherches
 */
@Entity(indices = {
        @Index(value = "query", unique = true)
})
public class Historique {
    // Champs
    @PrimaryKey(autoGenerate = true)
    public long _id = 0;

    @NonNull
    public String query = "";

    @NonNull
    public Date date = new Date();

    // Dao
    @Dao
    public interface HistoriqueDao {
        // Accès
        @Query("select * from Historique where `query` like :query order by date desc")
        List<Historique> suggestions(String query);

        // Modification
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        long ajouter(Historique historique);

        @Query("delete from Historique")
        int vider();
    }
}
