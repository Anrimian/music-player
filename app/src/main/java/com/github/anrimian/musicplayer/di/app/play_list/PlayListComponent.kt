package com.github.anrimian.musicplayer.di.app.play_list


import com.github.anrimian.musicplayer.ui.playlists.rename.RenamePlayListPresenter
import dagger.Subcomponent

@Subcomponent(modules = [ PlayListModule::class ])
interface PlayListComponent {

    fun changePlayListPresenter(): RenamePlayListPresenter

}
