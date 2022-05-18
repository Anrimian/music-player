package com.github.anrimian.musicplayer.ui.common.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogSoundBalanceBinding
import com.github.anrimian.musicplayer.domain.models.player.SoundBalance
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper

fun showSoundBalanceSelectorDialog(
    context: Context,
    balance: SoundBalance,
    onBalancePicked: (SoundBalance) -> Unit,
    onBalanceSelected: (SoundBalance) -> Unit,
    onReset: () -> Unit,
) {

    val viewBinding = DialogSoundBalanceBinding.inflate(LayoutInflater.from(context))
    viewBinding.sbSoundBalance.max = 200
    val seekBarViewWrapper = SeekBarViewWrapper(viewBinding.sbSoundBalance)
    seekBarViewWrapper.setProgressChangeListener { progress ->
        val left = if (progress < 100) progress else 100
        val right = if (progress > 100) 100 - (progress - 100) else 100
        viewBinding.tvLeftValue.text = context.getString(R.string.percent_template, left)
        viewBinding.tvRightValue.text = context.getString(R.string.percent_template, right)
        onBalancePicked(SoundBalance(left/100f, right/100f))
    }
    seekBarViewWrapper.setOnSeekStopListener { progress ->
        val left = if (progress < 100) progress else 100
        val right = if (progress > 100) 100 - (progress - 100) else 100
        onBalanceSelected(SoundBalance(left/100f, right/100f))
    }
    val balanceLeft = (balance.left * 100).toLong()
    val balanceRight = (balance.right * 100).toLong()
    val progress = if (balanceLeft < 100) balanceLeft else balanceLeft + (100 - balanceRight)
    seekBarViewWrapper.setProgress(progress)
    viewBinding.tvLeftValue.text = context.getString(R.string.percent_template, balanceLeft)
    viewBinding.tvRightValue.text = context.getString(R.string.percent_template, balanceRight)

    AlertDialog.Builder(context)
        .setTitle(R.string.sound_balance)
        .setView(viewBinding.root)
        .setPositiveButton(android.R.string.ok) { _, _ -> }
        .setNegativeButton(R.string.reset) { _, _ -> onReset()}
        .show()
}