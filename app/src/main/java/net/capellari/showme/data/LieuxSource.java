package net.capellari.showme.data;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LifecycleRegistry;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.OnLifecycleEvent;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import net.capellari.showme.R;
import net.capellari.showme.db.Lieu;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;

/**
 * Created by julien on 05/02/18.
 *
 * Récupération des lieux environnant
 */

public class LieuxSource implements LifecycleOwner, LifecycleObserver {
    // Constante
    private static final String TAG = "LieuxSource";

    // Attributs
    private Context m_context;
    private LifecycleRegistry m_lifecycle;

    private PositionSource m_position;
    private RequeteManager m_requeteManager;
    private SharedPreferences m_preferences;

    private LieuxModel m_lieuxModel;
    private FiltresModel m_filtresModel;

    private int m_compteur = 0;
    private String m_query = null;
    private boolean m_refreshing = false;

    // - livedata
    private MutableLiveData<Boolean> m_live_refreshing = new MutableLiveData<>();

    // Constructeur
    private LieuxSource(Context context) {
        m_context = context;

        // Init lifecycle
        m_lifecycle = new LifecycleRegistry(this);
        m_lifecycle.markState(Lifecycle.State.CREATED);

        // Ouverture des préférences
        m_preferences = PreferenceManager.getDefaultSharedPreferences(m_context);

        // Init position
        m_position = new PositionSource(m_context, m_lifecycle);
        m_lifecycle.addObserver(m_position);

        // Init requête
        m_requeteManager = RequeteManager.getInstance(m_context);
    }
    public LieuxSource(Fragment fragment) {
        this(fragment.getContext());

        // Récupération des models
        m_lieuxModel   = ViewModelProviders.of(fragment).get(LieuxModel.class);
        m_filtresModel = ViewModelProviders.of(fragment).get(FiltresModel.class);
    }
    public LieuxSource(FragmentActivity activity) {
        this((Context) activity);

        // Récupération des models
        m_lieuxModel   = ViewModelProviders.of(activity).get(LieuxModel.class);
        m_filtresModel = ViewModelProviders.of(activity).get(FiltresModel.class);
    }

    // Events
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        // lifecycle !
        m_lifecycle.markState(Lifecycle.State.STARTED);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        // lifecycle !
        m_lifecycle.markState(Lifecycle.State.RESUMED);

        // Maj UI
        m_live_refreshing.setValue(m_refreshing);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        // lifecycle !
        m_lifecycle.markState(Lifecycle.State.RESUMED);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        // lifecycle !
        m_lifecycle.markState(Lifecycle.State.STARTED);
    }

    public void onDestroy() {
        // lifecycle !
        m_lifecycle.markState(Lifecycle.State.DESTROYED);

        // Arrêt des requêtes
        m_requeteManager.getRequestQueue().cancelAll(TAG);
    }

    // Méthodes
    // - demande de rafraîchissement !
    public void rafraichir() {
        // Récupération des paramètres
        Location location = m_position.getLastLocation();
        int rayon = m_preferences.getInt(m_context.getString(R.string.pref_rayon), 10);

        // Lancement de la requête !
        if (location != null && m_lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED)) {
            LieuxRequete lieuxRequete;

            if (m_query == null) {
                lieuxRequete = new LieuxRequete(location, rayon);
            } else {
                try {
                    lieuxRequete = new LieuxRequete(location, rayon, m_query);

                } catch (UnsupportedEncodingException err) {
                    lieuxRequete = null;
                    Log.e(TAG, "Erreur d'encodage", err);
                }
            }

            if (lieuxRequete != null) {
                setRefreshing(true);
                m_requeteManager.addRequest(lieuxRequete);
            }
        }
    }

    // - recherche
    public void rechercher(String query) {
        m_query = query;
        rafraichir();
    }
    public String getQuery() {
        return m_query;
    }
    public void setQuery(String query) {
        m_query = query;
    }

    // - rafraîchissement
    public LiveData<Boolean> isRefreshing() {
        return m_live_refreshing;
    }
    private void setRefreshing(boolean actif) {
        m_refreshing = actif;
        m_live_refreshing.setValue(m_refreshing);
    }

    // - compteur
    private synchronized void initCompteur(int compteur) {
        m_compteur = compteur;
    }
    private synchronized void decrementer() {
        --m_compteur;

        if (m_compteur <= 0) {
            m_compteur = 0;
            setRefreshing(false);
        }
    }

    // - lifecycle
    @NonNull @Override
    public Lifecycle getLifecycle() {
        return m_lifecycle;
    }

    // - position source
    public PositionSource getPositionSource() {
        return m_position;
    }

    // Requête
    class LieuxRequete extends JsonArrayRequest {
        private LieuxRequete(String url) {
            super(url, new LieuxListener(), new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    // Message d'erreur
                    Log.w(TAG, error.toString());
                    setRefreshing(false);
                }
            });

            setTag(TAG);
        }

        public LieuxRequete(Location location, int rayon) {
            this(String.format(Locale.US, m_context.getString(R.string.url_lieux),
                    m_context.getString(R.string.serveur),
                    location.getLongitude(), location.getLatitude(),
                    rayon
            ));
        }
        public LieuxRequete(Location location, int rayon, String query) throws UnsupportedEncodingException {
            this(String.format(Locale.US, m_context.getString(R.string.url_rechr),
                    m_context.getString(R.string.serveur),
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
                setRefreshing(false);
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
            initCompteur(ids.length);

            for (Long id : ids) {
                // Cas spéciaux
                if (id == null) {
                    decrementer();
                    continue;
                }

                // Récupération du suivant !
                final LiveData<Lieu> liveData = m_lieuxModel.recup(id);
                liveData.observe(LieuxSource.this, new Observer<Lieu>() {
                    @Override
                    public void onChanged(@Nullable Lieu lieu) {
                        decrementer();

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
