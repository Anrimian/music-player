package com.github.anrimian.musicplayer.ui.settings.folders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentExcludedFoldersBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.folders.IgnoredFolder
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.format.MessagesUtils
import com.github.anrimian.musicplayer.ui.common.format.showSnackbar
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.settings.folders.view.ExcludedFolderAdapter
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import com.google.android.material.snackbar.Snackbar
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

class ExcludedFoldersFragment : MvpAppCompatFragment(), ExcludedFoldersView,
    FragmentNavigationListener {
    
    private val presenter by moxyPresenter { Components.getLibraryComponent().excludedFoldersPresenter() }

    private lateinit var binding: FragmentExcludedFoldersBinding

    private lateinit var adapter: ExcludedFolderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExcludedFoldersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.excluded_folders)
        toolbar.setSubtitle(null)
        toolbar.setTitleClickListener(null)

        adapter = ExcludedFolderAdapter(binding.rvExcludedFolders, presenter::onDeleteFolderClicked)
        binding.rvExcludedFolders.adapter = adapter
        binding.rvExcludedFolders.layoutManager = LinearLayoutManager(requireContext())

        SlidrPanel.simpleSwipeBack(binding.clContainer, this, toolbar::onStackFragmentSlided)
    }

    override fun onFragmentResumed() {
        val toolbar = requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar)
        toolbar.clearOptionsMenu()
    }

    override fun showListState() {
        binding.progressStateView.hideAll()
    }

    override fun showEmptyListState() {
        binding.progressStateView.showMessage(R.string.no_excluded_folders)
    }

    override fun showErrorState(errorCommand: ErrorCommand) {
        binding.progressStateView.showMessage(errorCommand.message, false)
    }

    override fun showExcludedFoldersList(folders: List<IgnoredFolder>) {
        adapter.submitList(folders)
    }

    override fun showRemovedFolderMessage(folder: IgnoredFolder) {
        MessagesUtils.makeSnackbar(
            binding.clContainer,
            R.string.ignored_folder_removed,
            Snackbar.LENGTH_LONG
        ).setAction(R.string.cancel, presenter::onRestoreRemovedFolderClicked)
            .show()
    }

    override fun showErrorMessage(errorCommand: ErrorCommand) {
        binding.clContainer.showSnackbar(errorCommand.message)
    }
}