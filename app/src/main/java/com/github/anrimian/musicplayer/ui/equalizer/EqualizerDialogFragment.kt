package com.github.anrimian.musicplayer.ui.equalizer

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.util.Pair
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.View.MeasureSpec
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
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
import com.github.anrimian.musicplayer.ui.common.dialogs.AppBottomSheetDialog
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.common.menu.PopupMenuWindow
import com.github.anrimian.musicplayer.ui.common.toolbar.ToolbarBackgroundDrawable
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.applyBottomInsets
import com.github.anrimian.musicplayer.ui.utils.attrColor
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr
import com.github.anrimian.musicplayer.ui.utils.getColorCompat
import com.github.anrimian.musicplayer.ui.utils.getDimension
import com.github.anrimian.musicplayer.ui.utils.views.bottom_sheet.SimpleBottomSheetCallback
import com.github.anrimian.musicplayer.ui.utils.views.delegate.BoundValuesDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.DelegateManager
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ElevationDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.InsetPaddingTopDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ItemDrawableTopCornersDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ReverseDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.SlideDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.TextColorDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.TintDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ToolbarBackgroundStatusBarDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ToolbarDrawableColorDelegate
import com.github.anrimian.musicplayer.ui.utils.views.delegate.ToolbarDrawableTopCornersDelegate
import com.github.anrimian.musicplayer.ui.utils.views.menu.SimpleMenuBuilder
import com.github.anrimian.musicplayer.ui.utils.views.recycler_view.ItemDrawable
import com.github.anrimian.musicplayer.ui.utils.views.seek_bar.SeekBarViewWrapper
import com.google.android.material.bottomsheet.BottomSheetBehavior
import moxy.MvpBottomSheetDialogFragment
import moxy.ktx.moxyPresenter
import java.util.LinkedList
import kotlin.math.abs

class EqualizerDialogFragment : MvpBottomSheetDialogFragment(), EqualizerView {

    private val presenter by moxyPresenter { Components.getAppComponent().equalizerPresenter() }

    private lateinit var binding: DialogEqualizerBinding

    private lateinit var equalizerController: EqualizerController
    private lateinit var slideDelegate: SlideDelegate

    private lateinit var toolbarBackgroundDrawable: ToolbarBackgroundDrawable
    private lateinit var backgroundDrawable: ItemDrawable

    private val bandsViewList = LinkedList<Pair<PartialEqualizerBandBinding, Band>>()
    private var inAppEqualizerSettingsEnabled = false

    private var insetTop = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AppBottomSheetDialog(
            requireContext(),
            theme,
            requireContext().colorFromAttr(R.attr.listItemBackground)
        )
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)
        binding = DialogEqualizerBinding.inflate(LayoutInflater.from(context))
        val view = binding.root
        dialog.setContentView(view)

        view.measure(
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        )

        toolbarBackgroundDrawable = ToolbarBackgroundDrawable(
            attrColor(R.attr.listItemBackground),
            requireContext().getColorCompat(R.color.translucent_gray)
        )
        binding.clContainer.background = toolbarBackgroundDrawable

        backgroundDrawable = ItemDrawable()
        backgroundDrawable.setColor(attrColor(R.attr.listItemBackground))
        (view.parent as View).background = backgroundDrawable

        slideDelegate = buildSlideDelegate()
        slideDelegate.onSlide(0f)
        val bottomSheetBehavior = ViewUtils.findBottomSheetBehavior(dialog)
        bottomSheetBehavior.addBottomSheetCallback(SimpleBottomSheetCallback(
            { newState ->
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismissAllowingStateLoss()
                }
            },
            this::onBottomSheetSlided
        ))
        bottomSheetBehavior.peekHeight = view.measuredHeight
        bottomSheetBehavior.skipCollapsed = true
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        binding.nestedScrollView.applyBottomInsets()

        equalizerController = Components.getAppComponent().equalizerController()

        binding.rbUseSystemEqualizer.setOnClickListener { enableSystemEqualizer() }
        binding.btnOpenSystemEqualizer.setOnClickListener { openSystemEqualizer() }
        binding.rbUseAppEqualizer.setOnClickListener { enableAppEqualizer() }
        binding.rbDisableEqualizer.setOnClickListener { disableEqualizer() }
        binding.ivClose.setOnClickListener { dismissAllowingStateLoss() }
        binding.btnRestartSystemEqualizer.setOnClickListener { presenter.onRestartAppEqClicked() }

        showActiveEqualizer(equalizerController.getSelectedEqualizerType())

        CompatUtils.setOutlineButtonStyle(binding.btnOpenSystemEqualizer)
        CompatUtils.setOutlineButtonStyle(binding.tvPresets)
        CompatUtils.setOutlineButtonStyle(binding.btnRestartSystemEqualizer)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            insetTop = insets.top
            onBottomSheetSlided(1f)
            windowInsets
        }
    }

    override fun onResume() {
        super.onResume()
        val textResId: Int
        val enabled: Boolean
        if (ExternalEqualizer.isExternalEqualizerExists(requireContext())) {
            textResId = R.string.external_equalizer_description
            enabled = true
        } else {
            textResId = R.string.external_equalizer_not_found
            enabled = false
        }
        binding.btnOpenSystemEqualizer.isEnabled = enabled
        binding.rbUseSystemEqualizer.isEnabled = enabled
        binding.tvSystemEqualizerText.isEnabled = enabled
        binding.tvSystemEqualizerDescription.isEnabled = enabled
        binding.tvSystemEqualizerDescription.text = getString(textResId)
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
            this.binding.llBands.addView(binding.root, lp) //recalculate dialog height?

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
        binding.tvPresets.setOnClickListener { view ->
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
            val currentRange = equalizerState.bandLevels[band.bandNumber] ?: return
            binding.tvLevel.text = FormatUtils.formatDecibels(currentRange)
            val lowestRange = config.lowestBandRange
            binding.sbLevel.progress = currentRange + abs(lowestRange.toInt())
        }
    }

    override fun showEqualizerRestartButton(show: Boolean) {
        binding.btnRestartSystemEqualizer.visibility = if (show) View.VISIBLE else View.GONE
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
        equalizerController.launchExternalEqualizerSetup(requireActivity())
        showActiveEqualizer(EqualizerType.EXTERNAL)
    }

    private fun disableEqualizer() {
        equalizerController.disableEqualizer()
        showActiveEqualizer(EqualizerType.NONE)
    }

    private fun showActiveEqualizer(type: Int) {
        binding.rbUseSystemEqualizer.isChecked = type == EqualizerType.EXTERNAL
        binding.rbUseAppEqualizer.isChecked = type == EqualizerType.APP
        binding.rbDisableEqualizer.isChecked = type == EqualizerType.NONE
        setInAppEqualizerSettingsEnabled(type == EqualizerType.APP)
    }

    private fun setInAppEqualizerSettingsEnabled(enabled: Boolean) {
        inAppEqualizerSettingsEnabled = enabled
        for (pair in bandsViewList) {
            val binding = pair.first
            binding.sbLevel.isEnabled = enabled
        }
        binding.tvPresets.isEnabled = enabled
    }

    private fun onBottomSheetSlided(slideOffset: Float) {
        binding.root.post {
            val contentView = AndroidUtils.getContentView(activity) ?: return@post

            var usableSlideOffset = slideOffset
            val activityHeight = contentView.height - insetTop
            val viewHeight = binding.root.height
            if (activityHeight > viewHeight) {
                usableSlideOffset = 0f
            }
            slideDelegate.onSlide(usableSlideOffset)
        }
    }

    private fun buildSlideDelegate(): SlideDelegate {
        val boundDelegate: SlideDelegate = DelegateManager()
            .addDelegate(
                BoundValuesDelegate(0.5f, 0.7f, ReverseDelegate(
                    DelegateManager().apply {
                        val radius = getDimension(R.dimen.bottom_sheet_corner_size)
                        addDelegate(ToolbarDrawableTopCornersDelegate(toolbarBackgroundDrawable, radius))
                        addDelegate(ItemDrawableTopCornersDelegate(backgroundDrawable, radius))
                    }
                ))
            )
            .addDelegate(
                BoundValuesDelegate(
                    0.7f,
                    1f,
                    DelegateManager()
                        .addDelegate(
                            TextColorDelegate(
                                binding.tvTitle,
                                android.R.attr.textColorPrimary,
                                R.attr.toolbarTextColorPrimary
                            )
                        )
                        .addDelegate(
                            TintDelegate(
                                binding.ivClose,
                                R.attr.buttonColorInverse,
                                R.attr.toolbarTextColorPrimary
                            )
                        )
                        .addDelegate(
                            ToolbarDrawableColorDelegate(
                                requireContext(),
                                toolbarBackgroundDrawable,
                                R.attr.listItemBackground,
                                R.attr.colorPrimary
                            )
                        )
                        .addDelegate(
                            BoundValuesDelegate(
                                0.8f,
                                1f,
                                ElevationDelegate(
                                    binding.clContainer,
                                    0f,
                                    getDimension(R.dimen.toolbar_elevation)
                                )
                            )
                        )
                        .addDelegate(
                            InsetPaddingTopDelegate(
                                binding.clContainer,
                                0f,
                                { insetTop.toFloat() }
                            )
                        )
                        .addDelegate(
                            ToolbarBackgroundStatusBarDelegate(
                                toolbarBackgroundDrawable,
                                { insetTop.toFloat() }
                            )
                        )
                )
            )
        return BoundValuesDelegate(0.008f, 0.95f, boundDelegate)
    }

}