package com.github.anrimian.musicplayer.ui.utils.fragments.navigation;

public interface FragmentNavigationListener {

    /**
     * Be careful, it can call before onCreateView().
     * Can be useful for update come common ui, like title in toolbar
     */
    void onFragmentResumed();
}
