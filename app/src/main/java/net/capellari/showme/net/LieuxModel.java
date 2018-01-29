package net.capellari.showme.net;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.LongSparseArray;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import net.capellari.showme.R;
import net.capellari.showme.db.Lieu;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by julien on 29/01/18.
 *
 * Gestion et sauvegarde des requetes sur lieu
 */
public class LieuxModel extends AndroidViewModel {
    // Constantes
    private static final String TAG = "LiveLieu";

    // Attributs
    private LongSparseArray<LiveLieu> m_cache = new LongSparseArray<>();

    private Location m_location;
    private String m_query;
    private int m_rayon;

    private int m_taille;
    private List<Lieu> m_liste;
    private MutableLiveData<List<Lieu>> m_lieux;

    //private Requete m_requete;
    private RequeteManager m_requeteManager;

    // Constructeur
    public LieuxModel(@NonNull Application application) {
        super(application);

        // Outils
        m_requeteManager = RequeteManager.getInstance(application);
    }

    // Méthodes
    public LiveData<Lieu> recup(long id) {
        // Check
        LiveLieu lieu = m_cache.get(id, null);

        // Lancement si jamais lancé
        if (lieu == null) {
            lieu = new LiveLieu(this.getApplication(), id);
            m_cache.append(id, lieu);
        }

        return lieu;
    }

    /*public LiveData<List<Lieu>> recup(Location location, int rayon) {
        // Nouveaux paramètres ?
        if (m_location != location || m_rayon != rayon || m_query != null) {
            // Annulation de la requête précédente
            if (m_requete != null) m_requete.cancel();

            // Enregistrement des paramètres
            m_location = location;
            m_rayon = rayon;
            m_query = null;

            // Vidage résultat
            m_lieux = new MutableLiveData<>();

            // Requete !!!
            m_requete = new Requete(location, rayon);
            m_requeteManager.addRequest(m_requete);
        }

        return m_lieux;
    }

    // Requete
    private class Requete extends JsonArrayRequest {
        private Requete(String url) {
            super(url, new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray reponse) {
                    // Récupération des IDs
                    Long[] ids = new Long[reponse.length()];
                    m_taille = 0;

                    for (int i = 0; i < reponse.length(); ++i) {
                        try {
                            ids[i] = reponse.getLong(i);
                            m_taille++;

                        } catch (JSONException err) {
                            ids[i] = null;
                            Log.e(TAG, "Erreur JSON", err);
                        }
                    }

                    // Préparation résultat
                    m_liste = new LinkedList<>();

                    // Demandes
                    for (Long id : ids) {
                        if (id == null) continue;

                        // Récupération !
                        recup(id).observe(LieuxModel.this, new Observer<Lieu>() {
                            @Override
                            public void onChanged(@Nullable Lieu lieu) {

                            }
                        });
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.w(TAG, error.toString());
                    m_lieux.setValue(null);
                }
            });
        }

        public Requete(Location location, int rayon) {
            this(String.format(Locale.US, getApplication().getString(R.string.url_lieux), getApplication().getString(R.string.serveur),
                    location.getLongitude(), location.getLatitude(),
                    rayon
            ));
        }
        public Requete(Location location, int rayon, String query) throws UnsupportedEncodingException {
            this(String.format(Locale.US, getApplication().getString(R.string.url_rechr), getApplication().getString(R.string.serveur),
                    location.getLongitude(), location.getLatitude(),
                    rayon, URLEncoder.encode(query, "UTF-8"), query.length() / 2
            ));
        }
    }*/
}
