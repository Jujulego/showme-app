package net.capellari.showme.data;

import android.support.v7.util.DiffUtil;

import net.capellari.showme.db.TypeBase;

import java.util.List;

/**
 * Created by julien on 04/02/18.
 *
 * Calcul des différences entre 2 listes de types
 */

public class DiffType<T extends TypeBase> extends DiffUtil.Callback {// Attributs
    private List<T> m_anc;
    private List<T> m_nouv;

    // Constructeur
    public DiffType(List<T> anc, List<T> nouv) {
        m_anc  = anc;
        m_nouv = nouv;
    }

    // Méthodes
    @Override
    public int getOldListSize() {
        return m_anc.size();
    }

    @Override
    public int getNewListSize() {
        return m_nouv.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return m_anc.get(oldItemPosition)._id == m_nouv.get(newItemPosition)._id;
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return m_anc.get(oldItemPosition).equals(m_nouv.get(newItemPosition));
    }
}
