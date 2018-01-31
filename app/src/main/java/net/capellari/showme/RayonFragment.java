package net.capellari.showme;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by julien on 03/01/18.
 *
 * Fragement de demande d'un rayon
 */

public class RayonFragment extends Fragment {
    // Attributs
    private SeekBar m_seek_bar;
    private TextView m_valeur;

    private int m_min  = 0;
    private int m_max  = 100;
    private int m_fact = 1;

    private int m_attr_fact  = 1;
    private int m_attr_max   = 100;
    private int m_attr_min   = 0;
    private int m_attr_rayon = 0;

    private OnRayonChangeListener m_listener;

    // Events
    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        // Traitement des attributs
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.RayonFragment);

        try {
            m_attr_fact  = a.getInt(R.styleable.RayonFragment_rayon_fact, m_attr_fact);
            m_attr_max   = a.getInt(R.styleable.RayonFragment_rayon_max,  m_attr_max);
            m_attr_min   = a.getInt(R.styleable.RayonFragment_rayon_min,  m_attr_min);
            m_attr_rayon = a.getInt(R.styleable.RayonFragment_rayon,      m_attr_min);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            m_listener = (OnRayonChangeListener) context;
        } catch (ClassCastException err) {
            throw new ClassCastException(context.toString() + " must implement OnRayonChangeListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rayon, container, false);

        // Gestion de la seekbar
        m_seek_bar = view.findViewById(R.id.seek_bar);
        m_valeur   = view.findViewById(R.id.valeur);

        m_seek_bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                m_valeur.setText(String.format(Locale.getDefault(), "%d m", get_rayon()));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (m_listener != null) m_listener.onRayonChange(get_rayon(), true);
            }
        });

        // Maj valeurs
        OnRayonChangeListener listener = m_listener;
        m_listener = null;

        set_fact( m_attr_fact);
        set_max(  m_attr_max);
        set_min(  m_attr_min);
        set_rayon(m_attr_rayon);

        m_listener = listener;
        listener.onRayonReady();

        return view;
    }

    // Méthodes
    @SuppressWarnings("unused")
    public int get_rayon() {
        int progress = 0;
        if (m_seek_bar != null) progress = m_seek_bar.getProgress();

        return progress * m_fact + m_min;
    }

    @SuppressWarnings("unused")
    public int get_fact() {
        if (m_seek_bar == null) return m_attr_fact;
        return m_fact;
    }

    @SuppressWarnings("unused")
    public int get_min() {
        if (m_seek_bar == null) return m_attr_min;
        return m_min;
    }

    @SuppressWarnings("unused")
    public int get_max() {
        if (m_seek_bar == null) return m_attr_max;
        return m_max;
    }

    public void set_rayon(int rayon) {
        if (m_seek_bar == null) {
            m_attr_rayon = rayon;
            return;
        }

        if (rayon < m_min) rayon = m_min;
        if (rayon > m_max) rayon = m_max;

        m_seek_bar.setProgress((rayon - m_min) / m_fact);
        //m_valeur.setText(String.format(Locale.getDefault(), "%d m", get_rayon()));

        if (m_listener != null) m_listener.onRayonChange(get_rayon(), false);
    }

    public void set_max(int max) {
        if (m_seek_bar == null) {
            m_attr_max = max;
            return;
        }

        // Alignement sur le facteur
        if (m_max % m_fact != 0) max += m_fact - (max % m_fact);
        if (max <= m_min) return;

        m_max = max;
        m_seek_bar.setMax((m_max - m_min) / m_fact);
    }

    public void set_min(int min) {
        if (m_seek_bar == null) {
            m_attr_min = min;
            return;
        }

        // Alignement sur le facteur
        min -= min % m_fact;
        if (m_max <= min) return;

        int rayon = get_rayon();
        m_min = min;
        m_seek_bar.setMax((m_max - m_min) / m_fact);
        set_rayon(rayon < min ? min : rayon);
    }

    public void set_fact(int fact) {
        if (m_seek_bar == null) {
            m_attr_fact = fact;
            return;
        }

        m_fact = fact;

        // Alignement des valeurs
        if (m_max % m_fact != 0) m_max += m_fact - (m_max % m_fact);
        m_min -= m_min % m_fact;

        // Limitage du rayon
        int rayon = get_rayon();
        if (rayon < m_min) {
            set_rayon(m_min);
        } else if (rayon > m_max) {
            set_rayon(m_max);
        }
    }

    // Interface
    interface OnRayonChangeListener {
        void onRayonReady();
        void onRayonChange(int rayon, boolean user);
    }
}
