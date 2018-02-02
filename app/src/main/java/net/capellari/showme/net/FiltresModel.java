package net.capellari.showme.net;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import net.capellari.showme.db.Lieu;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by julien on 30/01/18.
 *
 * Enregistre la liste des lieux et les filtres
 */
public class FiltresModel extends ViewModel {
    // Attributs
    private List<Lieu> m_lieux = new LinkedList<>();
    private MutableLiveData<Boolean> m_filtres_types = new MutableLiveData<>();

    // Constructeur
    public FiltresModel() {
        m_filtres_types.setValue(true); // par défaut !
    }

    // Méthodes
    public void ajouterLieu(Lieu lieu) {
        m_lieux.add(lieu);
    }
    public void vider() {
        m_lieux.clear();
    }

    public List<Lieu> getlieux() {
        return m_lieux;
    }
    public void setLieux(List<Lieu> lieux) {
        m_lieux = lieux;
    }

    public LiveData<Boolean> getFiltreTypes() {
        return m_filtres_types;
    }
    public void setFiltresTypes(boolean actif) {
        m_filtres_types.setValue(actif);
    }
}
