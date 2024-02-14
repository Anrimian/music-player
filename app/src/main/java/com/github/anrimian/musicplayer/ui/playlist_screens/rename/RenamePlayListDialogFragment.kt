package com.github.anrimian.musicplayer.ui.playlist_screens.rename

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogCommonInputBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import moxy.MvpAppCompatDialogFragment
import moxy.ktx.moxyPresenter

fun newRenamePlaylistDialog(playListId: Long) = RenamePlayListDialogFragment().apply {
    arguments = Bundle().apply {
        putLong(Constants.Arguments.PLAY_LIST_ID_ARG, playListId)
    }
}

class RenamePlayListDialogFragment : MvpAppCompatDialogFragment(), RenamePlayListView {

    private val presenter by moxyPresenter {
        Components.getPlayListComponent(getPlayListId()).changePlayListPresenter()
    }

    private lateinit var binding: DialogCommonInputBinding

    private lateinit var btnChange: Button

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogCommonInputBinding.inflate(LayoutInflater.from(context))
        
        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.edit_name)
            .setPositiveButton(R.string.change, null)
            .setNegativeButton(R.string.cancel) { _, _ -> }
            .setView(binding.root)
            .create()
        AndroidUtils.setSoftInputVisible(dialog.window)
        dialog.show()
        
        binding.etInput.imeOptions = EditorInfo.IME_ACTION_DONE
        binding.etInput.setRawInputType(InputType.TYPE_CLASS_TEXT)
        binding.etInput.setOnEditorActionListener { _, _, _ -> 
            onCompleteButtonClicked()
            true
        }
        binding.etInput.requestFocus()
        btnChange = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        btnChange.setOnClickListener { onCompleteButtonClicked() }
        return dialog
    }

    override fun showProgress() {
        btnChange.isEnabled = false
        binding.etInput.isEnabled = false
        binding.tvError.visibility = View.GONE
        binding.tvProgress.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun showInputState() {
        btnChange.isEnabled = true
        binding.etInput.isEnabled = true
        binding.tvError.visibility = View.GONE
        binding.tvProgress.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    override fun showError(errorCommand: ErrorCommand) {
        btnChange.isEnabled = true
        binding.etInput.isEnabled = true
        binding.tvProgress.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = getString(
            R.string.change_playlist_name_error_template,
            errorCommand.message
        )
    }

    override fun showPlayListName(initialName: String?) {
        if (binding.etInput.length() == 0) {
            ViewUtils.setEditableText(binding.etInput, initialName)
        }
    }

    override fun closeScreen() {
        dismissAllowingStateLoss()
    }

    private fun onCompleteButtonClicked() {
        presenter.onCompleteInputButtonClicked(binding.etInput.text.toString())
    }

    private fun getPlayListId() = requireArguments().getLong(Constants.Arguments.PLAY_LIST_ID_ARG)

}