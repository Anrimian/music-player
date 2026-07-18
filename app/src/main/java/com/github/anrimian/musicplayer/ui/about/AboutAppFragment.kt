package com.github.anrimian.musicplayer.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentAboutBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.settings.player.PlayerSettingsFragment
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.applyBottomInsets
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigation
import com.github.anrimian.musicplayer.ui.utils.fragments.navigation.FragmentNavigationListener
import com.github.anrimian.musicplayer.ui.utils.getAppInfo
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import com.github.anrimian.musicplayer.utils.logger.FileLog

class AboutAppFragment : Fragment(), FragmentNavigationListener {
    
    private lateinit var binding: FragmentAboutBinding
    
    private lateinit var fileLog: FileLog
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAboutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: AdvancedToolbar = requireActivity().findViewById(R.id.toolbar)

        binding.nsvContainer.applyBottomInsets()

        val appComponent = Components.getAppComponent()
        fileLog = appComponent.fileLog()
        val appLogger = appComponent.appLogger()
        val loggerRepository = appComponent.loggerRepository()

        val isLogExists = fileLog.isFileExists
        setLogActionsVisibility(isLogExists)
        if (isLogExists) {
            binding.tvLogInfo.text = getString(
                R.string.log_info_text,
                fileLog.fileSize / 1024
            )
        }
        appComponent.aboutTextBinder().bind(this, binding.tvAbout)

        binding.btnDelete.setOnClickListener { deleteLogFile() }
        binding.btnView.setOnClickListener { appLogger.startViewLogScreen(requireActivity()) }
        binding.btnSend.setOnClickListener { appLogger.startSendLogScreen(requireActivity()) }

        binding.cbShowReportDialogOnStart.isChecked = loggerRepository.isReportDialogOnStartEnabled()
        ViewUtils.onCheckChanged(binding.cbShowReportDialogOnStart, loggerRepository::showReportDialogOnStart)

        SlidrPanel.simpleSwipeBack(binding.nsvContainer, this, toolbar::setNavigationButtonProgress)
    }

    override fun onFragmentResumed() {
        requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar).setup {
            setTitle(R.string.app_name)
            val appInfo = requireContext().getAppInfo()
            setSubtitle(getString(
                R.string.version_template,
                appInfo.versionName,
                appInfo.versionCode
            ))
        }
    }

    fun openPlayerSettings() {
        FragmentNavigation.from(parentFragmentManager).addNewFragment(PlayerSettingsFragment())
    }

    private fun deleteLogFile() {
        fileLog.deleteLogFile()
        setLogActionsVisibility(false)
        Toast.makeText(requireContext(), R.string.log_file_deleted, Toast.LENGTH_SHORT).show()
    }

    private fun setLogActionsVisibility(isLogExists: Boolean) {
        val logActionsVisibility = if (isLogExists) View.VISIBLE else View.GONE
        binding.btnView.visibility = logActionsVisibility
        binding.btnSend.visibility = logActionsVisibility
        binding.btnDelete.visibility = logActionsVisibility
        binding.tvLogInfo.visibility = logActionsVisibility
    }

}