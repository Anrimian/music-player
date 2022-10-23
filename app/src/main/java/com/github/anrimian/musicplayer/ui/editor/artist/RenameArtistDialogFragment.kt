package com.github.anrimian.musicplayer.ui.editor.artist

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import com.github.anrimian.filesync.models.ProgressInfo
import com.github.anrimian.musicplayer.Constants.Arguments.ID_ARG
import com.github.anrimian.musicplayer.Constants.Arguments.NAME_ARG
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogCommonInputBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.asInt
import com.github.anrimian.musicplayer.ui.common.format.setExtProgress
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.text_view.SimpleTextWatcher
import moxy.MvpAppCompatDialogFragment
import moxy.ktx.moxyPresenter

fun newRenameArtistDialog(artistId: Long, initialName: String) = RenameArtistDialogFragment().apply {
    arguments = Bundle().apply {
        putLong(ID_ARG, artistId)
        putString(NAME_ARG, initialName)
    }
}

class RenameArtistDialogFragment: MvpAppCompatDialogFragment(), RenameArtistView {

    private val presenter by moxyPresenter {
        val id = requireArguments().getLong(ID_ARG)
        val name = requireArguments().getString(NAME_ARG)
        Components.getArtistEditorComponent(id, name).renameArtistPresenter()
    }

    private lateinit var viewBinding: DialogCommonInputBinding
    private lateinit var btnChange: Button

    private lateinit var errorHandler: ErrorHandler

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewBinding = DialogCommonInputBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.edit_name)
            .setPositiveButton(R.string.change, null)
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setView(viewBinding.root)
            .create()
        AndroidUtils.setSoftInputVisible(dialog.window)
        dialog.show()

        if (savedInstanceState == null) {
            ViewUtils.setEditableText(viewBinding.etInput, requireArguments().getString(NAME_ARG))
        }

        viewBinding.etInput.imeOptions = EditorInfo.IME_ACTION_DONE
        viewBinding.etInput.setRawInputType(InputType.TYPE_CLASS_TEXT)
        viewBinding.etInput.setOnEditorActionListener { _, _, _ ->
            presenter.onChangeButtonClicked()
            true
        }
        SimpleTextWatcher.onTextChanged(viewBinding.etInput, presenter::onInputTextChanged)
        viewBinding.etInput.requestFocus()

        btnChange = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        btnChange.setOnClickListener { presenter.onChangeButtonClicked() }

        errorHandler = ErrorHandler(
            this,
            presenter::onRetryFailedEditActionClicked
        ) { showError(getString(R.string.android_r_edit_file_permission_denied)) }

        return dialog
    }

    override fun showProgress() {
        btnChange.isEnabled = false
        viewBinding.etInput.isEnabled = false
        viewBinding.tvError.visibility = View.GONE
        viewBinding.tvProgress.visibility = View.VISIBLE
        viewBinding.tvProgress.setText(R.string.rename_progress)
        viewBinding.progressBar.visibility = View.VISIBLE
    }

    override fun showInputState() {
        btnChange.isEnabled = true
        viewBinding.etInput.isEnabled = true
        viewBinding.tvError.visibility = View.GONE
        viewBinding.tvProgress.visibility = View.GONE
        viewBinding.progressBar.visibility = View.GONE
    }

    override fun showError(errorCommand: ErrorCommand) {
        errorHandler.handleError(errorCommand) {
            showError(errorCommand.message)
        }
    }

    override fun closeScreen() {
        dismissAllowingStateLoss()
    }

    override fun showPreparedFilesCount(processed: Int, total: Int) {
        viewBinding.tvProgress.text = getString(R.string.downloading, processed, total)
    }

    override fun showDownloadingFileInfo(progressInfo: ProgressInfo) {
        viewBinding.progressBar.setExtProgress(progressInfo.asInt())
    }

    override fun showEditedFilesCount(processed: Int, total: Int) {
        val message = if (total > 1) {
            getString(R.string.rename_progress_count, processed, total)
        } else {
            getString(R.string.rename_progress)
        }
        viewBinding.tvProgress.text = message
    }

    override fun showChangeAllowed(enabled: Boolean) {
        btnChange.isEnabled = enabled
    }

    private fun showError(message: String) {
        btnChange.isEnabled = true
        viewBinding.etInput.isEnabled = true
        viewBinding.tvProgress.visibility = View.GONE
        viewBinding.progressBar.visibility = View.GONE
        viewBinding.tvError.visibility = View.VISIBLE
        viewBinding.tvError.text = message
    }

}