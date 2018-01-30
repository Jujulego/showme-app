package net.capellari.showme;

import android.arch.lifecycle.ViewModel;

import net.capellari.showme.db.Lieu;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by julien on 30/01/18.
 *
 * Enregistre la liste des lieux et les filtres
 */
public class FiltreModel extends ViewModel {
    // Attributs
    private List<Lieu> m_lieux = new LinkedList<>();

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
}
