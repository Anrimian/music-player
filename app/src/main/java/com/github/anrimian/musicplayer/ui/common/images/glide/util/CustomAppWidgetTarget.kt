package com.github.anrimian.musicplayer.ui.common.images.glide.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

/**
 * This class is used in order to display downloaded Bitmap inside an ImageView of an AppWidget
 * through RemoteViews.
 *
 * <p>Note - For cancellation to work correctly, you must pass in the same instance of this class
 * for every subsequent load.
 */

/**
 * Constructor using an int array of widgetIds to get a handle on the Widget in order to update
 * it.
 *
 * @param context Context to use in the AppWidgetManager initialization.
 * @param width Desired width in pixels of the bitmap that will be loaded. (Needs to be manually
 *     put because of RemoteViews limitations.)
 * @param height Desired height in pixels of the bitmap that will be loaded. (Needs to be manually
 *     put because of RemoteViews limitations.)
 * @param viewId The id of the ImageView view that will load the image.
 * @param remoteViews RemoteViews object which contains the ImageView that will load the bitmap.
 */
class CustomAppWidgetTarget(
    private val context: Context,
    width: Int,
    height: Int,
    @param:IdRes private val viewId: Int,
    private val remoteViews: RemoteViews,
    @param:DrawableRes private val placeholder: Int,
    private val widgetIds: IntArray?,
    private val componentName: ComponentName?
) : CustomTarget<Bitmap>(width, height) {

    /**
     * Constructor using an int array of widgetIds to get a handle on the Widget in order to update it
     * that uses {@link #SIZE_ORIGINAL} as the target width and height.
     *  @param context Context to use in the AppWidgetManager initialization.
     * @param viewId The id of the ImageView view that will load the image.
     * @param remoteViews RemoteViews object which contains the ImageView that will load the bitmap.
     * @param widgetIds The int[] that contains the widget ids of an application.
     */
    constructor(
        context: Context,
        @IdRes viewId: Int,
        remoteViews: RemoteViews,
        @DrawableRes placeholder: Int,
        vararg widgetIds: Int
    ) : this(
        context,
        SIZE_ORIGINAL,
        SIZE_ORIGINAL,
        viewId,
        remoteViews,
        placeholder,
        widgetIds,
        null
    ) {
        require(widgetIds.isNotEmpty()) { "WidgetIds must have length > 0" }
    }

    constructor(
        context: Context,
        @IdRes viewId: Int,
        remoteViews: RemoteViews,
        @DrawableRes placeholder: Int,
        componentName: ComponentName?
    ) : this(
        context,
        SIZE_ORIGINAL,
        SIZE_ORIGINAL,
        viewId,
        remoteViews,
        placeholder,
        null,
        componentName
    )

    /** Updates the AppWidget after the ImageView has loaded the Bitmap. */
    private fun update() {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        try {
            if (componentName != null) {
                appWidgetManager.updateAppWidget(componentName, remoteViews)
            } else {
                appWidgetManager.updateAppWidget(widgetIds, remoteViews)
            }
        } catch (_: Exception) {}
    }

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
        setBitmap(resource)
    }

    override fun onLoadCleared(placeholder: Drawable?) {
        setPlaceholder()
    }

    override fun onLoadFailed(errorDrawable: Drawable?) {
        setPlaceholder()
    }

    private fun setPlaceholder() {
        remoteViews.setImageViewResource(viewId, placeholder)
        update()
    }

    private fun setBitmap(bitmap: Bitmap) {
        remoteViews.setImageViewBitmap(viewId, bitmap)
        update()
    }
}