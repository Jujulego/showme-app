package net.capellari.showme;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.arch.lifecycle.LifecycleService;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import net.capellari.showme.data.LocationObserver;
import net.capellari.showme.data.RequeteManager;

import org.json.JSONArray;

import java.util.Locale;

/**
 * Created by julien on 26/01/18.
 *
 * Affiche dans sa notification le nombre de lieux environnants
 */

public class NombreService extends LifecycleService {
    // Constantes
    private static final String TAG = "NombreService";

    private static final int NOTIFICATION_ID = 1;
    private static final String NOTIFCHANNEL_ID = "NombreService.Channel";

    // Attributs
    private SharedPreferences m_preferences;
    private RequeteManager m_requeteManager;

    private NotificationCompat.Builder m_notifBuilder;
    private NotificationChannel m_notifChannel;
    private NotificationManager m_notifManager;

    private LiveData<Location> m_live_location;
    private LocationObserver m_locationObserver;

    // Events
    @Override
    public void onCreate() {
        super.onCreate();

        // Ouverture des préférences
        m_preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Init Location
        m_locationObserver = new LocationObserver(this);
        getLifecycle().addObserver(m_locationObserver);

        // Initialisation gestion des requetes
        m_requeteManager = RequeteManager.getInstance(this.getApplicationContext());

        // Initialisation Notif Channel
        m_notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            m_notifChannel = new NotificationChannel(
                    NOTIFCHANNEL_ID,
                    getText(R.string.channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        // Init notif
        Log.d(TAG, "Démarré !");
        setup();

        // Redémarrage si nécéssaire
        return START_NOT_STICKY;
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) {
        super.onBind(intent);
        return null;
    }

    // Méthodes
    private void setup() {
        // Test
        if (m_locationObserver.checkLocationPermission()) {
            // Notification !
            Intent notifIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifIntent, 0);

            m_notifBuilder = new NotificationCompat.Builder(this, NOTIFCHANNEL_ID)
                    .setContentTitle(getText(R.string.notif_nombre_titre))
                    .setContentText(getResources().getQuantityString(R.plurals.notif_nombre_texte, 0, 0))
                    .setSmallIcon(R.drawable.icone_notif)
                    .setColor(getColor(R.color.colorPrimary))
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                m_notifBuilder.setChannelId(m_notifChannel.getId());
            }

            startForeground(NOTIFICATION_ID, m_notifBuilder.build());

            // Requêtes
            if (m_live_location == null) {
                m_live_location = m_locationObserver.getLocation();
                m_live_location.observe(this, new Observer<Location>() {
                    @Override
                    public void onChanged(@Nullable Location location) {
                        // Envoi d'une requete
                        m_requeteManager.addRequest(new LieuxRequete(
                                location,
                                m_preferences.getInt(getString(R.string.pref_rayon), 10)
                        ));

                        // Log !
                        Log.d(TAG, "Requete !");
                    }
                });
            }

        } else {
            stopSelf();
        }
    }

    // Requete
    class LieuxRequete extends JsonArrayRequest {
        protected  LieuxRequete(String url) {
            super(url, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray reponse) {
                    m_notifBuilder.setContentText(getResources().getQuantityString(R.plurals.notif_nombre_texte, reponse.length(), reponse.length()));
                    m_notifManager.notify(NOTIFICATION_ID, m_notifBuilder.build());
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.w(TAG, error.toString());
                }
            });
        }

        public LieuxRequete(Location location, int rayon) {
            this(String.format(Locale.US, getString(R.string.url_lieux),
                    getString(R.string.serveur),
                    location.getLongitude(), location.getLatitude(),
                    rayon
            ));
        }
    }
}
