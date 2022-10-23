package com.github.anrimian.musicplayer.ui.library.folders.root

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.github.anrimian.musicplayer.Constants.Arguments.HIGHLIGHT_COMPOSITION_ID
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentRootLibraryFoldersBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.library.LibraryFragment
import com.github.anrimian.musicplayer.ui.library.folders.LibraryFoldersFragment
import com.github.anrimian.musicplayer.ui.library.folders.newFolderFragment
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.JugglerView
import com.github.anrimian.musicplayer.ui.utils.wrappers.DefferedObject
import moxy.ktx.moxyPresenter

fun newLibraryFoldersRootFragment(
    highlightCompositionId: Long = 0L
) = LibraryFoldersRootFragment().apply {
    arguments = Bundle().apply {
        putLong(HIGHLIGHT_COMPOSITION_ID, highlightCompositionId)
    }
}

class LibraryFoldersRootFragment : LibraryFragment(), FolderRootView, BackButtonListener {

    private val presenter by moxyPresenter {
        Components.getLibraryRootFolderComponent().folderRootPresenter()
    }

    private lateinit var binding: FragmentRootLibraryFoldersBinding

    private lateinit var jvFoldersContainer: JugglerView
    private lateinit var navigation: FragmentNavigation
    private val navigationWrapper = DefferedObject<FragmentNavigation>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRootLibraryFoldersBinding.inflate(inflater, container, false)
        jvFoldersContainer = binding.libraryFoldersContainer
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressStateView.onTryAgainClick { setupFolderTree() }

        navigation = FragmentNavigation.from(childFragmentManager)
        navigation.initialize(jvFoldersContainer, savedInstanceState)
        navigation.setExitAnimation(R.anim.anim_slide_out_right)
        navigation.setEnterAnimation(R.anim.anim_slide_in_right)
        navigationWrapper.setObject(navigation)

        setupFolderTree()
    }

    override fun onFragmentMovedOnTop() {
        super.onFragmentMovedOnTop()
        val toolbar = requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar)
        toolbar.setSubtitle(R.string.folders)
        val folderNavigation = FragmentNavigation.from(childFragmentManager)
        if (folderNavigation.isInitialized) {
            folderNavigation.dispatchMovedToTop()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        navigation.onSaveInstanceState(outState)
    }

    override fun onBackPressed(): Boolean {
        val fragment = navigation.fragmentOnTop
        return fragment is BackButtonListener && fragment.onBackPressed()
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        navigationWrapper.call { navigation -> navigation.setMenuVisible(menuVisible) }
    }

    override fun showFolderScreens(ids: List<Long?>) {
        val highlightCompositionId = requireArguments().getLong(HIGHLIGHT_COMPOSITION_ID)

        //if we have highlight request and target fragment is on top, just proceed call without creation
        val currentFragment = navigation.fragmentOnTop
        if (highlightCompositionId != 0L
            && currentFragment is LibraryFoldersFragment
            && currentFragment.getFolderId() == ids.last()
        ) {
            requireArguments().remove(HIGHLIGHT_COMPOSITION_ID)
            currentFragment.requestHighlightComposition(highlightCompositionId)
            return
        }

        navigation.addNewFragmentStack(
            ids.mapIndexed { index, folderId ->
                if (index == ids.lastIndex && highlightCompositionId != 0L) {
                    requireArguments().remove(HIGHLIGHT_COMPOSITION_ID)
                    newFolderFragment(folderId, highlightCompositionId)
                } else {
                    newFolderFragment(folderId)
                }
            },
            R.anim.anim_alpha_appear
        )
    }

    override fun showProgress() {
        binding.progressStateView.showProgress()
    }

    override fun showError(errorCommand: ErrorCommand) {
        binding.progressStateView.showMessage(errorCommand.message, true)
    }

    override fun showIdle() {
        binding.progressStateView.hideAll()
    }

    fun revealComposition(compositionId: Long) {
        requireArguments().putLong(HIGHLIGHT_COMPOSITION_ID, compositionId)
        setupFolderTree()
    }

    private fun setupFolderTree() {
        val highlightCompositionId = requireArguments().getLong(HIGHLIGHT_COMPOSITION_ID)
        if (highlightCompositionId != 0L) {
            presenter.onNavigateToCompositionRequested(highlightCompositionId)
        } else if (!navigation.hasScreens()) {
            presenter.onCreateFolderTreeRequested()
        }
    }

}