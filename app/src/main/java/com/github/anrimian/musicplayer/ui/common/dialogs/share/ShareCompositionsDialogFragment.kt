package com.github.anrimian.musicplayer.ui.common.dialogs.share

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.github.anrimian.filesync.models.ProgressInfo
import com.github.anrimian.musicplayer.Constants.Arguments.IDS_ARG
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogProgressHorizontalBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.content.CompositionContentSource
import com.github.anrimian.musicplayer.ui.common.dialogs.launchShareSourcesActivity
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.setProgressInfo
import moxy.MvpAppCompatDialogFragment
import moxy.ktx.moxyPresenter

class ShareCompositionsDialogFragment: MvpAppCompatDialogFragment(), ShareCompositionsView {

    companion object {
        fun newInstance(compositionIds: LongArray) = ShareCompositionsDialogFragment().apply {
            arguments = Bundle().apply {
                putLongArray(IDS_ARG, compositionIds)
            }
        }
    }

    private val presenter by moxyPresenter {
        val ids = requireArguments().getLongArray(IDS_ARG)!!
        Components.getShareComponent(ids).shareCompositionsPresenter()
    }

    private lateinit var binding: DialogProgressHorizontalBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogProgressHorizontalBinding.inflate(LayoutInflater.from(context))

        binding.btnTryAgain.setOnClickListener { presenter.onTryAgainClicked() }

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.preparing_files)
            .setView(binding.root)
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.dismiss() }
            .create()
        dialog.show()

        return dialog
    }

    override fun showShareDialog(sources: ArrayList<CompositionContentSource>) {
        launchShareSourcesActivity(requireContext(), sources)
        dismissAllowingStateLoss()
    }

    override fun showShareError(errorCommand: ErrorCommand?) {
        if (errorCommand == null) {
            binding.progressBar.visibility = View.VISIBLE
            binding.tvProgress.visibility = View.VISIBLE
            binding.tvError.visibility = View.GONE
            binding.btnTryAgain.visibility = View.GONE
            return
        }
        binding.progressBar.visibility = View.GONE
        binding.tvProgress.visibility = View.GONE
        binding.tvError.visibility = View.VISIBLE
        binding.tvError.text = errorCommand.message
        binding.btnTryAgain.visibility = View.VISIBLE
    }

    override fun showProcessedFileCount(processed: Int, total: Int) {
        binding.tvProgress.text = getString(R.string.downloading, processed, total)
    }

    override fun showDownloadingFileInfo(progressInfo: ProgressInfo) {
        binding.progressBar.setProgressInfo(progressInfo)
    }
}