package com.github.anrimian.musicplayer.ui.main.setup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.utils.Permissions
import com.github.anrimian.musicplayer.databinding.FragmentStartBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.ui.player_screen.PlayerFragment
import com.github.anrimian.musicplayer.ui.utils.PermissionRequester
import com.github.anrimian.musicplayer.ui.utils.applyBottomInsets
import com.github.anrimian.musicplayer.ui.utils.applyTopInsets
import com.github.anrimian.musicplayer.ui.utils.startAppSettings
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

/**
 * Created on 19.10.2017.
 */
class SetupFragment : MvpAppCompatFragment(), SetupView {
    
    private val presenter by moxyPresenter { SetupPresenter() }
    
    private lateinit var binding: FragmentStartBinding

    private val permissionRequester = PermissionRequester(this, this::onFilesPermissionResult)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.statusBarSpace.applyTopInsets()
        binding.root.applyBottomInsets()

        binding.progressStateView.onTryAgainClick { onTryAgainButtonClicked() }
    }

    override fun onResume() {
        super.onResume()
        if (Permissions.hasFilePermission(requireContext())) {
            presenter.onFilesPermissionResult(true)
        }
    }

    override fun requestFilesPermissions() {
        permissionRequester.request(Permissions.getFilePermissionName())
    }

    override fun showDeniedPermissionMessage() {
        binding.progressStateView.showMessage(R.string.can_not_work_without_file_permission, true)
    }

    override fun showStub() {
        binding.progressStateView.hideAll()
    }

    override fun startSystemServices() {
        val appComponent = Components.getAppComponent()
        appComponent.widgetUpdater().start()
        appComponent.wearableManager().init()
        appComponent.notificationsDisplayer().removeErrorNotification()
        appComponent.storageScannerInteractor().runStorageObserver()
        appComponent.musicServiceInteractor().prepare()
    }

    override fun goToMainScreen() {
        parentFragmentManager
            .beginTransaction()
            .setCustomAnimations(R.anim.anim_alpha_appear, R.anim.anim_alpha_disappear)
            .replace(R.id.main_activity_container, PlayerFragment.newInstance(false))
            .commit()
    }

    private fun onFilesPermissionResult(granted: Boolean) {
        presenter.onFilesPermissionResult(granted)
    }

    private fun onTryAgainButtonClicked() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Permissions.getFilePermissionName())) {
            startAppSettings(requireActivity())
            return
        }
        presenter.onTryAgainButtonClicked()
    }
}