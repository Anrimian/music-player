package com.github.anrimian.musicplayer.ui.main

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.anrimian.musicplayer.Constants
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.utils.Permissions
import com.github.anrimian.musicplayer.databinding.DialogErrorReportBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.ui.common.activity.BaseAppCompatActivity
import com.github.anrimian.musicplayer.ui.main.setup.SetupFragment
import com.github.anrimian.musicplayer.ui.player_screen.PlayerFragment
import com.github.anrimian.musicplayer.ui.utils.AndroidUtils
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.fragments.BackButtonListener
import com.github.anrimian.musicplayer.ui.utils.fragments.safeShow

class MainActivity : BaseAppCompatActivity() {

    companion object {

        fun showInFolders(activity: FragmentActivity, composition: Composition) {
            val currentFragment = activity.supportFragmentManager.findFragmentById(R.id.main_activity_container)
            if (currentFragment is PlayerFragment) {
                currentFragment.locateCompositionInFolders(composition)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Components.getAppComponent().themeController().applyCurrentTheme(this)
        super.onCreate(savedInstanceState)

        //trick to fix internal id issue in ViewPager2.
        //Delete after this issue will be solved: https://issuetracker.google.com/issues/185820237
        //Solution is taken from https://stackoverflow.com/a/59989710/5541688
        //counter reduced from 1000 to 100, return back if fix won't help
        for (i in 0..98) {
            ViewCompat.generateViewId()
        }

        setContentView(R.layout.activity_main)

        val loggerRepository = Components.getAppComponent().loggerRepository()
        val wasCriticalFatalError = loggerRepository.wasCriticalFatalError()
        if (Permissions.hasFilePermission(this) && !wasCriticalFatalError) {
            Components.getAppComponent().musicServiceInteractor().prepare()
        }

        if (savedInstanceState == null) {
            if (loggerRepository.wasFatalError() && loggerRepository.isReportDialogOnStartEnabled()
                || wasCriticalFatalError
            ) {
                ErrorReportDialogFragment().safeShow(supportFragmentManager, null)
                if (wasCriticalFatalError) {
                    return
                }
            }
            startScreens()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val fragment = supportFragmentManager.findFragmentById(R.id.main_activity_container)
        if (fragment is PlayerFragment) {
            if (getOpenPlayerPanelArg(intent)) {
                fragment.openPlayerPanel() //non-smooth update, why...
            }
            val playlistUri = getPlaylistArg(intent)
            if (playlistUri != null) {
                fragment.openImportPlaylistScreen(playlistUri)
            }
        }
    }

    override fun onBackPressed() {
        val fragment = supportFragmentManager.findFragmentById(R.id.main_activity_container)
        if (fragment is BackButtonListener && fragment.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    override fun onStop() {
        super.onStop()
        AndroidUtils.hideKeyboard(window.decorView)
    }

    private fun startScreens() {
        if (Permissions.hasFilePermission(this)) {
            goToMainScreen()
        } else {
            goToSetupScreen()
        }
    }

    private fun goToSetupScreen() {
        startFragment(SetupFragment())
    }

    private fun goToMainScreen() {
        val intent = intent
        val openPlayQueue = getOpenPlayerPanelArg(intent)
        var playlistUri: String? = null
        if (!AndroidUtils.isLaunchedFromHistory(this)) {
            playlistUri = getPlaylistArg(intent)
        }
        startFragment(PlayerFragment.newInstance(openPlayQueue, playlistUri))
    }

    private fun startFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val existFragment = fragmentManager.findFragmentById(R.id.main_activity_container)
        if (existFragment == null || existFragment.javaClass != fragment.javaClass) {
            fragmentManager.beginTransaction()
                .replace(R.id.main_activity_container, fragment)
                .commit()
        }
    }

    private fun getOpenPlayerPanelArg(intent: Intent): Boolean {
        val openPlayerPanel = intent.getBooleanExtra(Constants.Arguments.OPEN_PLAYER_PANEL_ARG, false)
        getIntent().removeExtra(Constants.Arguments.OPEN_PLAYER_PANEL_ARG)
        return openPlayerPanel
    }

    private fun getPlaylistArg(intent: Intent): String? {
        val type = intent.type
        return if ("audio/x-mpegurl" == type || "audio/mpegurl" == type) {
            intent.data.toString()
        } else {
            null
        }
    }

    class ErrorReportDialogFragment : DialogFragment() {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val loggerRepository = Components.getAppComponent().loggerRepository()
            val isCritical = loggerRepository.wasCriticalFatalError()

            val binding = DialogErrorReportBinding.inflate(layoutInflater)
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle(R.string.error_report)
                .setMessage(if (isCritical) R.string.critical_error_report_message else R.string.error_report_message)
                .setView(binding.root)
                .show()

            binding.cbShowReportDialogOnStart.visibility = if (isCritical) View.GONE else View.VISIBLE
            binding.cbShowReportDialogOnStart.isChecked = loggerRepository.isReportDialogOnStartEnabled()
            ViewUtils.onCheckChanged(binding.cbShowReportDialogOnStart, loggerRepository::showReportDialogOnStart)

            val fileLog = Components.getAppComponent().fileLog()
            binding.btnDelete.setOnClickListener {
                fileLog.deleteLogFile()
                dismissAllowingStateLoss()
                onReportDialogClosed()
            }
            val appLogger = Components.getAppComponent().appLogger()
            binding.btnView.setOnClickListener {
                appLogger.startViewLogScreen(requireActivity())
            }
            binding.btnSend.setOnClickListener {
                appLogger.startSendLogScreen(requireActivity())
                dismissAllowingStateLoss()
                onReportDialogClosed()
            }
            binding.btnClose.setOnClickListener {
                dismissAllowingStateLoss()
                onReportDialogClosed()
            }
            return dialog
        }

        override fun onCancel(dialog: DialogInterface) {
            super.onCancel(dialog)
            onReportDialogClosed()
        }

        private fun onReportDialogClosed() {
            val appComponent = Components.getAppComponent()
            val loggerRepository = appComponent.loggerRepository()
            val isCritical = loggerRepository.wasCriticalFatalError()
            loggerRepository.clearErrorFlags()
            if (isCritical) {
                (activity as MainActivity).startScreens()
                if (Permissions.hasFilePermission(requireContext())) {
                    appComponent.widgetUpdater().start()
                    appComponent.mediaScannerRepository().runStorageObserver()
                    appComponent.musicServiceInteractor().prepare()
                }
            }
        }

    }

}