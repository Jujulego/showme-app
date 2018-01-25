package net.capellari.showme;

import android.Manifest;
import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import net.capellari.showme.db.AppDatabase;
import net.capellari.showme.db.Lieu;

public class LieuActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Constantes
    public  static final String INTENT_LIEU = "lieu";
    private static final String TAG = "LieuActivity";

    private static final int RQ_PERM_LOCATION = 1;

    // Attributs
    private Lieu m_lieu = null;

    private AppDatabase m_db;
    private RequestManager m_requestManager;
    private SharedPreferences m_preferences;
    private FusedLocationProviderClient m_locationClient;
    private GoogleMap m_map = null;

    private CollapsingToolbarLayout m_collapsingToolbar;
    private SupportMapFragment m_mapFragment;
    private TextView m_prix;
    private TextView m_telephone;
    private TextView m_siteWeb;
    private NetworkImageView m_image;

    private boolean m_permDemandee = false;
    private boolean m_setPlace     = false;

    // Events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ouverture des préférences
        m_preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Inflate !
        setContentView(R.layout.activity_lieu);

        // Vues
        m_prix      = findViewById(R.id.prix);
        m_telephone = findViewById(R.id.telephone);
        m_siteWeb   = findViewById(R.id.site_web);
        m_image     = findViewById(R.id.image);

        // Clicks !
        m_telephone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m_lieu == null) return;

                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", m_lieu.telephone, null));
                startActivity(intent);
            }
        });
        m_siteWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m_lieu == null) return;

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(m_lieu.site.toString()));
                startActivity(intent);
            }
        });

        // Carte
        m_locationClient = LocationServices.getFusedLocationProviderClient(this);

        m_mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.carte);
        m_mapFragment.getMapAsync(this);

        // Toolbar
        m_collapsingToolbar = findViewById(R.id.toolbar_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // Initialisation gestion des requetes
        m_requestManager = RequestManager.getInstance(this.getApplicationContext());

        // Init DB
        new RecupLieu().execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RQ_PERM_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Execution !
                    if (m_setPlace) setPlace();
                }

                // Reset
                m_permDemandee = false;
                m_setPlace = false;

                break;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        m_map = googleMap;
        if (m_lieu != null) setPlace();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Fermeture de la base
        if (m_db != null) m_db.close();
    }

    // Méthodes
    private boolean checkLocationPermission() {
        // Préférence
        boolean gps = m_preferences.getBoolean(getString(R.string.pref_gps), true);
        String permission = gps ? Manifest.permission.ACCESS_FINE_LOCATION : Manifest.permission.ACCESS_COARSE_LOCATION;

        // Test
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (!m_permDemandee) {
                // On demande gentillement !
                ActivityCompat.requestPermissions(this,
                        new String[]{permission}, RQ_PERM_LOCATION
                );

                m_permDemandee = true;
            }

            return false;
        }

        return true;
    }

    private void setPlace() {
        // Y'a une carte ?
        if (m_map == null) return;

        // Centrage !
        if (checkLocationPermission()) {
            m_map.setMyLocationEnabled(true);

            m_locationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Gardien
                    if (location == null) return;

                    // Centrage
                    CameraPosition.Builder builder = new CameraPosition.Builder();
                    builder.target(new LatLng(
                            location.getLatitude(), location.getLongitude()
                    )).tilt(45);

                    m_map.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));

                    // Marqueur
                    m_map.addMarker(new MarkerOptions()
                            .position(new LatLng(m_lieu.coordonnees.latitude, m_lieu.coordonnees.longitude))
                    );
                }
            });
        } else {
            m_setPlace = true;
        }
    }

    // Taches
    @SuppressLint("StaticFieldLeak")
    class RecupLieu extends AsyncTask<Void,Void,Lieu> {
        @Override
        protected Lieu doInBackground(Void... voids) {
            m_db = Room.databaseBuilder(
                    LieuActivity.this, AppDatabase.class,
                    getString(R.string.database)
            ).build();

            // Log !
            Log.i(TAG, "Database initialisée");

            // Récupération du lieu
            Intent intent = getIntent();
            long idLieu = intent.getLongExtra(INTENT_LIEU, -1);

            return m_db.getLieuDAO().recup(idLieu);
        }

        @Override
        protected void onPostExecute(Lieu lieu) {
            m_lieu = lieu;

            if (lieu != null) {
                // Titre
                m_collapsingToolbar.setTitle(lieu.nom);

                // Marker
                if (m_map != null) setPlace();

                // Infos
                if (lieu.prix != null) {
                    m_prix.setText(lieu.getPrix());
                    m_prix.setEnabled(true);
                }
                if (lieu.telephone != null) {
                    m_telephone.setText(lieu.telephone);
                    m_telephone.setEnabled(true);
                }
                if (lieu.site != null) {
                    m_siteWeb.setText(lieu.site.getHost());
                    m_siteWeb.setEnabled(true);
                }

                // Image
                if (lieu.photo != null) m_image.setImageUrl(lieu.photo.toString(), m_requestManager.getImageLoader());
            } else {
                Log.d(TAG, "lieu == null !");
            }
        }
    }
}
