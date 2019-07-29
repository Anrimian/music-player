package com.github.anrimian.musicplayer.ui.editor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils;
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner;
import com.github.anrimian.musicplayer.ui.utils.moxy.ui.MvpAppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.r0adkll.slidr.Slidr;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.AUTHOR_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.FILE_NAME_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.TITLE_TAG;
import static com.github.anrimian.musicplayer.domain.utils.FileUtils.formatFileName;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;
import static com.github.anrimian.musicplayer.ui.common.format.MessagesUtils.makeSnackbar;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onLongClick;

public class CompositionEditorActivity extends MvpAppCompatActivity
        implements CompositionEditorView {

    @InjectPresenter
    CompositionEditorPresenter presenter;

    @BindView(R.id.container)
    View container;

    @BindView(R.id.tv_author)
    TextView tvAuthor;

    @BindView(R.id.tv_title)
    TextView tvTitle;

    @BindView(R.id.tv_filename)
    TextView tvFileName;

    @BindView(R.id.tv_author_hint)
    TextView tvAuthorHint;

    @BindView(R.id.tv_title_hint)
    TextView tvTitleHint;

    @BindView(R.id.tv_filename_hint)
    TextView tvFileNameHint;

    @BindView(R.id.change_author_clickable_area)
    View changeAuthorClickableArea;

    @BindView(R.id.change_title_clickable_area)
    View changeTitleClickableArea;

    @BindView(R.id.change_filename_clickable_area)
    View changeFilenameClickableArea;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private DialogFragmentRunner<InputTextDialogFragment> authorDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> titleDialogFragmentRunner;
    private DialogFragmentRunner<InputTextDialogFragment> filenameDialogFragmentRunner;

    public static Intent newIntent(Context context, long compositionId) {
        Intent intent = new Intent(context, CompositionEditorActivity.class);
        intent.putExtra(COMPOSITION_ID_ARG, compositionId);
        return intent;
    }

    @ProvidePresenter
    CompositionEditorPresenter providePresenter() {
        long compositionId = getIntent().getLongExtra(COMPOSITION_ID_ARG, 0);
        return Components.getCompositionEditorComponent(compositionId).compositionEditorPresenter();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Components.getAppComponent().themeController().applyCurrentSlidrTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_composition_edit);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.edit_tags);
        }

        changeAuthorClickableArea.setOnClickListener(v -> presenter.onChangeAuthorClicked());
        changeTitleClickableArea.setOnClickListener(v -> presenter.onChangeTitleClicked());
        changeFilenameClickableArea.setOnClickListener(v -> presenter.onChangeFileNameClicked());
        onLongClick(changeAuthorClickableArea, () -> copyText(tvAuthor, tvAuthorHint));
        onLongClick(changeTitleClickableArea, () -> copyText(tvTitle, tvTitleHint));
        onLongClick(changeFilenameClickableArea, () -> copyText(tvFileName, tvFileNameHint));

        Slidr.attach(this);

        FragmentManager fm = getSupportFragmentManager();
        authorDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                AUTHOR_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewAuthorEntered));

        titleDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                TITLE_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewTitleEntered));

        filenameDialogFragmentRunner = new DialogFragmentRunner<>(fm,
                FILE_NAME_TAG,
                fragment -> fragment.setOnCompleteListener(presenter::onNewFileNameEntered));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void closeScreen() {
        finish();
    }

    @Override
    public void showCompositionLoadingError(ErrorCommand errorCommand) {
        tvAuthor.setText(errorCommand.getMessage());
    }

    @Override
    public void showComposition(Composition composition) {
        tvTitle.setText(composition.getTitle());
        tvAuthor.setText(formatCompositionAuthor(composition, this));
        tvFileName.setText(formatFileName(composition.getFilePath(), true));
    }

    @Override
    public void showEnterAuthorDialog(Composition composition) {
        InputTextDialogFragment fragment = InputTextDialogFragment.newInstance(R.string.change_author_name,
                R.string.change,
                R.string.cancel,
                R.string.artist,
                composition.getArtist());
        authorDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showEnterTitleDialog(Composition composition) {
        InputTextDialogFragment fragment = InputTextDialogFragment.newInstance(R.string.change_title,
                R.string.change,
                R.string.cancel,
                R.string.title,
                composition.getTitle());
        titleDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showEnterFileNameDialog(Composition composition) {
        InputTextDialogFragment fragment = InputTextDialogFragment.newInstance(R.string.change_file_name,
                R.string.change,
                R.string.cancel,
                R.string.filename,
                formatFileName(composition.getFilePath()),
                false);
        filenameDialogFragmentRunner.show(fragment);
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        makeSnackbar(container, errorCommand.getMessage(), Snackbar.LENGTH_LONG).show();
    }

    private void copyText(TextView textView, TextView tvLabel) {
        AndroidUtils.copyText(this,
                textView.getText().toString(),
                tvLabel.getText().toString());
        onTextCopied();
    }

    private void onTextCopied() {
        makeSnackbar(container, R.string.copied_message, Snackbar.LENGTH_SHORT).show();
    }
}
