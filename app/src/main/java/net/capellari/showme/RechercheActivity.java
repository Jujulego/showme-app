package net.capellari.showme;

import android.Manifest;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Created by julien on 03/01/18.
 *
 * Permet des recherche sur texte
 */

public class RechercheActivity extends AppCompatActivity implements OnMapReadyCallback {
    // Constantes
    public static final int RQ_FINE_LOCATION = 1;

    // Attributs
    private Toolbar m_toolbar;

    private GoogleMap m_map;
    private FusedLocationProviderClient m_locationClient;

    private TextView m_erreur;

    // Events
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mise en place du layout
        setContentView(R.layout.activity_recherche);

        // Récupération des vues
        m_erreur = findViewById(R.id.erreur);

        // Gestion de la toolbar
        m_toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(m_toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // Gestion de la carte
        m_locationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment frag_map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.carte);
        frag_map.getMapAsync(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Ajout des options
        getMenuInflater().inflate(R.menu.recherche_toolbar, menu);

        // Gestion du SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (searchManager != null) {
            final SearchView searchView = (SearchView) menu.findItem(R.id.nav_rechr).getActionView();

            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(false);

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    searchView.clearFocus();
                    rechercher(query);

                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });

            ImageView searchViewIcon = searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
            ViewGroup linearLayoutSearchView = (ViewGroup) searchViewIcon.getParent();
            linearLayoutSearchView.removeView(searchViewIcon);

            // Récupération dans l'intent
            Intent intent = getIntent();
            if (intent != null && Intent.ACTION_SEARCH.equals(intent.getAction())) {
                String query = intent.getStringExtra(SearchManager.QUERY);
                searchView.setQuery(query, true);
            }
        }

        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        m_map = googleMap;

        // Positionnement
        centrer_carte();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RQ_FINE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    centrer_carte();
                }

                break;
        }
    }

    // Méthodes
    private void rechercher(String query) {
        m_erreur.setText(query);
    }

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
                    CameraPosition.Builder builder = new CameraPosition.Builder();
                    builder.target(new LatLng(
                            location.getLatitude(),
                            location.getLongitude()
                    )).zoom(15).tilt(45);

                    m_map.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                }
            });
        } else {
            // Show rationale and request permission.
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, RQ_FINE_LOCATION);
        }
    }
}
