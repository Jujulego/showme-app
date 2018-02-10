package net.capellari.showme.db;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

/**
 * Created by julien on 02/02/18.
 *
 * Paramètres associés aux types
 */

@Entity(tableName = "Type", inheritSuperIndices = true)
public class TypeParam extends TypeBase {
    // Champs

    // DAO
    @Dao
    public interface TypeParamDAO {
        // Accès
        @Query("select * from Type order by nom")
        List<TypeParam> recup();

         // Ajout
        @Insert
        void ajouter(TypeParam... tp);

        // Suppression
        @Delete
        void enlever(TypeParam... tp);

        @Query("delete from Type")
        void vider();
    }
}
