package com.github.anrimian.musicplayer.ui.equalizer

import android.annotation.SuppressLint
import android.app.Dialog
import android.util.Pair
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerController
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.EqualizerType
import com.github.anrimian.musicplayer.data.controllers.music.equalizer.external.ExternalEqualizer
import com.github.anrimian.musicplayer.databinding.DialogEqualizerBinding
import com.github.anrimian.musicplayer.databinding.PartialEqualizerBandBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.equalizer.Band
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerConfig
import com.github.anrimian.musicplayer.domain.models.equalizer.EqualizerState
import com.github.anrimian.musicplayer.ui.common.compat.CompatUtils
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet.SimpleBottomSheetCallback
import com.github.anrimian.musicplayer.ui.utils.views.menu.SimpleMenuBuilder
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.RecyclerViewUtils
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import moxy.MvpBottomSheetDialogFragment
import moxy.ktx.moxyPresenter
import java.util.*
import kotlin.math.abs

class EqualizerDialogFragment : MvpBottomSheetDialogFragment(), EqualizerView {

    private val presenter by moxyPresenter { Components.getAppComponent().equalizerPresenter() }

    private lateinit var viewBinding: DialogEqualizerBinding

    private lateinit var equalizerController: EqualizerController

    private val bandsViewList = LinkedList<Pair<PartialEqualizerBandBinding, Band>>()
    private var inAppEqualizerSettingsEnabled = false

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        viewBinding = DialogEqualizerBinding.inflate(LayoutInflater.from(context))
        val view = viewBinding.root
        dialog.setContentView(view)

        view.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )

        val bottomSheetBehavior = ViewUtils.findBottomSheetBehavior(dialog)
        bottomSheetBehavior.addBottomSheetCallback(SimpleBottomSheetCallback({ newState ->
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismissAllowingStateLoss()
            }
        }, {}))
        bottomSheetBehavior.peekHeight = view.measuredHeight
        bottomSheetBehavior.skipCollapsed = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        
        AndroidUtils.setDialogNavigationBarColorAttr(dialog, R.attr.dialogBackground)
        
        RecyclerViewUtils.attachDynamicShadow(viewBinding.nestedScrollView, viewBinding.titleShadow)
        
        equalizerController = Components.getAppComponent().equalizerController()
        
        viewBinding.rbUseSystemEqualizer.setOnClickListener { enableSystemEqualizer() }
        viewBinding.btnOpenSystemEqualizer.setOnClickListener { openSystemEqualizer() }
        viewBinding.rbUseAppEqualizer.setOnClickListener { enableAppEqualizer() }
        viewBinding.rbDisableEqualizer.setOnClickListener { disableEqualizer() }
        viewBinding.ivClose.setOnClickListener { dismissAllowingStateLoss() }
        viewBinding.btnRestartSystemEqualizer.setOnClickListener { presenter.onRestartAppEqClicked() }
        
        showActiveEqualizer(equalizerController.selectedEqualizerType)
        
        CompatUtils.setOutlineButtonStyle(viewBinding.btnOpenSystemEqualizer)
        CompatUtils.setOutlineButtonStyle(viewBinding.tvPresets)
        CompatUtils.setOutlineButtonStyle(viewBinding.btnRestartSystemEqualizer)
    }

    override fun onResume() {
        super.onResume()
        DialogUtils.setupBottomSheetDialogMaxWidth(this)
        val textResId: Int
        val enabled: Boolean
        if (ExternalEqualizer.isExternalEqualizerExists(requireContext())) {
            textResId = R.string.external_equalizer_description
            enabled = true
        } else {
            textResId = R.string.external_equalizer_not_found
            enabled = false
        }
        viewBinding.btnOpenSystemEqualizer.isEnabled = enabled
        viewBinding.rbUseSystemEqualizer.isEnabled = enabled
        viewBinding.tvSystemEqualizerText.isEnabled = enabled
        viewBinding.tvSystemEqualizerDescription.isEnabled = enabled
        viewBinding.tvSystemEqualizerDescription.text = getString(textResId)
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        Toast.makeText(requireContext(), errorCommand.message, Toast.LENGTH_LONG).show()
    }

    override fun displayEqualizerConfig(config: EqualizerConfig) {
        for (band in config.bands) {
            val binding = PartialEqualizerBandBinding.inflate(LayoutInflater.from(context))
            bandsViewList.add(Pair(binding, band))
            val lp = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            lp.weight = 1f
            viewBinding.llBands.addView(binding.root, lp) //recalculate dialog height?
            
            val lowestRange = config.lowestBandRange
            val highestRange = config.highestBandRange
            val max = highestRange - lowestRange
            binding.sbLevel.max = max
            val seekBarViewWrapper = SeekBarViewWrapper(binding.sbLevel)
            seekBarViewWrapper.setProgressChangeListener { progress: Int ->
                val value = (progress - abs(lowestRange.toInt())).toShort()
                binding.tvLevel.text = FormatUtils.formatDecibels(value)
                presenter.onBandLevelChanged(band, value)
            }
            seekBarViewWrapper.setOnSeekStopListener { presenter.onBandLevelDragStopped() }
            binding.tvFrequency.text = FormatUtils.formatMilliHz(band.centerFreq)
        }
        val presets = config.presets
        val menuBuilder = SimpleMenuBuilder(requireContext())
        for (i in presets.indices) {
            val preset = presets[i]
            menuBuilder.add(i, preset.presetName)
        }
        viewBinding.tvPresets.setOnClickListener { view ->
            PopupMenuWindow.showActionBarPopup(
                view,
                menuBuilder.items,
                { menu -> presenter.onPresetSelected(presets[menu.itemId]) },
                Gravity.END
            )
        }
        setInAppEqualizerSettingsEnabled(inAppEqualizerSettingsEnabled)
    }

    override fun displayEqualizerState(equalizerState: EqualizerState, config: EqualizerConfig) {
        for (pair in bandsViewList) {
            val binding = pair.first
            val band = pair.second
            val currentRange = equalizerState.bendLevels[band.bandNumber] ?: return
            binding.tvLevel.text = FormatUtils.formatDecibels(currentRange)
            val lowestRange = config.lowestBandRange
            binding.sbLevel.progress = currentRange + abs(lowestRange.toInt())
        }
    }

    override fun showEqualizerRestartButton(show: Boolean) {
        viewBinding.btnRestartSystemEqualizer.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun enableSystemEqualizer() {
        equalizerController.enableEqualizer(EqualizerType.EXTERNAL)
        showActiveEqualizer(EqualizerType.EXTERNAL)
    }

    private fun enableAppEqualizer() {
        equalizerController.enableEqualizer(EqualizerType.APP)
        showActiveEqualizer(EqualizerType.APP)
    }

    private fun openSystemEqualizer() {
        equalizerController.launchExternalEqualizerSetup(activity)
        showActiveEqualizer(EqualizerType.EXTERNAL)
    }

    private fun disableEqualizer() {
        equalizerController.disableEqualizer()
        showActiveEqualizer(EqualizerType.NONE)
    }

    private fun showActiveEqualizer(type: Int) {
        viewBinding.rbUseSystemEqualizer.isChecked = type == EqualizerType.EXTERNAL
        viewBinding.rbUseAppEqualizer.isChecked = type == EqualizerType.APP
        viewBinding.rbDisableEqualizer.isChecked = type == EqualizerType.NONE
        setInAppEqualizerSettingsEnabled(type == EqualizerType.APP)
    }

    private fun setInAppEqualizerSettingsEnabled(enabled: Boolean) {
        inAppEqualizerSettingsEnabled = enabled
        for (pair in bandsViewList) {
            val binding = pair.first
            binding.sbLevel.isEnabled = enabled
        }
        viewBinding.tvPresets.isEnabled = enabled
    }
}