package net.capellari.showme.data;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
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
}