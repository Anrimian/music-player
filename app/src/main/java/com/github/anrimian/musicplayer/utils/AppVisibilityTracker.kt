package com.github.anrimian.musicplayer.utils

import android.app.Activity
import android.app.Application
import android.os.Bundle
import java.util.concurrent.atomic.AtomicInteger

class AppVisibilityTracker(
    private val onAppVisible: (Boolean) -> Unit
) : Application.ActivityLifecycleCallbacks {

    private val runningActivities = AtomicInteger(0)

    override fun onActivityStarted(activity: Activity) {
        if (runningActivities.incrementAndGet() == 1) {
            onAppVisible(true)
        }
    }

    override fun onActivityStopped(activity: Activity) {
        if (runningActivities.get() > 0) {
            if (runningActivities.decrementAndGet() == 0) {
                onAppVisible(false)
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityResumed(activity: Activity) {}
    override fun onActivityPaused(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}

}