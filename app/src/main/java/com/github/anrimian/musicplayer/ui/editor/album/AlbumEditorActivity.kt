package com.github.anrimian.musicplayer.ui.editor.album

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.github.anrimian.filesync.models.ProgressInfo
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ActivityAlbumEditBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.ui.common.AppAndroidUtils
import com.github.anrimian.musicplayer.ui.common.activity.BaseMvpAppCompatActivity
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.format.asInt
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.dialogs.ProgressDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentDelayRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.setToolbar
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import com.google.android.material.snackbar.Snackbar
import moxy.ktx.moxyPresenter

class AlbumEditorActivity : BaseMvpAppCompatActivity(), AlbumEditorView {

    companion object {
        fun newIntent(
            context: Context,
            albumId: Long
        ) = Intent(context, AlbumEditorActivity::class.java).apply {
            putExtra(Constants.Arguments.ALBUM_ID_ARG, albumId)
        }
    }

    private val presenter by moxyPresenter {
        val albumId = intent.getLongExtra(Constants.Arguments.ALBUM_ID_ARG, 0)
        Components.getAlbumEditorComponent(albumId).albumEditorPresenter()
    }

    private lateinit var binding: ActivityAlbumEditBinding

    private lateinit var authorDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var nameDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var progressDialogRunner: DialogFragmentDelayRunner<ProgressDialogFragment>

    private lateinit var errorHandler: ErrorHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        Components.getAppComponent().themeController().applyCurrentSlidrTheme(this)
        super.onCreate(savedInstanceState)
        binding = ActivityAlbumEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AndroidUtils.setNavigationBarColorAttr(this, android.R.attr.colorBackground)

        setToolbar(binding.toolbar, R.string.edit_album_tags)

        binding.changeAuthorClickableArea.setOnClickListener { presenter.onChangeAuthorClicked() }
        binding.changeNameClickableArea.setOnClickListener { presenter.onChangeNameClicked() }

        ViewUtils.onLongClick(binding.changeAuthorClickableArea) {
            copyText(binding.tvAuthor, binding.tvAuthorHint)
        }
        ViewUtils.onLongClick(binding.changeNameClickableArea) {
            copyText(binding.tvName, binding.tvNameHint)
        }

        SlidrPanel.attachWithNavBarChange(
            this,
            R.attr.playerPanelBackground,
            android.R.attr.colorBackground
        )

        errorHandler = ErrorHandler(
            this,
            presenter::onRetryFailedEditActionClicked,
            this::showEditorRequestDeniedMessage
        )
        val fm = supportFragmentManager
        authorDialogFragmentRunner = DialogFragmentRunner(fm, Tags.AUTHOR_TAG) { fragment ->
            fragment.setOnCompleteListener(presenter::onNewAuthorEntered)
        }
        nameDialogFragmentRunner = DialogFragmentRunner(fm, Tags.NAME_TAG) { fragment ->
            fragment.setOnCompleteListener(presenter::onNewNameEntered)
        }
        progressDialogRunner = DialogFragmentDelayRunner(fm, Tags.PROGRESS_DIALOG_TAG, { fragment ->
            fragment.setCancellationListener { presenter.onEditActionCancelled() }
        })
    }

    override fun closeScreen() {
        finish()
    }

    override fun showAlbumLoadingError(errorCommand: ErrorCommand) {
        binding.tvAuthor.text = errorCommand.message
    }

    override fun showAlbum(album: Album) {
        binding.tvName.text = album.name
        binding.tvAuthor.text = FormatUtils.formatAuthor(album.artist, this)
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        errorHandler.handleError(errorCommand) {
            MessagesUtils.makeSnackbar(
                binding.root, errorCommand.message, Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun showRenameProgress() {
        progressDialogRunner.show(ProgressDialogFragment.newInstance(R.string.rename_progress))
    }

    override fun hideRenameProgress() {
        progressDialogRunner.cancel()
    }

    override fun showEnterAuthorDialog(album: Album, hints: Array<String>?) {
        val fragment = InputTextDialogFragment.newInstance(
            R.string.change_album_artist,
            R.string.change,
            R.string.cancel,
            R.string.artist,
            album.artist,
            hints = hints
        )
        authorDialogFragmentRunner.show(fragment)
    }

    override fun showEnterNameDialog(album: Album) {
        val fragment = InputTextDialogFragment.newInstance(
            R.string.change_name,
            R.string.change,
            R.string.cancel,
            R.string.name,
            album.name,
            canBeEmpty = false
        )
        nameDialogFragmentRunner.show(fragment)
    }

    override fun showPreparedFilesCount(processed: Int, total: Int) {
        progressDialogRunner.runAction { dialog ->
            dialog.setMessage(getString(R.string.downloading, processed, total))
        }
    }

    override fun showDownloadingFileInfo(progressInfo: ProgressInfo) {
        progressDialogRunner.runAction { dialog ->
            dialog.setProgress(progressInfo.asInt())
        }
    }

    override fun showEditedFilesCount(processed: Int, total: Int) {
        progressDialogRunner.runAction { dialog ->
            val message = if (total > 1) {
                getString(R.string.rename_progress_count, processed, total)
            } else {
                getString(R.string.rename_progress)
            }
            dialog.setMessage(message)
        }
    }

    private fun copyText(textView: TextView, tvLabel: TextView) {
        AppAndroidUtils.copyText(binding.root, textView.text.toString(), tvLabel.text.toString())
    }

    private fun showEditorRequestDeniedMessage() {
        MessagesUtils.makeSnackbar(
            binding.root,
            R.string.android_r_edit_file_permission_denied,
            Snackbar.LENGTH_LONG
        ).show()
    }

}