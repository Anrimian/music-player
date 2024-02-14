package com.github.anrimian.musicplayer.ui.editor.artist

import android.os.Bundle
import com.github.anrimian.musicplayer.Constants.Arguments.ID_ARG
import com.github.anrimian.musicplayer.Constants.Arguments.NAME_ARG
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.editor.batch.BatchEditorDialogFragment
import com.github.anrimian.musicplayer.ui.editor.batch.BatchEditorPresenter

class RenameArtistDialogFragment: BatchEditorDialogFragment<RenameArtistView>(), RenameArtistView {

    companion object {
        fun newInstance(artistId: Long, initialName: String) = RenameArtistDialogFragment().apply {
            arguments = Bundle().apply {
                putLong(ID_ARG, artistId)
                putString(NAME_ARG, initialName)
            }
        }
    }

    override fun createPresenter(): BatchEditorPresenter<RenameArtistView> {
        val id = requireArguments().getLong(ID_ARG)
        val name = requireArguments().getString(NAME_ARG)
        return Components.getArtistEditorComponent(id, name).renameArtistPresenter()
    }

    override fun getInitialValue(): String = requireArguments().getString(NAME_ARG)!!

}