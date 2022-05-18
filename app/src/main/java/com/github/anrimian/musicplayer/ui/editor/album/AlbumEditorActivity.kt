package com.github.anrimian.musicplayer.ui.editor.album

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.ActivityAlbumEditBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.dialogs.ProgressDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentDelayRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import com.google.android.material.snackbar.Snackbar
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter

fun newAlbumEditorIntent(context: Context, albumId: Long): Intent {
    val intent = Intent(context, AlbumEditorActivity::class.java)
    intent.putExtra(Constants.Arguments.ALBUM_ID_ARG, albumId)
    return intent
}

class AlbumEditorActivity : MvpAppCompatActivity(), AlbumEditorView {

    private val presenter by moxyPresenter {
        val albumId = intent.getLongExtra(Constants.Arguments.ALBUM_ID_ARG, 0)
        Components.getAlbumEditorComponent(albumId).albumEditorPresenter()
    }
    
    private lateinit var viewBinding: ActivityAlbumEditBinding
    
    private lateinit var authorDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var nameDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var progressDialogRunner: DialogFragmentDelayRunner
    
    private lateinit var errorHandler: ErrorHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        Components.getAppComponent().themeController().applyCurrentSlidrTheme(this)
        super.onCreate(savedInstanceState)
        viewBinding = ActivityAlbumEditBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        AndroidUtils.setNavigationBarColorAttr(this, android.R.attr.colorBackground)
        
        setSupportActionBar(viewBinding.toolbar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setTitle(R.string.edit_album_tags)
        }
        
        viewBinding.changeAuthorClickableArea.setOnClickListener { presenter.onChangeAuthorClicked() }
        viewBinding.changeNameClickableArea.setOnClickListener { presenter.onChangeNameClicked() }
        
        ViewUtils.onLongClick(viewBinding.changeAuthorClickableArea) {
            copyText(viewBinding.tvAuthor, viewBinding.tvAuthorHint)
        }
        ViewUtils.onLongClick(viewBinding.changeNameClickableArea) {
            copyText(viewBinding.tvName, viewBinding.tvNameHint)
        }
        
        SlidrPanel.attachWithNavBarChange(
            this,
            R.attr.playerPanelBackground,
            android.R.attr.colorBackground
        )
        
        val fm = supportFragmentManager
        errorHandler = ErrorHandler(
            fm,
            presenter::onRetryFailedEditActionClicked,
            this::showEditorRequestDeniedMessage
        )
        authorDialogFragmentRunner = DialogFragmentRunner(
            fm,
            Tags.AUTHOR_TAG
        ) { fragment -> fragment.setOnCompleteListener(presenter::onNewAuthorEntered) }
        nameDialogFragmentRunner = DialogFragmentRunner(
            fm,
            Tags.NAME_TAG
        ) { fragment -> fragment.setOnCompleteListener(presenter::onNewNameEntered) }
        progressDialogRunner = DialogFragmentDelayRunner(fm, Tags.PROGRESS_DIALOG_TAG)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(
            Components.getAppComponent().localeController().dispatchAttachBaseContext(base)
        )
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun closeScreen() {
        finish()
    }

    override fun showAlbumLoadingError(errorCommand: ErrorCommand) {
        viewBinding.tvAuthor.text = errorCommand.message
    }

    override fun showAlbum(album: Album) {
        viewBinding.tvName.text = album.name
        viewBinding.tvAuthor.text = FormatUtils.formatAuthor(album.artist, this)
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        errorHandler.handleError(errorCommand) {
            MessagesUtils.makeSnackbar(
                viewBinding.root, errorCommand.message, Snackbar.LENGTH_LONG
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
        val fragment = InputTextDialogFragment.Builder(
            R.string.change_album_artist,
            R.string.change,
            R.string.cancel,
            R.string.artist,
            album.artist
        ).hints(hints)
            .build()
        authorDialogFragmentRunner.show(fragment)
    }

    override fun showEnterNameDialog(album: Album) {
        val fragment = InputTextDialogFragment.newInstance(
            R.string.change_name,
            R.string.change,
            R.string.cancel,
            R.string.name,
            album.name,
            false
        )
        nameDialogFragmentRunner.show(fragment)
    }

    private fun copyText(textView: TextView, tvLabel: TextView) {
        AndroidUtils.copyText(this, textView.text.toString(), tvLabel.text.toString())
        onTextCopied()
    }

    private fun onTextCopied() {
        MessagesUtils.makeSnackbar(
            viewBinding.root,
            R.string.copied_message,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showEditorRequestDeniedMessage() {
        MessagesUtils.makeSnackbar(
            viewBinding.root,
            R.string.android_r_edit_file_permission_denied,
            Snackbar.LENGTH_LONG
        ).show()
    }

}