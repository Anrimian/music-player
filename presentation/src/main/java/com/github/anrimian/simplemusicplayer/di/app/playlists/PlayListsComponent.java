package com.github.anrimian.simplemusicplayer.di.app.playlists;

import com.github.anrimian.simplemusicplayer.ui.playlist_screens.create.CreatePlayListPresenter;
import com.github.anrimian.simplemusicplayer.ui.playlist_screens.playlists.PlayListsPresenter;

import dagger.Subcomponent;

@PlayListsScope
@Subcomponent(modules = PlayListsModule.class)
public interface PlayListsComponent {

    PlayListsPresenter playListsPresenter();
    CreatePlayListPresenter createPlayListsPresenter();
}
