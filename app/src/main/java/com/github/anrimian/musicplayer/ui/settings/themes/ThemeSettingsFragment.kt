package com.github.anrimian.musicplayer.ui.settings.themes

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentSettingsThemesBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.theme.AppTheme
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.settings.themes.view.ThemesAdapter
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.colorFromAttr
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import com.r0adkll.slidr.model.SlidrInterface

class ThemeSettingsFragment : Fragment() {
    
    private lateinit var binding: FragmentSettingsThemesBinding

    private lateinit var slidrInterface: SlidrInterface
    private lateinit var themeController: ThemeController
    private lateinit var adapter: ThemesAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsThemesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        themeController = Components.getAppComponent().themeController()
        
        val toolbar = requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.settings)
        toolbar.setSubtitle(R.string.theme)
        toolbar.setTitleClickListener(null)
        
        slidrInterface = SlidrPanel.simpleSwipeBack(
            binding.nsvContainer, 
            this, 
            toolbar::onStackFragmentSlided
        )
        
        binding.rvThemes.layoutManager = GridLayoutManager(
            requireContext(), 
            2, 
            RecyclerView.HORIZONTAL, 
            false
        )
        binding.rvThemes.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                onThemesScrolled(binding.rvThemes.computeHorizontalScrollOffset() == 0)
            }
        })
        adapter = ThemesAdapter(AppTheme.appThemes(), themeController.getCurrentTheme(), ::onThemeClicked)
        binding.rvThemes.adapter = adapter
        
        ViewUtils.setChecked(binding.cbAutoNightMode, themeController.isAutoDarkThemeEnabled())
        binding.cbAutoNightMode.setOnCheckedChangeListener { _, isChecked: Boolean ->
            themeController.setAutoDarkModeEnabled(requireActivity(), isChecked)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ViewUtils.setChecked(binding.cbFollowSystemTheme, themeController.isFollowSystemThemeEnabled())
            binding.cbFollowSystemTheme.setOnCheckedChangeListener { _, isChecked: Boolean ->
                themeController.setFollowSystemThemeEnabled(requireActivity(), isChecked)
            }
        } else {
            binding.cbFollowSystemTheme.visibility = View.GONE
        }

        if (themeController.isCircleShapeEnabled()) {
            binding.ivCircle.setColorFilter(requireContext().colorFromAttr(R.attr.colorAccent))
            binding.ivRectangle.setColorFilter(requireContext().colorFromAttr(R.attr.disabledColor))
        } else {
            binding.ivCircle.setColorFilter(requireContext().colorFromAttr(R.attr.disabledColor))
            binding.ivRectangle.setColorFilter(requireContext().colorFromAttr(R.attr.colorAccent))
        }
        binding.ivCircle.setOnClickListener {
            themeController.setCircleShapeEnabled(requireActivity(), true)
        }
        binding.ivRectangle.setOnClickListener {
            themeController.setCircleShapeEnabled(requireActivity(), false)
        }
    }

    private fun onThemesScrolled(onStart: Boolean) {
        if (onStart) {
            slidrInterface.unlock()
        } else {
            slidrInterface.lock()
        }
    }

    private fun onThemeClicked(appTheme: AppTheme) {
        binding.cbFollowSystemTheme.isChecked = false
        themeController.setTheme(requireActivity(), appTheme)
        adapter.setCurrentTheme(appTheme)
    }

}