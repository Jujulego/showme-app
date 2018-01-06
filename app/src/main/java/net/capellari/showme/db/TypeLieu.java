package net.capellari.showme.db;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by julien on 06/01/18.
 *
 * Crée le lien entre Type et Lieu car un Lieu peux être associé à plusieurs types
 */

@Entity(foreignKeys = {
        @ForeignKey(
                entity = Lieu.class,
                parentColumns = "id",
                childColumns = "lieu_id",
                onDelete = CASCADE
        ),
        @ForeignKey(
                entity = Type.class,
                parentColumns = "id",
                childColumns = "type_id",
                onDelete = CASCADE
        )
})
public class TypeLieu {
    @PrimaryKey
    public int id;

    public int lieu_id;
    public int type_id;
}
