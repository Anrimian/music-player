package com.github.anrimian.musicplayer.ui.library

import android.os.Bundle
import android.view.Gravity
import android.view.View
import androidx.fragment.app.FragmentManager
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.Screens
import com.github.anrimian.musicplayer.domain.repositories.UiStateRepository
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.library.albums.list.AlbumsListFragment
import com.github.anrimian.musicplayer.ui.library.artists.list.ArtistsListFragment
import com.github.anrimian.musicplayer.ui.library.compositions.LibraryCompositionsFragment
import com.github.anrimian.musicplayer.ui.library.folders.root.LibraryFoldersRootFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import moxy.MvpAppCompatFragment

open class LibraryFragment : MvpAppCompatFragment(), FragmentLayerListener {
    
    private lateinit var uiStateRepository: UiStateRepository
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        uiStateRepository = Components.getAppComponent().uiStateRepository()
    }

    override fun onFragmentMovedOnTop() {
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.library)
        toolbar.setTitleClickListener(this::onLibraryTitleClicked)
    }

    private fun onLibraryTitleClicked(view: View) {
        PopupMenuWindow.showPopup(view, R.menu.library_categories_menu, Gravity.BOTTOM) { item ->
            //can be null in some state, don't know which
            val fm = safeGetFragmentManager() ?: return@showPopup

            val navigation = FragmentNavigation.from(fm)
            when (item.itemId) {
                R.id.menu_compositions -> {
                    uiStateRepository.selectedLibraryScreen = Screens.LIBRARY_COMPOSITIONS
                    navigation.newRootFragment(LibraryCompositionsFragment())
                }
                R.id.menu_files -> {
                    navigation.newRootFragment(LibraryFoldersRootFragment())
                    uiStateRepository.selectedLibraryScreen = Screens.LIBRARY_FOLDERS
                }
                R.id.menu_artists -> {
                    uiStateRepository.selectedLibraryScreen = Screens.LIBRARY_ARTISTS
                    navigation.newRootFragment(ArtistsListFragment())
                }
                R.id.menu_albums -> {
                    uiStateRepository.selectedLibraryScreen = Screens.LIBRARY_ALBUMS
                    navigation.newRootFragment(AlbumsListFragment())
                }
                //<return genres after deep scan implementation>
//                        case R.id.menu_genres: {
//                            uiStateRepository.setSelectedLibraryScreen(Screens.LIBRARY_GENRES);
//                            navigation.newRootFragment(new GenresListFragment());
//                            break;
//                        }
            }
        }
    }

    private fun safeGetFragmentManager(): FragmentManager? {
        return try {
            parentFragmentManager
        } catch (e: IllegalStateException) {
            null
        }
    }

    protected fun parentFragmentManager(): FragmentManager {
        return parentFragmentManager
    }
}