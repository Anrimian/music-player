package com.github.anrimian.musicplayer.ui.library;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.PopupMenu;
import android.view.View;

import com.arellomobile.mvp.MvpAppCompatFragment;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.library.compositions.LibraryCompositionsFragment;
import com.github.anrimian.musicplayer.ui.library.folders.LibraryFoldersFragment;
import com.github.anrimian.musicplayer.ui.library.folders.LibraryFoldersRootFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener;

import static com.github.anrimian.musicplayer.ui.utils.fragments.FragmentUtils.startFragment;

public class LibraryFragment extends MvpAppCompatFragment {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdvancedToolbar toolbar = getActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.library);
        toolbar.setTitleClickListener(this::onLibraryTitleClicked);
    }

    private void onLibraryTitleClicked(View view) {
        PopupMenu popup = new PopupMenu(getContext(), view);
        popup.inflate(R.menu.library_categories_menu);
        popup.setOnMenuItemClickListener(item -> {
            LibraryFragment fragment = null;
            switch (item.getItemId()) {
                case R.id.menu_compositions: {
                    fragment = new LibraryCompositionsFragment();
                    break;
                }
                case R.id.menu_files: {
                    fragment = new LibraryFoldersRootFragment();
                    break;
                }
            }
            startFragment(fragment, getFragmentManager(), R.id.drawer_fragment_container);
            return true;
        });
        popup.show();
    }

}
