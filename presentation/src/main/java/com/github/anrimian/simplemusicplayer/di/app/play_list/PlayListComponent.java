package com.github.anrimian.simplemusicplayer.di.app.play_list;

import com.github.anrimian.simplemusicplayer.ui.playlist_screens.playlist.PlayListPresenter;

import dagger.Subcomponent;

@Subcomponent(modules = PlayListModule.class)
public interface PlayListComponent {

    PlayListPresenter playListPresenter();

}
