package com.github.anrimian.musicplayer.ui.settings.headset

import android.Manifest
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.databinding.FragmentSettingsHeadsetBinding
import com.github.anrimian.musicplayer.di.Components
import com.github.anrimian.musicplayer.infrastructure.receivers.BluetoothConnectionReceiver
import com.github.anrimian.musicplayer.ui.common.dialogs.showNumberPickerDialog
import com.github.anrimian.musicplayer.ui.common.snackbars.AppSnackbar
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar
import com.github.anrimian.musicplayer.ui.utils.PermissionRequester
import com.github.anrimian.musicplayer.ui.utils.ViewUtils
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel
import com.github.anrimian.musicplayer.ui.utils.startAppSettings
import com.google.android.material.snackbar.Snackbar
import moxy.MvpAppCompatFragment
import moxy.ktx.moxyPresenter

class HeadsetSettingsFragment : MvpAppCompatFragment(), HeadsetSettingsView {

    private val presenter by moxyPresenter {
        Components.getSettingsComponent().headsetSettingsPresenter()
    }

    private lateinit var binding: FragmentSettingsHeadsetBinding

    private val permissionRequester = PermissionRequester(this, ::onBluetoothConnectPermissionResult)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingsHeadsetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar = requireActivity().findViewById<AdvancedToolbar>(R.id.toolbar)
        toolbar.setTitle(R.string.settings)
        toolbar.setSubtitle(R.string.headset)
        toolbar.setTitleClickListener(null)

        SlidrPanel.simpleSwipeBack(binding.clContainer, this, toolbar::onStackFragmentSlided)

        ViewUtils.onCheckChanged(binding.cbPlayOnConnect, ::onPlayOnConnectChecked)
        binding.ivPlayOnConnectDelay.setOnClickListener { presenter.onPickPlayDelayClicked() }

        ViewUtils.setChecked(binding.cbPlayOnConnect, BluetoothConnectionReceiver.isEnabled(requireContext()))
    }

    override fun showConnectAutoPlayDelay(millis: Long) {
        binding.tvPlayOnConnectDelay.text = getString(R.string.with_delay, millis/1000f)
    }

    override fun showPlayDelayPickerDialog(currentValue: Long) {
        showNumberPickerDialog(
            requireContext(),
            0,
            5000,
            currentValue,
            100,
            { value -> (value/1000f).toString() },
            presenter::onConnectAutoPlayDelaySelected
        )
    }

    private fun onPlayOnConnectChecked(checked: Boolean) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && checked) {
            permissionRequester.request(Manifest.permission.BLUETOOTH_CONNECT)
            return
        }
        BluetoothConnectionReceiver.setEnabled(requireContext(), checked)
    }

    @TargetApi(Build.VERSION_CODES.S)
    private fun onBluetoothConnectPermissionResult(granted: Boolean) {
        BluetoothConnectionReceiver.setEnabled(requireContext(), granted)
        ViewUtils.setChecked(binding.cbPlayOnConnect, granted)
        if (!granted) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.BLUETOOTH_CONNECT)) {
                AppSnackbar.make(binding.clContainer, getString(R.string.permission_required))
                    .setAction(R.string.open_app_settings) { startAppSettings(requireActivity()) }
                    .duration(Snackbar.LENGTH_INDEFINITE)
                    .show()
            }
        }
    }
}