package net.capellari.showme;

import android.content.Context;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by julien on 27/01/18.
 *
 * Suivre l'evolution !
 */

public class SuivreAppBarBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {
    // Constructeur
    public SuivreAppBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Méthodes
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependency) {
        child.setBottom(child.getHeight() + dependency.getBottom());
        child.setTop(dependency.getBottom());

        return true;
    }
}