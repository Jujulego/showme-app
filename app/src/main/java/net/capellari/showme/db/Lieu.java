package net.capellari.showme.db;

import android.arch.persistence.room.Embedded;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.net.URL;

/**
 * Created by julien on 06/01/18.
 *
 * Représente un lieu
 */

@Entity
public class Lieu {
    // Champs
    @PrimaryKey
    public int id;

    public int date; // Date d'ajout, permet le nettoyage
    public String nom;
    public double note;
    public int prix;
    public String telephone;
    public String site;
    public String photo;

    @Embedded
    public Adresse adresse;

    @Embedded
    public GeoPoint coordonnees;

    // Attributs
    @Ignore
    private double m_distance;

    // Classes
    public class GeoPoint {
        // Champs
        public double latitude;
        public double longitude;
    }

    public class Adresse {
        // Champs
        public String numero;
        public String rue;
        public String ville;
        public String codePostal;
        public String departement;
        public String region;
        public String pays;
    }
}
