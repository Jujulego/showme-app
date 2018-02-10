package net.capellari.showme.data;

import android.annotation.SuppressLint;
import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.LongSparseArray;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import net.capellari.showme.R;
import net.capellari.showme.db.AppDatabase;
import net.capellari.showme.db.Horaire;
import net.capellari.showme.db.Lieu;
import net.capellari.showme.db.ParamDatabase;
import net.capellari.showme.db.Type;
import net.capellari.showme.db.TypeBase;
import net.capellari.showme.db.TypeParam;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by julien on 30/01/18.
 *
 * Enregistre la liste des lieux et les filtres
 */
@SuppressLint("StaticFieldLeak")
public class LieuxModel extends AndroidViewModel {
    // Constantes
    private static final String TAG = "LieuxModel";

    // Énumération
    public enum LieuStatus { INDETERMINE, OUVERT, FERME }

    // Attributs
    // - données brutes
    private List<Lieu> m_lieux = new LinkedList<>();
    private LongSparseArray<List<TypeBase>> m_typesLieux = new LongSparseArray<>();
    private LongSparseArray<List<Horaire>> m_horairesLieux = new LongSparseArray<>();

    // - cache
    private LongSparseArray<LiveLieu> m_cacheLieu = new LongSparseArray<>();
    private LongSparseArray<LiveData<List<TypeBase>>> m_cacheTypes = new LongSparseArray<>();
    private LongSparseArray<LiveData<List<Horaire>>> m_cacheHoraires = new LongSparseArray<>();

    // - filtres
    private LieuStatus m_lieuStatus = LieuStatus.INDETERMINE;
    private boolean m_filtreParams = true;
    private List<TypeParam> m_typesParam = new LinkedList<>();

    private List<TypeBase> m_types = new LinkedList<>();
    private LongSparseArray<Boolean> m_typesAffiches = new LongSparseArray<>();

    // - données filtrées
    private List<Lieu>     m_lieuxFiltres = new LinkedList<>();
    private List<TypeBase> m_typesFiltres = new LinkedList<>();

    // - livedata
    private MutableLiveData<List<Lieu>>     m_live_lieux = new MutableLiveData<>();
    private MutableLiveData<List<TypeBase>> m_live_types = new MutableLiveData<>();

    // - outils
    private AppDatabase m_appdb;
    private ParamDatabase m_paramdb;
    private LieuxSource m_lieuxSource;
    private RequeteManager m_requeteManager;
    private List<SelectTypesTask> m_taches = new LinkedList<>();

    // Constructeur
    public LieuxModel(Application application) {
        super(application);

        // Ouverture des bases
        m_appdb   = AppDatabase.getInstance(application);
        m_paramdb = ParamDatabase.getInstance(application);

        // Gestion des requêtes
        m_requeteManager = RequeteManager.getInstance(application);

        // Nettoyage
        nettoyerCache();

        // Mise à jour des types
        JsonArrayRequest rq = new JsonArrayRequest(
                application.getString(R.string.url_types, application.getString(R.string.serveur)),
                new TypesRequeteListener(), new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.w(TAG, error.toString());
            }
        });
        rq.setTag(TAG);

        m_requeteManager.addRequest(rq);

        // Récupération des paramètres
        new RecupTypeParamTask().execute();
    }

    // Events
    @Override
    protected void onCleared() {
        // Arrêt des taches
        stopTaches();

        // DESTRUCTION !!! MOUHAHAHA !
        if (m_lieuxSource != null) {
            m_lieuxSource.onDestroy();
        }

        // Arrêt des requêtes
        m_requeteManager.getRequestQueue().cancelAll(TAG);

        // Fermeture des bases
        m_appdb.close();
        m_paramdb.close();
    }

    // Méthodes
    // - accès a un lieu
    public LiveData<Lieu> recup(long id) {
        // Check
        LiveLieu lieu = m_cacheLieu.get(id, null);

        // Lancement si jamais lancé
        if (lieu == null) {
            lieu = new LiveLieu(getApplication(), id);
            m_cacheLieu.append(id, lieu);
        }

        return lieu;
    }
    public LiveData<List<TypeBase>> recupTypes(long id) {
        // Check
        LiveData<List<TypeBase>> types = m_cacheTypes.get(id, null);

        // Lancement si jamais lancé
        if (types == null) {
            types = m_appdb.getLieuDAO().selectLiveTypes(id);
            m_cacheTypes.append(id, types);
        }

        return types;
    }
    public LiveData<List<Horaire>> recupHoraires(long id) {
        // Check
        LiveData<List<Horaire>> horaires = m_cacheHoraires.get(id, null);

        // Lancement si jamais lancé
        if (horaires == null) {
            horaires = m_appdb.getLieuDAO().selectLiveHoraires(id);
            m_cacheHoraires.append(id, horaires);
        }

        return horaires;
    }

    // - accès aux données filtrées
    public LiveData<List<Lieu>>     recupLieux() {
        return m_live_lieux;
    }
    public LiveData<List<TypeBase>> recupTypes() {
        return m_live_types;
    }

    // - gestion du contenu
    public void ajouterLieu(Lieu lieu) {
        if (!m_lieux.contains(lieu)) {
            // Ajout !
            m_lieux.add(lieu);

            // Récupération des types associés
            SelectTypesTask tache = new SelectTypesTask(lieu);
            tache.execute();

            m_taches.add(tache);
        }
    }
    public void vider() {
        // Reset !
        m_lieux.clear();
        m_types.clear();
        m_typesLieux.clear();
        m_typesAffiches.clear();
        m_lieuxFiltres.clear();
        m_typesFiltres.clear();

        // Maj UI
        m_live_lieux.setValue(m_lieuxFiltres);
        m_live_types.setValue(m_typesFiltres);

        // Arrêt des taches
        stopTaches();
    }

    // - gestion des filtres
    public void setLieuStatus(LieuStatus status) {
        if (m_lieuStatus != status) {
            m_lieuStatus = status;
            filtrerLieux();
        }
    }
    public void setFiltreParam(boolean actif) {
        if (m_filtreParams != actif) {
            m_filtreParams = actif;
            filtrerTout();
        }
    }

    public boolean getFiltreType(long type) {
        return m_typesAffiches.get(type, false);
    }
    public void setFiltreType(long type, boolean affiche) {
        m_typesAffiches.put(type, affiche);
        filtrerLieux();
    }

    // - accès sources
    public synchronized LieuxSource getLieuxSource(FragmentActivity activity) {
        // Initialisation !
        if (m_lieuxSource == null) {
            m_lieuxSource = new LieuxSource(activity, this);
            activity.getLifecycle().addObserver(m_lieuxSource);
        }

        return m_lieuxSource;
    }
    public PositionSource getPositionSource(FragmentActivity activity) {
        return getLieuxSource(activity).getPositionSource();
    }

    // - forcer maj ui
    public void maj_ui() {
        m_live_lieux.setValue(m_lieuxFiltres);
        m_live_types.setValue(m_typesFiltres);
    }

    // - gestion du cache
    public void viderCache() {
        new VidageTask().execute();
    }
    public void nettoyerCache() {
        new NettoyageTask().execute();
    }

    // - traitement interne
    private void ajouterTypes(Collection<TypeBase> types) {
        boolean modif = false;

        for (TypeBase type : types) {
            // Ajout au tableau affichés si jamais traités
            if (!m_types.contains(type)) {
                m_types.add(type);
                m_typesAffiches.put(type._id, true);
            }

            // filtrage !
            modif |= filtrerType(type);
        }

        if (modif) {
            m_live_types.setValue(m_typesFiltres);
        }
    }
    private boolean filtrerType(TypeBase type) {
        // Déjà présent ?
        if (m_typesFiltres.contains(type)) return false;

        // Type sélectionné ?
        //noinspection SuspiciousMethodCalls
        if (m_filtreParams && !m_typesParam.contains(type)) return false;

        // Ajout !
        m_typesFiltres.add(type);
        return true;
    }

    private boolean filtrerLieu(Lieu lieu) {
        // Déjà présent ?
        if (m_lieuxFiltres.contains(lieu)) return false;

        // Récupération des types
        List<TypeBase> types = m_typesLieux.get(lieu._id, null);
        if (types == null) return false;

        // Test types
        boolean ok = false;
        for (TypeBase type : types) {
            ok = m_typesFiltres.contains(type) && m_typesAffiches.get(type._id, false);
            if (ok) break;
        }

        // Test status
        if (ok && (m_lieuStatus != LieuStatus.INDETERMINE)) {
            ok = Lieu.estOuvert(m_horairesLieux.get(lieu._id, null), m_lieuStatus == LieuStatus.OUVERT);

            if (m_lieuStatus == LieuStatus.FERME) {
                ok = !ok;
            }
        }

        // Ajout
        if (ok) {
            m_lieuxFiltres.add(lieu);
        }

        return ok;
    }
    private void filtrerLieux() {
        // Filtrage
        m_lieuxFiltres = new LinkedList<>();
        for (Lieu lieu : m_lieux) {
            filtrerLieu(lieu);
        }

        // Maj UI
        m_live_lieux.setValue(m_lieuxFiltres);
    }

    private void filtrerTout() {
        // Types
        m_typesFiltres = new LinkedList<>();
        for (TypeBase type : m_types) {
            filtrerType(type);
        }

        // Lieux
        m_lieuxFiltres = new LinkedList<>();
        filtrerLieux();

        // Maj UI
        m_live_types.setValue(m_typesFiltres);
    }

    private void stopTaches() {
        for (SelectTypesTask tache : m_taches) {
            tache.cancel(true);
        }

        m_taches.clear();
    }

    // Requêtes
    private class TypesRequeteListener implements Response.Listener<JSONArray> {
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
            new UpdateTypes().execute(types);
        }
    }

    // Taches
    // - types
    private class UpdateTypes extends AsyncTask<Type,Void,Void> {
        @Override
        protected Void doInBackground(Type... types) {
            Type.TypeDAO dao = m_appdb.getTypeDAO();
            m_appdb.beginTransaction();

            try {
                for (Type t : types) {
                    if (dao.recup(t._id) == null) {
                        dao.insert(t);
                    } else {
                        dao.maj(t);
                    }
                }

                m_appdb.setTransactionSuccessful();
            } finally {
                m_appdb.endTransaction();
            }

            return null;
        }
    }
    private class RecupTypeParamTask extends AsyncTask<Void,Void,List<TypeParam>> {
        // Events
        @Override
        protected void onPostExecute(List<TypeParam> typesParam) {
            m_typesParam = typesParam;
            filtrerTout();
        }

        // Méthodes
        @Override
        protected List<TypeParam> doInBackground(Void... voids) {
            return m_paramdb.getTypeDAO().recup();
        }
    }

    // - types pour d'un lieu
    private class SelectTypesTask extends AsyncTask<Void,Void,Void> {
        // Attributs
        private Lieu m_lieu;
        private List<TypeBase> m_types;
        private List<Horaire> m_horaires;

        // Constructeur
        public SelectTypesTask(Lieu lieu) {
            m_lieu = lieu;
        }

        // Events
        @Override
        protected void onPostExecute(Void v) {
            // Sauvegarde du résultat
            m_typesLieux.put(m_lieu._id, m_types);
            m_horairesLieux.put(m_lieu._id, m_horaires);

            // Traitements
            ajouterTypes(m_types);
            if (filtrerLieu(m_lieu)) {
                m_live_lieux.setValue(m_lieuxFiltres);
            }
        }

        // Méthodes
        @Override
        protected Void doInBackground(Void... voids) {
            m_types    = m_appdb.getLieuDAO().selectTypes(m_lieu._id);
            m_horaires = m_appdb.getLieuDAO().selectHoraires(m_lieu._id);

            return null;
        }
    }

    // - nettoyage & vidage
    private class NettoyageTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            m_appdb.getLieuDAO().nettoyer();
            return null;
        }
    }
    private class VidageTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            m_appdb.getLieuDAO().viderLieux();
            return null;
        }
    }
}
