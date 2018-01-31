package net.capellari.showme.db;

import android.arch.lifecycle.ComputableLiveData;
import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import net.capellari.showme.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by julien on 06/01/18.
 *
 * Représente un lieu
 */

@Entity
public class Lieu {
    // Champs
    @PrimaryKey @ColumnInfo(index = true)
    public long _id;

    @NonNull
    public Date date = new Date(); // Date d'ajout, permet le nettoyage

    @NonNull
    public String nom = "";

    public Double note;
    public Integer prix;
    public String telephone;
    public URL site;
    public URL photo;

    @Embedded @NonNull
    public Adresse adresse = new Adresse();

    @Embedded @NonNull
    public GeoPoint coordonnees = new GeoPoint();

    // Constructeur
    public Lieu() {}
    public Lieu(Context context, JSONObject obj) throws JSONException {
        // Obligatoire
        _id = obj.getInt("id");
        nom = obj.getString("nom");

        // Position
        coordonnees.latitude  = obj.getJSONObject("pos").getDouble("lat");
        coordonnees.longitude = obj.getJSONObject("pos").getDouble("lng");

        // Adresse
        adresse.numero      = obj.getJSONObject("adresse").getString("numero");
        adresse.rue         = obj.getJSONObject("adresse").getString("route");
        adresse.codePostal  = obj.getJSONObject("adresse").getString("codepostal");
        adresse.ville       = obj.getJSONObject("adresse").getString("ville");
        adresse.departement = obj.getJSONObject("adresse").getString("departement");
        adresse.region      = obj.getJSONObject("adresse").getString("region");
        adresse.pays        = obj.getJSONObject("adresse").getString("pays");

        // Optionnel
        note      = obj.has("note")      ? obj.getDouble("note")      : null;
        prix      = obj.has("prix")      ? obj.getInt(   "prix")      : null;
        telephone = obj.has("telephone") ? obj.getString("telephone") : null;

        // URLs
        try {
            site = obj.has("site") ? new URL(obj.getString("site")) : null;
        } catch (MalformedURLException err) {
            site = null;
            Log.w("Lieu", String.format(Locale.getDefault(),"Erreur format URL (site, _id = %d)", _id), err);
        }

        try {
            photo = obj.has("photo") ? new URL("https", context.getString(R.string.serveur), "media/" + obj.getString("photo")) : null;
        } catch (MalformedURLException err) {
            photo = null;
            Log.w("Lieu", String.format(Locale.getDefault(),"Erreur format URL (photo, _id = %d)", _id), err);
        }
    }

    // Méthodes
    @Nullable public String getPrix() {
        switch (prix) {
            case 0:
                return "Gratuit";

            case 1:
                return "Bon marché";

            case 2:
                return "Modéré";

            case 3:
                return "Cher";

            case 4:
                return "Très cher";
        }

        return null;
    }
    public Location getLocation() {
        Location location = new Location("net.capellari.showme");
        location.setLatitude(coordonnees.latitude);
        location.setLongitude(coordonnees.longitude);

        return location;
    }

    // Classes
    public static class GeoPoint {
        // Champs
        public double latitude = 0.;
        public double longitude = 0.;
    }
    public static class Adresse {
        // Champs
        public String numero;
        public String rue;
        public String codePostal;
        public String ville;
        public String departement;
        public String region;
        public String pays;
    }

    // DAO
    @Dao
    public static abstract class LieuDAO {
        // Acces
        @Query("select * from Lieu where _id == :id")
        public abstract Lieu select(long id);

        @Query("select Type.* from TypeLieu join Type on TypeLieu.type_id == Type._id where lieu_id == :id")
        public abstract List<Type> selectTypes(long id);

        // Modif
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        public abstract void ajouter(Lieu... lieux);

        @Insert(onConflict = OnConflictStrategy.IGNORE)
        public abstract void ajoutLienType(TypeLieu typeLieu);
    }
}
