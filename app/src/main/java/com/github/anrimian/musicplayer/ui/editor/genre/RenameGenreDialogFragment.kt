package com.github.anrimian.musicplayer.ui.editor.genre

import android.os.Bundle
import com.github.anrimian.musicplayer.Constants.Arguments.ID_ARG
import com.github.anrimian.musicplayer.Constants.Arguments.NAME_ARG
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.editor.batch.BatchEditorDialogFragment
import com.github.anrimian.musicplayer.ui.editor.batch.BatchEditorPresenter

class RenameGenreDialogFragment: BatchEditorDialogFragment<RenameGenreView>(), RenameGenreView {

    companion object {
        fun newInstance(genreId: Long, initialName: String) = RenameGenreDialogFragment().apply {
            arguments = Bundle().apply {
                putLong(ID_ARG, genreId)
                putString(NAME_ARG, initialName)
            }
        }
    }

    override fun createPresenter(): BatchEditorPresenter<RenameGenreView> {
        val id = requireArguments().getLong(ID_ARG)
        val name = requireArguments().getString(NAME_ARG)
        return Components.getGenreEditorComponent(id, name).renameGenrePresenter()
    }

    override fun getInitialValue(): String = requireArguments().getString(NAME_ARG)!!

}