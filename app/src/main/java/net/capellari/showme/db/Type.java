package net.capellari.showme.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by julien on 06/01/18.
 *
 * Représente un type de lieu
 */

@Entity
public class Type {
    // Champs
    @PrimaryKey
    @ColumnInfo(index = true)
    public long id;

    public String nom;

    // DAO
    @Dao
    public interface TypeDAO {
        // Accès
        @Query("select * from Type")
        List<Type> recup();

        // Edition
        @Insert
        List<Long> insert(Type... types);
    }
}