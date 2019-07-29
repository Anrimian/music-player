package com.github.anrimian.musicplayer.ui.common.dialogs.input;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.domain.utils.java.Callback;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.Constants.Arguments.EDIT_TEXT_HINT;
import static com.github.anrimian.musicplayer.Constants.Arguments.EDIT_TEXT_VALUE;
import static com.github.anrimian.musicplayer.Constants.Arguments.NEGATIVE_BUTTON_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.POSITIVE_BUTTON_ARG;
import static com.github.anrimian.musicplayer.Constants.Arguments.TITLE_ARG;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.setEditableText;

public class InputTextDialogFragment extends DialogFragment {

    @BindView(R.id.edit_text)
    EditText editText;

    @Nullable
    private Callback<String> onCompleteListener;

    public static InputTextDialogFragment newInstance(@StringRes int title,
                                                      @StringRes int positiveButtonText,
                                                      @StringRes int negativeButtonText,
                                                      @StringRes int editTextHint,
                                                      String editTextValue) {
        Bundle args = new Bundle();
        args.putInt(TITLE_ARG, title);
        args.putInt(POSITIVE_BUTTON_ARG, positiveButtonText);
        args.putInt(NEGATIVE_BUTTON_ARG, negativeButtonText);
        args.putInt(EDIT_TEXT_HINT, editTextHint);
        args.putString(EDIT_TEXT_VALUE, editTextValue);
        InputTextDialogFragment fragment = new InputTextDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_common_input_simple, null);

        ButterKnife.bind(this, view);

        Bundle args = getArguments();
        assert args != null;
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(args.getInt(TITLE_ARG))
                .setPositiveButton(args.getInt(POSITIVE_BUTTON_ARG), null)
                .setNegativeButton(args.getInt(NEGATIVE_BUTTON_ARG), (dialog1, which) -> {})
                .setView(view)
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

        editText.setHint(args.getInt(EDIT_TEXT_HINT));
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        editText.setRawInputType(InputType.TYPE_CLASS_TEXT);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            onCompleteButtonClicked();
            return true;
        });
        setEditableText(editText, args.getString(EDIT_TEXT_VALUE));
        editText.requestFocus();

        Button btnCreate = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnCreate.setOnClickListener(v -> onCompleteButtonClicked());

        return dialog;
    }

    public void setOnCompleteListener(@Nullable Callback<String> onCompleteListener) {
        this.onCompleteListener = onCompleteListener;
    }

    private void onCompleteButtonClicked() {
        if (onCompleteListener != null) {
            onCompleteListener.call(editText.getText().toString());
        }
        dismiss();
    }
}
