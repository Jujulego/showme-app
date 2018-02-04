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

import net.capellari.showme.R;

/**
 * Created by julien on 04/02/18.
 *
 * Gestion des mise à jour position
 */

public class LocationObserver implements LifecycleObserver, SharedPreferences.OnSharedPreferenceChangeListener {
    // Constante
    private static final String TAG            = "LocationObserver";
    public static final int REQUEST_PERMISSION = 100;

    // Attributs
    private Location m_location;
    private LocationCallback m_locationCallback;
    private FusedLocationProviderClient m_locationClient;

    private Context m_context;
    private SharedPreferences m_preferences;

    private MutableLiveData<Location> m_live_location = new MutableLiveData<>();

    // Constructeur
    public LocationObserver(Context context) {
        m_context = context;

        // Ouverture des préférences
        m_preferences = PreferenceManager.getDefaultSharedPreferences(context);
        m_preferences.registerOnSharedPreferenceChangeListener(this);

        // Récupération du client
        m_locationClient = LocationServices.getFusedLocationProviderClient(context);
        m_locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                m_location = locationResult.getLastLocation();
                m_live_location.setValue(m_location);
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

    // Events
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        // Préférence
        boolean gps = m_preferences.getBoolean(m_context.getString(R.string.pref_gps), true);

        // Préparation de la requete
        LocationRequest rq = new LocationRequest();
        rq.setFastestInterval(3000);
        rq.setPriority(gps ? LocationRequest.PRIORITY_HIGH_ACCURACY : LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Activation !
        if (checkLocationPermission()) {
            m_locationClient.requestLocationUpdates(rq, m_locationCallback, null);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        // On arrête tout !
        m_locationClient.removeLocationUpdates(m_locationCallback);
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
        if (key.equals(m_context.getString(R.string.pref_gps))) {
            onStop();
            onStart();

            Log.d(TAG, "restart !");
        }
    }
}
