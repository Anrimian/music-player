package com.github.anrimian.musicplayer.ui.playlist_screens.create

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogCommonInputBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.playlist.PlayList
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import moxy.MvpAppCompatDialogFragment
import moxy.ktx.moxyPresenter

class CreatePlayListDialogFragment : MvpAppCompatDialogFragment(), CreatePlayListView {

    private val presenter by moxyPresenter { Components.getAppComponent().createPlayListsPresenter() }
    
    private lateinit var binding: DialogCommonInputBinding
    
    private lateinit var btnCreate: Button
    
    private var onCompleteListener: ((PlayList) -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogCommonInputBinding.inflate(LayoutInflater.from(requireActivity()))

        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.create_playlist)
            .setPositiveButton(R.string.create, null)
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
        
        btnCreate = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        btnCreate.setOnClickListener { onCompleteButtonClicked() }
        
        return dialog
    }

    override fun showProgress() {
        btnCreate.isEnabled = false
        binding.etInput.isEnabled = false
        binding.tvError.visibility = View.GONE
        binding.tvProgress.visibility = View.VISIBLE
        binding.progressBar.visibility = View.VISIBLE
    }

    override fun showInputState() {
        btnCreate.isEnabled = true
        binding.etInput.isEnabled = true
        binding.tvError.visibility = View.GONE
        binding.tvProgress.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
    }

    override fun showError(errorCommand: ErrorCommand) {
        btnCreate.isEnabled = true
        binding.etInput.isEnabled = true
        binding.tvProgress.visibility = View.GONE
        binding.progressBar.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = getString(R.string.create_playlist_error_template, errorCommand.message)
    }

    override fun onPlayListCreated(playList: PlayList) {
        onCompleteListener?.invoke(playList)
        dismissAllowingStateLoss()
    }

    fun setOnCompleteListener(onCompleteListener: (PlayList) -> Unit) {
        this.onCompleteListener = onCompleteListener
    }

    private fun onCompleteButtonClicked() {
        presenter.onCompleteInputButtonClicked(binding.etInput.text.toString())
    }
}