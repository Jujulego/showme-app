package net.capellari.showme.net;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import net.capellari.showme.db.ParamDatabase;
import net.capellari.showme.db.Type;
import net.capellari.showme.db.TypeParam;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by julien on 02/02/18.
 *
 * Gestion des types
 */

public class TypesModel extends AndroidViewModel {
    // Attributs
    private ParamDatabase m_paramdb;

    private List<TypeParam> m_types;
    private MutableLiveData<List<TypeParam>> m_livedata;

    // Constructeur
    public TypesModel(@NonNull Application application) {
        super(application);

        // Ouverture de la base
        m_paramdb = ParamDatabase.getInstance(application);
    }

    // Méthodes
    @NonNull
    public synchronized LiveData<List<TypeParam>> getTypes() {
        if (m_livedata == null) {
            m_livedata = new MutableLiveData<>();

            // Récupération initiale
            new SelectTask().execute();
        }

        return m_livedata;
    }

    public void ajouter(Type type) {
        // Construction de l'objet
        TypeParam tp = new TypeParam();
        tp._id = type._id;
        tp.nom = type.nom;

        // Ajout et tri
        if (m_livedata != null) {
            m_types.add(tp);
            Collections.sort(m_types, new TriTypes());
            m_livedata.setValue(m_types);
        }

        // Insertion dans la base
        new InsertTask().execute(tp);
    }

    public void enlever(int pos) {
        // Gardien
        if (m_livedata == null) return;

        // Suppression de la liste
        TypeParam tp = m_types.remove(pos);
        m_livedata.setValue(m_types);

        // Suppression dans la base
        new DeleteTask().execute(tp);
    }

    // Events
    @Override
    protected void onCleared() {
        // Fermeture de la base
        m_paramdb.close();
    }

    // Classe
    private class TriTypes implements Comparator<TypeParam> {
        @Override
        public int compare(TypeParam tp1, TypeParam tp2) {
            return tp1.nom.compareTo(tp2.nom);
        }
    }

    // Taches
    private class SelectTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            m_types = m_paramdb.getTypeDAO().recup();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            m_livedata.setValue(m_types);
        }
    }

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
