package com.github.anrimian.musicplayer.di.app.editor.lyrics

import com.github.anrimian.musicplayer.ui.editor.lyrics.LyricsEditorPresenter
import dagger.Subcomponent

@Subcomponent(modules = [ LyricsEditorModule::class ])
interface LyricsEditorComponent {
    fun lyricsEditorPresenter(): LyricsEditorPresenter
}