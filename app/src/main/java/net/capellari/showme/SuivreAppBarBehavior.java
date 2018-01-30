package net.capellari.showme;

import android.content.Context;
import android.os.Parcelable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.AbsSavedState;
import android.view.View;

import java.util.List;

/**
 * Created by julien on 27/01/18.
 *
 * Suivre l'evolution !
 */
@SuppressWarnings("unused")
public class SuivreAppBarBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {
    // Constructeur
    @SuppressWarnings("unused")
    public SuivreAppBarBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // Méthodes
    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, V child, View dependency) {
        return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
        List<View> dependences = parent.getDependencies(child);
        View dependence = dependences.get(0);

        // Préparation
        parent.onLayoutChild(child, layoutDirection);

        // Positionement
        child.setBottom(dependence.getBottom() + child.getMeasuredHeight());
        child.setTop(dependence.getBottom());

        return true;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, V child, View dependence) {
        child.setBottom(child.getMeasuredHeight() + dependence.getBottom());
        child.setTop(dependence.getBottom());

        return true;
    }
}