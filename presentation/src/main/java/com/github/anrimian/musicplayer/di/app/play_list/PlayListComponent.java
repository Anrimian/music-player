package com.github.anrimian.musicplayer.di.app.play_list;

import com.github.anrimian.musicplayer.ui.playlist_screens.playlist.PlayListPresenter;

import dagger.Subcomponent;

@Subcomponent(modules = PlayListModule.class)
public interface PlayListComponent {

    PlayListPresenter playListPresenter();

}
