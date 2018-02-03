package net.capellari.showme;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.StyleRes;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by julien on 01/02/18.
 *
 * Bouton de nettoyage !
 */

public class NettoyagePreference extends DialogPreference {
    // Constante
    private static final String TAG = "NettoyagePreference";

    // Constructeur
    @SuppressWarnings("SameParameterValue")
    public NettoyagePreference(Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        // Set default layout
        setLayoutResource(R.layout.preference_bouton);
    }
    @SuppressWarnings("SameParameterValue")
    public NettoyagePreference(Context context, AttributeSet attrs, @AttrRes int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }
    @SuppressWarnings("SameParameterValue")
    public NettoyagePreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }
    @SuppressWarnings("unused")
    public NettoyagePreference(Context context) {
        this(context, null);
    }

    // Events
    @Override
    public void onBindViewHolder(final PreferenceViewHolder view) {
        super.onBindViewHolder(view);

        view.findViewById(R.id.titre_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.itemView.performClick();
            }
        });
    }
}
