package net.capellari.showme.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RawRes;

import net.capellari.showme.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by julien on 06/01/18.
 *
 * Représente un type de lieu
 */

@Entity(inheritSuperIndices = true)
public class Type extends TypeBase {
    // Champs
    @NonNull public String pluriel = "";

    // Constructeur
    public Type() {}
    public Type(JSONObject obj) throws JSONException {
        _id     = obj.getInt("id");
        nom     = obj.getString("nom");
        pluriel = obj.getString("pluriel");
    }

    // DAO
    @Dao
    public interface TypeDAO {
        // Accès
        @Query("select * from Type order by nom")
        LiveData<List<Type>> recup();

        @Query("select * from Type where _id == :id")
        Type recup(long id);

        @Query("select Type._id,Type.nom,Type.pluriel,count(distinct TypeLieu.lieu_id) as nb_lieux from TypeLieu join Type on type_id = Type._id where lieu_id in (:lieux) group by Type._id order by Type.nom")
        LiveData<TypeNb[]> recupTypes(List<Long> lieux);

        // Edition
        @Insert void insert(Type... types);
        @Update void maj(Type... types);
    }

    // Pojo retour
    public static class TypeNb {
        public long _id;
        public String nom;
        public String pluriel;
        public int nb_lieux;
    }
}