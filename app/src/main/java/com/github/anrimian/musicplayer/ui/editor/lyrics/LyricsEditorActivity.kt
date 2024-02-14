package com.github.anrimian.musicplayer.ui.editor.lyrics

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ActivityLyricsEditBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.showSnackbar
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.dialogs.ProgressDialogFragment
import com.github.anrimian.musicplayer.ui.utils.dialogs.newProgressDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentDelayRunner
import com.github.anrimian.musicplayer.ui.utils.setToolbar
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import com.github.anrimian.musicplayer.ui.utils.views.text_view.SimpleTextWatcher
import com.google.android.material.snackbar.Snackbar
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter

class LyricsEditorActivity : MvpAppCompatActivity(), LyricsEditorView {

    companion object {
        fun newIntent(context: Context, compositionId: Long): Intent {
            val intent = Intent(context, LyricsEditorActivity::class.java)
            intent.putExtra(Constants.Arguments.COMPOSITION_ID_ARG, compositionId)
            return intent
        }
    }

    private val presenter by moxyPresenter {
        val compositionId = intent.getLongExtra(Constants.Arguments.COMPOSITION_ID_ARG, 0)
        Components.getLyricsEditorComponent(compositionId).lyricsEditorPresenter()
    }

    private lateinit var binding: ActivityLyricsEditBinding

    private lateinit var errorHandler: ErrorHandler

    private lateinit var progressDialogRunner: DialogFragmentDelayRunner<ProgressDialogFragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        Components.getAppComponent().themeController().applyCurrentSlidrTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityLyricsEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AndroidUtils.setNavigationBarColorAttr(this, android.R.attr.colorBackground)

        setToolbar(binding.toolbar, R.string.edit_lyrics)

        binding.progressStateView.onTryAgainClick(presenter::onTryAgainButtonClicked)

        errorHandler = ErrorHandler(
            this,
            presenter::onRetryFailedEditActionClicked,
            this::showEditorRequestDeniedMessage
        )

        SimpleTextWatcher.onTextChanged(binding.evLyrics, presenter::onTextChanged)

        binding.btnChange.setOnClickListener { presenter.onChangeButtonClicked() }

        progressDialogRunner = DialogFragmentDelayRunner(
            supportFragmentManager,
            Constants.Tags.PROGRESS_DIALOG_TAG,
            delayMillis = Constants.EDIT_DIALOG_DELAY_MILLIS,
        )

        SlidrPanel.attachWithNavBarChange(
            this,
            R.attr.playerPanelBackground,
            android.R.attr.colorBackground
        )
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(
            Components.getAppComponent().localeController().dispatchAttachBaseContext(base)
        )
    }

    override fun showLyrics(text: String) {
        binding.progressStateView.hideAll()
        ViewUtils.setEditableText(binding.evLyrics, text)
    }

    override fun showLyricsLoadingError(errorCommand: ErrorCommand) {
        binding.progressStateView.showMessage(errorCommand.message, R.string.try_again)
    }

    override fun showChangeFileProgress() {
        val fragment = newProgressDialogFragment(R.string.changing_file_progress)
        progressDialogRunner.show(fragment)
    }

    override fun hideChangeFileProgress() {
        progressDialogRunner.cancel()
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        errorHandler.handleError(errorCommand) {
            binding.container.showSnackbar(errorCommand.message, Snackbar.LENGTH_LONG)
        }
    }

    override fun closeScreen() {
        finish()
    }

    private fun showEditorRequestDeniedMessage() {
        binding.container.showSnackbar(R.string.android_r_edit_file_permission_denied, Snackbar.LENGTH_LONG)
    }

}