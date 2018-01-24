package net.capellari.showme;

import android.arch.persistence.room.Room;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import net.capellari.showme.db.AppDatabase;

/**
 * Created by julien on 12/01/18.
 *
 * Gestion catégories
 */

public class TypesActivity extends AppCompatActivity {
    // Constantes
    private static final String TAG = "TypesActivity";

    // Attributs
    private TypesFragment m_typesFragment;

    private AppDatabase m_db;

    // Events
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate !
        setContentView(R.layout.activity_types);

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // Récupération du fragement
        m_typesFragment = (TypesFragment) getSupportFragmentManager().findFragmentById(R.id.types_fragment);

        // Récupération de la base de données
        new DBInit().execute(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_types, menu);
        return true;
    }

    // Taches
    static class DBInit extends AsyncTask<TypesActivity,Void,Void> {
        @Override
        protected Void doInBackground(TypesActivity... activities) {
            TypesActivity activity = activities[0];

            AppDatabase db = Room.databaseBuilder(
                    activity, AppDatabase.class,
                    "showme.db"
            ).build();

            Log.i(TAG, "Database initialisée");
            activity.m_typesFragment.getAdapter().setLiveData(db.getTypeDAO().recup());
            activity.m_db = db;

            return null;
        }
    }
}
