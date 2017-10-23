package com.github.anrimian.simplemusicplayer.ui.library.storage;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.github.anrimian.simplemusicplayer.R;

/**
 * Created on 23.10.2017.
 */

public class StorageLibraryFragment extends MvpAppCompatFragment implements StorageLibraryView {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library_storage, container, false);
    }
}
