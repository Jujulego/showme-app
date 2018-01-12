package net.capellari.showme.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.NonNull;

import java.util.List;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by julien on 06/01/18.
 *
 * Représente une plage horaire ou le lieu associé est ouvert
 */

@Entity(foreignKeys = {
        @ForeignKey(
                entity = Lieu.class,
                parentColumns = "id",
                childColumns = "lieu_id",
                onDelete = CASCADE
        )
})
public class Horaire {
    // Champs
    @PrimaryKey @ColumnInfo(index = true)
    public long id;

    @ColumnInfo(index = true)
    public long lieu_id = -1L;

    public int jour = 0;
    public int ouverture = 0;
    public int fermeture = 0;

    // DAO
    @Dao
    public interface HoraireDAO {
        // Accès
        @Query("select * from Horaire")
        List<Horaire> recup();

        @Query("select * from Horaire where lieu_id == :lieu")
        List<Horaire> recupLieu(long lieu);

        // Edition
        @Insert
        List<Long> insert(Horaire... horaires);

        @Update
        int update(Horaire... horaires);

        @Delete
        int delete(Horaire... horaires);
    }
}
