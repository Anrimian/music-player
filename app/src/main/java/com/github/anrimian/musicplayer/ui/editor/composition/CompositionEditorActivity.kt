package com.github.anrimian.musicplayer.ui.editor.composition

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.ItemTouchHelper
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.github.anrimian.filesync.models.state.file.FileSyncState
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.Constants.Tags
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.models.image.UriImageSource
import com.github.anrimian.musicplayer.databinding.ActivityCompositionEditBinding
import com.github.anrimian.musicplayer.databinding.ItemGenreChipAddBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.utils.FileUtils
import com.github.anrimian.musicplayer.domain.utils.TextUtils
import com.github.anrimian.musicplayer.ui.common.AppAndroidUtils
import com.github.anrimian.musicplayer.ui.common.activity.PickImageContract
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.format.asInt
import com.github.anrimian.musicplayer.ui.common.format.showFileSyncState
import com.github.anrimian.musicplayer.ui.common.format.showSnackbar
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.editor.composition.list.GenreChipViewHolder
import com.github.anrimian.musicplayer.ui.editor.composition.list.GenreChipsAdapter
import com.github.anrimian.musicplayer.ui.editor.lyrics.LyricsEditorActivity
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.dialogs.ProgressDialogFragment
import com.github.anrimian.musicplayer.ui.utils.dialogs.menu.MenuDialogFragment
import com.github.anrimian.musicplayer.ui.utils.dialogs.newProgressDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentDelayRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.setDrawableStart
import com.github.anrimian.musicplayer.ui.utils.setToolbar
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.SingleItemAdapter
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.touch_helper.drag_and_drop.SimpleItemTouchHelperCallback
import com.google.android.material.snackbar.Snackbar
import moxy.MvpAppCompatActivity
import moxy.ktx.moxyPresenter

class CompositionEditorActivity : MvpAppCompatActivity(), CompositionEditorView {

    companion object {
        fun newIntent(context: Context, compositionId: Long): Intent {
            val intent = Intent(context, CompositionEditorActivity::class.java)
            intent.putExtra(Constants.Arguments.COMPOSITION_ID_ARG, compositionId)
            return intent
        }
    }

    private val presenter by moxyPresenter {
        val compositionId = intent.getLongExtra(Constants.Arguments.COMPOSITION_ID_ARG, 0)
        Components.getCompositionEditorComponent(compositionId).compositionEditorPresenter()
    }

    private lateinit var binding: ActivityCompositionEditBinding
    private lateinit var genresAdapter: GenreChipsAdapter
    private lateinit var errorHandler: ErrorHandler

    private lateinit var authorDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var titleDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var filenameDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var albumDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var albumArtistDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var addGenreDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var editGenreDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var trackNumberDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var discNumberDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var commentDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
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
        binding = ActivityCompositionEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AndroidUtils.setNavigationBarColorAttr(this, android.R.attr.colorBackground)

        setToolbar(binding.toolbar, R.string.edit_tags)

        val touchHelperCallback = SimpleItemTouchHelperCallback(
            horizontalDrag = true,
            shouldNotDragViewHolder = { holder -> holder !is GenreChipViewHolder },
            dragElevation = 4f
        )
        touchHelperCallback.setOnMovedListener(presenter::onGenreItemMoved)
        touchHelperCallback.setOnStartDragListener(presenter::onGenreItemDragStarted)
        touchHelperCallback.setOnEndDragListener(presenter::onGenreItemDragEnded)
        val itemTouchHelper = ItemTouchHelper(touchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.rvGenres)

        genresAdapter = GenreChipsAdapter(
            binding.rvGenres,
            presenter::onGenreItemClicked,
            presenter::onRemoveGenreClicked
        )
        binding.rvGenres.layoutManager = ChipsLayoutManager.newBuilder(this).build()

        binding.changeAuthorClickableArea.setOnClickListener { presenter.onChangeAuthorClicked() }
        binding.changeTitleClickableArea.setOnClickListener { presenter.onChangeTitleClicked() }
        binding.changeFilenameClickableArea.setOnClickListener { presenter.onChangeFileNameClicked() }
        binding.changeAlbumClickableArea.setOnClickListener { presenter.onChangeAlbumClicked() }
        binding.changeAlbumArtistClickableArea.setOnClickListener { presenter.onChangeAlbumArtistClicked() }
        binding.ivGenreEdit.setOnClickListener { presenter.onAddGenreItemClicked() }
        binding.changeCoverClickableArea.setOnClickListener { presenter.onChangeCoverClicked() }
        binding.changeTrackNumberClickableArea.setOnClickListener { presenter.onChangeTrackNumberClicked() }
        binding.changeDiscNumberClickableArea.setOnClickListener { presenter.onChangeDiscNumberClicked() }
        binding.changeCommentClickableArea.setOnClickListener { presenter.onChangeCommentClicked() }
        binding.changeLyricsClickableArea.setOnClickListener { presenter.onChangeLyricsClicked() }

        ViewUtils.onLongClick(binding.changeAuthorClickableArea) {
            copyText(binding.tvAuthor, binding.tvAuthorHint)
        }
        ViewUtils.onLongClick(binding.changeTitleClickableArea) {
            copyText(binding.tvTitle, binding.tvTitleHint)
        }
        ViewUtils.onLongClick(binding.changeFilenameClickableArea) {
            presenter.onCopyFileNameClicked()
        }
        ViewUtils.onLongClick(binding.changeAlbumClickableArea) {
            copyText(binding.tvAlbum, binding.tvAlbumHint)
        }
        ViewUtils.onLongClick(binding.changeAlbumArtistClickableArea) {
            copyText(binding.tvAlbumArtist, binding.tvAlbumAuthorHint)
        }
        ViewUtils.onLongClick(binding.changeLyricsClickableArea) {
            copyText(binding.tvLyrics, binding.tvLyricsHint)
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
            this::onEditorRequestDenied
        )
        authorDialogFragmentRunner = DialogFragmentRunner(fm, Tags.AUTHOR_TAG) { fragment ->
            fragment.setOnCompleteListener(presenter::onNewAuthorEntered)
        }
        titleDialogFragmentRunner = DialogFragmentRunner(fm, Tags.TITLE_TAG) { fragment ->
            fragment.setOnCompleteListener(presenter::onNewTitleEntered)
        }
        filenameDialogFragmentRunner = DialogFragmentRunner(fm, Tags.FILE_NAME_TAG) { fragment ->
            fragment.setOnCompleteListener(presenter::onNewFileNameEntered)
        }
        albumDialogFragmentRunner = DialogFragmentRunner(fm, Tags.ALBUM_TAG) { fragment ->
            fragment.setOnCompleteListener(presenter::onNewAlbumEntered)
        }
        albumArtistDialogFragmentRunner = DialogFragmentRunner(fm, Tags.ALBUM_ARTIST_TAG) { fragment ->
            fragment.setOnCompleteListener(presenter::onNewAlbumArtistEntered)
        }
        addGenreDialogFragmentRunner = DialogFragmentRunner(fm, Tags.ADD_GENRE_TAG) { fragment ->
            fragment.setOnCompleteListener(presenter::onNewGenreEntered)
        }
        editGenreDialogFragmentRunner = DialogFragmentRunner(fm, Tags.EDIT_GENRE_TAG) { fragment ->
            fragment.setComplexCompleteListener { name, extra ->
                presenter.onNewGenreNameEntered(name, extra.getString(Constants.Arguments.NAME_ARG)!!)
            }
        }
        trackNumberDialogFragmentRunner = DialogFragmentRunner(fm, Tags.TRACK_NUMBER_TAG) { fragment ->
            fragment.setOnCompleteListener { text ->
                presenter.onNewTrackNumberEntered(text.toLongOrNull())
            }
        }
        discNumberDialogFragmentRunner = DialogFragmentRunner(fm, Tags.DISC_NUMBER_TAG) { fragment ->
            fragment.setOnCompleteListener { text ->
                presenter.onNewDiscNumberEntered(text.toLongOrNull())
            }
        }
        commentDialogFragmentRunner = DialogFragmentRunner(fm, Tags.COMMENT_TAG) { fragment ->
            fragment.setOnCompleteListener(presenter::onNewCommentEntered)
        }
        coverMenuDialogRunner = DialogFragmentRunner(fm, Tags.EDIT_COVER_TAG) { fragment ->
            fragment.setOnCompleteListener(this::onCoverActionSelected) }
        progressDialogRunner = DialogFragmentDelayRunner(
            fm,
            Tags.PROGRESS_DIALOG_TAG,
            delayMillis = Constants.EDIT_DIALOG_DELAY_MILLIS,
            fragmentInitializer = { fragment -> fragment.setCancellationListener {
                presenter.onEditActionCancelled()
            } }
        )
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
        binding.tvAuthor.text = errorCommand.message
    }

    override fun showComposition(composition: FullComposition, genres: List<String>) {
        binding.tvTitle.text = composition.title

        val album = composition.album
        binding.tvAlbum.text = album

        val albumFieldsVisibility = if (album == null) View.GONE else View.VISIBLE
        binding.tvAlbumArtist.visibility = albumFieldsVisibility
        binding.tvAlbumAuthorHint.visibility = albumFieldsVisibility
        binding.ivAlbumArtist.visibility = albumFieldsVisibility
        binding.dividerAlbumArtist.visibility = albumFieldsVisibility

        binding.tvTrackNumber.visibility = albumFieldsVisibility
        binding.tvTrackNumberHint.visibility = albumFieldsVisibility
        binding.ivTrackNumber.visibility = albumFieldsVisibility
        binding.dividerTrackNumber.visibility = albumFieldsVisibility
        binding.tvDiscNumber.visibility = albumFieldsVisibility
        binding.tvDiscNumberHint.visibility = albumFieldsVisibility
        binding.dividerTrackNumberVertical.visibility = albumFieldsVisibility

        binding.tvAlbumArtist.text = composition.albumArtist
        binding.tvLyrics.text = composition.lyrics
        binding.tvAuthor.text = FormatUtils.formatAuthor(composition.artist, this)
        binding.tvFilename.text = FileUtils.formatFileName(composition.fileName, true)
        binding.tvTrackNumber.text = TextUtils.toString(composition.trackNumber)
        binding.tvDiscNumber.text = TextUtils.toString(composition.discNumber)
        binding.tvComment.text = composition.comment

        genresAdapter.submitList(genres)
        if (binding.rvGenres.adapter == null) {
            val addGenreAdapterItem = SingleItemAdapter { inflater, parent ->
                ItemGenreChipAddBinding.inflate(inflater, parent, false).apply {
                    tvAdd.setDrawableStart(R.drawable.ic_shape_circle_plus, R.dimen.chip_drawable_size)
                    root.setOnClickListener { presenter.onAddGenreItemClicked() }
                }
            }
            binding.rvGenres.adapter = ConcatAdapter(genresAdapter, addGenreAdapterItem)
        }
    }

    override fun showCompositionCover(composition: FullComposition) {
        Components.getAppComponent()
            .imageLoader()
            .displayImageInReusableTarget(
                binding.ivCover,
                composition,
                R.drawable.ic_music_placeholder
            )
    }

    override fun showEnterAuthorDialog(composition: FullComposition, hints: Array<String>?) {
        val fragment = InputTextDialogFragment.newInstance(
            R.string.change_author_name,
            R.string.change,
            R.string.cancel,
            R.string.artist,
            composition.artist,
            hints = hints
        )
        authorDialogFragmentRunner.show(fragment)
    }

    override fun showEnterAlbumArtistDialog(composition: FullComposition, hints: Array<String>?) {
        val fragment = InputTextDialogFragment.newInstance(
            R.string.change_album_artist,
            R.string.change,
            R.string.cancel,
            R.string.artist,
            composition.albumArtist,
            hints = hints
        )
        albumArtistDialogFragmentRunner.show(fragment)
    }

    override fun showEnterLyricsDialog(composition: FullComposition) {
        startActivity(LyricsEditorActivity.newIntent(this, composition.id))
    }

    override fun showAddGenreDialog(genres: Array<String>?) {
        val fragment = InputTextDialogFragment.newInstance(
            R.string.add_composition_genre,
            R.string.add,
            R.string.cancel,
            R.string.genre,
            null,
            hints = genres,
            canBeEmpty = false
        )
        addGenreDialogFragmentRunner.show(fragment)
    }

    override fun showEditGenreDialog(genre: String, genres: Array<String>?) {
        val fragment = InputTextDialogFragment.newInstance(
            R.string.change_composition_genre,
            R.string.change,
            R.string.cancel,
            R.string.genre,
            genre,
            hints = genres,
            canBeEmpty = false,
            extra = Bundle().apply { putString(Constants.Arguments.NAME_ARG, genre) }
        )
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
            canBeEmpty = false
        )
        filenameDialogFragmentRunner.show(fragment)
    }

    override fun showEnterAlbumDialog(composition: FullComposition, hints: Array<String>?) {
        val fragment = InputTextDialogFragment.newInstance(
            R.string.change_album_name,
            R.string.change,
            R.string.cancel,
            R.string.album,
            composition.album,
            hints = hints
        )
        albumDialogFragmentRunner.show(fragment)
    }

    override fun showEnterTrackNumberDialog(composition: FullComposition) {
        val fragment = InputTextDialogFragment.newInstance(
            R.string.change_track_number,
            R.string.change,
            R.string.cancel,
            R.string.track_number,
            composition.trackNumber?.toString(),
            inputType = InputType.TYPE_CLASS_NUMBER,
            digits = "0123456789"
        )
        trackNumberDialogFragmentRunner.show(fragment)
    }

    override fun showEnterDiscNumberDialog(composition: FullComposition) {
        val fragment = InputTextDialogFragment.newInstance(
            R.string.change_disc_number,
            R.string.change,
            R.string.cancel,
            R.string.disc_number,
            composition.discNumber?.toString(),
            inputType = InputType.TYPE_CLASS_NUMBER,
            digits = "0123456789"
        )
        discNumberDialogFragmentRunner.show(fragment)
    }

    override fun showEnterCommentDialog(composition: FullComposition) {
        val fragment = InputTextDialogFragment.newInstance(
            R.string.change_comment,
            R.string.change,
            R.string.cancel,
            R.string.comment,
            composition.comment,
            completeOnEnterButton = false
        )
        commentDialogFragmentRunner.show(fragment)
    }

    override fun notifyGenreItemMoved(from: Int, to: Int) {
        genresAdapter.notifyItemMoved(from, to)
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        errorHandler.handleError(errorCommand) {
            binding.container.showSnackbar(errorCommand.message, Snackbar.LENGTH_LONG)
        }
    }

    override fun showCheckTagsErrorMessage(errorCommand: ErrorCommand) {
        val message = getString(R.string.check_tags_error, errorCommand.message)
        MessagesUtils.makeSnackbar(binding.container, message, Snackbar.LENGTH_LONG).show()
    }

    override fun copyFileNameText(filePath: String) {
        AppAndroidUtils.copyText(binding.container, FileUtils.formatFileName(filePath), getString(R.string.filename))
    }

    override fun showRemovedGenreMessage(genre: String) {
        val text = getString(R.string.genre_removed_message, genre)
        MessagesUtils.makeSnackbar(binding.container, text, Snackbar.LENGTH_LONG)
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
        showFileSyncState(fileSyncState, isFileRemote, binding.pvFileState)
        progressDialogRunner.runAction { dialog ->
            val message = if (fileSyncState is FileSyncState.Downloading) {
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

    private fun copyText(textView: TextView, tvLabel: TextView): Boolean {
        val text = textView.text.toString()
        if (text.isEmpty()) {
            return false
        }
        AppAndroidUtils.copyText(binding.container, text, tvLabel.text.toString())
        return true
    }

    private fun onEditorRequestDenied() {
        presenter.onEditRequestDenied()
        MessagesUtils.makeSnackbar(
            binding.container,
            R.string.android_r_edit_file_permission_denied,
            Snackbar.LENGTH_LONG
        ).show()
    }

}