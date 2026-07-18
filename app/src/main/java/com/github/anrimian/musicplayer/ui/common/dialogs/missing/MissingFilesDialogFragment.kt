package com.github.anrimian.musicplayer.ui.common.dialogs.missing

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.method.LinkMovementMethod
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.view.isVisible
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogMissingFilesBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.AudioFileInfo
import com.github.anrimian.musicplayer.domain.models.composition.DeletedComposition
import com.github.anrimian.musicplayer.ui.common.dialogs.missing.actions.MissingFilesActionsBinder
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.format.formatExpandableTextList
import com.github.anrimian.musicplayer.ui.common.format.formatFilePath
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr
import moxy.MvpAppCompatDialogFragment
import moxy.ktx.moxyPresenter

class MissingFilesDialogFragment : MvpAppCompatDialogFragment(), MissingFilesView {

    private val presenter by moxyPresenter { Components.getAppComponent().missingFilesPresenter() }

    private lateinit var binding: DialogMissingFilesBinding

    private lateinit var actionsBinder: MissingFilesActionsBinder

    private var audioFiles: List<AudioFileInfo> = emptyList()
    private var currentErrors: Map<AudioFileInfo, ErrorCommand> = emptyMap()
    private var isExpanded: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogMissingFilesBinding.inflate(LayoutInflater.from(context))
        actionsBinder = Components.getAppComponent().missingFilesActionsBinder()

        binding.tvMissingCompositionsDescription.movementMethod = LinkMovementMethod.getInstance()

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.missing_files_detected)
            .setView(binding.root)
            .create()

        dialog.show()

        return dialog
    }

    override fun showMissingAudioFiles(audioFiles: List<AudioFileInfo>) {
        if (audioFiles.isEmpty()) {
            dismissAllowingStateLoss()
            return
        }
        this.audioFiles = audioFiles
        displayMissingFilesDescription()

        actionsBinder.configureActions(this, binding, presenter, audioFiles)
    }

    override fun showRestoreInProgress(show: Boolean) {
        binding.tvProgress.isVisible = show
        actionsBinder.showRestoreProgress(binding, show)
    }

    override fun showRestoreErrors(errors: Map<AudioFileInfo, ErrorCommand>) {
        this.currentErrors = errors
        displayMissingFilesDescription()
    }

    override fun showConfirmDeleteMissingCompositionsDialog(files: List<AudioFileInfo>) {
        val message = Components.getAppComponent()
            .messageTextFormatter()
            .getConfirmDeleteMissingCompositionsText(requireContext(), files.size)
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.delete_missing_compositions_title)
            .setMessage(message)
            .setPositiveButton(R.string.delete) { _, _ -> presenter.onDeleteMissingCompositionsConfirmed() }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    override fun onMissingCompositionsDeleted(deletedCompositions: List<DeletedComposition>) {
        val message = MessagesUtils.getDeleteCompleteMessage(requireContext(), deletedCompositions)
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()

        dismissAllowingStateLoss()
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        Toast.makeText(requireContext(), errorCommand.message, Toast.LENGTH_LONG).show()
    }

    private fun displayMissingFilesDescription() {
        val textView = binding.tvMissingCompositionsDescription
        val descriptionResId = Components.getAppComponent().messageTextFormatter().getMissingFilesDescriptionResId()
        val description = getString(descriptionResId, audioFiles.size)
        val message = getMissingFilesDescription(audioFiles, description, isExpanded) {
            isExpanded = true
            displayMissingFilesDescription()
        }
        textView.text = message
    }

    private fun getMissingFilesDescription(
        files: List<AudioFileInfo>,
        description: String,
        isExpanded: Boolean,
        onExpandButtonClick: () -> Unit = {}
    ): SpannableStringBuilder {
        return formatExpandableTextList(
            requireContext(),
            files,
            description,
            isExpanded,
            { file ->
                val sb = SpannableStringBuilder(formatFilePath(file.parentPath, file.fileName))
                val error = currentErrors[file]
                if (error != null) {
                    sb.append(" - ")
                    val start = sb.length
                    sb.append(error.message)
                    sb.setSpan(
                        ForegroundColorSpan(requireContext().colorFromAttr(R.attr.colorError)),
                        start,
                        sb.length,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
                sb
            },
            itemPrefixDrawableRes = R.drawable.ic_primary_text_circle,
            onExpandButtonClick = onExpandButtonClick
        )
    }

}