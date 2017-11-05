package com.github.anrimian.simplemusicplayer.utils.services;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

/**
 * Created on 05.11.2017.
 */

public class BoundServiceWrapper<T extends IBinder> {
    private Context context;
    private Class serviceClass;
    private int serviceFlags;

    private T binder;
    private Action<T> deferAction;

    public BoundServiceWrapper(Context context, Class serviceClass, int serviceFlags) {
        this.context = context;
        this.serviceClass = serviceClass;
        this.serviceFlags = serviceFlags;
    }

    public void call(Action<T> action) {
        if (binder != null) {
            action.call(binder);
        } else {
            deferAction = action;
            Intent intent = new Intent(context, serviceClass);
            context.bindService(intent, serviceConnection, serviceFlags);
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            //noinspection unchecked
            binder = (T) iBinder;
            if (deferAction != null) {
                deferAction.call(binder);
                deferAction = null;
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            binder = null;
        }
    };

    public interface Action<T extends IBinder> {
        void call(T binder);
    }
}