package com.github.anrimian.musicplayer.di.app.editor.genre

import com.github.anrimian.musicplayer.ui.editor.genre.RenameGenrePresenter
import dagger.Subcomponent

@Subcomponent(modules = [ GenreEditorModule::class ])
interface GenreEditorComponent {
    fun renameGenrePresenter(): RenameGenrePresenter
}