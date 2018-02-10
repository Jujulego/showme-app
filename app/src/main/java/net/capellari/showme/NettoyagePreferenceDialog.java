package net.capellari.showme;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceDialogFragmentCompat;

/**
 * Created by julien on 01/02/18.
 *
 * Dialog pour NettoyagePreference
 */
public class NettoyagePreferenceDialog extends PreferenceDialogFragmentCompat {
    // Attributs
    private OnDialogClosed m_listener;

    // Méthodes statique
    public static NettoyagePreferenceDialog newInstance(String key, @NonNull OnDialogClosed listener) {
        NettoyagePreferenceDialog fragment = new NettoyagePreferenceDialog();
        fragment.m_listener = listener;

        // Attributs
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);

        return fragment;
    }

    // Events
    @Override
    public void onDialogClosed(boolean positiveResult) {
        m_listener.onDialogClosed(positiveResult);
    }

    // Interface
    public interface OnDialogClosed {
        void onDialogClosed(boolean positiveResult);
    }
}
