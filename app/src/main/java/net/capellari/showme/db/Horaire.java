package net.capellari.showme.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by julien on 06/01/18.
 *
 * Représente une plage horaire ou le lieu associé est ouvert
 */

@Entity(foreignKeys = {
        @ForeignKey(
                entity = Lieu.class,
                parentColumns = "id",
                childColumns = "lieu_id",
                onDelete = CASCADE
        )
})
public class Horaire {
    // Champs
    @PrimaryKey
    public int id;

    public int lieu_id;
    public int jour;
    public int ouverture;
    public int fermeture;
}
