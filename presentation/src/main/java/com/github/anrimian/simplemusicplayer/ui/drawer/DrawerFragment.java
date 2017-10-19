package com.github.anrimian.simplemusicplayer.ui.drawer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.github.anrimian.simplemusicplayer.R;

/**
 * Created on 19.10.2017.
 */

public class DrawerFragment extends MvpAppCompatFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_drawer, container, false);
    }
}
