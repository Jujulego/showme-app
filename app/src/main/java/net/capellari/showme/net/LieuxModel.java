package net.capellari.showme.net;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.LongSparseArray;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;

import net.capellari.showme.R;
import net.capellari.showme.db.Lieu;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by julien on 29/01/18.
 *
 * Gestion et sauvegarde des requetes sur lieu
 */
public class LieuxModel extends AndroidViewModel {
    // Constantes
    private static final String TAG = "LieuModel";

    // Attributs
    private LongSparseArray<LiveLieu> m_cache = new LongSparseArray<>();

    // Constructeur
    public LieuxModel(@NonNull Application application) {
        super(application);
    }

    // Méthodes
    public LiveData<Lieu> recup(long id) {
        // Check
        LiveLieu lieu = m_cache.get(id, null);

        // Lancement si jamais lancé
        if (lieu == null) {
            lieu = new LiveLieu(this.getApplication(), id);
            m_cache.append(id, lieu);
        }

        return lieu;
    }
}
