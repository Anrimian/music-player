package com.github.anrimian.musicplayer.ui.settings;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;

/**
 * Created on 19.10.2017.
 */

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdvancedToolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setSubtitle(null);
        toolbar.setTitleClickListener(null);
    }
}
