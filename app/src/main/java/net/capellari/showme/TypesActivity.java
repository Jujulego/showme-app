package net.capellari.showme;

import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import net.capellari.showme.data.TypesModel;

/**
 * Created by julien on 12/01/18.
 *
 * Gestion catégories
 */

public class TypesActivity extends AppCompatActivity {
    // Constantes
    private static final String STATUS = "showme.LieuStatus";
    private static final String TAG    = "TypesActivity";

    private static final String LISTE_TAG = "liste";
    private static final String AJOUT_TAG = "ajout";

    // Énumération
    enum Status {
        VIDE, LISTE, AJOUT
    }

    // Attributs
    private Status m_status = Status.VIDE;
    private MenuItem m_itemAjout;
    private MenuItem m_itemVider;

    private TypesFragment m_typesFragment;
    private TypeAjoutFragment m_typeAjoutFragment;

    private TypesModel m_typesModel;

    // Events
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Chargement de la base
        m_typesModel = ViewModelProviders.of(this).get(TypesModel.class);

        // Inflate !
        setContentView(R.layout.activity_types);

        // Préparations et affichage de l'accueil
        if (savedInstanceState != null) { // Ne rien faire en cas de restoration
            // Récupération de l'état
            m_status = Status.valueOf(savedInstanceState.getString(STATUS));

            // Récupération des fragments
            m_typesFragment = (TypesFragment) getSupportFragmentManager().findFragmentByTag(LISTE_TAG);
            m_typeAjoutFragment = (TypeAjoutFragment) getSupportFragmentManager().findFragmentByTag(AJOUT_TAG);
        }

        // Complète les fragments manquants
        prepareFragments();

        // Défaut : Ordre
        if (savedInstanceState == null) setupListe();

        // Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Enregistrement du status
        outState.putString(STATUS, m_status.name());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restauration de l'état recherche
        if (m_status == Status.AJOUT) {
            setupAjout();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Ajout !
        m_itemAjout = menu.add(R.string.nav_ajouter);
        m_itemAjout.setIcon(R.drawable.add_blanc);
        m_itemAjout.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        // Vider
        m_itemVider = menu.add(R.string.nav_vider);
        m_itemVider.setIcon(R.drawable.clear_all_blanc);
        m_itemVider.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // On vide le menu !
        menu.removeItem(m_itemAjout.getItemId());
        menu.removeItem(m_itemVider.getItemId());

        // On ajoute les items
        if (m_status == Status.LISTE) {
            // Ajout !
            m_itemAjout = menu.add(R.string.nav_ajouter);
            m_itemAjout.setIcon(R.drawable.add_blanc);
            m_itemAjout.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

            // Vider
            m_itemVider = menu.add(R.string.nav_vider);
            m_itemVider.setIcon(R.drawable.clear_all_blanc);
            m_itemVider.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Ajout ?
        if (item == m_itemAjout) {
            setupAjout();
            return true;
        } else if (item == m_itemVider) {
            viderDialog();
            return true;
        }

        // Retour arrière
        if (item.getItemId() == android.R.id.home && m_status == Status.AJOUT) {
            setupListe();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Méthodes
    private void prepareFragments() {
        // Ordre
        if (m_typesFragment == null) {
            m_typesFragment = new TypesFragment();
        }

        // Ajout
        if (m_typeAjoutFragment == null) {
            m_typeAjoutFragment = new TypeAjoutFragment();
        }
    }

    private void setupAjout() {
        // Gardien
        if (m_status == Status.AJOUT) return;

        // Evolution des fragments
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.layout, m_typeAjoutFragment, AJOUT_TAG);
        transaction.commit();

        // Chg de status
        m_status = Status.AJOUT;
        invalidateOptionsMenu();
    }
    private void setupListe() {
        // Gardien
        if (m_status == Status.LISTE) return;

        // Evolution des fragments
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.replace(R.id.layout, m_typesFragment, LISTE_TAG);
        transaction.commit();

        // Chg de status
        m_status = Status.LISTE;
        invalidateOptionsMenu();
    }

    private void viderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setIcon(R.drawable.ic_clear_all_black_48dp);
        builder.setTitle(R.string.dialog_vider_titre);
        builder.setMessage(R.string.dialog_vider_texte);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_typesModel.vider();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.create().show();
    }
}
