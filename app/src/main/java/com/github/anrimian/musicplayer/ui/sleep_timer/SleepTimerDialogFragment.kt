package com.github.anrimian.musicplayer.ui.sleep_timer

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogSleepTimerBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerState
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerState.DISABLED
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerState.ENABLED
import com.github.anrimian.musicplayer.domain.interactors.sleep_timer.SleepTimerState.PAUSED
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.sleep_timer.view.TimePickerWrapper
import moxy.MvpAppCompatDialogFragment
import moxy.ktx.moxyPresenter

class SleepTimerDialogFragment : MvpAppCompatDialogFragment(), SleepTimerView {

    private val presenter by moxyPresenter { Components.getAppComponent().sleepTimerPresenter() }

    private lateinit var binding: DialogSleepTimerBinding

    private lateinit var timePickerWrapper: TimePickerWrapper

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DialogSleepTimerBinding.inflate(LayoutInflater.from(context))

        timePickerWrapper = TimePickerWrapper(
            binding.etSeconds,
            binding.etMinutes,
            binding.etHours,
            presenter::onSleepTimerTimeChanged
        )

        binding.btnClose.setOnClickListener { dismissAllowingStateLoss() }

        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.sleep_timer)
            .setView(binding.root)
            .create()
        dialog.show()

        return dialog
    }

    override fun showSleepTimerTime(sleepTimerTimeMillis: Long) {
        timePickerWrapper.showTime(sleepTimerTimeMillis)
    }

    override fun showSleepTimerState(state: SleepTimerState) {
        when(state) {
            ENABLED -> {
                setPickerEnabled(false)
                setTimerEnabled(true)

                binding.btnAction.setText(R.string.stop)
                binding.btnAction.setOnClickListener { presenter.onStopClicked() }

            }
            DISABLED -> {
                setPickerEnabled(true)
                setTimerEnabled(false)

                binding.btnAction.setText(R.string.start)
                binding.btnAction.setOnClickListener { presenter.onStartClicked() }
            }
            PAUSED -> {
                setPickerEnabled(false)
                setTimerEnabled(true)

                binding.btnAction.setText(R.string.resume)
                binding.btnAction.setOnClickListener { presenter.onResumeClicked() }
            }
        }
    }

    override fun showRemainingTimeMillis(millis: Long) {
        binding.tvRemainingTime.text = FormatUtils.formatMilliseconds(millis, false)
    }

    private fun setPickerVisibility(visibility: Int) {
        timePickerWrapper.setVisibility(visibility)
        binding.tvHoursDivider.visibility = visibility
        binding.tvMinutesDivider.visibility = visibility
    }

    private fun setPickerEnabled(enabled: Boolean) {
        timePickerWrapper.setEnabled(enabled)
        binding.tvHoursDivider.isEnabled = enabled
        binding.tvMinutesDivider.isEnabled = enabled
    }

    private fun setTimerEnabled(enabled: Boolean) {
        binding.tvRemainingTimeMessage.isEnabled = enabled
        binding.tvRemainingTime.isEnabled = enabled
    }

}