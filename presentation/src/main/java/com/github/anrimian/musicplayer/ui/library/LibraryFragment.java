package com.github.anrimian.musicplayer.ui.library;

import android.os.Bundle;
import android.view.View;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.data.preferences.UiStatePreferences;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.Screens;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.library.compositions.LibraryCompositionsFragment;
import com.github.anrimian.musicplayer.ui.library.folders.root.LibraryFoldersRootFragment;
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation;
import com.github.anrimian.musicplayer.ui.utils.moxy.ui.MvpAppCompatFragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;

public class LibraryFragment extends MvpAppCompatFragment {

    private UiStatePreferences uiStatePreferences;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.library);
        toolbar.setTitleClickListener(this::onLibraryTitleClicked);

        uiStatePreferences = Components.getAppComponent().uiStatePreferences();
    }

    private void onLibraryTitleClicked(View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view);
        popup.inflate(R.menu.library_categories_menu);
        popup.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.menu_compositions: {
                    uiStatePreferences.setSelectedLibraryScreen(Screens.LIBRARY_COMPOSITIONS);
                    FragmentNavigation.from(requireFragmentManager())
                            .newRootFragment(new LibraryCompositionsFragment());
                    break;
                }
                case R.id.menu_files: {
                    FragmentNavigation.from(requireFragmentManager())
                            .newRootFragment(new LibraryFoldersRootFragment());
                    uiStatePreferences.setSelectedLibraryScreen(Screens.LIBRARY_FOLDERS);
                    break;
                }
            }
            return true;
        });
        popup.show();
    }

}
