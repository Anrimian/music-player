package com.github.anrimian.musicplayer.di.app.editor.artist

import com.github.anrimian.musicplayer.ui.editor.artist.RenameArtistPresenter
import dagger.Subcomponent

@Subcomponent(modules = [ ArtistEditorModule::class ])
interface ArtistEditorComponent {
    fun renameArtistPresenter(): RenameArtistPresenter
}