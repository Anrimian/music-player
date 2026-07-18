package com.github.anrimian.musicplayer.ui.settings.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.github.anrimian.musicplayer.AppConstants.Tags.ENABLED_MEDIA_PLAYERS
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerType
import com.github.anrimian.musicplayer.databinding.FragmentSettingsPlayerBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
import com.github.anrimian.musicplayer.domain.utils.millisToMinutes
import com.github.anrimian.musicplayer.domain.utils.minutesToMillis
import com.github.anrimian.musicplayer.ui.common.dialogs.showNumberPickerDialog
import com.github.anrimian.musicplayer.ui.common.dialogs.showSoundBalanceSelectorDialog
import com.github.anrimian.musicplayer.ui.common.format.getMediaPlayerName
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.equalizer.EqualizerDialogFragment
import com.github.anrimian.musicplayer.ui.settings.player.impls.EnabledMediaPlayersDialogFragment
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.applyBottomInsets
import com.github.anrimian.musicplayer.ui.utils.fragments.DialogFragmentRunner
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow
import com.github.anrimian.musicplayer.ui.utils.onCheckChanged
import com.github.anrimian.musicplayer.ui.utils.setCheckedImmediately
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

class PlayerSettingsFragment : MvpAppCompatFragment(), PlayerSettingsView {

    private val presenter by moxyPresenter { Components.getSettingsComponent().playerSettingsPresenter() }
    
    private lateinit var binding: FragmentSettingsPlayerBinding

    private lateinit var enabledMediaPlayersDialogFragmentRunner: DialogFragmentRunner<EnabledMediaPlayersDialogFragment>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.nsvContainer.applyBottomInsets()

        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.settings)
        toolbar.setSubtitle(R.string.playing)
        toolbar.setTitleClickListener(null)

        val silenceDurationSeconds = 2_000_000L / 1_000_000f
        binding.tvSkipSilenceSummary.text = getString(R.string.skip_silence_summary, silenceDurationSeconds)

        ViewUtils.onCheckChanged(binding.cbDecreaseVolume, presenter::onDecreaseVolumeOnAudioFocusLossChecked)
        ViewUtils.onCheckChanged(binding.cbPauseOnAudioFocusLoss, presenter::onPauseOnAudioFocusLossChecked)
        ViewUtils.onCheckChanged(binding.cbPauseOnZeroVolumeLevel, presenter::onPauseOnZeroVolumeLevelChecked)
        binding.cbPauseOnAudioDeviceRemove.onCheckChanged(presenter::onPauseOnAudioDeviceRemoveChecked)
        binding.cbSkipSilence.onCheckChanged(presenter::onSkipSilenceChecked)

        binding.flEqualizerClickableArea.setOnClickListener { showEqualizerDialog() }
        binding.flMediaPlayersClickableArea.setOnClickListener { showMediaPlayersSettingScreen() }
        binding.flKeepNotificationClickableArea.setOnClickListener { presenter.onSelectKeepNotificationTimeClicked() }
        binding.flSoundBalanceClickableArea.setOnClickListener { presenter.onSoundBalanceClicked() }

        SlidrPanel.simpleSwipeBack(binding.nsvContainer, this, toolbar::setNavigationButtonProgress)

        enabledMediaPlayersDialogFragmentRunner = DialogFragmentRunner(
            childFragmentManager,
            ENABLED_MEDIA_PLAYERS
        ) { fragment -> fragment.onCompleteListener = presenter::onEnabledMediaPlayersSelected }
    }

    override fun showDecreaseVolumeOnAudioFocusLossEnabled(checked: Boolean) {
        ViewUtils.setChecked(binding.cbDecreaseVolume, checked)
    }

    override fun showPauseOnAudioFocusLossEnabled(checked: Boolean) {
        ViewUtils.setChecked(binding.cbPauseOnAudioFocusLoss, checked)
    }

    override fun showPauseOnZeroVolumeLevelEnabled(enabled: Boolean) {
        ViewUtils.setChecked(binding.cbPauseOnZeroVolumeLevel, enabled)
    }

    override fun showPauseOnAudioDeviceRemoveEnabled(enabled: Boolean) {
        binding.cbPauseOnAudioDeviceRemove.setCheckedImmediately(enabled)
    }

    override fun showSkipSilenceEnabled(enabled: Boolean) {
        binding.cbSkipSilence.setCheckedImmediately(enabled)
    }

    override fun showSoundBalance(soundBalance: SoundBalance) {
        val left = (soundBalance.left * 100).toInt()
        val right = (soundBalance.right * 100).toInt()
        binding.tvSoundBalanceState.text = getString(R.string.sound_balance_state, left, right)
    }

    override fun showSelectedEqualizerType(type: Int) {
        binding.tvEqualizerState.setText(getEqualizerTypeDescription(type))
    }

    override fun showKeepNotificationTime(millis: Long) {
        val minutes = millis.millisToMinutes().toInt()
        binding.tvKeepNotificationTimeValue.text = resources.getQuantityString(
            R.plurals.for_at_least_minutes,
            minutes,
            minutes
        )
    }

    override fun showSelectKeepNotificationTimeDialog(currentValue: Long) {
        showNumberPickerDialog(
            requireContext(),
            0,
            15,
            currentValue.millisToMinutes()
        ) { value -> presenter.onKeepNotificationTimeSelected(value.minutesToMillis()) }
    }

    override fun showEnabledMediaPlayers(players: IntArray) {
        val sb = StringBuilder()
        players.forEachIndexed { index, id ->
            sb.append(getString(getMediaPlayerName(id)))
            if (index < players.size - 1) {
                sb.append(", ")
            }
        }
        binding.tvMediaPlayersState.text = sb.toString()
    }

    override fun showSoundBalanceDialog(soundBalance: SoundBalance) {
        showSoundBalanceSelectorDialog(
            requireContext(),
            soundBalance,
            presenter::onSoundBalancePicked,
            presenter::onSoundBalanceSelected,
            presenter::onResetSoundBalanceClick
        )
    }

    @StringRes
    private fun getEqualizerTypeDescription(type: Int): Int {
        return when (type) {
            EqualizerType.EXTERNAL -> R.string.system_equalizer
            EqualizerType.APP -> R.string.app_equalizer
            else -> R.string.no_equalizer
        }
    }

    private fun showEqualizerDialog() {
        EqualizerDialogFragment().safeShow(childFragmentManager)
    }

    private fun showMediaPlayersSettingScreen() {
        enabledMediaPlayersDialogFragmentRunner.show(EnabledMediaPlayersDialogFragment())
    }



}