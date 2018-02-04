package net.capellari.showme.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import net.capellari.showme.db.AppDatabase;
import net.capellari.showme.db.ParamDatabase;
import net.capellari.showme.db.Type;
import net.capellari.showme.db.TypeBase;
import net.capellari.showme.db.TypeParam;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by julien on 02/02/18.
 *
 * Gestion des types
 */

public class TypesModel extends AndroidViewModel {
    // Attributs
    // - données
    private List<Type> m_types       = new LinkedList<>();
    private List<TypeParam> m_params = new LinkedList<>();

    // - données filtrées
    private List<Type> m_typesNonSelect = new LinkedList<>();

    // - live data
    private MutableLiveData<List<Type>>      m_live_types  = new MutableLiveData<>();
    private MutableLiveData<List<TypeParam>> m_live_params = new MutableLiveData<>();

    private AppDatabase m_appdb;
    private ParamDatabase m_paramdb;

    // Constructeur
    public TypesModel(@NonNull Application application) {
        super(application);

        // Ouverture des bases
        m_appdb   = AppDatabase.getInstance(application);
        m_paramdb = ParamDatabase.getInstance(application);

        // Récupération des types
        new SelectTypesTask().execute();
        new SelectParamsTask().execute();
    }

    // Méthodes
    public LiveData<List<TypeParam>> getParams() {
        return m_live_params;
    }
    public LiveData<List<Type>> getTypesNonSelect() {
        return m_live_types;
    }

    public void ajouter(Type type) {
        // Construction de l'objet
        TypeParam tp = new TypeParam();
        tp._id = type._id;
        tp.nom = type.nom;

        // Ajout et tri
        m_params.add(tp);
        Collections.sort(m_params, new TriTypes());

        // Suppression
        m_typesNonSelect.remove(type);

        // Maj UI
        m_live_params.setValue(m_params);
        m_live_types.setValue(m_typesNonSelect);

        // Insertion dans la base
        new InsertTask().execute(tp);
    }
    public void enlever(int pos) {
        // Suppression
        TypeParam tp = m_params.remove(pos);

        // Ajout et tri
        pos = m_types.indexOf(tp);
        m_typesNonSelect.add(m_types.get(pos));
        Collections.sort(m_typesNonSelect, new TriTypes());

        // Maj UI
        m_live_types.setValue(m_typesNonSelect);
        m_live_params.setValue(m_params);

        // Suppression dans la base
        new DeleteTask().execute(tp);
    }

    // traitement interne
    private void filtrer() {
        // Reset
        m_typesNonSelect = new LinkedList<>();

        // Filtrage
        for (Type type : m_types) {
            //noinspection SuspiciousMethodCalls
            if (!m_params.contains(type)) {
                m_typesNonSelect.add(type);
            }
        }

        // Triage
        Collections.sort(m_typesNonSelect, new TriTypes());

        // Maj UI
        m_live_types.setValue(m_typesNonSelect);
    }

    // Events
    @Override
    protected void onCleared() {
        // Fermeture de la base
        m_appdb.close();
        m_paramdb.close();
    }

    // Tri
    private class TriTypes implements Comparator<TypeBase> {
        @Override
        public int compare(TypeBase tp1, TypeBase tp2) {
            return tp1.nom.compareTo(tp2.nom);
        }
    }

    // Taches
    // - accès
    private class SelectParamsTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            m_params = m_paramdb.getTypeDAO().recup();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            m_live_params.setValue(m_params);
            filtrer();
        }
    }
    private class SelectTypesTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            m_types = m_appdb.getTypeDAO().recup();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            filtrer();
        }
    }

    // - modif
    private class InsertTask extends AsyncTask<TypeParam,Void,Void> {
        @Override
        protected Void doInBackground(TypeParam... typeParams) {
            m_paramdb.getTypeDAO().ajouter(typeParams);
            return null;
        }
    }
    private class DeleteTask extends AsyncTask<TypeParam,Void,Void> {
        @Override
        protected Void doInBackground(TypeParam... typeParams) {
            m_paramdb.getTypeDAO().enlever(typeParams);
            return null;
        }
    }
}
