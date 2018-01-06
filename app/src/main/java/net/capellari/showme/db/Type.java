package net.capellari.showme.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Index;
import android.arch.persistence.room.PrimaryKey;

/**
 * Created by julien on 06/01/18.
 *
 * Représente un type de lieu
 */

@Entity(indices = {@Index(value = "nom", unique = true)})
public class Type {
    // Champs
    @PrimaryKey
    public int id;

    public String nom;
}