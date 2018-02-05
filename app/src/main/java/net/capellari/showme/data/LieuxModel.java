package net.capellari.showme.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.LongSparseArray;

import net.capellari.showme.db.AppDatabase;
import net.capellari.showme.db.Lieu;
import net.capellari.showme.db.TypeBase;

import java.util.List;

/**
 * Created by julien on 29/01/18.
 *
 * Gestion et sauvegarde des requêtes sur lieu
 */
public class LieuxModel extends AndroidViewModel {
    // Constantes
    private static final String TAG = "LieuModel";

    // Attributs
    private LongSparseArray<LiveLieu> m_cacheLieu = new LongSparseArray<>();
    private LongSparseArray<LiveData<List<TypeBase>>> m_cacheTypes = new LongSparseArray<>();

    private AppDatabase m_db;
    private LieuxSource m_lieuxSource;

    // Constructeur
    public LieuxModel(@NonNull Application application) {
        super(application);

        // Ouverture de la base
        m_db = AppDatabase.getInstance(application);
    }

    // Events
    @Override
    protected void onCleared() {
        m_db.close();

        // DESTRUCTION !!! Ouais !
        if (m_lieuxSource != null) {
            m_lieuxSource.onDestroy();
        }
    }

    // Méthodes
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
            types = m_db.getLieuDAO().selectLiveTypes(id);
            m_cacheTypes.append(id, types);
        }

        return types;
    }

    public synchronized LieuxSource getLieuxSource(Fragment fragment) {
        // Initialisation !
        if (m_lieuxSource == null) {
            m_lieuxSource = new LieuxSource(fragment);
            fragment.getLifecycle().addObserver(m_lieuxSource);
        }

        return m_lieuxSource;
    }
    public synchronized LieuxSource getLieuxSource(FragmentActivity activity) {
        // Initialisation !
        if (m_lieuxSource == null) {
            m_lieuxSource = new LieuxSource(activity);
            activity.getLifecycle().addObserver(m_lieuxSource);
        }

        return m_lieuxSource;
    }
}