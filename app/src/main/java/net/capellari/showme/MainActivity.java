package net.capellari.showme;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Created by julien on 31/12/17.
 *
 * Activité principale
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, RayonFragment.OnRayonChangeListener {
    // Constantes
    public static final int RQ_FINE_LOCATION = 1;

    // Attributs
    private GoogleMap m_map;
    private FusedLocationProviderClient m_locationClient;
    private LocationCallback m_locationCallback;
    private boolean m_locationStarted = false;

    private Toolbar m_toolbar;
    private DrawerLayout m_drawerLayout;
    private NavigationView m_drawerNav;
    private ActionBarDrawerToggle m_drawerToggle;

    private Toolbar m_searchToolbar;
    private Menu m_searchMenu;
    private MenuItem m_searchMenuItem;

    private RayonFragment m_seekRayon;

    private SharedPreferences m_prefs;

    // Events
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ajout du layout
        setContentView(R.layout.activity_main);

        // Ouverture des préférences
        m_prefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Gestion du drawer
        m_drawerLayout = findViewById(R.id.drawer_layout);
        m_drawerNav    = findViewById(R.id.drawer_nav);

        m_drawerToggle = new ActionBarDrawerToggle(
                this, m_drawerLayout,
                R.string.nav_open, R.string.nav_close
        );
        m_drawerLayout.addDrawerListener(m_drawerToggle);

        m_drawerNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent = null;

                switch (item.getItemId()) {
                    case R.id.nav_pref:
                        intent = new Intent(MainActivity.this, ParametresActivity.class);
                        break;

                    case R.id.nav_recherche:
                        intent = new Intent(MainActivity.this, RechercheActivity.class);
                        break;
                }

                if (intent != null) {
                    startActivity(intent);
                    return true;
                }

                return false;
            }
        });

        // Action bar !
        m_toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(m_toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        setupSearchToolbar();

        // Initialisation carte
        m_locationClient = LocationServices.getFusedLocationProviderClient(this);
        m_locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();

                // Centrage
                m_map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
                    location.getLatitude(),
                    location.getLongitude()
                )));
            }
        };

        SupportMapFragment frag_map = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.carte);
        frag_map.getMapAsync(this);

        // Gestion du rayon de recherche
        int rayon = m_prefs.getInt(getString(R.string.pref_rayon), 0);

        m_seekRayon = (RayonFragment) getFragmentManager().findFragmentById(R.id.seek_rayon);
        m_seekRayon.set_max(m_prefs.getInt(getString(R.string.pref_rayon_max), 100));
        m_seekRayon.set_rayon(rayon);
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

        // Mise en place de l'animation
        m_searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                circleReveal(R.id.search_toolbar, 1, true, true);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                circleReveal(R.id.search_toolbar, 1, true, false);
                return false;
            }
        });
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Ajout des options
        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (m_drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.nav_recherche:
                m_searchMenuItem.expandActionView();

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRayonChange(int rayon) {
        // Enregistrement
        SharedPreferences.Editor editor = m_prefs.edit();
        editor.putInt(getString(R.string.pref_rayon), rayon);
        editor.apply();
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

    private void start_location_updates() {
        LocationRequest rq = new LocationRequest();
        rq.setFastestInterval(1000);
        rq.setPriority(m_prefs.getBoolean(getString(R.string.pref_gps), true) ? LocationRequest.PRIORITY_HIGH_ACCURACY : LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

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

    // Animation search toolbar
    public void setupSearchToolbar() {
        m_searchToolbar = findViewById(R.id.search_toolbar);

        if (m_searchToolbar != null) {
            // Préparation toolbar
            m_searchToolbar.inflateMenu(R.menu.main_search);
            m_searchMenu = m_searchToolbar.getMenu();

            m_searchMenuItem = m_searchMenu.findItem(R.id.nav_recherche);

            // Préparation search view
            SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
            if (searchManager != null) {
                SearchView searchView = (SearchView) m_searchMenu.findItem(R.id.nav_recherche).getActionView();

                searchView.setSearchableInfo(searchManager.getSearchableInfo(
                        new ComponentName(this, RechercheActivity.class)
                ));

                searchView.setOnSearchClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        m_drawerLayout.closeDrawers();
                    }
                });

                ImageView searchViewIcon = searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
                ViewGroup linearLayoutSearchView = (ViewGroup) searchViewIcon.getParent();
                linearLayoutSearchView.removeView(searchViewIcon);
            }

        } else {
            Log.d("MainActivity", "setSearchtollbar: NULL");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void circleReveal(int viewID, int posFromRight, boolean containsOverflow, final boolean isShow) {
        final View myView = findViewById(viewID);
        int width = myView.getWidth();

        if (posFromRight > 0) {
            width -= (posFromRight - 0.5) * myView.getHeight(); // les icones sont carrées !
        }

        if (containsOverflow) {
            width -= 2 * myView.getHeight() / 3; // bah oui !
        }

        int cx = width;
        int cy = myView.getHeight() / 2;

        Animator anim;
        if (isShow) {
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, (float) width);
        } else {
            anim = ViewAnimationUtils.createCircularReveal(myView, cx, cy, (float) width, 0);
        }

        anim.setDuration((long) 500);

        // make the view invisible when the animation is done
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (!isShow) {
                    super.onAnimationEnd(animation);
                    myView.setVisibility(View.INVISIBLE);
                }
            }
        });

        // make the view visible and start the animation
        if (isShow) myView.setVisibility(View.VISIBLE);

        // start the animation
        anim.start();
    }
}
