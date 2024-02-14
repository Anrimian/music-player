package com.github.anrimian.musicplayer.ui.player_screen.lyrics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ActionMenuView
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentLyricsBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.editor.lyrics.LyricsEditorActivity
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.views.menu.ActionMenuUtil
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

class LyricsFragment: MvpAppCompatFragment(), LyricsView {

    private val presenter by moxyPresenter { Components.getLibraryComponent().lyricsPresenter() }

    private lateinit var binding: FragmentLyricsBinding

    private lateinit var clPlayQueueContainer: CoordinatorLayout
    private lateinit var acvToolbar: ActionMenuView

    private var isActionMenuEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLyricsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clPlayQueueContainer = requireActivity().findViewById(R.id.cl_play_queue_container)
        acvToolbar = requireActivity().findViewById(R.id.acvPlayQueue)

        binding.progressStateView.onTryAgainClick(presenter::onEditLyricsClicked)
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
            binding.progressStateView.showMessage(R.string.no_current_composition)
            binding.tvLyrics.visibility = View.GONE
            isActionMenuEnabled = false
            showMenuState()
            return
        }

        isActionMenuEnabled = true
        showMenuState()

        if (text.isEmpty()) {
            binding.progressStateView.showMessage(
                R.string.no_lyrics_for_current_composition,
                R.string.edit_lyrics
            )
            binding.tvLyrics.visibility = View.GONE
        } else {
            binding.progressStateView.hideAll()
            binding.tvLyrics.visibility = View.VISIBLE
            binding.tvLyrics.text = text
        }
    }

    override fun showEditLyricsScreen(compositionId: Long) {
        startActivity(LyricsEditorActivity.newIntent(requireContext(), compositionId))
    }

    override fun resetTextPosition() {
        binding.nsvContainer.scrollTo(0, 0)
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

}