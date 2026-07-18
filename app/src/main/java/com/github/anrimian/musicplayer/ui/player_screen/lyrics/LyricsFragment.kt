package com.github.anrimian.musicplayer.ui.player_screen.lyrics

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.ActionMenuView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.utils.viewModel
import com.github.anrimian.musicplayer.ui.common.compose.AppTheme
import com.github.anrimian.musicplayer.ui.common.effects.BaseEffect
import com.github.anrimian.musicplayer.ui.common.effects.CommonEffect
import com.github.anrimian.musicplayer.ui.common.navigation.Screen
import com.github.anrimian.musicplayer.ui.editor.lyrics.LyricsEditorActivity
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.sleep_timer.SleepTimerDialogFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.views.menu.ActionMenuUtil

class LyricsFragment: Fragment() {

    private val viewModel by viewModel<LyricsViewModel>()

    private lateinit var acvToolbar: ActionMenuView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        acvToolbar = requireActivity().findViewById(R.id.acvPlayQueue)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme {
                    LyricsScreen(
                        viewModel = viewModel,
                        navigationCallback = ::handleNavigationEffect,
                        actionsCallback = ::handleActionEffect,
                        menuStateCallback = ::onMenuStateChanged
                    )
                }
            }
        }
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (menuVisible && ::acvToolbar.isInitialized) {
            ActionMenuUtil.setupMenu(acvToolbar, R.menu.lyrics_menu, this::onLyricsMenuItemClicked)
            onMenuStateChanged(viewModel.state.value.isEditLyricsEnabled)
        }
    }

    private fun onLyricsMenuItemClicked(menuItem: MenuItem) {
        when(menuItem.itemId) {
            R.id.menu_edit_lyrics -> viewModel.onEditLyricsClicked()
            R.id.menu_sleep_timer -> viewModel.onSleepTimerClicked()
            R.id.menu_equalizer -> viewModel.onEqualizerClicked()
        }
    }

    private fun onMenuStateChanged(isEditLyricsEnabled: Boolean) {
        if (::acvToolbar.isInitialized) {
            acvToolbar.menu.findItem(R.id.menu_edit_lyrics)?.isEnabled = isEditLyricsEnabled
        }
    }

    private fun handleNavigationEffect(effect: CommonEffect.NavigationEffect) {
        when (val screen = effect.screen) {
            is Screen.LyricsEditor -> {
                startActivity(LyricsEditorActivity.newIntent(requireContext(), screen.compositionId))
            }
            else -> {}
        }
    }

    private fun handleActionEffect(effect: BaseEffect) {
        when(effect) {
            LyricsEffect.ShowSleepTimerDialog -> {
                SleepTimerDialogFragment().safeShow(childFragmentManager)
            }
            LyricsEffect.ShowEqualizerDialog -> {
                EqualizerDialogFragment().safeShow(childFragmentManager)
            }
        }
    }

}