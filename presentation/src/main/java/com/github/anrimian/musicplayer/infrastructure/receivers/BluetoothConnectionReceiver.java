package com.github.anrimian.musicplayer.infrastructure.receivers;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.github.anrimian.musicplayer.di.Components;

import java.util.Objects;

import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
import static android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
import static android.content.pm.PackageManager.DONT_KILL_APP;

public class BluetoothConnectionReceiver extends BroadcastReceiver {

    public static void setEnabled(Context context, boolean enabled) {
        context.getPackageManager().setComponentEnabledSetting(
                new ComponentName(context, BluetoothConnectionReceiver.class),
                enabled? COMPONENT_ENABLED_STATE_ENABLED : COMPONENT_ENABLED_STATE_DISABLED,
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
            Components.getAppComponent().musicPlayerInteractor().play();
        }
    }
}
