package net.capellari.showme;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import net.capellari.showme.db.TypeBase;
import net.capellari.showme.data.DiffType;
import net.capellari.showme.data.LieuxModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by julien on 03/02/18.
 *
 * Selection de type
 */

public class SelectTypeFragment extends Fragment {
    // Constantes
    private static final String TAG = "SelectTypeFragment";

    // Attributs
    private RecyclerView m_liste;
    private RecyclerView m_listeIcone;
    private ImageButton m_bouton;

    private TypeAdapter  m_typeAdapter  = new TypeAdapter();
    private IconeAdapter m_iconeAdapter = new IconeAdapter();

    private boolean m_actif = false;
    private OnSelectTypeListener m_listener;

    private LieuxModel m_lieuxModel;
    private LiveData<List<TypeBase>> m_liveTypes;
    protected List<TypeBase> m_types = new ArrayList<>();

    // Events
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Récupération du model
        m_lieuxModel = ViewModelProviders.of(getActivity()).get(LieuxModel.class);
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);

        // Traitement des attributs
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SelectTypeFragment);

        try {
            m_actif = a.getBoolean(R.styleable.SelectTypeFragment_selecttype_actif, m_actif);
        } finally {
            a.recycle();
        }
    }

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_selecttype, container, false);

        // Gestion des listes
        m_liste = view.findViewById(R.id.liste);
        m_liste.setAdapter(m_typeAdapter);

        m_listeIcone = view.findViewById(R.id.liste_icones);
        m_listeIcone.setAdapter(m_iconeAdapter);

        // Récupération des types
        m_liveTypes = m_lieuxModel.recupTypes();
        m_liveTypes.observe(this, new Observer<List<TypeBase>>() {
            @Override
            public void onChanged(@Nullable List<TypeBase> types) {
                // Maj données
                setTypes(types);
            }
        });

        // Gestion du bouton
        m_bouton = view.findViewById(R.id.bouton);
        m_bouton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_bouton.isSelected()) {
                    m_bouton.setSelected(false);
                    m_liste.setVisibility(View.GONE);
                } else {
                    m_bouton.setSelected(true);
                    m_liste.setVisibility(View.VISIBLE);
                }
            }
        });

        // Init
        m_liste.setVisibility(m_bouton.isSelected() ? View.VISIBLE : View.GONE);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Arrêt récupérations types
        m_liveTypes.removeObservers(this);
    }

    // Menu contextuel
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // Inflate menu
        MenuInflater inflater = getActivity().getMenuInflater();
        inflater.inflate(R.menu.context_selecttype, menu);

        // header
        menu.setHeaderIcon(R.drawable.ic_filter_list_black_24dp);
        menu.setHeaderTitle(R.string.menu_select_title);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        // Click !
        boolean select = true;

        switch (item.getItemId()) {
            case R.id.menu_deselect:
                select = false;

            case R.id.menu_select:
                for (TypeBase type : m_types) {
                    if (select) {
                        selectType(type);
                    } else {
                        unSelectType(type);
                    }
                }

                return true;
        }

        return super.onContextItemSelected(item);
    }

    // Méthodes
    public void setTypes(List<TypeBase> types) {
        // Calcul et application des différences
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffType<>(m_types, types), false);
        m_types.clear();
        m_types.addAll(types);

        // Maj UI
        result.dispatchUpdatesTo(m_typeAdapter);
        result.dispatchUpdatesTo(m_iconeAdapter);
    }

    public void setListener(OnSelectTypeListener listener) {
        m_listener = listener;
    }

    private void selectType(TypeBase type) {
        // chg de status
        m_lieuxModel.setFiltreType(type._id, true);

        // Maj UI
        int pos = m_types.indexOf(type);
        m_typeAdapter.notifyItemChanged(pos);
        m_iconeAdapter.notifyItemChanged(pos);

        // listener
        if (m_listener != null) {
            m_listener.onSelectType(type);
        }
    }
    private void unSelectType(TypeBase type) {
        // chg de status
        m_lieuxModel.setFiltreType(type._id, false);

        // Maj UI
        int pos = m_types.indexOf(type);
        m_typeAdapter.notifyItemChanged(pos);
        m_iconeAdapter.notifyItemChanged(pos);

        // listener
        if (m_listener != null) {
            m_listener.onUnSelectType(type);
        }
    }

    // Listener
    public interface OnSelectTypeListener {
        void onSelectType(TypeBase type);
        void onUnSelectType(TypeBase type);
    }

    // ViewHolders
    private abstract class AbstractViewHolder extends RecyclerView.ViewHolder {
        // Attributs
        protected TypeBase m_type;

        // Constructeur
        public AbstractViewHolder(View itemView) {
            super(itemView);
        }

        // Méthodes
        public abstract void setType(TypeBase type);
        protected void setupView(final View view) {
            // Click !
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Gardiens
                    if (m_type == null) return;
                    if (!m_actif) return;

                    // Toggle !
                    if (view.isSelected()) {
                        unSelectType(m_type);
                    } else {
                        selectType(m_type);
                    }
                }
            });
        }
    }
    private class TypeViewHolder extends AbstractViewHolder {
        private TextView m_nom;

        // Constructeur
        public TypeViewHolder(View itemView) {
            super(itemView);

            // Récupération des vues
            m_nom = itemView.findViewById(R.id.nom);
            setupView(m_nom);

            if (m_actif) registerForContextMenu(m_nom);
        }

        // Méthodes
        @Override
        public void setType(final TypeBase type) {
            m_type = type;

            // Remplissage
            m_nom.setText(type.nom);
            m_nom.setSelected(!m_actif || m_lieuxModel.getFiltreType(type._id));
            m_nom.setCompoundDrawablesRelativeWithIntrinsicBounds(
                    TypeBase.getIconRessource((int) type._id),
                    0, 0,0
            );
        }
    }
    private class IconeViewHolder extends AbstractViewHolder {
        // Attributs
        private ImageView m_icone;

        // Constructeur
        public IconeViewHolder(View itemView) {
            super(itemView);

            // Récupération des vues
            m_icone = itemView.findViewById(R.id.icone);
            setupView(m_icone);
        }

        // Méthodes
        @Override
        public void setType(TypeBase type) {
            m_type = type;

            // Remplissage
            m_icone.setSelected(!m_actif || m_lieuxModel.getFiltreType(type._id));
            m_icone.setImageDrawable(TypeBase.getIcone(getContext(), (int) type._id));
        }
    }

    // Adapters
    private abstract class AbstractAdapter<TVH extends AbstractViewHolder> extends RecyclerView.Adapter<TVH> {
        // Events
        @Override
        public void onBindViewHolder(TVH holder, int position) {
            holder.setType(m_types.get(position));
        }

        @Override
        public int getItemCount() {
            return m_types.size();
        }
    }
    private class TypeAdapter extends AbstractAdapter<TypeViewHolder> {
        @Override
        public TypeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_type, parent, false);
            return new TypeViewHolder(view);
        }
    }
    private class IconeAdapter extends AbstractAdapter<IconeViewHolder> {
        // Events
        @Override
        public IconeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_typeicone, parent, false);
            return new IconeViewHolder(view);
        }
    }
}
