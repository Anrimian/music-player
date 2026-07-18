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
import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.github.anrimian.musicplayer.di.Components;
import com.github.anrimian.musicplayer.domain.repositories.SettingsRepository;
import com.github.anrimian.musicplayer.infrastructure.service.SystemServiceControllerImpl;

import java.util.List;

public class BluetoothConnectionReceiver extends BroadcastReceiver {

    private static final List<Integer> ALLOWED_DEVICES_TO_START = asList(
            AUDIO_VIDEO_UNCATEGORIZED,
            AUDIO_VIDEO_WEARABLE_HEADSET,
            AUDIO_VIDEO_HANDSFREE,
            AUDIO_VIDEO_HEADPHONES,
            AUDIO_VIDEO_PORTABLE_AUDIO);

    public static final long PLAY_EVENT_LOCK_WINDOW_MILLIS = 3000L;
    public static volatile long lastBluetoothConnectionTime = 0L;

    public static boolean shouldIgnorePlayEvent() {
        return System.currentTimeMillis()
                - BluetoothConnectionReceiver.lastBluetoothConnectionTime < BluetoothConnectionReceiver.PLAY_EVENT_LOCK_WINDOW_MILLIS;
    }

    public static void setEnabled(Context context, boolean enabled) {
        context.getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, BluetoothConnectionReceiver.class),
                enabled ? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED,
                DONT_KILL_APP);
    }

    public static boolean isEnabled(Context context) {
        return context.getPackageManager()
                .getComponentEnabledSetting(
                        new ComponentName(context,
                                BluetoothConnectionReceiver.class)) == COMPONENT_ENABLED_STATE_ENABLED;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action) ||
                BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action) ||
                BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                lastBluetoothConnectionTime = System.currentTimeMillis();
            }

            if (BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED.equals(action) ||
                    BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED);
                if (state != BluetoothProfile.STATE_CONNECTED) {
                    return;
                }
            }

            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null) {
                if (ActivityCompat.checkSelfPermission(context,
                        Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                BluetoothClass bluetoothClass = device.getBluetoothClass();
                if (bluetoothClass == null) {
                    return;
                }
                int deviceClass = bluetoothClass.getDeviceClass();
                // add setting to start without check?
                if (!ALLOWED_DEVICES_TO_START.contains(deviceClass)) {
                    return;
                }
            }

            SettingsRepository settingsRepository = Components.INSTANCE.getAppComponent().settingsRepository();
            if (settingsRepository.isBluetoothAutoPlayEnabled()) {
                long delay = settingsRepository.getBluetoothConnectAutoPlayDelay();
                SystemServiceControllerImpl.startPlayForegroundService(context, delay);
            }
        }
    }
}
