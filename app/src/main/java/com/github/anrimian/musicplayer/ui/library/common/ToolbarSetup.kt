package com.github.anrimian.musicplayer.ui.library.common

import android.view.Gravity
import androidx.fragment.app.Fragment
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.Screens
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.library.albums.list.AlbumsListFragment
import com.github.anrimian.musicplayer.ui.library.artists.list.ArtistsListFragment
import com.github.anrimian.musicplayer.ui.library.compositions.LibraryCompositionsFragment
import com.github.anrimian.musicplayer.ui.library.folders.root.newLibraryFoldersRootFragment
import com.github.anrimian.musicplayer.ui.library.genres.list.GenresListFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation

fun AdvancedToolbar.setupLibraryTitle(fragment: Fragment) {
    setTitle(R.string.library)
    setTitleClickListener { view ->
        val fm = try {
            fragment.parentFragmentManager
        } catch (ignored: Exception) {
            return@setTitleClickListener
        }
        val navigation = FragmentNavigation.from(fm)
        PopupMenuWindow.showPopup(view, R.menu.library_categories_menu, Gravity.BOTTOM) { item ->
            val uiStateRepository = Components.getAppComponent().uiStateRepository()
            when (item.itemId) {
                R.id.menu_compositions -> {
                    uiStateRepository.selectedLibraryScreen = Screens.LIBRARY_COMPOSITIONS
                    navigation.newRootFragment(LibraryCompositionsFragment())
                }
                R.id.menu_files -> {
                    navigation.newRootFragment(newLibraryFoldersRootFragment())
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
                R.id.menu_genres -> {
                    uiStateRepository.selectedLibraryScreen = Screens.LIBRARY_GENRES
                    navigation.newRootFragment(GenresListFragment())
                }
            }
        }
    }
}