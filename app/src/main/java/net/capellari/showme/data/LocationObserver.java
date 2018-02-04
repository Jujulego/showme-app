package net.capellari.showme.data;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.LocationSource;

import net.capellari.showme.R;

/**
 * Created by julien on 04/02/18.
 *
 * Gestion des mise à jour position
 */

public class LocationObserver implements LifecycleObserver, LocationSource, SharedPreferences.OnSharedPreferenceChangeListener {
    // Constante
    private static final String TAG = "LocationObserver";
    private static final float  DISTANCE_MINIMUM = 5; // metres

    public static final int REQUEST_PERMISSION = 100;

    // Attributs
    private Location m_location;
    private boolean m_utiliseGPS = false;
    private LocationCallback m_locationCallback;
    private FusedLocationProviderClient m_locationClient;

    private Context m_context;
    private Lifecycle m_lifecycle;
    private SharedPreferences m_preferences;

    private OnLocationChangedListener m_mapListener;

    private MutableLiveData<Location> m_live_location = new MutableLiveData<>();

    // Constructeur
    public LocationObserver(Context context, Lifecycle lifecycle) {
        m_context = context;
        m_lifecycle = lifecycle;

        // Ouverture des préférences
        m_preferences = PreferenceManager.getDefaultSharedPreferences(context);
        m_preferences.registerOnSharedPreferenceChangeListener(this);

        // Récupération du client
        m_locationClient = LocationServices.getFusedLocationProviderClient(context);
        m_locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // Test Distance minimum
                Location location = locationResult.getLastLocation();
                if (m_location != null && m_location.distanceTo(location) < DISTANCE_MINIMUM) return;

                // Maj
                m_location = location;

                // Maj UI
                m_live_location.setValue(m_location);
                if (m_mapListener != null) m_mapListener.onLocationChanged(m_location);
            }
        };
    }

    // Méthodes
    @Nullable
    public Location getLastLocation() {
        return m_location;
    }
    public LiveData<Location> getLocation() {
        return m_live_location;
    }

    public boolean getGPSParam() {
        return m_preferences.getBoolean(m_context.getString(R.string.pref_gps), true);
    }
    public boolean checkLocationPermission() {
        // Préférence
        boolean gps = getGPSParam();
        String permission = gps ? Manifest.permission.ACCESS_FINE_LOCATION : Manifest.permission.ACCESS_COARSE_LOCATION;

        // Test
        if (ContextCompat.checkSelfPermission(m_context, permission) != PackageManager.PERMISSION_GRANTED) {
            Activity activity = getActivity();

            if (activity != null) {
                // On demande gentillement !
                ActivityCompat.requestPermissions(activity,
                        new String[]{permission}, REQUEST_PERMISSION
                );
            }

            return false;
        }

        return true;
    }

    @Nullable
    private Activity getActivity() {
        return m_context instanceof Activity ?  (Activity) m_context : null;
    }

    private void startLocationUpdate(int accuracy) {
        // Préparation de la requete
        LocationRequest rq = new LocationRequest();
        rq.setFastestInterval(3000);
        rq.setPriority(accuracy);

        // Activation !
        if (checkLocationPermission()) {
            m_locationClient.requestLocationUpdates(rq, m_locationCallback, null);
        }
    }
    private void stopLocationUpdate() {
        m_locationClient.removeLocationUpdates(m_locationCallback);
    }

    // Location source
    @Override
    public void activate(OnLocationChangedListener listener) {
        m_mapListener = listener;
    }

    @Override
    public void deactivate() {
        m_mapListener = null;
    }

    // Events
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        startLocationUpdate(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        // Préférence
        boolean gps = getGPSParam();

        // Passage en GPS si autorisé
        if (gps) {
            stopLocationUpdate();
            startLocationUpdate(LocationRequest.PRIORITY_HIGH_ACCURACY);

            m_utiliseGPS = true;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        // Si utilisait le GPS, arrête son utilisation
        if (m_utiliseGPS) {
            stopLocationUpdate();
            startLocationUpdate(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        }

        m_utiliseGPS = false;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        // On arrête tout !
        stopLocationUpdate();
    }

    // Permission
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Execution !
                    onStart();
                }

                break;
        }
    }

    // Préférences
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(m_context.getString(R.string.pref_gps)) && m_lifecycle.getCurrentState().isAtLeast(Lifecycle.State.RESUMED)) {
            boolean gps = getGPSParam();

            if (!gps && m_utiliseGPS) { // on réduit ...
                stopLocationUpdate();
                startLocationUpdate(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            } else if (gps && !m_utiliseGPS) { // ... ou on augmente !
                stopLocationUpdate();
                startLocationUpdate(LocationRequest.PRIORITY_HIGH_ACCURACY);
            }
        }
    }
}
