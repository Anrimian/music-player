package com.github.anrimian.musicplayer.ui.utils.fragments.navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

public class NavigationFragment extends Fragment {

    private FragmentNavigation fragmentNavigation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    FragmentNavigation getFragmentNavigation() {
        if (fragmentNavigation == null) {
            fragmentNavigation = new FragmentNavigation(this::getFragmentManager);
        }
        return fragmentNavigation;
    }
}
