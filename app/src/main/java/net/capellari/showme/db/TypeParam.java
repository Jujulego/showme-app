package net.capellari.showme.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.support.annotation.NonNull;

import java.util.List;

/**
 * Created by julien on 02/02/18.
 *
 * Parametres associés aux types
 */

@Entity(tableName = "Type")
public class TypeParam {
    // Champs
    @PrimaryKey
    @ColumnInfo(index = true)
    public long _id;

    @NonNull
    public String nom = "";

    // DAO
    @Dao
    public interface TypeParamDAO {
        // Accès
        @Query("select * from Type order by nom")
        List<TypeParam> recup();

        @Query("select * from Type order by nom")
        LiveData<List<TypeParam>> recupLive();

         // Ajout
        @Insert
        void ajouter(TypeParam... tp);

        @Delete
        void enlever(TypeParam... tp);
    }
}
