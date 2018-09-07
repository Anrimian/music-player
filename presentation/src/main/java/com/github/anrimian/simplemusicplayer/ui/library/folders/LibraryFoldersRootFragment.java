package com.github.anrimian.simplemusicplayer.ui.library.folders;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.simplemusicplayer.ui.library.LibraryFragment;
import com.github.anrimian.simplemusicplayer.ui.utils.fragments.BackButtonListener;

public class LibraryFoldersRootFragment extends LibraryFragment implements BackButtonListener {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_root_library_folders, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdvancedToolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setSubtitle(R.string.files);

        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.library_folders_container, LibraryFoldersFragment.newInstance(null))
                    .commit();
        }
    }

    @Override
    public boolean onBackPressed() {
        FragmentManager fm = getChildFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.library_folders_container);
        return fragment instanceof BackButtonListener
                && ((BackButtonListener) fragment).onBackPressed();
    }
}
