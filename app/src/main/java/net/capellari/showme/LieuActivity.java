package net.capellari.showme;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RatingBar;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import net.capellari.showme.data.LieuxModel;
import net.capellari.showme.data.RequeteManager;
import net.capellari.showme.db.Horaire;
import net.capellari.showme.db.Lieu;
import net.capellari.showme.db.TypeBase;

import java.util.List;

public class LieuActivity extends AppCompatActivity implements CarteFragment.OnCarteEventListener {
    // Constantes
    public  static final String INTENT_LIEU = "lieu";
    private static final String TAG         = "LieuActivity";

    // Attributs
    private Lieu m_lieu;
    private LieuxModel m_lieuxModel;

    private RequeteManager m_requeteManager;
    private SharedPreferences m_preferences;

    private CollapsingToolbarLayout m_collapsingToolbar;
    private RatingBar m_note;
    private CarteFragment m_carteFragment;
    private TypeSpinnerFragment m_typeSpinnerFragment;
    private TextView m_adresse;
    private HoraireFragment m_horaireFragment;
    private TextView m_prix;
    private TextView m_telephone;
    private TextView m_siteWeb;
    private NetworkImageView m_image;

    // Events
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ouverture des préférences
        m_preferences = PreferenceManager.getDefaultSharedPreferences(this);

        // Initialisation gestion des requetes
        m_requeteManager = RequeteManager.getInstance(this.getApplicationContext());

        // Inflate !
        setContentView(R.layout.activity_lieu);
        setupViews();
        setupFragments();
        setupToolbar();

        // Récupération du lieu
        recupLieu();
    }

    // Carte
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (m_lieu != null) ajouterMarker();
    }

    @Override
    public void onMarkerClick(@NonNull Lieu lieu) {

    }

    // Méthodes
    private void setupViews() {
        // Récupération des vues
        m_note      = findViewById(R.id.note);
        m_adresse   = findViewById(R.id.adresse);
        m_prix      = findViewById(R.id.prix);
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
    }
    private void setupFragments() {
        // Carte
        m_carteFragment = (CarteFragment) getSupportFragmentManager().findFragmentById(R.id.carte);
        m_carteFragment.setOnCarteEventListener(this);

        // Affichage des types
        m_typeSpinnerFragment = (TypeSpinnerFragment) getSupportFragmentManager().findFragmentById(R.id.selecttype);

        // Affichage horaires
        m_horaireFragment = (HoraireFragment) getSupportFragmentManager().findFragmentById(R.id.horaires);
    }
    private void setupToolbar() {
        m_collapsingToolbar = findViewById(R.id.toolbar_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void recupLieu() {
        // Récupération du Model
        m_lieuxModel = ViewModelProviders.of(this).get(LieuxModel.class);

        // Récupération de l'identifiant dans l'intent
        Intent intent = getIntent();
        long idLieu = intent.getLongExtra(INTENT_LIEU, -1);

        if (idLieu != -1) {
            m_lieuxModel.recup(idLieu).observe(this, new LieuObs());
        }
    }
    private void ajouterMarker() {
        Marker marker = m_carteFragment.ajouterLieu(m_lieu);
        if (marker != null) marker.showInfoWindow();
    }

    // Observer
    private class LieuObs implements Observer<Lieu> {
        @Override
        public void onChanged(@Nullable Lieu lieu) {
            // Gardien
            if (lieu == null) return;

            // Enregistrement !
            m_lieu = lieu;

            // Marqueur
            ajouterMarker();

            // Titre
            m_collapsingToolbar.setTitle(lieu.nom);

            // Types
            m_lieuxModel.recupTypes(lieu._id).observe(LieuActivity.this, new Observer<List<TypeBase>>() {
                @Override
                public void onChanged(@Nullable List<TypeBase> types) {
                    if (types == null) return;
                    m_typeSpinnerFragment.setTypes(types);
                }
            });

            // Adresse
            m_adresse.setText(lieu.getAdresse());
            m_adresse.setEnabled(true);

            // Horaires
            m_lieuxModel.recupHoraires(lieu._id).observe(LieuActivity.this, new Observer<List<Horaire>>() {
                @Override
                public void onChanged(@Nullable List<Horaire> horaires) {
                    m_horaireFragment.setHoraires(horaires);
                }
            });

            // Infos
            if (lieu.note != null) {
                m_note.setRating(lieu.note.floatValue());
                m_note.setVisibility(View.VISIBLE);
            }
            if (lieu.prix != null) {
                m_prix.setText(lieu.getPrix());
                m_prix.setEnabled(true);
            }
            if (lieu.telephone != null) {
                m_telephone.setText(lieu.telephone);
                m_telephone.setEnabled(true);
            }
            if (lieu.site != null) {
                m_siteWeb.setText(lieu.site.getHost());
                m_siteWeb.setEnabled(true);
            }

            // Image
            if (lieu.photo != null && m_preferences.getBoolean(getString(R.string.pref_internet), true)) {
                m_image.setImageUrl(lieu.photo.toString(), m_requeteManager.getImageLoader());
            }
        }
    }
}
