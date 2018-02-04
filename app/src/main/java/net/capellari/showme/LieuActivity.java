package net.capellari.showme;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RatingBar;
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

import net.capellari.showme.data.LocationObserver;
import net.capellari.showme.db.Lieu;
import net.capellari.showme.db.TypeBase;
import net.capellari.showme.data.LieuxModel;
import net.capellari.showme.data.RequeteManager;

import java.util.List;

public class LieuActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Constantes
    public  static final String INTENT_LIEU = "lieu";
    private static final String TAG         = "LieuActivity";

    private static final String MAP_TAG = "map";

    // Attributs
    private Lieu m_lieu;
    private LieuxModel m_lieuxModel;

    private RequeteManager m_requeteManager;
    private SharedPreferences m_preferences;

    private GoogleMap m_map = null;
    private LiveData<Location> m_live_location;
    private Observer<Location> m_observer;
    private LocationObserver m_locationObserver;

    private CollapsingToolbarLayout m_collapsingToolbar;
    private RatingBar m_note;
    private SupportMapFragment m_mapFragment;
    private SelectTypeFragment m_selectTypeFragment;
    private TextView m_adresse;
    private TextView m_prix;
    private TextView m_telephone;
    private TextView m_siteWeb;
    private NetworkImageView m_image;

    // Events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ouverture des préférences
        m_preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Inflate !
        setContentView(R.layout.activity_lieu);
        m_note      = findViewById(R.id.note);
        m_adresse   = findViewById(R.id.adresse);
        m_prix      = findViewById(R.id.prix);
        m_telephone = findViewById(R.id.telephone);
        m_siteWeb   = findViewById(R.id.site_web);
        m_image     = findViewById(R.id.image);

        // Fragment
        m_selectTypeFragment = (SelectTypeFragment) getSupportFragmentManager().findFragmentById(R.id.selecttype);

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

        // Récupération / Création du fragment
        if (savedInstanceState != null) {
            m_mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag(MAP_TAG);
        } else {
            m_mapFragment = new SupportMapFragment();

            // Ajout !
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.layout_carte, m_mapFragment, MAP_TAG);
            transaction.commit();
        }

        // Récupération de la carte
        m_locationObserver = new LocationObserver(this);
        getLifecycle().addObserver(m_locationObserver);

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
        m_requeteManager = RequeteManager.getInstance(this.getApplicationContext());

        // Récupération du lieu
        m_lieuxModel = ViewModelProviders.of(this).get(LieuxModel.class);

        Intent intent = getIntent();
        long idLieu = intent.getLongExtra(INTENT_LIEU, -1);

        if (idLieu != -1) {
            m_lieuxModel.recup(idLieu).observe(this, new Observer<Lieu>() {
                @Override
                public void onChanged(@Nullable Lieu lieu) {
                    // Enregistrement !
                    m_lieu = lieu;
                    if (lieu == null) return;
                    
                    // Titre
                    m_collapsingToolbar.setTitle(lieu.nom);

                    // Typesb
                    m_lieuxModel.recupTypes(lieu._id).observe(LieuActivity.this, new Observer<List<TypeBase>>() {
                        @Override
                        public void onChanged(@Nullable List<TypeBase> types) {
                            if (types == null) return;

                            m_selectTypeFragment.setTypes(types);
                        }
                    });

                    // Adresse
                    String adresse = "";
                    if (m_lieu.adresse.numero.length() != 0) {
                        adresse += m_lieu.adresse.numero;
                    }
                    if (m_lieu.adresse.rue.length() != 0) {
                        if (adresse.length() != 0) adresse += " ";
                        adresse += m_lieu.adresse.rue;
                    }
                    if (m_lieu.adresse.codePostal.length() != 0 && m_lieu.adresse.ville.length() != 0) {
                        if (adresse.length() != 0) adresse += ", ";
                        adresse += m_lieu.adresse.codePostal + " " + m_lieu.adresse.ville;
                    }
                    m_adresse.setText(adresse);
                    m_adresse.setEnabled(true);

                    // Marker
                    if (m_map != null) setPlace();

                    // Infos
                    if (lieu.note != null) {
                        m_note.setRating(lieu.note.floatValue());
                        m_note.setVisibility(View.VISIBLE);
                    }
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
                    if (lieu.photo != null && m_preferences.getBoolean(getString(R.string.pref_internet), true)) {
                        m_image.setImageUrl(lieu.photo.toString(), m_requeteManager.getImageLoader());
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        m_locationObserver.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        m_map = googleMap;
        if (m_lieu != null) setPlace();
    }

    // Méthodes
    private void setPlace() {
        // Y'a une carte ?
        if (m_map == null) return;

        // Centrage !
        m_observer = new Observer<Location>() {
            @Override
            public void onChanged(@Nullable Location location) {
                if (location == null) return;

                // Centrage
                CameraPosition.Builder builder = new CameraPosition.Builder();
                builder.target(new LatLng(
                        location.getLatitude(), location.getLongitude()
                )).zoom(15).tilt(45);

                m_map.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));

                // Marqueur
                m_map.addMarker(new MarkerOptions()
                        .position(new LatLng(m_lieu.coordonnees.latitude, m_lieu.coordonnees.longitude))
                        .title(m_lieu.nom)
                ).showInfoWindow();

                // Location
                try {
                    m_map.setMyLocationEnabled(true);

                } catch (SecurityException err) { // N'arrive pas : sinon on atteint jamais cette ligne !
                    Log.e(TAG, "Pas cool ...", err);
                }

                // Suppression observer
                m_live_location.removeObserver(m_observer);
            }
        };

        // Récupération !
        m_live_location = m_locationObserver.getLocation();
        m_live_location.observe(this, m_observer);
    }
}
