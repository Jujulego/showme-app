package net.capellari.showme.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.Index;
import android.support.annotation.NonNull;

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
                childColumns = "lieu_id"
        ),
        @ForeignKey(
                entity = Type.class,
                parentColumns = "id",
                childColumns = "type_id"
        )
}, primaryKeys = {"lieu_id", "type_id"})
public class TypeLieu {
    // Champs
    @ColumnInfo(index = true)
    public long lieu_id;

    @ColumnInfo(index = true)
    public long type_id;
}
