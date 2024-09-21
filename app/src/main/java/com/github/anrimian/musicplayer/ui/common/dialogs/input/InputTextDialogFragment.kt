package com.github.anrimian.musicplayer.ui.common.dialogs.input

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.text.method.DigitsKeyListener
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo.IME_ACTION_DONE
import android.view.inputmethod.EditorInfo.IME_ACTION_UNSPECIFIED
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.annotation.StringRes
import androidx.fragment.app.DialogFragment
import com.github.anrimian.musicplayer.Constants.Arguments.CAN_BE_EMPTY_ARG
import com.github.anrimian.musicplayer.Constants.Arguments.COMPLETE_ON_ENTER_ARG
import com.github.anrimian.musicplayer.Constants.Arguments.DESCRIPTION_ARG
import com.github.anrimian.musicplayer.Constants.Arguments.DIGITS_ARG
import com.github.anrimian.musicplayer.Constants.Arguments.EDIT_TEXT_HINT
import com.github.anrimian.musicplayer.Constants.Arguments.EDIT_TEXT_VALUE
import com.github.anrimian.musicplayer.Constants.Arguments.EXTRA_DATA_ARG
import com.github.anrimian.musicplayer.Constants.Arguments.HINTS_ARG
import com.github.anrimian.musicplayer.Constants.Arguments.INPUT_TYPE_ARG
import com.github.anrimian.musicplayer.Constants.Arguments.NEGATIVE_BUTTON_ARG
import com.github.anrimian.musicplayer.Constants.Arguments.NEUTRAL_BUTTON_ARG
import com.github.anrimian.musicplayer.Constants.Arguments.POSITIVE_BUTTON_ARG
import com.github.anrimian.musicplayer.Constants.Arguments.TITLE_ARG
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogCommonInputSimpleBinding
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.text_view.SimpleTextWatcher

class InputTextDialogFragment : DialogFragment() {

    companion object {
        fun newInstance(
            @StringRes title: Int,
            @StringRes positiveButtonText: Int,
            @StringRes negativeButtonText: Int,
            @StringRes editTextHint: Int,
            editTextValue: String?,
            @StringRes description: Int = 0,
            @StringRes neutralButtonText: Int = 0,
            canBeEmpty: Boolean = true,
            completeOnEnterButton: Boolean = true,
            inputType: Int = InputType.TYPE_CLASS_TEXT,
            digits: String? = null,
            hints: Array<String>? = null,
            extra: Bundle? = null
        ) = InputTextDialogFragment().apply {
            arguments = Bundle().apply {
                putInt(TITLE_ARG, title)
                putInt(POSITIVE_BUTTON_ARG, positiveButtonText)
                putInt(NEGATIVE_BUTTON_ARG, negativeButtonText)
                putInt(EDIT_TEXT_HINT, editTextHint)
                putInt(DESCRIPTION_ARG, description)
                putInt(NEUTRAL_BUTTON_ARG, neutralButtonText)
                putString(EDIT_TEXT_VALUE, editTextValue)
                putBoolean(CAN_BE_EMPTY_ARG, canBeEmpty)
                putBoolean(COMPLETE_ON_ENTER_ARG, completeOnEnterButton)
                putInt(INPUT_TYPE_ARG, inputType)
                putString(DIGITS_ARG, digits)
                putBundle(EXTRA_DATA_ARG, extra)
                putStringArray(HINTS_ARG, hints)
            }
        }
    }

    private lateinit var editText: AutoCompleteTextView
    
    private var onCompleteListener: ((String) -> Unit)? = null
    private var complexCompleteListener: ((String, Bundle) -> Unit)? = null
    private var onNeutralClickListener: (() -> Unit)? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogCommonInputSimpleBinding.inflate(
            LayoutInflater.from(requireActivity())
        )
        editText = binding.editText
        val args = requireArguments()
        val dialog = AlertDialog.Builder(activity)
            .setTitle(args.getInt(TITLE_ARG))
            .setView(binding.root)
            .create()

        binding.btnPositive.setText(args.getInt(POSITIVE_BUTTON_ARG))
        binding.btnPositive.setOnClickListener { onCompleteButtonClicked() }
        val canBeEmpty = args.getBoolean(CAN_BE_EMPTY_ARG)
        val startText = args.getString(EDIT_TEXT_VALUE)
        if (!canBeEmpty) {
            binding.btnPositive.isEnabled = isEnterButtonEnabled(startText)
            SimpleTextWatcher.onTextChanged(editText) { text ->
                binding.btnPositive.isEnabled = isEnterButtonEnabled(text)
            }
        }

        binding.btnNegative.setText(args.getInt(NEGATIVE_BUTTON_ARG))
        binding.btnNegative.setOnClickListener { dismissAllowingStateLoss() }

        val neutralButtonArg = args.getInt(NEUTRAL_BUTTON_ARG)
        if (neutralButtonArg > 0) {
            binding.btnNeutral.visibility = View.VISIBLE
            binding.btnNeutral.setText(neutralButtonArg)
            binding.btnNeutral.setOnClickListener {
                dismissAllowingStateLoss()
                onNeutralClickListener?.invoke()
            }
        }

        AndroidUtils.setSoftInputVisible(dialog.window)
        dialog.show()

        val completeOnEnterButton = args.getBoolean(COMPLETE_ON_ENTER_ARG)
        val hint = args.getInt(EDIT_TEXT_HINT)
        if (hint > 0) {
            editText.setHint(hint)
        }
        val description = args.getInt(DESCRIPTION_ARG)
        if (description > 0) {
            binding.tvDescription.setText(description)
            binding.tvDescription.visibility = View.VISIBLE
        } else {
            binding.tvDescription.visibility = View.GONE
        }
        editText.imeOptions = if (completeOnEnterButton) IME_ACTION_DONE else IME_ACTION_UNSPECIFIED
        editText.setRawInputType(args.getInt(INPUT_TYPE_ARG))
        val digits = args.getString(DIGITS_ARG)
        if (digits != null) {
            editText.keyListener = DigitsKeyListener.getInstance(digits)
        }
        editText.setOnEditorActionListener { _, actionId: Int, _ ->
            if (!canBeEmpty && !isEnterButtonEnabled(editText.text.toString().trim())) {
                return@setOnEditorActionListener true
            }
            if (actionId == IME_ACTION_DONE) {
                onCompleteButtonClicked()
                return@setOnEditorActionListener true
            }
            false
        }
        ViewUtils.setEditableText(editText, startText)
        val hints = args.getStringArray(HINTS_ARG)
        if (hints != null) {
            val adapter = ArrayAdapter(
                requireContext(),
                R.layout.item_autocomplete,
                R.id.text_view,
                hints
            )
            editText.setAdapter(adapter)
        }
        editText.requestFocus()

        return dialog
    }

    fun setOnCompleteListener(onCompleteListener: (String) -> Unit) {
        this.onCompleteListener = onCompleteListener
    }

    fun setComplexCompleteListener(complexCompleteListener: (String, Bundle) -> Unit) {
        this.complexCompleteListener = complexCompleteListener
    }

    fun setOnNeutralClickListener(onNeutralClickListener: () -> Unit) {
        this.onNeutralClickListener = onNeutralClickListener
    }

    private fun onCompleteButtonClicked() {
        val text = editText.text.toString()
        if (!TextUtils.equals(text, requireArguments().getString(EDIT_TEXT_VALUE))) {
            onCompleteListener?.invoke(text)
            complexCompleteListener?.invoke(text, requireArguments().getBundle(EXTRA_DATA_ARG)!!)
        }
        dismissAllowingStateLoss()
    }

    private fun isEnterButtonEnabled(text: String?): Boolean {
        return !TextUtils.isEmpty(text)
    }

}