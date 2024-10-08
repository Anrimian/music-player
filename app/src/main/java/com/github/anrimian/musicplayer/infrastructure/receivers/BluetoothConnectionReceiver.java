package com.github.anrimian.musicplayer.infrastructure.receivers;

import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HANDSFREE;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_PORTABLE_AUDIO;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_UNCATEGORIZED;
import static android.bluetooth.BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;
import static com.github.anrimian.musicplayer.domain.utils.ListUtils.asList;

import android.Manifest;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.infrastructure.service.SystemServiceControllerImpl;

import java.util.List;
import java.util.Objects;

public class BluetoothConnectionReceiver extends BroadcastReceiver {

    private static final List<Integer> ALLOWED_DEVICES_TO_START = asList(
            AUDIO_VIDEO_UNCATEGORIZED,
            AUDIO_VIDEO_WEARABLE_HEADSET,
            AUDIO_VIDEO_HANDSFREE,
            AUDIO_VIDEO_HEADPHONES,
            AUDIO_VIDEO_PORTABLE_AUDIO
    );

    public static void setEnabled(Context context, boolean enabled) {
        context.getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, BluetoothConnectionReceiver.class),
                enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED,
                DONT_KILL_APP
        );
    }

    public static boolean isEnabled(Context context) {
        return context.getPackageManager()
                .getComponentEnabledSetting(
                        new ComponentName(context, BluetoothConnectionReceiver.class)
                ) == COMPONENT_ENABLED_STATE_ENABLED;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Objects.equals(intent.getAction(), BluetoothDevice.ACTION_ACL_CONNECTED)) {
            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                BluetoothClass bluetoothClass = device.getBluetoothClass();
                if (bluetoothClass == null) {
                    return;
                }
                int deviceClass = bluetoothClass.getDeviceClass();
                //add setting to start without check?
                if (!ALLOWED_DEVICES_TO_START.contains(deviceClass)) {
                    return;
                }
            }
            long delay = Components.getAppComponent()
                    .settingsRepository()
                    .getBluetoothConnectAutoPlayDelay();
            SystemServiceControllerImpl.startPlayForegroundService(context, delay);
        }
    }
}
