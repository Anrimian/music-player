package com.github.anrimian.musicplayer.ui.sleep_timer

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogSleepTimerBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.interactors.player.SleepTimerInteractor
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.sleep_timer.view.TimePickerWrapper
import moxy.MvpAppCompatDialogFragment
import moxy.ktx.moxyPresenter

class SleepTimerDialogFragment : MvpAppCompatDialogFragment(), SleepTimerView {

    private val presenter by moxyPresenter { Components.getAppComponent().sleepTimerPresenter() }

    private lateinit var viewBinding: DialogSleepTimerBinding

    private lateinit var timePickerWrapper: TimePickerWrapper

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewBinding = DialogSleepTimerBinding.inflate(LayoutInflater.from(context))

        timePickerWrapper = TimePickerWrapper(
                viewBinding.secondsPicker,
                viewBinding.minutesPicker,
                viewBinding.hoursPicker,
                presenter::onSleepTimerTimeChanged
        )

        viewBinding.btnClose.setOnClickListener { dismissAllowingStateLoss() }
        viewBinding.btnReset.setOnClickListener { presenter.onResetButtonClicked() }

        val dialog = AlertDialog.Builder(activity)
                .setTitle(R.string.sleep_timer)
                .setView(viewBinding.root)
                .create()
        dialog.show()

        return dialog
    }

    override fun showSleepTimerTime(sleepTimerTimeMillis: Long) {
        timePickerWrapper.showTime(sleepTimerTimeMillis)
    }

    override fun showSleepTimerState(state: SleepTimerInteractor.SleepTimerState) {
        viewBinding.tvRemainingTime.visibility

        when(state) {
            SleepTimerInteractor.SleepTimerState.ENABLED -> {
                viewBinding.tvRemainingTimeMessage.visibility = VISIBLE
                viewBinding.tvRemainingTime.visibility = VISIBLE
                setPickerVisibility(INVISIBLE)

                viewBinding.btnAction.setText(R.string.pause)
                viewBinding.btnAction.setOnClickListener { presenter.onStopClicked() }

            }
            SleepTimerInteractor.SleepTimerState.DISABLED -> {
                viewBinding.tvRemainingTimeMessage.visibility = INVISIBLE
                viewBinding.tvRemainingTime.visibility = INVISIBLE
                setPickerVisibility(VISIBLE)

                viewBinding.btnAction.setText(R.string.start)
                viewBinding.btnAction.setOnClickListener { presenter.onStartClicked() }
            }
            SleepTimerInteractor.SleepTimerState.PAUSED -> {
                viewBinding.tvRemainingTimeMessage.visibility = VISIBLE
                viewBinding.tvRemainingTime.visibility = VISIBLE
                setPickerVisibility(INVISIBLE)

                viewBinding.btnAction.setText(R.string.resume)
                viewBinding.btnAction.setOnClickListener { presenter.onResumeClicked() }
            }
        }
    }

    override fun showRemainingTimeMillis(millis: Long) {
        viewBinding.tvRemainingTime.text = FormatUtils.formatMilliseconds(millis)
    }

    private fun setPickerVisibility(visibility: Int) {
        timePickerWrapper.setVisibility(visibility)
        viewBinding.tvHoursDivider.visibility = visibility
        viewBinding.tvMinutesDivider.visibility = visibility
    }

}