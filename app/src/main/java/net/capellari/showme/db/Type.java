package net.capellari.showme.db;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.support.annotation.NonNull;

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
    public boolean blacklist = false;

    // Constructeur
    public Type() {}
    public Type(JSONObject obj) throws JSONException {
        _id     = obj.getInt("id");
        nom     = obj.getString("nom");
        pluriel = obj.getString("pluriel");
        blacklist = obj.getBoolean("blacklist");
    }

    // DAO
    @Dao
    public interface TypeDAO {
        // Accès
        @Query("select * from Type where not blacklist order by nom")
        List<Type> recup();

        @Query("select * from Type where not blacklist order by nom")
        LiveData<List<Type>> recupLive();

        @Query("select * from Type where _id == :id")
        Type recup(long id);

        // Edition
        @Insert void insert(Type... types);
        @Update void maj(Type... types);
    }
}