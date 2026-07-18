package com.github.anrimian.musicplayer.ui.library.folders.root

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.anrimian.musicplayer.AppConstants.Arguments.HIGHLIGHT_COMPOSITION_ID
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentRootLibraryFoldersBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.error.ErrorCommand
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.library.common.setupLibraryTitle
import com.github.anrimian.musicplayer.ui.library.folders.LibraryFoldersFragment
import com.github.anrimian.musicplayer.ui.library.folders.volumes.LibraryVolumesFragment
import com.github.anrimian.musicplayer.ui.utils.args
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.BackActionRemoveCallback
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.JugglerView
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.attachBackPressedCallback
import com.github.anrimian.musicplayer.ui.utils.wrappers.DefferedObject
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

class LibraryFoldersRootFragment : MvpAppCompatFragment(), FolderRootView,
    FragmentNavigationListener {

    companion object {

        fun newInstance(
            highlightCompositionId: Long = 0L
        ) = LibraryFoldersRootFragment().apply {
            arguments = Bundle().apply {
                putLong(HIGHLIGHT_COMPOSITION_ID, highlightCompositionId)
            }
        }

    }

    private val presenter by moxyPresenter {
        Components.getLibraryRootFolderComponent().folderRootPresenter()
    }

    private lateinit var binding: FragmentRootLibraryFoldersBinding

    private lateinit var toolbar: AdvancedToolbar

    private lateinit var jvFoldersContainer: JugglerView
    private lateinit var navigation: FragmentNavigation
    private val navigationWrapper = DefferedObject<FragmentNavigation>()
    private lateinit var backActionRemoveCallback: BackActionRemoveCallback
    private val backActionWrapper = DefferedObject<BackActionRemoveCallback>()

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
        toolbar = requireActivity().findViewById(R.id.toolbar)

        binding.progressStateView.onTryAgainClick { setupFolderTree() }

        navigation = FragmentNavigation.from(childFragmentManager)
        navigation.initialize(jvFoldersContainer, savedInstanceState)
        backActionRemoveCallback = navigation.attachBackPressedCallback(requireActivity())
        backActionWrapper.setObject(backActionRemoveCallback)
        navigation.setExitAnimation(R.anim.anim_slide_out_right)
        navigation.setEnterAnimation(R.anim.anim_slide_in_right)
        navigationWrapper.setObject(navigation)

        setupFolderTree()
    }

    override fun onFragmentResumed() {
        requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar).setup {
            setupLibraryTitle(this@LibraryFoldersRootFragment)
            setSubtitle(R.string.folders)
        }
        val folderNavigation = FragmentNavigation.from(childFragmentManager)
        if (folderNavigation.isInitialized) {
            folderNavigation.dispatchMovedToTop()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        navigation.onSaveInstanceState(outState)
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        backActionWrapper.call { callback -> callback.setPaused(!menuVisible) }
        navigationWrapper.call { navigation -> navigation.setMenuVisible(menuVisible) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        backActionRemoveCallback.remove()
    }

    override fun showFolderScreens(ids: List<Long>) {
        val highlightCompositionId = args.getLong(HIGHLIGHT_COMPOSITION_ID)

        //if we have highlight request and target fragment is on top without active search,
        // just proceed call without creation
        val currentFragment = navigation.fragmentOnTop
        if (highlightCompositionId != 0L && currentFragment is LibraryFoldersFragment) {
            val isSearchActive = toolbar.isInSearchMode()
            if (isSearchActive) {
                toolbar.setSearchModeEnabled(false)
                if (toolbar.isSearchLocked()) {
                    toolbar.setSearchLocked(false)
                }
            }
            if (!isSearchActive && currentFragment.getFolderId() == ids.last()) {
                args.remove(HIGHLIGHT_COMPOSITION_ID)
                currentFragment.requestHighlightComposition(highlightCompositionId)
                return
            }
        }

        val fragments = ArrayList<Fragment>(ids.size + 1)
        fragments.add(LibraryVolumesFragment())
        ids.forEachIndexed { index, folderId ->
            if (index == ids.lastIndex && highlightCompositionId != 0L) {
                args.remove(HIGHLIGHT_COMPOSITION_ID)
                fragments.add(LibraryFoldersFragment.newInstance(folderId, highlightCompositionId))
            } else {
                fragments.add(LibraryFoldersFragment.newInstance(folderId))
            }
        }
        navigation.addNewFragmentStack(fragments, R.anim.anim_alpha_appear)
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
        args.putLong(HIGHLIGHT_COMPOSITION_ID, compositionId)
        setupFolderTree()
    }

    private fun setupFolderTree() {
        val highlightCompositionId = args.getLong(HIGHLIGHT_COMPOSITION_ID)
        if (highlightCompositionId != 0L) {
            presenter.onNavigateToCompositionRequested(highlightCompositionId)
        } else if (!navigation.hasScreens()) {
            presenter.onCreateFolderTreeRequested()
        }
    }

}