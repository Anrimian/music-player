package com.github.anrimian.musicplayer.ui.utils.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.github.anrimian.musicplayer.R;

import static com.github.anrimian.musicplayer.Constants.Tags.MESSAGE_ARG;

public class ProgressDialogFragment extends DialogFragment {

    public static ProgressDialogFragment newInstance(String message) {
        Bundle args = new Bundle();
        args.putString(MESSAGE_ARG, message);
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_progress, null);

        TextView tvProgress = view.findViewById(R.id.tv_progress);
        assert getArguments() != null;
        tvProgress.setText(getArguments().getString(MESSAGE_ARG));

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setCancelable(false)
                .create();
    }
}
