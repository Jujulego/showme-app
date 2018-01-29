package net.capellari.showme;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import net.capellari.showme.net.RequeteManager;

import org.json.JSONArray;

import java.util.Locale;

/**
 * Created by julien on 26/01/18.
 *
 * Affiche dans sa notif le nombre de lieux environnants
 */

public class NombreService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener {
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

    private boolean m_locationStarted = false;
    private LocationCallback m_locationCallback;
    private FusedLocationProviderClient m_locationClient;

    // Events
    @Override
    public void onCreate() {
        // Ouverture des préférences
        m_preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Init Location
        m_locationClient = LocationServices.getFusedLocationProviderClient(this);
        m_locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // Envoi d'une requete
                m_requeteManager.addRequest(new LieuxRequete(
                        locationResult.getLastLocation(),
                        m_preferences.getInt(getString(R.string.pref_rayon), 10)
                ));

                // Log !
                Log.d(TAG, "Requete !");
            }
        };

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
        // Préférence
        startLocationUpdates();

        // Redémarrage si nécéssaire
        return START_NOT_STICKY;
    }

    @Nullable @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_gps))) {
            stopLocationUpdates();
            startLocationUpdates();
        }
    }

    @Override
    public void onDestroy() {
        stopLocationUpdates();
    }

    // Méthodes
    private void startLocationUpdates() {
        // Préférence
        boolean gps = m_preferences.getBoolean(getString(R.string.pref_gps), true);
        String permission = gps ? Manifest.permission.ACCESS_FINE_LOCATION : Manifest.permission.ACCESS_COARSE_LOCATION;

        // Test
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            // Démarage des requetes de positions
            if (!m_locationStarted) {

                // Préparation de la requete
                LocationRequest rq = new LocationRequest();
                rq.setFastestInterval(5000); // 5 sec
                rq.setPriority(gps ? LocationRequest.PRIORITY_HIGH_ACCURACY : LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

                // Activation !
                m_locationClient.requestLocationUpdates(rq, m_locationCallback, null);
                m_locationStarted = true;

                // Notification !
                Intent notifIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifIntent, 0);

                m_notifBuilder = new NotificationCompat.Builder(this, NOTIFCHANNEL_ID)
                        .setContentTitle(getText(R.string.notif_nombre_titre))
                        .setContentText(getString(R.string.notif_nombre_texte, 0))
                        .setSmallIcon(R.drawable.icone_notif)
                        .setColor(getColor(R.color.colorPrimary))
                        .setContentIntent(pendingIntent);

                startForeground(NOTIFICATION_ID, m_notifBuilder.build());
            }

        } else {
            stopSelf();
        }
    }
    private void stopLocationUpdates() {
        // Gardien
        if (!m_locationStarted) return;

        // On arrête tout !
        m_locationClient.removeLocationUpdates(m_locationCallback);
        m_locationStarted = false;
    }

    // Requete
    class LieuxRequete extends JsonArrayRequest {
        protected  LieuxRequete(String url) {
            super(url, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray reponse) {
                    m_notifBuilder.setContentText(getString(R.string.notif_nombre_texte, reponse.length()));
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
