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

fun newShareCompositionsDialogFragment(
    compositionIds: LongArray,
) : ShareCompositionsDialogFragment {
    return ShareCompositionsDialogFragment().apply {
        arguments = Bundle().apply {
            putLongArray(IDS_ARG, compositionIds)
        }
    }
}

class ShareCompositionsDialogFragment: MvpAppCompatDialogFragment(), ShareCompositionsView {

    private val presenter by moxyPresenter {
        val ids = requireArguments().getLongArray(IDS_ARG)!!
        Components.getShareComponent(ids).shareCompositionsPresenter()
    }

    private lateinit var viewBinding: DialogProgressHorizontalBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewBinding = DialogProgressHorizontalBinding.inflate(LayoutInflater.from(context))

        viewBinding.btnTryAgain.setOnClickListener { presenter.onTryAgainClicked() }

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.preparing_files)
            .setView(viewBinding.root)
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
            viewBinding.progressBar.visibility = View.VISIBLE
            viewBinding.tvProgress.visibility = View.VISIBLE
            viewBinding.tvError.visibility = View.GONE
            viewBinding.btnTryAgain.visibility = View.GONE
            return
        }
        viewBinding.progressBar.visibility = View.GONE
        viewBinding.tvProgress.visibility = View.GONE
        viewBinding.tvError.visibility = View.VISIBLE
        viewBinding.tvError.text = errorCommand.message
        viewBinding.btnTryAgain.visibility = View.VISIBLE
    }

    override fun showProcessedFileCount(processed: Int, total: Int) {
        viewBinding.tvProgress.text = getString(R.string.downloading, processed, total)
    }

    override fun showDownloadingFileInfo(progressInfo: ProgressInfo) {
        viewBinding.progressBar.setProgressInfo(progressInfo)
    }
}