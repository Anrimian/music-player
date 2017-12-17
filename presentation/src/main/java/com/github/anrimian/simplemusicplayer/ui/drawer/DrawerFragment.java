package com.github.anrimian.simplemusicplayer.ui.drawer;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.ui.library.main.LibraryFragment;
import com.github.anrimian.simplemusicplayer.ui.settings.SettingsFragment;
import com.github.anrimian.simplemusicplayer.ui.start.StartFragment;
import com.github.anrimian.simplemusicplayer.utils.fragments.BackButtonListener;
import com.github.anrimian.simplemusicplayer.utils.view_pager.FragmentCreator;
import com.tbruyelle.rxpermissions2.RxPermissions;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created on 19.10.2017.
 */

public class DrawerFragment extends MvpAppCompatFragment implements BackButtonListener {

    private static final int NO_ITEM = -1;
    private static final String SELECTED_DRAWER_ITEM = "selected_drawer_item";

    private static final SparseArray<FragmentCreator> fragmentIdMap = new SparseArray<>();

    static {
        fragmentIdMap.put(R.id.menu_library, LibraryFragment::new);
        fragmentIdMap.put(R.id.menu_settings, SettingsFragment::new);
    }

    @BindView(R.id.drawer)
    DrawerLayout drawer;

    @BindView(R.id.navigation_view)
    NavigationView navigationView;

    private ActionBarDrawerToggle drawerToggle;

    private int selectedDrawerItemId = NO_ITEM;
    private int itemIdToStart = NO_ITEM;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drawer, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        ButterKnife.bind(this, view);

        RxPermissions rxPermissions = new RxPermissions(getActivity());
        if (!rxPermissions.isGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.main_activity_container, new StartFragment())
                    .commit();
        }

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (selectedDrawerItemId != itemId) {
                    selectedDrawerItemId = itemId;
                    itemIdToStart = itemId;
                    clearFragment();
                }
                drawer.closeDrawer(Gravity.START);

            return true;
        });

        drawerToggle = new ActionBarDrawerToggle(getActivity(), drawer, R.string.open_drawer, R.string.close_drawer);
        drawerToggle.setDrawerIndicatorEnabled(true);
        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (itemIdToStart != NO_ITEM) {
                    FragmentCreator fragmentCreator = fragmentIdMap.get(itemIdToStart);
                    startFragment(fragmentCreator.createFragment());
                    itemIdToStart = NO_ITEM;
                }
            }
        });

        //View headerView = navigationView.getHeaderView(0);

        Fragment currentFragment = getFragmentManager().findFragmentById(R.id.drawer_fragment_container);
        if (currentFragment == null || savedInstanceState == null) {
            showLibraryScreen();
        } else {
            selectedDrawerItemId = savedInstanceState.getInt(SELECTED_DRAWER_ITEM, NO_ITEM);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean toggleSelected = drawerToggle.onOptionsItemSelected(item);
        return toggleSelected || super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_DRAWER_ITEM, selectedDrawerItemId);//maybe another solution?
    }

    @Override
    public boolean onBackPressed() {
        Fragment fragment = getChildFragmentManager().findFragmentById(R.id.drawer_fragment_container);
        return fragment instanceof BackButtonListener && ((BackButtonListener) fragment).onBackPressed();
    }

    private void showLibraryScreen() {
        selectedDrawerItemId = R.id.menu_library;
        navigationView.setCheckedItem(selectedDrawerItemId);
        startFragment(new LibraryFragment());
    }

    private void startFragment(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .setCustomAnimations(R.anim.anim_alpha_appear, R.anim.anim_alpha_disappear)
                .replace(R.id.drawer_fragment_container, fragment)
                .commit();
    }

    private void clearFragment() {
        FragmentManager fm = getFragmentManager();
        Fragment currentFragment = fm.findFragmentById(R.id.drawer_fragment_container);
        if (currentFragment != null) {
            fm.beginTransaction()
                    .remove(currentFragment)
                    .commit();
        }
    }
}
