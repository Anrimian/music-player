package com.github.anrimian.musicplayer.ui.common.dialogs.files

import android.os.Bundle
import android.text.InputType
import com.github.anrimian.musicplayer.AppConstants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.domain.Constants
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment
import com.github.anrimian.musicplayer.ui.common.format.AppFormatUtils

class AllowedExtensionsDialogFragment : InputTextDialogFragment() {

    private var onCompleteListener: ((Set<String>) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        super.setOnCompleteListener { text ->
            val newExtensions = AppFormatUtils.parseExtensions(text)
            onCompleteListener?.invoke(newExtensions)
        }
        super.setOnNeutralClickListener {
            onCompleteListener?.invoke(Constants.DEFAULT_REMOTE_EXTENSIONS)
        }
    }

    fun setCompleteListener(listener: (Set<String>) -> Unit) {
        this.onCompleteListener = listener
    }

    companion object {

        fun newInstance(extensions: Set<String>): AllowedExtensionsDialogFragment {
            val formattedExtensions = AppFormatUtils.formatExtensions(extensions)
            val args = Bundle().apply {
                putInt(AppConstants.Arguments.TITLE_ARG, R.string.allowed_file_extensions)
                putInt(AppConstants.Arguments.POSITIVE_BUTTON_ARG, R.string.change)
                putInt(AppConstants.Arguments.NEGATIVE_BUTTON_ARG, R.string.cancel)
                putInt(AppConstants.Arguments.EDIT_TEXT_HINT, 0)
                putInt(AppConstants.Arguments.DESCRIPTION_ARG, R.string.allowed_file_extensions_description)
                putInt(AppConstants.Arguments.NEUTRAL_BUTTON_ARG, R.string.restore_default)
                putString(AppConstants.Arguments.EDIT_TEXT_VALUE, formattedExtensions)
                putBoolean(AppConstants.Arguments.CAN_BE_EMPTY_ARG, true)
                putBoolean(AppConstants.Arguments.COMPLETE_ON_ENTER_ARG, true)
                putInt(AppConstants.Arguments.INPUT_TYPE_ARG, InputType.TYPE_CLASS_TEXT)
            }
            return AllowedExtensionsDialogFragment().apply {
                arguments = args
            }
        }

    }

}