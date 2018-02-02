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

@Entity
public class Type {
    // Champs
    @PrimaryKey @ColumnInfo(index = true)
    public long _id;

    @NonNull public String nom = "";
    @NonNull public String pluriel = "";

    // Constructeur
    public Type() {}
    public Type(JSONObject obj) throws JSONException {
        _id     = obj.getInt("id");
        nom     = obj.getString("nom");
        pluriel = obj.getString("pluriel");
    }

    // Méthodes statiques
    @DrawableRes
    public static int getIconRessource(int id) {
        switch (id) {
            case 1:   return R.drawable.point_interet;
            case 2:   return R.drawable.etablissement;
            case 3:   return R.drawable.bar;
            case 4:   return R.drawable.restaurant;
            case 5:   return R.drawable.restauration;
            case 6:   return R.drawable.boulangerie;
            case 7:   return R.drawable.magasin;
            case 9:   return R.drawable.sante;
            case 10:  return R.drawable.livreur;
            case 12:  return R.drawable.finance;
            case 13:  return R.drawable.magasin_vetements;
            case 14:  return R.drawable.agence_voyage;
            case 15:  return R.drawable.magasin_meubles;
            case 16:  return R.drawable.magasin_fournitures;
            case 17:  return R.drawable.epicerie;
            case 18:  return R.drawable.hotel;
            case 20:  return R.drawable.agence_immobiliere;
            case 22:  return R.drawable.politique;
            case 23:  return R.drawable.magasin_alcools;
            case 24:  return R.drawable.fleuriste;
            case 25:  return R.drawable.lieu_culte;
            case 27:  return R.drawable.hopital;
            case 28:  return R.drawable.ecole;
            case 29:  return R.drawable.quincaillerie;
            case 31:  return R.drawable.poste;
            case 32:  return R.drawable.magasin_chaussures;
            case 33:  return R.drawable.bijouterie;
            case 34:  return R.drawable.centre_commercial;
            case 35:  return R.drawable.laveauto;
            case 36:  return R.drawable.magasin_electronique;
            case 37:  return R.drawable.garage;
            case 39:  return R.drawable.coiffeur;
            case 40:  return R.drawable.arret_bus;
            case 41:  return R.drawable.transports;
            case 42:  return R.drawable.pharmacie;
            case 46:  return R.drawable.banque;
            case 47:  return R.drawable.concessionnaire;
            case 48:  return R.drawable.adresse;
            case 49:  return R.drawable.location_voiture;
            case 50:  return R.drawable.gym;
            case 51:  return R.drawable.peintre;
            case 52:  return R.drawable.spa;
            case 53:  return R.drawable.comptable;
            case 54:  return R.drawable.electricien;
            case 55:  return R.drawable.a_emporter;
            case 56:  return R.drawable.cinema;
            case 59:  return R.drawable.boite_nuit;
            case 61:  return R.drawable.parc;
            case 62:  return R.drawable.cabinet_avocats;
            case 63:  return R.drawable.eglise;
            case 64:  return R.drawable.parking;
            case 65:  return R.drawable.bibliotheque;
            case 66:  return R.drawable.mairie;
            case 67:  return R.drawable.local_gouv;
            case 70:  return R.drawable.demenageur;
            case 71:  return R.drawable.animalerie;
            case 73:  return R.drawable.plombier;
            case 74:  return R.drawable.station_service;
            case 75:  return R.drawable.depannage;
            case 77:  return R.drawable.universite;
            case 79:  return R.drawable.veterinaire;
            case 80:  return R.drawable.dab;
            case 81:  return R.drawable.pressing;
            case 82:  return R.drawable.librairie;
            case 83:  return R.drawable.serrurier;
            case 84:  return R.drawable.grand_magasin;
            case 85:  return R.drawable.galerie_art;
            case 86:  return R.drawable.tribunal;
            case 87:  return R.drawable.box_stockage;
            case 88:  return R.drawable.cafe;
            case 89:  return R.drawable.station_metro;
            case 90:  return R.drawable.mosquee;
            case 91:  return R.drawable.musee;
            case 92:  return R.drawable.magasin_velo;
            case 93:  return R.drawable.gare;
            case 94:  return R.drawable.station_tram;
            case 96:  return R.drawable.ambassade;
            case 97:  return R.drawable.videotheque;
            case 98:  return R.drawable.parc_attractions;
            case 99:  return R.drawable.zoo;
            case 100: return R.drawable.caserne_pompier;
            case 101: return R.drawable.taxi;
            case 102: return R.drawable.synagogue;
            case 103: return R.drawable.aeroport;
            case 105: return R.drawable.casino;
            case 106: return R.drawable.nature;
            case 107: return R.drawable.camping;
            case 108: return R.drawable.temple_hindou;
            case 110: return R.drawable.code_postal;
            case 111: return R.drawable.supermarche;
            default:  return R.drawable.autre;
        }
    }
    public static Drawable getIcon(Context context, int id) {
        return context.getDrawable(getIconRessource(id));
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
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        void insert(Type... types);

        @Update void maj(Type type);
    }

    // Pojo retour
    public static class TypeNb {
        public long _id;
        public String nom;
        public String pluriel;
        public int nb_lieux;
    }
}