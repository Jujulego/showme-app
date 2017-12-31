package net.capellari.showme;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Locale;

/**
 * Created by julien on 31/12/17.
 *
 * Activité principale
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Constantes
    public static final int RQ_FINE_LOCATION = 1;

    // Attributs
    private GoogleMap m_map;
    private FusedLocationProviderClient m_locationClient;
    private LocationCallback m_locationCallback;
    private boolean m_locationStarted = false;

    private DrawerLayout m_drawerLayout;
    private ActionBarDrawerToggle m_drawerToggle;

    private SeekBar m_seekRayon;
    private TextView m_affRayon;

    // Events
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ajout du layout
        setContentView(R.layout.activity_main);

        // Gestion du drawer
        m_drawerLayout = findViewById(R.id.drawer_layout);

        m_drawerToggle = new ActionBarDrawerToggle(
                this, m_drawerLayout,
                R.string.nav_open, R.string.nav_close
        );
        m_drawerLayout.addDrawerListener(m_drawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // Initialisation carte
        m_locationClient = LocationServices.getFusedLocationProviderClient(this);
        m_locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();

                // Centrage
                m_map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(
                                location.getLatitude(),
                                location.getLongitude()
                        ), 15
                ));
            }
        };

        SupportMapFragment frag_map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.main_carte);
        frag_map.getMapAsync(this);

        // Gestion du rayon de recherche
        m_seekRayon = findViewById(R.id.seek_rayon);
        m_affRayon  = findViewById(R.id.aff_rayon);

        m_seekRayon.setMax(9);
        m_seekRayon.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                m_affRayon.setText(String.format(Locale.getDefault(),"%d m", get_rayon()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        m_seekRayon.setProgress(0);

        //m_affRayon.setText(String.format(Locale.getDefault(),"%d m", get_rayon()));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the toggle state after onRestoreInstanceState has occurred.
        m_drawerToggle.syncState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start location updates
        if (!m_locationStarted) start_location_updates();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Stop location updates
        stop_location_updates();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        m_drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        m_map = googleMap;

        // Centrage sur la position actuelle
        centrer_carte();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RQ_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    centrer_carte();

                    if (!m_locationStarted) start_location_updates();
                }

                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (m_drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Méthodes
    private void centrer_carte() {
        // On a la carte ?
        if (m_map == null) return;

        // Check permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            m_map.setMyLocationEnabled(true);

            m_locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Gardien
                    if (location == null) return;

                    // Centrage
                    m_map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(
                                    location.getLatitude(),
                                    location.getLongitude()
                            ), 15
                    ));
                }
            });
        } else {
            // Show rationale and request permission.
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, RQ_FINE_LOCATION);
        }
    }

    private int get_rayon() {
        return 10 * (m_seekRayon.getProgress() + 1);
    }

    private void start_location_updates() {
        LocationRequest rq = new LocationRequest();
        rq.setFastestInterval(1000);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            m_locationClient.requestLocationUpdates(rq, m_locationCallback, null);
            m_locationStarted = true;
        } else {
            // Show rationale and request permission.
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, RQ_FINE_LOCATION);
        }
    }
    private void stop_location_updates() {
        m_locationClient.removeLocationUpdates(m_locationCallback);
        m_locationStarted = false;
    }
}
