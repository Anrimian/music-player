package com.github.anrimian.musicplayer.ui.sleep_timer

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogSleepTimerBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.interactors.player.SleepTimerInteractor
import moxy.MvpAppCompatDialogFragment
import moxy.ktx.moxyPresenter

class SleepTimerDialogFragment : MvpAppCompatDialogFragment(), SleepTimerView {

    private val presenter by moxyPresenter { Components.getAppComponent().sleepTimerPresenter() }

    private lateinit var viewBinding: DialogSleepTimerBinding

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewBinding = DialogSleepTimerBinding.inflate(LayoutInflater.from(context))
        val dialog = AlertDialog.Builder(activity)
                .setTitle(R.string.sleep_timer)
                .setView(viewBinding.root)
                .setNegativeButton(R.string.close) { _, _ -> }
                .create()
        dialog.show()
        return dialog
    }

    override fun showSleepTimerTime(sleepTimerTimeMillis: Long) {

    }

    override fun showSleepTimerState(sleepTimerState: SleepTimerInteractor.SleepTimerState) {

    }

    override fun showSleepRemainingSeconds(remainingSeconds: Long) {

    }
}