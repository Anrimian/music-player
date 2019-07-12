package com.github.anrimian.musicplayer.di.app.editor;

import com.github.anrimian.musicplayer.ui.editor.CompositionEditorPresenter;

import dagger.Subcomponent;

@Subcomponent(modules = CompositionEditorModule.class)
public interface CompositionEditorComponent {
    CompositionEditorPresenter compositionEditorPresenter();
}
