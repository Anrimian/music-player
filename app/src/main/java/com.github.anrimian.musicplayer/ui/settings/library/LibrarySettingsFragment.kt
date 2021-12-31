package com.github.anrimian.musicplayer.ui.settings.library

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentLibrarySettingsBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.dialogs.DialogUtils
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.settings.folders.ExcludedFoldersFragment
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils.onCheckChanged
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentLayerListener
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

/**
 * Created on 19.10.2017.
 */
class LibrarySettingsFragment : MvpAppCompatFragment(), FragmentLayerListener, LibrarySettingsView {

    private val presenter by moxyPresenter { Components.getSettingsComponent().librarySettingsPresenter() }

    private lateinit var viewBinding: FragmentLibrarySettingsBinding

    private lateinit var navigation: FragmentNavigation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentLibrarySettingsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)

        navigation = FragmentNavigation.from(parentFragmentManager)

        viewBinding.tvExcludedFolders.setOnClickListener {
            navigation.addNewFragment(ExcludedFoldersFragment())
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            viewBinding.cbDoNotShowDeleteDialog.visibility = View.GONE
        }

        onCheckChanged(viewBinding.cbDoNotShowDeleteDialog, presenter::doNotAppConfirmDialogChecked)
        onCheckChanged(viewBinding.cbShowAllAudioFiles, presenter::onShowAllAudioFilesChecked)

        viewBinding.flAudioMinDurationClickableArea.setOnClickListener {
            presenter.onSelectMinDurationClicked()
        }

        SlidrPanel.simpleSwipeBack(viewBinding.flContainer, this, toolbar::onStackFragmentSlided)
    }

    override fun onFragmentMovedOnTop() {
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.settings)
        toolbar.setSubtitle(R.string.library)
        toolbar.setTitleClickListener(null)
    }

    override fun showAppConfirmDeleteDialogEnabled(enabled: Boolean) {
        ViewUtils.setChecked(viewBinding.cbDoNotShowDeleteDialog, !enabled)
    }

    override fun showAllAudioFilesEnabled(enabled: Boolean) {
        ViewUtils.setChecked(viewBinding.cbShowAllAudioFiles, enabled)
    }

    override fun showAudioFileMinDurationMillis(millis: Long) {
        val seconds = (millis/1000L).toInt()
        viewBinding.tvAudioMinDurationValue.text = getString(
            R.string.with_duration_less_than,
            resources.getQuantityString(R.plurals.seconds_template, seconds, seconds)
        )
    }

    override fun showSelectMinAudioDurationDialog(currentValue: Long) {
        DialogUtils.showNumberPickerDialog(
            requireContext(),
            0,
            60,
            (currentValue / 1000L).toInt()
        ) { value -> presenter.onAudioFileMinDurationMillisPicked(value * 1000L) }
    }

}