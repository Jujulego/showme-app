package net.capellari.showme;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.os.Bundle;
import android.transition.Slide;
import android.util.Log;
import android.view.Gravity;
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
import com.google.android.gms.tasks.Task;

public class FragmentActivity extends AppCompatActivity
        implements OnMapReadyCallback,
                   RayonFragment.OnRayonChangeListener,
                   SharedPreferences.OnSharedPreferenceChangeListener {

    // Constantes
    private static final int SEARCH_ICON_POS   = 2;     // Position de la loupe dans la toolbar, depuis la droite
    private static final boolean MENU_OVERFLOW = false; // Les 3 points dans la toolbar

    private static final String STATUS = "showme.STATUS";
    private static final String TAG    = "FragmentActivity";

    private static final String MAP_TAG        = "map";
    private static final String RESULTAT_TAG   = "resultat";
    private static final String RAYON_TAG      = "rayon";
    private static final String PARAMETRES_TAG = "parametres";

    private static final int RQ_PERM_START_LOCATION_UPDATE = 1;
    private static final int RQ_PERM_CENTRER_CARTE = 2;

    // Enuméraion
    enum Status {
        VIDE, ACCUEIL, RECHERCHE, PARAMETRES
    }

    // Attributs
    private Toolbar m_toolbar;
    private Toolbar m_searchBar;
    private SearchView m_searchView;
    private MenuItem m_searchMenuItem;
    private MenuItem m_searchIcone;
    private MenuItem m_refreshIcone;

    private DrawerLayout m_drawerLayout;
    private ActionBarDrawerToggle m_drawerToggle;

    private boolean m_locationStarted = false;
    private LocationCallback m_locationCallback;
    private FusedLocationProviderClient m_locationClient;

    private Status m_status = Status.VIDE;
    private GoogleMap m_map;
    private SharedPreferences m_preferences;

    private SupportMapFragment m_mapFragment;
    private ResultatFragment m_resultatFragment;
    private RayonFragment m_rayonFragment;
    private ParametresFragment m_parametresFragment;

    // Events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ouverture des préférences
        m_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        m_preferences.registerOnSharedPreferenceChangeListener(this);

        // Ajout du layout
        setContentView(R.layout.activity_fragment);
        setupDrawer();

        // Préparation des maj location
        setupLocation();

        // Préparations et affichage de l'accueil
        if (savedInstanceState != null) { // Ne rien faire en cas de restoration
            // Récupération de l'état
            m_status = Status.valueOf(savedInstanceState.getString(STATUS));

            // Récupération des fragements
            m_mapFragment        = (SupportMapFragment) getSupportFragmentManager().findFragmentByTag(MAP_TAG);
            m_resultatFragment   = (ResultatFragment)   getSupportFragmentManager().findFragmentByTag(RESULTAT_TAG);
            m_rayonFragment      = (RayonFragment)      getSupportFragmentManager().findFragmentByTag(RAYON_TAG);
            m_parametresFragment = (ParametresFragment) getSupportFragmentManager().findFragmentByTag(PARAMETRES_TAG);
        }

        // Complète les fragements manquants
        prepareFragments();

        if (savedInstanceState == null) setupAccueil(); // Ne rien faire en cas de restoration

        // Mise en place de la toolbar
        setupToolbar();
        setupSearchbar();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restauration de l'état recherche
        if (m_status == Status.RECHERCHE) {
            m_searchMenuItem.expandActionView();
        }
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

        // Lancement maj location
        if (!m_locationStarted && (m_status == Status.RECHERCHE || m_status == Status.ACCUEIL))
            startLocationUpdates();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        m_drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case RQ_PERM_START_LOCATION_UPDATE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                }

                break;

            case RQ_PERM_CENTRER_CARTE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    centrerCarte();
                }

                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Ajout des options
        getMenuInflater().inflate(R.menu.main_toolbar, menu);

        // Récupération de l'icone search
        m_searchIcone  = m_toolbar.getMenu().findItem(R.id.nav_recherche);
        m_refreshIcone = m_toolbar.getMenu().findItem(R.id.nav_refresh);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle
        if (m_drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        // Passage en mode recherche
        switch (item.getItemId()) {
            case R.id.nav_recherche:
                setupRecherche();
                return true;

            case R.id.nav_refresh:
                rafraichir();
                return true;
        }

        // Action par défaut
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        m_map = googleMap;
        centrerCarte();
    }

    @Override
    public void onRayonReady() {
        // Suivi du rayon
        int rayon = m_preferences.getInt(getString(R.string.pref_rayon), 10);

        m_rayonFragment.set_fact(10);
        m_rayonFragment.set_min(10);
        m_rayonFragment.set_max(m_preferences.getInt(getString(R.string.pref_rayon_max), 100));
        m_rayonFragment.set_rayon(rayon);
    }

    @Override
    public void onRayonChange(int rayon) {
        // Enregistrement
        SharedPreferences.Editor editor = m_preferences.edit();
        editor.putInt(getString(R.string.pref_rayon), rayon);
        editor.apply();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, key);

        if (key.equals(getString(R.string.pref_gps))) {
            stopLocationUpdates();
            startLocationUpdates();
        } else if (key.equals(getString(R.string.pref_rayon_max))) {
            m_rayonFragment.set_max(m_preferences.getInt(getString(R.string.pref_rayon_max), 100));
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Arret des maj
        stopLocationUpdates();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Enregistrement du status
        outState.putString(STATUS, m_status.name());
    }

    // Méthodes
    private void rechercher(String query) {
        if (m_status != Status.ACCUEIL && m_status != Status.RECHERCHE) return;

        m_resultatFragment.setMessage(query);
        m_resultatFragment.setStatus(ResultatFragment.Status.MESSAGE);
    }
    private void rafraichir() {
        if (m_status != Status.ACCUEIL && m_status != Status.RECHERCHE) return;

        m_resultatFragment.setMessage("Rafraîchi !!!");
        m_resultatFragment.setStatus(ResultatFragment.Status.MESSAGE);
    }

    private void prepareFragments() {
        // Carte
        if (m_mapFragment == null) {
            m_mapFragment = new SupportMapFragment();

            m_mapFragment.getMapAsync(this);
        }

        // Résultat
        if (m_resultatFragment == null) {
            m_resultatFragment = new ResultatFragment();
        }

        // Rayon
        if (m_rayonFragment == null) {
            m_rayonFragment = new RayonFragment();
            m_rayonFragment.setEnterTransition(new Slide(Gravity.BOTTOM));
            m_rayonFragment.setExitTransition(new Slide());
        }

        // Paramètres
        if (m_parametresFragment == null) {
            m_parametresFragment = new ParametresFragment();
        }
    }

    private void setupAccueil() {
        // Gardien
        if (m_status == Status.ACCUEIL) return;

        // Retour de l'icone de recherche
        if (m_searchIcone != null) {
            m_searchIcone.setEnabled(true);
            m_searchIcone.setVisible(true);
        }

        // Evolution des fragments
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (m_status) {
            case PARAMETRES:
                transaction.remove(m_parametresFragment);

            case VIDE:
                transaction.add(R.id.layout_central, m_resultatFragment, RESULTAT_TAG);
                transaction.add(R.id.layout_carte, m_mapFragment, MAP_TAG);

                m_mapFragment.getMapAsync(this);
                startLocationUpdates();

            case RECHERCHE:
                transaction.add(R.id.layout_rayon, m_rayonFragment, RAYON_TAG);
        }

        m_resultatFragment.setStatus(ResultatFragment.Status.VIDE);

        transaction.commit();

        // Chg de status
        m_status = Status.ACCUEIL;
    }
    private void setupRecherche() {
        // Gardien
        if (m_status == Status.RECHERCHE) return;

        // Retour de l'icone de recherche
        if (m_searchIcone != null) {
            m_searchIcone.setEnabled(true);
            m_searchIcone.setVisible(true);
        }

        // Gestion du drawer et du searchView
        m_drawerLayout.closeDrawers();
        m_searchMenuItem.expandActionView();

        // Evolution des fragments
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (m_status) {
            case PARAMETRES:
                transaction.remove(m_parametresFragment);

            case VIDE:
                transaction.add(R.id.layout_central, m_resultatFragment, RESULTAT_TAG);
                transaction.add(R.id.layout_carte, m_mapFragment, MAP_TAG);

                m_mapFragment.getMapAsync(this);
                startLocationUpdates();

                break;

            case ACCUEIL:
                transaction.remove(m_rayonFragment);
        }

        m_resultatFragment.setMessage(getString(R.string.rechr_tuto));
        m_resultatFragment.setStatus(ResultatFragment.Status.MESSAGE);

        transaction.commit();

        // Chg de status
        m_status = Status.RECHERCHE;
    }
    private void setupParametres() {
        // Gardien
        if (m_status == Status.PARAMETRES) return;

        // On cache l'icone de recherche
        m_searchIcone.setEnabled(false);
        m_searchIcone.setVisible(false);

        // Evolution des fragments
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        switch (m_status) {
            case ACCUEIL:
                transaction.remove(m_rayonFragment);

            case RECHERCHE:
                transaction.remove(m_resultatFragment);
                transaction.remove(m_mapFragment);
                stopLocationUpdates();

            case VIDE:
                transaction.add(R.id.layout_full, m_parametresFragment, PARAMETRES_TAG);
        }

        transaction.commit();

        // Chg de status
        m_status = Status.PARAMETRES;
    }

    private void setupDrawer() {
        // Récupération des éléments
        m_drawerLayout           = findViewById(R.id.drawer_layout);
        NavigationView drawerNav = findViewById(R.id.drawer_nav);

        // Gestion de l'ouverture
        m_drawerToggle = new ActionBarDrawerToggle(
                this, m_drawerLayout,
                R.string.nav_open, R.string.nav_close
        );
        m_drawerLayout.addDrawerListener(m_drawerToggle);

        // Gestion des boutons
        drawerNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                boolean ret = false;

                // Evolution des fragments
                switch (item.getItemId()) {
                    case R.id.nav_pref:
                        setupParametres();
                        ret = true;
                        break;

                    case R.id.nav_recherche:
                        setupRecherche();
                        ret = true;
                        break;

                    case R.id.nav_frag:
                        setupAccueil();
                        ret = true;
                        break;
                }

                // Fermeture !
                if (ret) m_drawerLayout.closeDrawers();
                return ret;
            }
        });
    }
    private void setupToolbar() {
        // Récupération de la toolbar
        m_toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(m_toolbar);

        // Bouton Home
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }
    private void setupSearchbar() {
        // Récupération de la searchBar
        m_searchBar = findViewById(R.id.search_bar);
        if (m_searchBar == null) {
            Log.w(TAG, "Pas de search bar !");
            return;
        }

        // Ajout du menu
        m_searchBar.inflateMenu(R.menu.main_search);
        Menu menu = m_searchBar.getMenu();

        // On renvoie les events à l'activité
        m_searchBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });

        // Mise en place de l'animation
        m_searchMenuItem = menu.findItem(R.id.nav_recherche);
        m_searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                circleReveal(R.id.search_bar, SEARCH_ICON_POS, true);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                circleReveal(R.id.search_bar, SEARCH_ICON_POS, false);
                setupAccueil();
                return false;
            }
        });

        // Préparation du searchView
        SearchManager manager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        if (manager == null) return;

        m_searchView = (SearchView) m_searchMenuItem.getActionView();
        m_searchView.setSearchableInfo(manager.getSearchableInfo(getComponentName()));

        // gestion du on submit
        m_searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                m_searchView.clearFocus();
                rechercher(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // suppression de l'icone loupe
        ImageView searchViewIcon = m_searchView.findViewById(android.support.v7.appcompat.R.id.search_mag_icon);
        ViewGroup linearLayoutSearchView = (ViewGroup) searchViewIcon.getParent();
        linearLayoutSearchView.removeView(searchViewIcon);

        // Traitement de l'intent ACTION_SEARCH
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);

            setupRecherche();
            m_searchView.setQuery(query, true);
        }
    }
    private void setupLocation() {
        m_locationClient = LocationServices.getFusedLocationProviderClient(this);
        m_locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // Gardien
                if (m_map == null) return;

                // Centrage
                Location location = locationResult.getLastLocation();

                m_map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
                        location.getLatitude(), location.getLongitude()
                )));
            }
        };
    }

    private void centrerCarte() {
        // Y'a une carte ?
        if (m_map == null) return;

        // Centrage !
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
                            location.getLatitude(), location.getLongitude()
                    )).zoom(15).tilt(45);

                    m_map.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                }
            });
        } else {
            // On demande gentillement !
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    RQ_PERM_CENTRER_CARTE
            );
        }
    }

    private void startLocationUpdates() {
        // Gardien
        if (m_locationStarted) return;

        // Préférence
        boolean gps = m_preferences.getBoolean(getString(R.string.pref_gps), true);

        // Préparation de la requete
        LocationRequest rq = new LocationRequest();
        rq.setFastestInterval(3000);
        rq.setPriority(gps ? LocationRequest.PRIORITY_HIGH_ACCURACY : LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        // Activation !
        if (!gps || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            m_locationClient.requestLocationUpdates(rq, m_locationCallback, null);
            m_locationStarted = true;
        } else {
            // On demande gentillement !
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    RQ_PERM_START_LOCATION_UPDATE
            );
        }
    }
    private void stopLocationUpdates() {
        // Gardien
        if (!m_locationStarted) return;

        // On arrête tout !
        m_locationClient.removeLocationUpdates(m_locationCallback);
        m_locationStarted = false;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void circleReveal(int viewID, int posFromRight, final boolean isShow) {
        final View myView = findViewById(viewID);
        if (!myView.isAttachedToWindow()) {
            if (isShow) {
                myView.setVisibility(View.VISIBLE);
            } else {
                myView.setVisibility(View.INVISIBLE);
            }

            return;
        }

        int width = myView.getWidth();

        if (posFromRight > 0) {
            width -= (posFromRight - 0.5) * myView.getHeight(); // les icones sont carrées !
        }

        if (MENU_OVERFLOW) {
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

        anim.setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime));

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