package net.capellari.showme.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.ForeignKey;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Locale;

import static android.arch.persistence.room.ForeignKey.CASCADE;

/**
 * Created by julien on 06/01/18.
 *
 * Représente une plage horaire ou le lieu associé est ouvert
 */

@Entity(foreignKeys = {
        @ForeignKey(
                entity = Lieu.class,
                parentColumns = "_id",
                childColumns = "lieu_id",
                onDelete = CASCADE
        )
})
public class Horaire implements Comparable<Horaire> {
    // Constantes
    private static final int[] TABLE_JOURS = {
            Calendar.SUNDAY,
            Calendar.MONDAY,
            Calendar.TUESDAY,
            Calendar.WEDNESDAY,
            Calendar.THURSDAY,
            Calendar.FRIDAY,
            Calendar.SATURDAY
    };

    // Champs
    @PrimaryKey @ColumnInfo(index = true)
    public long _id;

    @ColumnInfo(index = true)
    public long lieu_id = -1L;

    public int jour = 0;
    public int ouverture = 0;
    public int fermeture = 0;

    // Constructeurs
    public Horaire() {
    }
    public Horaire(JSONObject obj, Lieu lieu) throws JSONException {
        // Lien !
        lieu_id = lieu._id;

        // Infos
        _id       = obj.getInt("id");
        jour      = obj.getInt("jour");
        ouverture = obj.getInt("open");
        fermeture = obj.getInt("close");
    }

    // Méthodes
    public int getCalendarDay() {
        return TABLE_JOURS[jour];
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%d:%02d - %d:%02d",
                ouverture / 100, ouverture % 100, fermeture / 100, fermeture % 100);
    }

    @Override
    public int compareTo(@NonNull Horaire obj) {
        return ouverture - obj.ouverture;
    }

    public Calendar getJour(Calendar calendar) {
        // Copie avant modif
        calendar = (Calendar) calendar.clone();

        // Semaine suivante si le jour avant "aujourd'hui" dans la semaine
        if (calendar.get(Calendar.DAY_OF_WEEK) > getCalendarDay()) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }

        // Modification du jour !
        calendar.set(Calendar.DAY_OF_WEEK, getCalendarDay());

        return calendar;
    }
    public Calendar getOuverture(Calendar calendar) {
        calendar = getJour(calendar);

        // Analyse horaire
        int heure  = ouverture / 100;
        int minute = ouverture % 100;

        // Modification de l'heure !
        calendar.set(Calendar.HOUR_OF_DAY, heure);
        calendar.set(Calendar.MINUTE,      minute);

        return calendar;
    }
    public Calendar getFermeture(Calendar calendar) {
        calendar = getJour(calendar);

        // Analyse horaire
        int heure  = fermeture / 100;
        int minute = fermeture % 100;

        // Modification de l'heure !
        calendar.set(Calendar.HOUR_OF_DAY, heure);
        calendar.set(Calendar.MINUTE,      minute);

        return calendar;
    }
}
