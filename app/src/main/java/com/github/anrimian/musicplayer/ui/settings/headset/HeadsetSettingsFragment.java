package com.github.anrimian.musicplayer.ui.settings.headset;

import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.onCheckChanged;
import static com.github.anrimian.musicplayer.ui.utils.ViewUtils.setChecked;

import android.Manifest;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.github.anrimian.musicplayer.R;
import com.github.anrimian.musicplayer.databinding.FragmentSettingsHeadsetBinding;
import com.github.anrimian.musicplayer.infrastructure.receivers.BluetoothConnectionReceiver;
import com.github.anrimian.musicplayer.ui.common.snackbars.AppSnackbar;
import com.github.anrimian.musicplayer.ui.common.toolbar.AdvancedToolbar;
import com.github.anrimian.musicplayer.ui.utils.AndroidUtilsKt;
import com.github.anrimian.musicplayer.ui.utils.PermissionRequester;
import com.github.anrimian.musicplayer.ui.utils.slidr.SlidrPanel;
import com.google.android.material.snackbar.Snackbar;

public class HeadsetSettingsFragment extends Fragment {

    private FragmentSettingsHeadsetBinding viewBinding;

    private final PermissionRequester permissionRequester = new PermissionRequester(this,
            this::onBluetoothConnectPermissionResult);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        viewBinding = FragmentSettingsHeadsetBinding.inflate(inflater, container, false);
        return viewBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AdvancedToolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.settings);
        toolbar.setSubtitle(R.string.headset);
        toolbar.setTitleClickListener(null);

        SlidrPanel.simpleSwipeBack(viewBinding.clContainer, this, toolbar::onStackFragmentSlided);

        onCheckChanged(viewBinding.cbPlayOnConnect, this::onPlayOnConnectChecked);

        setChecked(viewBinding.cbPlayOnConnect, BluetoothConnectionReceiver.isEnabled(requireContext()));
    }

    private void onPlayOnConnectChecked(boolean checked) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && checked) {
            permissionRequester.request(Manifest.permission.BLUETOOTH_CONNECT);
            return;
        }
        BluetoothConnectionReceiver.setEnabled(requireContext(), checked);
    }

    @TargetApi(Build.VERSION_CODES.S)
    private void onBluetoothConnectPermissionResult(boolean granted) {
        BluetoothConnectionReceiver.setEnabled(requireContext(), granted);
        setChecked(viewBinding.cbPlayOnConnect, granted);
        if (!granted) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.BLUETOOTH_CONNECT)) {
                AppSnackbar.make(viewBinding.clContainer, "Bluetooth connect permission required")
                        .setAction("Open app settings", () -> AndroidUtilsKt.startAppSettings(requireActivity()))
                        .duration(Snackbar.LENGTH_INDEFINITE)
                        .show();
            }
        }
    }
}
