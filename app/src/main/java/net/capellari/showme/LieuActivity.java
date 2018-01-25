package net.capellari.showme;

import android.annotation.SuppressLint;
import android.arch.persistence.room.Room;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import net.capellari.showme.db.AppDatabase;
import net.capellari.showme.db.Lieu;

import java.net.URISyntaxException;

public class LieuActivity extends AppCompatActivity {
    // Constantes
    public  static final String INTENT_LIEU = "lieu";
    private static final String TAG = "LieuActivity";

    // Attributs
    private Lieu m_lieu = null;

    private AppDatabase m_db;
    private RequestManager m_requestManager;

    private CollapsingToolbarLayout m_collapsingToolbar;
    private TextView m_telephone;
    private TextView m_siteWeb;
    private NetworkImageView m_image;

    // Events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate !
        setContentView(R.layout.activity_lieu);

        // Vues
        m_telephone = findViewById(R.id.telephone);
        m_siteWeb   = findViewById(R.id.site_web);
        m_image     = findViewById(R.id.image);

        // Clicks !
        m_telephone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m_lieu == null) return;

                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", m_lieu.telephone, null));
                startActivity(intent);
            }
        });
        m_siteWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (m_lieu == null) return;

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(m_lieu.site.toString()));
                startActivity(intent);
            }
        });

        // Toolbar
        m_collapsingToolbar = findViewById(R.id.toolbar_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }

        // Initialisation gestion des requetes
        m_requestManager = RequestManager.getInstance(this.getApplicationContext());

        // Init DB
        new RecupLieu().execute();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Fermeture de la base
        if (m_db != null) m_db.close();
    }

    // Taches
    @SuppressLint("StaticFieldLeak")
    class RecupLieu extends AsyncTask<Void,Void,Lieu> {
        @Override
        protected Lieu doInBackground(Void... voids) {
            m_db = Room.databaseBuilder(
                    LieuActivity.this, AppDatabase.class,
                    getString(R.string.database)
            ).build();

            // Log !
            Log.i(TAG, "Database initialisée");

            // Récupération du lieu
            Intent intent = getIntent();
            long idLieu = intent.getLongExtra(INTENT_LIEU, -1);

            return m_db.getLieuDAO().recup(idLieu);
        }

        @Override
        protected void onPostExecute(Lieu lieu) {
            m_lieu = lieu;

            if (lieu != null) {
                // Titre
                m_collapsingToolbar.setTitle(lieu.nom);

                // Infos
                if (lieu.telephone != null) {
                    m_telephone.setText(lieu.telephone);
                    m_telephone.setEnabled(true);
                }
                if (lieu.site != null) {
                    m_siteWeb.setText(lieu.site.toString());
                    m_siteWeb.setEnabled(true);
                }

                // Image
                if (lieu.photo != null) m_image.setImageUrl(lieu.photo.toString(), m_requestManager.getImageLoader());
            } else {
                Log.d(TAG, "lieu == null !");
            }
        }
    }
}
