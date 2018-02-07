package net.capellari.showme;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.github.vivchar.viewpagerindicator.ViewPagerIndicator;

/**
 * Created by julien on 06/02/18.
 *
 * Écran de bienvenue !
 */

public class BienvenueActivity extends AppCompatActivity {
    // Attributs
    private ViewPager m_pager;
    private SlideAdapter m_slides;
    private ViewPagerIndicator m_indicator;

    // Events
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate !
        setContentView(R.layout.activity_bienvenue);

        // Gestion du pager
        m_pager     = findViewById(R.id.pager);
        m_indicator = findViewById(R.id.indicator);

        m_slides = new SlideAdapter(getSupportFragmentManager());
        m_pager.setAdapter(m_slides);

        m_indicator.setupWithViewPager(m_pager);
    }

    // Adapter
    private class SlideAdapter extends FragmentStatePagerAdapter {
        // Attributs
        // - fragments
        BienvenueFragment m_bienvenueFragment = new BienvenueFragment();
        BienvenueTypesFragment m_bienvenueTypesFragment = new BienvenueTypesFragment();
        BienvenueFinFragment m_bienvenueFinFragment = new BienvenueFinFragment();

        // Constructeur
        public SlideAdapter(FragmentManager fm) {
            super(fm);
        }

        // Méthodes
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return m_bienvenueFragment;

                case 1:
                    return m_bienvenueTypesFragment;

                case 2:
                    return m_bienvenueFinFragment;
            }

            return null;
        }

        @Override
        public int getCount() {
            return 3;
        }
    }
}
