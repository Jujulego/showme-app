package net.capellari.showme;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.NestedScrollView;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.maps.GoogleMap;

import net.capellari.showme.data.LocationObserver;
import net.capellari.showme.db.AppDatabase;
import net.capellari.showme.db.Lieu;
import net.capellari.showme.db.Type;
import net.capellari.showme.data.FiltresModel;
import net.capellari.showme.data.LieuxModel;
import net.capellari.showme.data.RequeteManager;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

public class MainActivity extends AppCompatActivity
        implements CarteFragment.OnCarteEventListener,
                   RayonFragment.OnRayonListener,
                   ResultatFragment.OnResultatListener,
                   SharedPreferences.OnSharedPreferenceChangeListener {

    // Constantes
    private static final int SEARCH_ICON_POS   = 2;     // Position de la loupe dans la toolbar, depuis la droite
    private static final boolean MENU_OVERFLOW = false; // Les 3 points dans la toolbar

    private static final String QUERY = "showme.QUERY";
    private static final String TAG   = "MainActivity";

    // Attributs
    private String m_query = null;

    // - toolbar
    private Toolbar m_toolbar;
    private Toolbar m_searchBar;
    private SearchView m_searchView;
    private MenuItem m_searchMenuItem;

    // - fragments
    private CarteFragment m_carteFragment;
    private ResultatFragment m_resultatFragment;
    private RayonFragment m_rayonFragment;
    private FiltresFragment m_filtresFragment;

    // - bottom sheet
    private NestedScrollView m_bottomSheet;
    private BottomSheetBehavior m_bottomSheetBehavior;

    // - drawer
    private DrawerLayout m_drawerLayout;
    private ActionBarDrawerToggle m_drawerToggle;

    // - position
    private LiveData<Location> m_live_location;
    private LocationObserver m_locationObserver;

    // - models
    private LieuxModel m_lieuxModel;
    private FiltresModel m_filtresModel;

    // - outils
    private AppDatabase m_db;
    private RequeteManager m_requeteManager;
    private SharedPreferences m_preferences;

    // Events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ouverture des préférences
        m_preferences = PreferenceManager.getDefaultSharedPreferences(this);
        m_preferences.registerOnSharedPreferenceChangeListener(this);

        // Models
        m_lieuxModel  = ViewModelProviders.of(this).get(LieuxModel.class);
        m_filtresModel = ViewModelProviders.of(this).get(FiltresModel.class);

        // Ouverture DB
        m_db = AppDatabase.getInstance(this);

        // Gestion des requêtes
        m_requeteManager = RequeteManager.getInstance(this.getApplicationContext());
        getTypes();

        // Préparation des maj position
        setupLocation();

        // Préparations et affichage de l'accueil
        if (savedInstanceState != null) {
            // Récupération de l'état
            m_query = savedInstanceState.getString(QUERY);
        }

        // Inflate !
        setContentView(R.layout.activity_main);

        // Mise en place de la toolbar
        setupToolbar();
        setupSearchbar();

        // Préparation
        setupFragments();
        setupDrawer();
        setupBottomSheet();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Sync the toggle state after onRestoreInstanceState has occurred.
        m_drawerToggle.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Service
        gestionService();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        m_drawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Arrêt des requetes
        m_requeteManager.getRequestQueue().cancelAll(TAG);
    }

    // Statut
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restauration de l'état recherche
        if (m_query != null) {
            setupRecherche();
            m_searchView.setQuery(m_query, true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Enregistrement du status
        outState.putString(QUERY, m_query);
    }

    // Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        // Transmission à l'observer
        m_locationObserver.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Ajout des options
        getMenuInflater().inflate(R.menu.toolbar_main, menu);
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
        }

        // Action par défaut
        return super.onOptionsItemSelected(item);
    }

    // Carte
    @Override
    public void onMapReady(@NonNull GoogleMap map) {

    }

    @Override
    public void onMarkerClick(@NonNull Lieu lieu) {
        onLieuClick(lieu);
    }

    // Rayon
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
    public void onRayonChange(int rayon, boolean user) {
        // Enregistrement
        SharedPreferences.Editor editor = m_preferences.edit();
        editor.putInt(getString(R.string.pref_rayon), rayon);
        editor.apply();

        // Rafraichissement !
        if (user) rafraichir();
    }

    // Resultat
    @Override
    public void onRefresh() {
        rafraichir();
    }

    @Override
    public void onLieuClick(Lieu lieu) {
        Intent intent = new Intent(this, LieuActivity.class);
        intent.putExtra(LieuActivity.INTENT_LIEU, lieu._id);

        startActivity(intent);
    }

    // Préférences
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_rayon_max))) {
            m_rayonFragment.set_max(m_preferences.getInt(getString(R.string.pref_rayon_max), 100));

        } else if (key.equals(getString(R.string.pref_nombre))) {
            gestionService();
        }
    }

    // Méthodes
    private void rechercher(final String query) {
        // Récupération de la postion
        Location location = m_locationObserver.getLastLocation();

        // Chargement ...
        if (location != null) {
            m_resultatFragment.setRefreshing(true);
            getLieux(location, m_rayonFragment.get_rayon(), query);

            // Enregistrement !
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    HistoriqueProvider.AUTORITE,
                    HistoriqueProvider.MODE
            );
            suggestions.saveRecentQuery(query, null);

            // Sauvegarde pour rafraichissement
            m_query = query;
        }
    }
    private void rafraichir() {
        // Récupération de la position
        Location location = m_locationObserver.getLastLocation();

        // Chargement ...
        if (location != null) {
            m_resultatFragment.setRefreshing(true);
            m_bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            if (m_query != null) {
                getLieux(location, m_rayonFragment.get_rayon(), m_query);
            } else {
                getLieux(location, m_rayonFragment.get_rayon());
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void getTypes() {
        // Check pref
        if (!m_preferences.getBoolean(getString(R.string.pref_internet), true)) {
            return;
        }

        // Requete
        m_requeteManager.addRequest(new JsonArrayRequest(getString(R.string.url_types, getString(R.string.serveur)), new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray reponse) {
                Type[] types = new Type[reponse.length()];

                // Création des objets
                for (int i = 0; i < reponse.length(); ++i) {
                    try {
                        types[i] = new Type(reponse.getJSONObject(i));

                    } catch (JSONException err) {
                        Log.e(TAG, "Erreur JSON types", err);
                    }
                }

                // Log
                Log.i(TAG, "Types mis à jour !");

                // Ajout au fragment
                new AsyncTask<Type,Void,Void>() {
                    @Override
                    protected Void doInBackground(Type... types) {
                        Type.TypeDAO dao = m_db.getTypeDAO();
                        m_db.beginTransaction();

                        try {
                            for (Type t : types) {
                                try {
                                    dao.insert(t);
                                } catch (SQLiteConstraintException err) {
                                    dao.maj(t);
                                }
                            }

                            m_db.setTransactionSuccessful();
                        } finally {
                            m_db.endTransaction();
                        }

                        return null;
                    }
                }.execute(types);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w(TAG, error.toString());
            }
        }));
    }
    private void getLieux(Location location, int rayon) {
        // Check pref
        /*if (!m_preferences.getBoolean(getString(R.string.pref_internet), true)) {
            m_resultatFragment.setRefreshing(false);
            return;
        }*/

        // Requete
        m_requeteManager.addRequest(new LieuxRequete(location, rayon));
    }
    private void getLieux(Location location, int rayon, String query) {
        // Check pref
        /*if (!m_preferences.getBoolean(getString(R.string.pref_internet), true)) {
            m_resultatFragment.setRefreshing(false);
            return;
        }*/

        // Requete
        try {
            m_requeteManager.addRequest(new LieuxRequete(location, rayon, query));
        } catch (UnsupportedEncodingException err) {
            Log.w(TAG, "Erreur !", err);
        }
    }

    private void setupFragments() {
        FragmentManager manager = getSupportFragmentManager();

        // Récupération
        m_carteFragment    = (CarteFragment)    manager.findFragmentById(R.id.carte);
        m_resultatFragment = (ResultatFragment) manager.findFragmentById(R.id.resultat);
        m_rayonFragment    = (RayonFragment)    manager.findFragmentById(R.id.rayon);
        m_filtresFragment  = (FiltresFragment)  manager.findFragmentById(R.id.filtres);

        // Paramétrages
        m_resultatFragment.setRefreshMenuItem(R.id.nav_refresh);

        m_carteFragment.setOnCarteEventListener(this);
        m_resultatFragment.setOnResultatListener(this);
        m_rayonFragment.setOnRayonListener(this);
    }
    private void gestionService() {
        boolean start = m_preferences.getBoolean(getString(R.string.pref_nombre), false);
        Intent intent = new Intent(this, NombreService.class);

        if (start) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        } else {
            stopService(intent);
        }
    }

    private void setupAccueil() {
        // Vidage m_query
        m_query = null;
    }
    private void setupRecherche() {
        // Gestion du drawer et du searchView
        m_drawerLayout.closeDrawers();
        m_searchMenuItem.expandActionView();
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
                Intent intent;

                // Evolution des fragments
                switch (item.getItemId()) {
                    case R.id.nav_accueil:
                        setupAccueil();
                        ret = true;
                        break;

                    case R.id.nav_recherche:
                        setupRecherche();
                        ret = true;
                        break;

                    case R.id.nav_lieu:
                        intent = new Intent(MainActivity.this, LieuActivity.class);
                        startActivity(intent);

                        ret = true;
                        break;

                    case R.id.nav_types:
                        intent = new Intent(MainActivity.this, TypesActivity.class);
                        startActivity(intent);

                        ret = true;
                        break;

                    case R.id.nav_pref:
                        intent = new Intent(MainActivity.this, ParametresActivity.class);
                        startActivity(intent);

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
        m_searchBar.inflateMenu(R.menu.searchbar_main);
        Menu menu = m_searchBar.getMenu();

        // On renvoie les events à l'activité
        m_searchBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return m_resultatFragment.onOptionsItemSelected(item) || onOptionsItemSelected(item);
            }
        });

        // Mise en place de l'animation
        m_searchMenuItem = menu.findItem(R.id.nav_recherche);
        m_searchMenuItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                circleReveal(R.id.search_bar, SEARCH_ICON_POS, true);

                // Affichage du clavier
                m_searchView.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

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
        m_searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                // Récupération du curseur
                Cursor cursor = m_searchView.getSuggestionsAdapter().getCursor();
                cursor.moveToPosition(position);

                // Récupération & lancement de la recherche
                String query = cursor.getString(2); // 3eme colonne !
                rechercher(query);

                // suivi de l'ecran !
                m_searchView.clearFocus();
                m_searchView.setQuery(query, false);

                return true;
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
    private void setupBottomSheet() {
        // Récupération des éléments
        m_bottomSheet = findViewById(R.id.nested_bottom_view);
        m_bottomSheetBehavior = BottomSheetBehavior.from(m_bottomSheet);
    }
    private void setupLocation() {
        // Récupération observer
        m_locationObserver = new LocationObserver(this, getLifecycle());
        getLifecycle().addObserver(m_locationObserver);

        // Récupération location
        if (m_live_location == null) {
            m_live_location = m_locationObserver.getLocation();
            m_live_location.observe(this, new Observer<Location>() {
                @Override
                public void onChanged(@Nullable Location location) {
                    // Gardien
                    if (location == null) return;

                    // Maj distances
                    if (m_resultatFragment != null) {
                        m_resultatFragment.majDistances(location);
                    }
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void circleReveal(@SuppressWarnings("SameParameterValue") int viewID, @SuppressWarnings("SameParameterValue") int posFromRight, final boolean isShow) {
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

    // Requêtes
    class LieuxRequete extends JsonArrayRequest {
        private LieuxRequete(String url) {
            super(url, new LieuxListener(), new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.w(TAG, error.toString());

                    // Message d'erreur
                    m_resultatFragment.setRefreshing(false);
                }
            });

            setTag(TAG);
        }

        public LieuxRequete(Location location, int rayon) {
            this(String.format(Locale.US, getString(R.string.url_lieux),
                    getString(R.string.serveur),
                    location.getLongitude(), location.getLatitude(),
                    rayon
            ));
        }

        public LieuxRequete(Location location, int rayon, String query) throws UnsupportedEncodingException {
            this(String.format(Locale.US, getString(R.string.url_rechr),
                    getString(R.string.serveur),
                    location.getLongitude(), location.getLatitude(),
                    rayon, URLEncoder.encode(query, "UTF-8"), query.length() / 2
            ));
        }
    }
    class LieuxListener implements Response.Listener<JSONArray> {
        @Override
        public void onResponse(JSONArray reponse) {
            // Vidage
            m_filtresModel.vider();

            // Cas de la réponse vide :
            if (reponse.length() == 0) {
                m_resultatFragment.setRefreshing(false);
                return;
            }

            // Traitement
            Long[] ids = new Long[reponse.length()];

            // Récupération des IDs
            for (int i = 0; i < reponse.length(); ++i) {
                try {
                    ids[i] = reponse.getLong(i);

                } catch (JSONException err) {
                    ids[i] = null;
                    Log.e(TAG, "Erreur JSON lieux", err);
                }
            }

            // Récupération des lieux
            m_resultatFragment.initCompteur(ids.length);

            for (Long id : ids) {
                // Cas spéciaux
                if (id == null) {
                    m_resultatFragment.decrementer();
                    continue;
                }

                // Récupération du suivant !
                final LiveData<Lieu> liveData = m_lieuxModel.recup(id);
                liveData.observe(MainActivity.this, new Observer<Lieu>() {
                    @Override
                    public void onChanged(@Nullable Lieu lieu) {
                        m_resultatFragment.decrementer();

                        if (lieu != null) {
                            m_filtresModel.ajouterLieu(lieu);

                            liveData.removeObserver(this);
                        }
                    }
                });
            }
        }
    }
}