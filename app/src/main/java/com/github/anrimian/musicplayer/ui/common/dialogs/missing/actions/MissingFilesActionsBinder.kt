package com.github.anrimian.musicplayer.ui.common.dialogs.missing.actions

import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogMissingFilesBinding
import com.github.anrimian.musicplayer.domain.models.composition.AudioFileInfo
import com.github.anrimian.musicplayer.ui.common.dialogs.missing.MissingFilesDialogFragment
import com.github.anrimian.musicplayer.ui.common.dialogs.missing.MissingFilesPresenter

open class MissingFilesActionsBinder {

    open fun configureActions(
        dialog: MissingFilesDialogFragment,
        binding: DialogMissingFilesBinding,
        presenter: MissingFilesPresenter,
        audioFiles: List<AudioFileInfo>
    ) {
        binding.btnPositive.setText(R.string.remove)
        binding.btnPositive.setOnClickListener { presenter.onDeleteMissingCompositionsClicked(audioFiles) }

        binding.btnNegative.setText(R.string.close)
        binding.btnNegative.setOnClickListener { dialog.dismissAllowingStateLoss() }
    }

    open fun showRestoreProgress(binding: DialogMissingFilesBinding, show: Boolean) {}

}