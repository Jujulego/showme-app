package net.capellari.showme.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.List;

/**
 * Created by julien on 06/01/18.
 *
 * Représente un type de lieu
 */

@Entity
public class Type {
    // Champs
    @PrimaryKey @ColumnInfo(index = true)
    public long id;
    public Integer ordre;

    @NonNull public String nom = "";
    @NonNull public String pluriel = "";

    // Constructeur
    public Type() {}
    public Type(JSONObject obj) throws JSONException {
        id      = obj.getInt("id");
        nom     = obj.getString("nom");
        pluriel = obj.getString("pluriel");
    }

    // DAO
    @Dao
    public interface TypeDAO {
        // Accès
        @Query("select * from Type where ordre not null order by ordre")
        LiveData<List<Type>> recup();

        @Query("select * from Type where ordre is null order by nom")
        LiveData<List<Type>> recupNonOrdonnes();

        @Query("select * from Type where id == :id")
        Type recup(long id);

        @Query("select Type.id,Type.nom,Type.pluriel,count(distinct TypeLieu.lieu_id) as nb_lieux from TypeLieu join Type on type_id = Type.id where lieu_id in (:lieux) group by Type.id order by Type.ordre")
        TypeNb[] recupTypes(List<Long> lieux);

        // Edition
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        List<Long> insert(Type... types);

        @Update void maj(Type type);
    }

    // Pojo retour
    public static class TypeNb {
        public long id;
        public String nom;
        public String pluriel;
        public int nb_lieux;
    }
}