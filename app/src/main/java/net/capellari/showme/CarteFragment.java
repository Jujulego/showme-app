package net.capellari.showme;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import net.capellari.showme.data.LieuxModel;
import net.capellari.showme.data.PositionSource;
import net.capellari.showme.db.Lieu;

import java.util.List;

/**
 * Created by julien on 04/02/18.
 *
 * Wrap de la carte !
 */

public class CarteFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {
    // Attributs
    private GoogleMap m_map;
    private boolean m_centree;
    private SupportMapFragment m_fragmentMap;

    private LiveData<Location> m_location;
    private PositionSource m_positionSource;

    private LieuxModel m_lieuxModel;
    private LiveData<List<Lieu>> m_lieux;

    private OnCarteEventListener m_listener;

    // Events
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Récupération du model
        m_lieuxModel = ViewModelProviders.of(getActivity()).get(LieuxModel.class);

        // Récupération position
        m_positionSource = m_lieuxModel.getPositionSource(getActivity());

        m_location = m_positionSource.getLocation();
        m_location.observe(this, new LocationObs());

        // Récupération des lieux
        m_lieux = m_lieuxModel.recupLieux();
        m_lieux.observe(this, new LieuxObs());
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate !
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        // Récupération de la carte
        m_fragmentMap = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.carte);
        m_fragmentMap.getMapAsync(this);
        m_centree = false;

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Vidage !
        m_map = null;
        m_fragmentMap = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();

        // Reset listener
        m_listener = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Arrêt des mise à jours
        m_lieux.removeObservers(this);
        m_location.removeObservers(this);
    }

    // Map
    @Override
    public void onMapReady(GoogleMap map) {
        m_map = map;

        // Paramétrage de la carte
        m_map.setOnInfoWindowClickListener(this);
        m_map.setLocationSource(m_positionSource);
        if (m_positionSource.checkLocationPermission()) {
            m_map.setMyLocationEnabled(true);
        }

        // Ajout des marqueurs
        m_lieuxModel.maj_ui();

        // Transmission de l'event
        if (m_listener != null) m_listener.onMapReady(map);
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Object obj = marker.getTag();

        if (obj instanceof Lieu && m_listener != null) {
            m_listener.onMarkerClick((Lieu) obj);
        }
    }

    // Méthode
    @Nullable
    public Marker ajouterLieu(Lieu lieu) {
        // Gardien
        if (m_map == null) return null;

        // Ajout du marqueur
        Marker marker = m_map.addMarker(new MarkerOptions()
                .title(lieu.nom)
                .position(lieu.getLatLng())
        );
        marker.setTag(lieu);

        return marker;
    }
    public void setOnCarteEventListener(OnCarteEventListener listener) {
        m_listener = listener;

        // Si on a déjà reçu la carte
        if (m_map != null) m_listener.onMapReady(m_map);
    }

    // Listener
    public interface OnCarteEventListener {
        void onMapReady(@NonNull GoogleMap map);
        void onMarkerClick(@NonNull Lieu lieu);
    }

    // Observers
    private class LocationObs implements Observer<Location> {
        @Override
        public void onChanged(@Nullable Location location) {
            // Gardiens
            if (m_map == null) return;
            if (location == null) return;

            // Centrage carte
            if (m_centree) {
                m_map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(
                        location.getLatitude(), location.getLongitude()
                )));

            } else {
                CameraPosition.Builder builder = new CameraPosition.Builder();
                builder.target(new LatLng(
                        location.getLatitude(), location.getLongitude()
                )).zoom(15).tilt(45);

                m_centree = true;
                m_map.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
            }
        }
    }
    private class LieuxObs implements Observer<List<Lieu>> {
        @Override
        public void onChanged(@Nullable List<Lieu> lieux) {
            // Gardiens
            if (lieux == null) return;
            if (m_map == null) return;

            // Nettoyage des marqueurs
            m_map.clear();

            // Ajout des marqueurs
            for (Lieu lieu : lieux) {
                ajouterLieu(lieu);
            }
        }
    }
}
