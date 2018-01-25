package net.capellari.showme.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.content.Context;
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
    public long id;

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

    // Attributs
    @Ignore public List<Type> types = new LinkedList<>();

    // Constructeur
    public Lieu() {}
    public Lieu(Context context, Type.TypeDAO typeDAO, JSONObject obj) throws JSONException {
        // Obligatoire
        id  = obj.getInt("id");
        nom = obj.getString("nom");

        // Types
        JSONArray jsonTypes = obj.getJSONArray("types");
        for (int i = 0; i < jsonTypes.length(); ++i) {
            long id = jsonTypes.getLong(i);
            types.add(typeDAO.recup(id));
        }

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
            Log.w("Lieu", String.format(Locale.getDefault(),"Erreur format URL (site, id = %d)", id), err);
        }

        try {
            photo = obj.has("photo") ? new URL("https", context.getString(R.string.serveur), "media/" + obj.getString("photo")) : null;
        } catch (MalformedURLException err) {
            photo = null;
            Log.w("Lieu", String.format(Locale.getDefault(),"Erreur format URL (photo, id = %d)", id), err);
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
        @Query("select * from Lieu where id == :id")
        protected abstract Lieu select(long id);

        @Query("select Type.* from TypeLieu join Type on TypeLieu.type_id == Type.id where lieu_id == :id")
        protected abstract List<Type> selectTypes(long id);

        // Modif
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        protected abstract void insert(Lieu... lieux);

        @Insert
        protected abstract void ajoutLienType(TypeLieu typeLieu);

        // Generales
        public Lieu recup(long id) {
            Lieu lieu = select(id);

            // Récupération des types
            if (lieu != null) lieu.types = selectTypes(id);

            return lieu;
        }
        public List<Lieu> recup(Long... ids) {
            List<Lieu> lieux = new LinkedList<>();

            for (Long id : ids) {
                Lieu lieu = recup(id);
                if (lieu != null) lieux.add(lieu);
            }

            return lieux;
        }

        public void ajouter(Lieu... lieux) {
            // Ajout à la base
            insert(lieux);

            // Lien avec les types
            for (Lieu lieu : lieux) {
                for (Type type : lieu.types) {
                    TypeLieu tl = new TypeLieu();
                    tl.lieu_id = lieu.id;
                    tl.type_id = type.id;

                    ajoutLienType(tl);
                }
            }
        }
    }
}
