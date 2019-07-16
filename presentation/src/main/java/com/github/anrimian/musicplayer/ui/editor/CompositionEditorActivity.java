package com.github.anrimian.musicplayer.ui.editor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.arellomobile.mvp.presenter.InjectPresenter;
import com.arellomobile.mvp.presenter.ProvidePresenter;
import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.models.composition.Composition;
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment;
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand;
import com.github.anrimian.musicplayer.ui.utils.moxy.ui.MvpAppCompatActivity;
import com.r0adkll.slidr.Slidr;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.github.anrimian.musicplayer.Constants.Arguments.COMPOSITION_ID_ARG;
import static com.github.anrimian.musicplayer.Constants.Tags.AUTHOR_TAG;
import static com.github.anrimian.musicplayer.Constants.Tags.TITLE_TAG;
import static com.github.anrimian.musicplayer.ui.common.format.FormatUtils.formatCompositionAuthor;

public class CompositionEditorActivity extends MvpAppCompatActivity
        implements CompositionEditorView {

    @InjectPresenter
    CompositionEditorPresenter presenter;

    @BindView(R.id.tv_author)
    TextView tvAuthor;

    @BindView(R.id.tv_title)
    TextView tvTitle;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

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
            actionBar.setTitle(R.string.edit);
        }

        tvAuthor.setOnClickListener(v -> presenter.onChangeAuthorClicked());
        tvTitle.setOnClickListener(v -> presenter.onChangeTitleClicked());

        Slidr.attach(this);

        InputTextDialogFragment fragment = (InputTextDialogFragment)
                getSupportFragmentManager().findFragmentByTag(AUTHOR_TAG);
        if (fragment != null) {
            fragment.setOnCompleteListener(presenter::onNewAuthorEntered);
        }

        InputTextDialogFragment titleFragment = (InputTextDialogFragment)
                getSupportFragmentManager().findFragmentByTag(TITLE_TAG);
        if (titleFragment != null) {
            titleFragment.setOnCompleteListener(presenter::onNewTitleEntered);
        }
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
    }

    @Override
    public void showEnterAuthorDialog(Composition composition) {
        InputTextDialogFragment fragment = InputTextDialogFragment.newInstance(R.string.change_author_name,
                R.string.change,
                R.string.cancel,
                R.string.enter_author_name,
                composition.getArtist());
        fragment.setOnCompleteListener(presenter::onNewAuthorEntered);
        fragment.show(getSupportFragmentManager(), AUTHOR_TAG);
    }

    @Override
    public void showEnterTitleDialog(Composition composition) {
        InputTextDialogFragment fragment = InputTextDialogFragment.newInstance(R.string.change_title,
                R.string.change,
                R.string.cancel,
                R.string.enter_title,
                composition.getTitle());
        fragment.setOnCompleteListener(presenter::onNewTitleEntered);
        fragment.show(getSupportFragmentManager(), TITLE_TAG);
    }

    @Override
    public void showErrorMessage(ErrorCommand errorCommand) {
        Toast.makeText(this, errorCommand.getMessage(), Toast.LENGTH_LONG).show();
    }
}
