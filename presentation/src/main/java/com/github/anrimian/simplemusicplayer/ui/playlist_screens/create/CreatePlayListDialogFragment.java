package com.github.anrimian.simplemusicplayer.ui.playlist_screens.create;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatDialogFragment;
import com.arellomobile.mvp.MvpAppCompatFragment;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.simplemusicplayer.R;
import com.github.anrimian.simplemusicplayer.di.Components;
import com.github.anrimian.simplemusicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.simplemusicplayer.ui.common.order.adapter.OrderAdapter;

import org.w3c.dom.Text;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreatePlayListDialogFragment extends MvpAppCompatDialogFragment
        implements CreatePlayListView {

    @InjectPresenter
    CreatePlayListPresenter presenter;

    @BindView(R.id.et_playlist_name)
    EditText etPlayListName;

    @BindView(R.id.tv_error)
    TextView tvError;

    @BindView(R.id.tv_progress)
    TextView tvProgress;

    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private Button btnCreate;

    @ProvidePresenter
    CreatePlayListPresenter providePresenter() {
        return Components.getPlayListsComponent().createPlayListsPresenter();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = View.inflate(getActivity(), R.layout.dialog_create_playlist, null);

        ButterKnife.bind(this, view);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.create_playlist)
                .setPositiveButton(R.string.create, null)
                .setView(view)
                .create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();

        etPlayListName.setImeOptions(EditorInfo.IME_ACTION_DONE);
        etPlayListName.setRawInputType(InputType.TYPE_CLASS_TEXT);
        etPlayListName.setOnEditorActionListener((v, actionId, event) -> {
            onCompleteButtonClicked();
            return true;
        });

        btnCreate = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        btnCreate.setOnClickListener(v -> onCompleteButtonClicked());

        return dialog;
    }

    @Override
    public void showProgress() {
        btnCreate.setEnabled(false);
        etPlayListName.setEnabled(false);
        tvError.setVisibility(View.GONE);
        tvProgress.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void showInputState() {
        btnCreate.setEnabled(true);
        etPlayListName.setEnabled(true);
        tvError.setVisibility(View.GONE);
        tvProgress.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showError(ErrorCommand errorCommand) {
        btnCreate.setEnabled(true);
        etPlayListName.setEnabled(true);
        tvProgress.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        tvError.setVisibility(View.VISIBLE);
        tvError.setText(getString(R.string.create_playlist_error_template, errorCommand.getMessage()));
    }

    @Override
    public void closeScreen() {
        dismiss();
    }

    private void onCompleteButtonClicked() {
        presenter.onCompleteInputButtonClicked(etPlayListName.getText().toString());
    }
}
