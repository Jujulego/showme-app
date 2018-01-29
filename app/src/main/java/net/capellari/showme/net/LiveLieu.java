package net.capellari.showme.net;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import net.capellari.showme.R;
import net.capellari.showme.db.AppDatabase;
import net.capellari.showme.db.Lieu;
import net.capellari.showme.db.TypeLieu;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by julien on 29/01/18.
 *
 * Récupération d'un lieu dans la base locale, sinon sur la distante
 */
public class LiveLieu extends LiveData<Lieu> {
    // Constantes
    private static final String TAG = "LiveLieu";

    // Attributs
    private long m_id;
    private Context m_context;

    private Requete m_requete;
    private SelectTask m_selectTask;
    private AnalyzeTask m_analyzeTask;

    private AppDatabase m_db;
    private RequeteManager m_requeteManager;

    // Constructeur
    public LiveLieu(Context context, long id) {
        // Initialisation
        m_id = id;
        m_context = context;

        // Outils
        m_db = AppDatabase.getInstance(context);
        m_requeteManager = RequeteManager.getInstance(context);
    }

    // Events
    @Override
    protected void onActive() {
        m_selectTask = new SelectTask();
        m_selectTask.execute();
    }

    @Override
    protected void onInactive() {
        // Annulations !
        m_selectTask.cancel(true);

        if (m_requete != null)    m_requete.cancel();
        if (m_analyzeTask != null) m_analyzeTask.cancel(true);
    }

    // Méthodes
    public long get_lieuId() {
        return m_id;
    }

    // Requete
    private class Requete extends JsonObjectRequest {
        // Constructeur
        public Requete() {
            super(Method.GET, m_context.getString(R.string.url_lieu, m_context.getString(R.string.serveur), m_id), new JSONObject(), new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject reponse) {
                    // Insertion dans la base
                    m_analyzeTask = new AnalyzeTask();
                    m_analyzeTask.execute(reponse);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.w(TAG, error.toString());
                    setValue(null);
                }
            });
        }
    }

    // Taches
    @SuppressLint("StaticFieldLeak")
    private class SelectTask extends AsyncTask<Void,Void,Lieu> {
        @Override
        protected Lieu doInBackground(Void... voids) {
            Lieu.LieuDAO dao = m_db.getLieuDAO();

            // Récupération du lieu
            return dao.select(m_id);
        }

        @Override
        protected void onPostExecute(Lieu lieu) {
            if (lieu != null) {
                setValue(lieu);
            } else {
                m_requete = new Requete();
                m_requeteManager.addRequest(m_requete);
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class AnalyzeTask extends AsyncTask<JSONObject,Void,Lieu> {
        @Override
        protected Lieu doInBackground(JSONObject... objets) {
            // Initialisation
            Lieu.LieuDAO dao = m_db.getLieuDAO();
            m_db.beginTransaction();
            Lieu lieu = null;

            try {
                // Analyse lieux
                lieu = new Lieu(m_context, objets[0]);
                dao.ajouter(lieu);

                // Liens types
                JSONArray types = objets[0].getJSONArray("types");
                TypeLieu tl = new TypeLieu();

                for (int i = 0; i < types.length(); ++i) {
                    tl.type_id = types.getLong(i);
                    tl.lieu_id = lieu._id;

                    dao.ajoutLienType(tl);
                }

            } catch (JSONException err) {
                Log.e(TAG, "Erreur JSON", err);

            } finally {
                m_db.endTransaction();
            }

            return lieu;
        }

        @Override
        protected void onPostExecute(Lieu lieu) {
            setValue(lieu);
        }
    }
}
