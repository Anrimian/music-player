package com.github.anrimian.musicplayer.ui.player_screen.lyrics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ActionMenuView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentLyricsBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.dialogs.input.InputTextDialogFragment
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.editor.common.ErrorHandler
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.dialogs.ProgressDialogFragment
import com.github.anrimian.musicplayer.ui.utils.dialogs.newProgressDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentDelayRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.views.menu.ActionMenuUtil
import com.google.android.material.snackbar.Snackbar
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

class LyricsFragment: MvpAppCompatFragment(), LyricsView {

    private val presenter by moxyPresenter { Components.getLibraryComponent().lyricsPresenter() }

    private lateinit var viewBinding: FragmentLyricsBinding

    private lateinit var clPlayQueueContainer: CoordinatorLayout
    private lateinit var acvToolbar: ActionMenuView

    private lateinit var errorHandler: ErrorHandler

    private lateinit var lyricsDialogFragmentRunner: DialogFragmentRunner<InputTextDialogFragment>
    private lateinit var progressDialogRunner: DialogFragmentDelayRunner<ProgressDialogFragment>

    private var isActionMenuEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentLyricsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clPlayQueueContainer = requireActivity().findViewById(R.id.cl_play_queue_container)
        acvToolbar = requireActivity().findViewById(R.id.acvPlayQueue)

        viewBinding.progressStateView.onTryAgainClick(presenter::onEditLyricsClicked)

        errorHandler = ErrorHandler(
            this,
            presenter::onRetryFailedEditActionClicked,
            this::showEditorRequestDeniedMessage
        )

        val fm = childFragmentManager
        lyricsDialogFragmentRunner = DialogFragmentRunner(
            fm,
            Constants.Tags.LYRICS
        ) { fragment -> fragment.setOnCompleteListener(presenter::onNewLyricsEntered) }
        progressDialogRunner = DialogFragmentDelayRunner(fm, Constants.Tags.PROGRESS_DIALOG_TAG)
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible) {
            ActionMenuUtil.setupMenu(acvToolbar, R.menu.lyrics_menu, this::onLyricsMenuItemClicked)
            showMenuState()
        }
    }

    override fun showLyrics(text: String?) {
        if (text == null) {
            viewBinding.progressStateView.showMessage(R.string.no_current_composition)
            viewBinding.tvLyrics.visibility = View.GONE
            isActionMenuEnabled = false
            showMenuState()
            return
        }

        isActionMenuEnabled = true
        showMenuState()

        if (text.isEmpty()) {
            viewBinding.progressStateView.showMessage(
                R.string.no_lyrics_for_current_composition,
                R.string.edit_lyrics
            )
            viewBinding.tvLyrics.visibility = View.GONE
        } else {
            viewBinding.progressStateView.hideAll()
            viewBinding.tvLyrics.visibility = View.VISIBLE
            viewBinding.tvLyrics.text = text
        }
    }

    override fun showEnterLyricsDialog(lyrics: String) {
        val fragment = InputTextDialogFragment.Builder(
            R.string.edit_lyrics,
            R.string.change,
            R.string.cancel,
            R.string.lyrics,
            lyrics
        ).completeOnEnterButton(false)
            .build()
        lyricsDialogFragmentRunner.show(fragment)
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
            MessagesUtils.makeSnackbar(
                clPlayQueueContainer, errorCommand.message, Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun resetTextPosition() {
        viewBinding.nsvContainer.scrollTo(0, 0)
    }

    private fun onLyricsMenuItemClicked(menuItem: MenuItem) {
        when(menuItem.itemId) {
            R.id.menu_edit_lyrics -> presenter.onEditLyricsClicked()
            R.id.menu_sleep_timer -> SleepTimerDialogFragment().safeShow(childFragmentManager)
            R.id.menu_equalizer -> EqualizerDialogFragment().safeShow(childFragmentManager)
        }
    }

    private fun showMenuState() {
        acvToolbar.menu.findItem(R.id.menu_edit_lyrics)?.isEnabled = isActionMenuEnabled
    }

    private fun showEditorRequestDeniedMessage() {
        MessagesUtils.makeSnackbar(
            clPlayQueueContainer,
            R.string.android_r_edit_file_permission_denied,
            Snackbar.LENGTH_LONG
        ).show()
    }
}