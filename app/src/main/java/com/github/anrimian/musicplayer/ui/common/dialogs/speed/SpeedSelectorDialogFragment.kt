package com.github.anrimian.musicplayer.ui.common.dialogs.speed

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.DialogSpeedSelectorBinding
import com.github.anrimian.musicplayer.ui.common.view.onSpeedHold
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper

class SpeedSelectorDialogFragment : DialogFragment() {

    companion object {
        private const val CURRENT_SPEED = "current_speed"
        private const val MIN_SPEED = 0.25f
        private const val MAX_SPEED = 2.00f
        private const val DEFAULT_SPEED = 1f

        fun newInstance(currentSpeed: Float) = SpeedSelectorDialogFragment().apply {
            arguments = Bundle().apply {
                putFloat(CURRENT_SPEED, currentSpeed)
            }
        }
    }

    private lateinit var binding: DialogSpeedSelectorBinding
    private lateinit var btnReset: Button

    private var speedChangeListener: ((Float) -> Unit)? = null

    private var currentSpeed: Float = 0f

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        currentSpeed = savedInstanceState?.getFloat(CURRENT_SPEED)
            ?: requireArguments().getFloat(CURRENT_SPEED)

        binding = DialogSpeedSelectorBinding.inflate(LayoutInflater.from(context))
        binding.sbSpeed.max = ((MAX_SPEED - MIN_SPEED) * 100).toInt()

        val seekBarViewWrapper = SeekBarViewWrapper(binding.sbSpeed)
        seekBarViewWrapper.setProgressChangeListener(::onProgressValueChanged)
        seekBarViewWrapper.setProgress(((currentSpeed - MIN_SPEED) * 100).toInt())

        binding.ivDecreaseSpeed.setOnClickListener { changeSpeedBy(-1) }
        binding.ivDecreaseSpeed.onSpeedHold { changeSpeedBy(-1) }
        binding.ivIncreaseSpeed.setOnClickListener { changeSpeedBy(1) }
        binding.ivIncreaseSpeed.onSpeedHold { changeSpeedBy(1) }

        binding.tvSpeedMin.text = getString(R.string.playback_speed_template, MIN_SPEED)
        binding.tvSpeedMax.text = getString(R.string.playback_speed_template, MAX_SPEED)
        binding.tvCurrentSpeed.text = getString(R.string.playback_speed_template, currentSpeed)

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.playback_speed)
            .setView(binding.root)
            .setPositiveButton(R.string.close, null)
            .setNegativeButton(R.string.reset) { _, _ -> }
            .create()
        dialog.show()

        btnReset = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        btnReset.isEnabled = currentSpeed != DEFAULT_SPEED
        btnReset.setOnClickListener {
            currentSpeed = DEFAULT_SPEED
            onSpeedChanged()
            dialog.dismiss()
        }
        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putFloat(CURRENT_SPEED, currentSpeed)
    }

    fun setSpeedChangeListener(listener: (Float) -> Unit) {
        speedChangeListener = listener
    }

    private fun onSpeedChanged() {
        speedChangeListener?.invoke(currentSpeed)
    }

    private fun onProgressValueChanged(progress: Int) {
        currentSpeed = progress / 100f + MIN_SPEED
        btnReset.isEnabled = currentSpeed != DEFAULT_SPEED
        binding.ivDecreaseSpeed.isEnabled = currentSpeed != MIN_SPEED
        binding.ivIncreaseSpeed.isEnabled = currentSpeed != MAX_SPEED
        binding.tvCurrentSpeed.text = getString(R.string.playback_speed_template, currentSpeed)
        onSpeedChanged()
    }

    private fun changeSpeedBy(valueBy: Int) {
        val progress = binding.sbSpeed.progress + valueBy
        binding.sbSpeed.progress = progress
        onProgressValueChanged(binding.sbSpeed.progress)
    }

}