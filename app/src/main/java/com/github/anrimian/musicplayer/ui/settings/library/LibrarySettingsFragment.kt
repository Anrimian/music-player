package com.github.anrimian.musicplayer.ui.settings.library

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentLibrarySettingsBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.dialogs.showNumberPickerDialog
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.settings.folders.ExcludedFoldersFragment
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils.onCheckChanged
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

/**
 * Created on 19.10.2017.
 */
class LibrarySettingsFragment : MvpAppCompatFragment(),
    FragmentNavigationListener, LibrarySettingsView {

    private val presenter by moxyPresenter { Components.getSettingsComponent().librarySettingsPresenter() }

    private lateinit var binding: FragmentLibrarySettingsBinding

    private lateinit var navigation: FragmentNavigation

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLibrarySettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)

        navigation = FragmentNavigation.from(parentFragmentManager)

        binding.tvExcludedFolders.setOnClickListener {
            navigation.addNewFragment(ExcludedFoldersFragment())
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            binding.cbDoNotShowDeleteDialog.visibility = View.GONE
        }

        onCheckChanged(binding.cbDoNotShowDeleteDialog, presenter::doNotAppConfirmDialogChecked)
        onCheckChanged(binding.cbShowAllAudioFiles, presenter::onShowAllAudioFilesChecked)
        onCheckChanged(binding.cbPlaylistInsertStart, presenter::onPlaylistInsertStartChecked)
        onCheckChanged(binding.cbPlaylistDuplicateCheck, presenter::onPlaylistDuplicateCheckChecked)

        binding.flAudioMinDurationClickableArea.setOnClickListener {
            presenter.onSelectMinDurationClicked()
        }

        SlidrPanel.simpleSwipeBack(binding.nsvContainer, this, toolbar::onStackFragmentSlided)
    }

    override fun onFragmentResumed() {
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)
        toolbar.setTitle(R.string.settings)
        toolbar.setSubtitle(R.string.library)
        toolbar.setTitleClickListener(null)
    }

    override fun showAppConfirmDeleteDialogEnabled(enabled: Boolean) {
        ViewUtils.setChecked(binding.cbDoNotShowDeleteDialog, !enabled)
    }

    override fun showAllAudioFilesEnabled(enabled: Boolean) {
        ViewUtils.setChecked(binding.cbShowAllAudioFiles, enabled)
    }

    override fun showAudioFileMinDurationMillis(millis: Long) {
        val seconds = (millis/1000L).toInt()
        binding.tvAudioMinDurationValue.text = getString(
            R.string.with_duration_less_than,
            resources.getQuantityString(R.plurals.seconds_template, seconds, seconds)
        )
    }

    override fun showPlaylistInsertStartEnabled(enabled: Boolean) {
        ViewUtils.setChecked(binding.cbPlaylistInsertStart, enabled)
    }

    override fun showPlaylistDuplicateCheckEnabled(enabled: Boolean) {
        ViewUtils.setChecked(binding.cbPlaylistDuplicateCheck, enabled)
    }

    override fun showSelectMinAudioDurationDialog(currentValue: Long) {
        showNumberPickerDialog(
            requireContext(),
            0,
            60,
            currentValue / 1000L
        ) { value -> presenter.onAudioFileMinDurationMillisPicked(value * 1000L) }
    }

}