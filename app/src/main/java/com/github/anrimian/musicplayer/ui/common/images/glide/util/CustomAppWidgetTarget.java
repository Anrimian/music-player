package com.github.anrimian.musicplayer.ui.common.images.glide.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.util.Preconditions;

/**
 * This class is used in order to display downloaded Bitmap inside an ImageView of an AppWidget
 * through RemoteViews.
 *
 * <p>Note - For cancellation to work correctly, you must pass in the same instance of this class
 * for every subsequent load.
 */
// Public API.
@SuppressWarnings("WeakerAccess")
public class CustomAppWidgetTarget extends CustomTarget<Bitmap> {
  private final int[] widgetIds;
  private final ComponentName componentName;
  private final RemoteViews remoteViews;
  private final Context context;
  private final int viewId;

  private final int placeholder;

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
  public CustomAppWidgetTarget(
      Context context,
      int width,
      int height,
      int viewId,
      RemoteViews remoteViews,
      int placeholder,
      ComponentName componentName) {
    super(width, height);
    this.context = Preconditions.checkNotNull(context, "Context can not be null!");
    this.remoteViews =
        Preconditions.checkNotNull(remoteViews, "RemoteViews object can not be null!");
    this.viewId = viewId;
    this.placeholder = placeholder;
    this.componentName = componentName;
    widgetIds = null;
  }

    public CustomAppWidgetTarget(
            Context context,
            int width,
            int height,
            int viewId,
            RemoteViews remoteViews,
            int placeholder,
            int... widgetIds) {
        super(width, height);
        if (widgetIds.length == 0) {
            throw new IllegalArgumentException("WidgetIds must have length > 0");
        }
        this.context = Preconditions.checkNotNull(context, "Context can not be null!");
        this.remoteViews =
                Preconditions.checkNotNull(remoteViews, "RemoteViews object can not be null!");
        this.widgetIds = Preconditions.checkNotNull(widgetIds, "WidgetIds can not be null!");
        this.viewId = viewId;
        this.placeholder = placeholder;
        componentName = null;
    }

  /**
   * Constructor using an int array of widgetIds to get a handle on the Widget in order to update it
   * that uses {@link #SIZE_ORIGINAL} as the target width and height.
   *  @param context Context to use in the AppWidgetManager initialization.
   * @param viewId The id of the ImageView view that will load the image.
   * @param remoteViews RemoteViews object which contains the ImageView that will load the bitmap.
   * @param widgetIds The int[] that contains the widget ids of an application.
   */
  public CustomAppWidgetTarget(Context context,
                               int viewId,
                               RemoteViews remoteViews,
                               int placeholder,
                               int... widgetIds) {
    this(context, SIZE_ORIGINAL, SIZE_ORIGINAL, viewId, remoteViews, placeholder, widgetIds);
  }

    public CustomAppWidgetTarget(Context context,
                                 int viewId,
                                 RemoteViews remoteViews,
                                 int placeholder,
                                 ComponentName componentName) {
        this(context, SIZE_ORIGINAL, SIZE_ORIGINAL, viewId, remoteViews, placeholder, componentName);
    }

  /** Updates the AppWidget after the ImageView has loaded the Bitmap. */
  private void update() {
    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.context);
    try {
        if (this.componentName != null) {
            appWidgetManager.updateAppWidget(this.componentName, this.remoteViews);
        } else {
            appWidgetManager.updateAppWidget(this.widgetIds, this.remoteViews);
        }
    } catch (Exception ignored) {}
  }

  @Override
  public void onResourceReady(
      @NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
    setBitmap(resource);
  }

  @Override
  public void onLoadCleared(@Nullable Drawable placeholder) {
    this.remoteViews.setImageViewResource(viewId, this.placeholder);
    update();
  }

  @Override
  public void onLoadFailed(@Nullable Drawable errorDrawable) {
    this.remoteViews.setImageViewResource(viewId, placeholder);
    update();
  }

  private void setBitmap(@Nullable Bitmap bitmap) {
    this.remoteViews.setImageViewBitmap(viewId, bitmap);
    update();
  }
}