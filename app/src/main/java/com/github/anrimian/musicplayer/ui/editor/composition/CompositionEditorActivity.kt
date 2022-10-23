package com.github.anrimian.musicplayer.ui.editor.composition

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.github.anrimian.filesync.models.state.file.Downloading
import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.models.image.UriImageSource
import com.github.anrimian.musicplayer.databinding.ActivityCompositionEditBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.genres.ShortGenre
import com.github.anrimian.musicplayer.domain.utils.FileUtils
import com.github.anrimian.musicplayer.ui.common.activity.PickImageContract
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.format.asInt
import com.github.anrimian.musicplayer.ui.common.format.showFileSyncState
import com.github.anrimian.musicplayer.ui.common.serialization.GenreSerializer
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.editor.composition.list.ShortGenresAdapter
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.dialogs.ProgressDialogFragment
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuDialogFragment
import com.github.anrimian.musicplayer.ui.utils.dialogs.newProgressDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentDelayRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.setToolbar
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import com.google.android.material.snackbar.Snackbar
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter

fun newCompositionEditorIntent(context: Context, compositionId: Long): Intent {
    val intent = Intent(context, CompositionEditorActivity::class.java)
    intent.putExtra(Constants.Arguments.COMPOSITION_ID_ARG, compositionId)
    return intent
}

class CompositionEditorActivity : MvpAppCompatActivity(), CompositionEditorView {

    private val presenter by moxyPresenter {
        val compositionId = intent.getLongExtra(Constants.Arguments.COMPOSITION_ID_ARG, 0)
        Components.getCompositionEditorComponent(compositionId).compositionEditorPresenter()
    }

    private lateinit var viewBinding: ActivityCompositionEditBinding
    private lateinit var genresAdapter: ShortGenresAdapter
    private lateinit var errorHandler: ErrorHandler

    private lateinit var authorDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var titleDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var filenameDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var albumDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var albumArtistDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var addGenreDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var editGenreDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var lyricsDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var coverMenuDialogRunner: DialogFragmentRunner<MenuDialogFragment>
    private lateinit var progressDialogRunner: DialogFragmentDelayRunner<ProgressDialogFragment>

    private val pickImageContract = registerForActivityResult(PickImageContract()) { uri ->
        if (uri != null) {
            presenter.onNewImageForCoverSelected(UriImageSource(uri))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Components.getAppComponent().themeController().applyCurrentSlidrTheme(this)
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCompositionEditBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        AndroidUtils.setNavigationBarColorAttr(this, android.R.attr.colorBackground)

        setToolbar(viewBinding.toolbar, R.string.edit_tags)

        genresAdapter = ShortGenresAdapter(
            viewBinding.rvGenres,
            presenter::onGenreItemClicked,
            this::onGenreItemLongClicked,
            presenter::onRemoveGenreClicked
        )
        viewBinding.rvGenres.adapter = genresAdapter
        viewBinding.rvGenres.layoutManager = ChipsLayoutManager.newBuilder(this).build()

        viewBinding.changeAuthorClickableArea.setOnClickListener { presenter.onChangeAuthorClicked() }
        viewBinding.changeTitleClickableArea.setOnClickListener { presenter.onChangeTitleClicked() }
        viewBinding.changeFilenameClickableArea.setOnClickListener { presenter.onChangeFileNameClicked() }
        viewBinding.changeAlbumClickableArea.setOnClickListener { presenter.onChangeAlbumClicked() }
        viewBinding.changeAlbumArtistClickableArea.setOnClickListener { presenter.onChangeAlbumArtistClicked() }
        viewBinding.changeGenreClickableArea.setOnClickListener { presenter.onAddGenreItemClicked() }
        viewBinding.changeCoverClickableArea.setOnClickListener { presenter.onChangeCoverClicked() }
        viewBinding.changeLyricsClickableArea.setOnClickListener { presenter.onChangeLyricsClicked() }

        ViewUtils.onLongClick(viewBinding.changeAuthorClickableArea) {
            copyText(viewBinding.tvAuthor, viewBinding.tvAuthorHint)
        }
        ViewUtils.onLongClick(viewBinding.changeTitleClickableArea) {
            copyText(viewBinding.tvTitle, viewBinding.tvTitleHint)
        }
        ViewUtils.onLongClick(viewBinding.changeFilenameClickableArea) {
            presenter.onCopyFileNameClicked()
        }
        ViewUtils.onLongClick(viewBinding.changeAlbumClickableArea) {
            copyText(viewBinding.tvAlbum, viewBinding.tvAlbumHint)
        }
        ViewUtils.onLongClick(viewBinding.changeAlbumArtistClickableArea) {
            copyText(viewBinding.tvAlbumArtist, viewBinding.tvAlbumAuthorHint)
        }
        ViewUtils.onLongClick(viewBinding.changeLyricsClickableArea) {
            copyText(viewBinding.tvLyricsHint, viewBinding.tvLyrics)
        }

        SlidrPanel.attachWithNavBarChange(
            this,
            R.attr.playerPanelBackground,
            android.R.attr.colorBackground
        )

        val fm = supportFragmentManager
        errorHandler = ErrorHandler(
            this,
            presenter::onRetryFailedEditActionClicked,
            this::showEditorRequestDeniedMessage
        )
        authorDialogFragmentRunner = DialogFragmentRunner(
            fm,
            Tags.AUTHOR_TAG
        ) { fragment -> fragment.setOnCompleteListener(presenter::onNewAuthorEntered) }
        titleDialogFragmentRunner = DialogFragmentRunner(
            fm,
            Tags.TITLE_TAG
        ) { fragment -> fragment.setOnCompleteListener(presenter::onNewTitleEntered) }
        filenameDialogFragmentRunner = DialogFragmentRunner(
            fm,
            Tags.FILE_NAME_TAG
        ) { fragment -> fragment.setOnCompleteListener(presenter::onNewFileNameEntered) }
        albumDialogFragmentRunner = DialogFragmentRunner(
            fm,
            Tags.ALBUM_TAG
        ) { fragment ->
            fragment.setOnCompleteListener(presenter::onNewAlbumEntered)
        }
        albumArtistDialogFragmentRunner = DialogFragmentRunner(
            fm,
            Tags.ALBUM_ARTIST_TAG
        ) { fragment ->
            fragment.setOnCompleteListener(presenter::onNewAlbumArtistEntered)
        }
        addGenreDialogFragmentRunner = DialogFragmentRunner(
            fm,
            Tags.ADD_GENRE_TAG
        ) { fragment -> fragment.setOnCompleteListener(presenter::onNewGenreEntered) }
        lyricsDialogFragmentRunner = DialogFragmentRunner(
            fm,
            Tags.LYRICS
        ) { fragment -> fragment.setOnCompleteListener(presenter::onNewLyricsEntered) }
        editGenreDialogFragmentRunner = DialogFragmentRunner(
            fm,
            Tags.EDIT_GENRE_TAG
        ) { fragment ->
            fragment.setComplexCompleteListener { name, extra ->
                presenter.onNewGenreNameEntered(name, GenreSerializer.deserializeShort(extra))
            }
        }
        coverMenuDialogRunner = DialogFragmentRunner(
            fm,
            Tags.EDIT_COVER_TAG
        ) { fragment -> fragment.setOnCompleteListener(this::onCoverActionSelected) }
        progressDialogRunner = DialogFragmentDelayRunner(
            fm,
            Tags.PROGRESS_DIALOG_TAG,
            fragmentInitializer = { fragment -> fragment.setCancellationListener {
                presenter.onEditActionCancelled()
            } }
        )

        //<return genres after deep scan implementation>
        viewBinding.dividerLyrics.visibility = View.INVISIBLE
        viewBinding.tvGenreHint.visibility = View.GONE
        viewBinding.ivGenreEdit.visibility = View.GONE
        viewBinding.changeGenreClickableArea.visibility = View.GONE
        viewBinding.rvGenres.visibility = View.GONE
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(
            Components.getAppComponent().localeController().dispatchAttachBaseContext(base)
        )
    }

    override fun closeScreen() {
        finish()
    }

    override fun showCompositionLoadingError(errorCommand: ErrorCommand) {
        viewBinding.tvAuthor.text = errorCommand.message
    }

    override fun showComposition(composition: FullComposition) {
        viewBinding.tvTitle.text = composition.title

        val album = composition.album
        viewBinding.tvAlbum.text = album

        val albumArtistVisibility = if (album == null) View.GONE else View.VISIBLE
        viewBinding.tvAlbumArtist.visibility = albumArtistVisibility
        viewBinding.tvAlbumAuthorHint.visibility = albumArtistVisibility
        viewBinding.ivAlbumArtist.visibility = albumArtistVisibility
        viewBinding.dividerAlbumArtist.visibility = albumArtistVisibility

        //<return genres after deep scan implementation>
//        dividerLyrics.setVisibility(albumArtistVisibility);

        viewBinding.tvAlbumArtist.text = composition.albumArtist
        viewBinding.tvLyrics.text = composition.lyrics
        viewBinding.tvAuthor.text = FormatUtils.formatAuthor(composition.artist, this)
        viewBinding.tvFilename.text = FileUtils.formatFileName(composition.fileName, true)
    }

    override fun showCompositionCover(composition: FullComposition) {
        Components.getAppComponent()
            .imageLoader()
            .displayImageInReusableTarget(
                viewBinding.ivCover,
                composition,
                R.drawable.ic_music_placeholder
            )
    }

    override fun showGenres(shortGenres: List<ShortGenre>) {
        genresAdapter.submitList(shortGenres)
    }

    override fun showEnterAuthorDialog(composition: FullComposition, hints: Array<String>?) {
        val fragment = InputTextDialogFragment.Builder(
            R.string.change_author_name,
            R.string.change,
            R.string.cancel,
            R.string.artist,
            composition.artist
        ).hints(hints)
            .build()
        authorDialogFragmentRunner.show(fragment)
    }

    override fun showEnterAlbumArtistDialog(composition: FullComposition, hints: Array<String>?) {
        val fragment = InputTextDialogFragment.Builder(
            R.string.change_album_artist,
            R.string.change,
            R.string.cancel,
            R.string.artist,
            composition.albumArtist
        ).hints(hints)
            .build()
        albumArtistDialogFragmentRunner.show(fragment)
    }

    override fun showEnterLyricsDialog(composition: FullComposition) {
        val fragment = InputTextDialogFragment.Builder(
            R.string.edit_lyrics,
            R.string.change,
            R.string.cancel,
            R.string.lyrics,
            composition.lyrics
        ).completeOnEnterButton(false)
            .build()
        lyricsDialogFragmentRunner.show(fragment)
    }

    override fun showAddGenreDialog(genres: Array<String>?) {
        val fragment = InputTextDialogFragment.Builder(
            R.string.add_composition_genre,
            R.string.add,
            R.string.cancel,
            R.string.genre,
            null
        ).hints(genres)
            .canBeEmpty(false)
            .build()
        addGenreDialogFragmentRunner.show(fragment)
    }

    override fun showEditGenreDialog(shortGenre: ShortGenre, genres: Array<String>?) {
        val fragment = InputTextDialogFragment.Builder(
            R.string.change_composition_genre,
            R.string.change,
            R.string.cancel,
            R.string.genre,
            shortGenre.name
        ).hints(genres)
            .extra(GenreSerializer.serialize(shortGenre))
            .canBeEmpty(false)
            .build()
        editGenreDialogFragmentRunner.show(fragment)
    }

    override fun showEnterTitleDialog(composition: FullComposition) {
        val fragment = InputTextDialogFragment.newInstance(
            R.string.change_title,
            R.string.change,
            R.string.cancel,
            R.string.title,
            composition.title
        )
        titleDialogFragmentRunner.show(fragment)
    }

    override fun showEnterFileNameDialog(composition: FullComposition) {
        val fragment = InputTextDialogFragment.newInstance(
            R.string.change_file_name,
            R.string.change,
            R.string.cancel,
            R.string.filename,
            FileUtils.formatFileName(composition.fileName),
            false
        )
        filenameDialogFragmentRunner.show(fragment)
    }

    override fun showEnterAlbumDialog(composition: FullComposition, hints: Array<String>?) {
        val fragment = InputTextDialogFragment.Builder(
            R.string.change_album_name,
            R.string.change,
            R.string.cancel,
            R.string.album,
            composition.album
        ).hints(hints)
            .build()
        albumDialogFragmentRunner.show(fragment)
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        errorHandler.handleError(errorCommand) {
            MessagesUtils.makeSnackbar(
                viewBinding.container, errorCommand.message, Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun showCheckTagsErrorMessage(errorCommand: ErrorCommand) {
        val message = getString(R.string.check_tags_error, errorCommand.message)
        MessagesUtils.makeSnackbar(viewBinding.container, message, Snackbar.LENGTH_LONG).show()
    }

    override fun copyFileNameText(filePath: String) {
        AndroidUtils.copyText(this, FileUtils.formatFileName(filePath), getString(R.string.filename))
        onTextCopied()
    }

    override fun showRemovedGenreMessage(genre: ShortGenre) {
        val text = getString(R.string.genre_removed_message, genre.name)
        MessagesUtils.makeSnackbar(viewBinding.container, text, Snackbar.LENGTH_LONG)
            .setAction(R.string.cancel, presenter::onRestoreRemovedGenreClicked)
            .show()
    }

    override fun showCoverActionsDialog() {
        val fragment = MenuDialogFragment.newInstance(R.menu.cover_actions_menu, getString(R.string.change_cover))
        coverMenuDialogRunner.show(fragment)
    }

    override fun showSelectImageFromGalleryScreen() {
        pickImageContract.launch(null)
    }

    override fun showChangeFileProgress() {
        val fragment = newProgressDialogFragment(R.string.changing_file_progress)
        progressDialogRunner.show(fragment)
    }

    override fun hideChangeFileProgress() {
        progressDialogRunner.cancel()
    }

    override fun showSyncState(fileSyncState: FileSyncState, composition: FullComposition) {
        val isFileRemote = composition.storageId == null && composition.initialSource == InitialSource.REMOTE
        showFileSyncState(fileSyncState, isFileRemote, viewBinding.pvFileState)
        progressDialogRunner.runAction { dialog ->
            val message = if (fileSyncState is Downloading) {
                val progress = fileSyncState.getProgress()
                val progressPercentage = progress.asInt()
                dialog.setProgress(progressPercentage)
                getString(R.string.downloading_progress, progressPercentage.coerceAtLeast(0))
            } else {
                dialog.setIndeterminate(true)
                getString(R.string.changing_file_progress)
            }
            dialog.setMessage(message)
        }
    }

    private fun onCoverActionSelected(menuItem: MenuItem) {
        when (menuItem.itemId) {
            R.id.menu_pick_from_gallery -> presenter.onNewCoverSelected()
            R.id.menu_clear -> presenter.onClearCoverClicked()
        }
    }

    private fun onGenreItemLongClicked(genre: ShortGenre) {
        AndroidUtils.copyText(this, genre.name, getString(R.string.genre))
        onTextCopied()
    }

    private fun copyText(textView: TextView, tvLabel: TextView) {
        AndroidUtils.copyText(this, textView.text.toString(), tvLabel.text.toString())
        onTextCopied()
    }

    private fun onTextCopied() {
        MessagesUtils.makeSnackbar(viewBinding.container, R.string.copied_message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showEditorRequestDeniedMessage() {
        MessagesUtils.makeSnackbar(
            viewBinding.container,
            R.string.android_r_edit_file_permission_denied,
            Snackbar.LENGTH_LONG
        ).show()
    }

}