package com.github.anrimian.musicplayer.ui.utils.fragments.navigation;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class NavigationFragment extends Fragment {

    private FragmentNavigation fragmentNavigation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    FragmentNavigation getFragmentNavigation() {
        if (fragmentNavigation == null) {
            fragmentNavigation = new FragmentNavigation(this::safeGetFragmentManager);
        }
        return fragmentNavigation;
    }

    @Nullable
    private FragmentManager safeGetFragmentManager() {
        try {
            //now we can get IllegalStateException also
            return getParentFragmentManager();
        } catch (Exception e) {
            return null;
        }
    }
}
