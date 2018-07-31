package com.github.anrimian.simplemusicplayer.ui.library.compositions;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.ui.library.LibraryFragment;

import butterknife.ButterKnife;

public class CompositionsFragment extends LibraryFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library_compositions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        Toolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.library);
        toolbar.setSubtitle(R.string.compositions);
    }
}
