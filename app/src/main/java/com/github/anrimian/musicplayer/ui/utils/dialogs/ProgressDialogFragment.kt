package com.github.anrimian.musicplayer.ui.utils.dialogs

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.common.format.indeterminate
import com.github.anrimian.musicplayer.ui.common.format.setExtProgress
import com.google.android.material.progressindicator.CircularProgressIndicator

fun newProgressDialogFragment(message: String?) = ProgressDialogFragment().apply {
    arguments = Bundle().apply {
        putString(Constants.Tags.MESSAGE_ARG, message)
    }
}

fun newProgressDialogFragment(@StringRes messageResId: Int) = ProgressDialogFragment().apply {
    arguments = Bundle().apply {
        putInt(Constants.Tags.MESSAGE_RES_ARG, messageResId)
    }
}

class ProgressDialogFragment : DialogFragment() {

    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var tvProgress: TextView

    private var cancelListener: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = View.inflate(activity, R.layout.dialog_progress, null)

        progressBar = view.findViewById(R.id.progressBar)
        tvProgress = view.findViewById(R.id.tvProgress)

        tvProgress.text = getMessage()

        isCancelable = cancelListener != null

        return AlertDialog.Builder(activity)
            .setView(view)
            .apply {
                if (cancelListener != null) {
                    setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
                }
            }
            .create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        cancelListener?.invoke()
    }

    fun setMessage(message: String?) {
        tvProgress.text = message
        requireArguments().putString(Constants.Tags.MESSAGE_ARG, message)
    }

    fun setIndeterminate(indeterminate: Boolean) {
        progressBar.indeterminate(indeterminate)
    }

    fun setProgress(progress: Int) {
        progressBar.setExtProgress(progress)
    }

    fun setCancellationListener(listener: () -> Unit) {
        this.cancelListener = listener
    }

    private fun getMessage(): String? {
        val args = requireArguments()
        val message = args.getString(Constants.Tags.MESSAGE_ARG)
        return if (message == null) {
            val resId = args.getInt(Constants.Tags.MESSAGE_RES_ARG)
            if (resId == 0) {
                null
            } else {
                getString(args.getInt(Constants.Tags.MESSAGE_RES_ARG))
            }
        } else {
            message
        }
    }
}