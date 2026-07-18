package com.github.anrimian.musicplayer.ui.common.images

import android.app.Activity
import android.content.ComponentName
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import android.widget.RemoteViews
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.core.graphics.createBitmap
import com.bumptech.glide.integration.compose.GlideImage
import com.bumptech.glide.integration.compose.placeholder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.bitmap.DownsampleStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.data.models.composition.source.ExternalCompositionSource
import com.github.anrimian.musicplayer.domain.models.albums.Album
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.composition.FullComposition
import com.github.anrimian.musicplayer.domain.models.utils.isFileExists
import com.github.anrimian.musicplayer.domain.utils.functions.Opt
import com.github.anrimian.musicplayer.infrastructure.providers.CoversFileProvider
import com.github.anrimian.musicplayer.infrastructure.providers.ProviderAuthorities
import com.github.anrimian.musicplayer.ui.common.compose.coverOverlay
import com.github.anrimian.musicplayer.ui.common.images.glide.GlideApp
import com.github.anrimian.musicplayer.ui.common.images.glide.util.CustomAppWidgetTarget
import com.github.anrimian.musicplayer.ui.common.images.models.CompositionImage
import com.github.anrimian.musicplayer.ui.common.images.models.UriCompositionImage
import com.github.anrimian.musicplayer.ui.common.theme.ThemeController
import io.reactivex.rxjava3.core.Single
import java.io.File

private const val COVER_SIZE = 130
private const val TIMEOUT_MILLIS = 5000
private val DEFAULT_PLACEHOLDER = R.drawable.ic_music_placeholder_simple

@Composable
fun CoverImage(
    model: CompositionModel,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
) {
    var isLoaded by remember(model) { mutableStateOf(false) }

    Box(modifier = modifier) {
        GlideImage(
            model = model.toImageRequest(),
            contentDescription = model.title,
            loading = placeholder(DEFAULT_PLACEHOLDER),
            failure = placeholder(DEFAULT_PLACEHOLDER),
            contentScale = contentScale
        ) { requestBuilder ->
            requestBuilder.override(COVER_SIZE)
                .timeout(TIMEOUT_MILLIS)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable?>,
                        isFirstResource: Boolean,
                    ): Boolean {
                        isLoaded = false
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable,
                        model: Any,
                        target: Target<Drawable>?,
                        dataSource: DataSource,
                        isFirstResource: Boolean
                    ): Boolean {
                        isLoaded = true
                        return false
                    }
                })
        }

        if (isLoaded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.coverOverlay)
            )
        }
    }
}

private fun CompositionModel.toImageRequest(): CompositionImage {
    return CompositionImage(
        id,
        modifiedTime,
        coverModifyTime,
        size,
        isFileExists
    )
}


class CoverImageLoader(
    private val context: Context,
    private val themeController: ThemeController,
) {

    private var defaultNotificationBitmap: Bitmap? = null

    fun displayImage(
        imageView: ImageView,
        data: CompositionModel,
        listener: (Boolean) -> Unit,
    ) {
        if (!isValidContextForGlide(imageView)) {
            return
        }

        GlideApp.with(imageView)
            .asBitmap()
            .load(data.toImageRequest())
            .override(getCoverSize())
            .placeholder(DEFAULT_PLACEHOLDER)
            .error(DEFAULT_PLACEHOLDER)
            .timeout(TIMEOUT_MILLIS)
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>,
                    isFirstResource: Boolean,
                ): Boolean {
                    listener(false)
                    return false
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    listener(true)
                    return false
                }
            })
            .into(imageView)
    }

    fun clearImage(imageView: ImageView) {
        if (!isValidContextForGlide(imageView)) {
            return
        }
        GlideApp.with(imageView).clear(imageView)
    }

    fun displayImageInReusableTarget(
        imageView: ImageView,
        data: ExternalCompositionSource,
        @DrawableRes errorPlaceholder: Int,
    ) {
        displayImageInReusableTarget(
            imageView,
            UriCompositionImage(data.uri),
            null,
            errorPlaceholder
        )
    }

    fun displayImageInReusableTarget(
        imageView: ImageView,
        data: FullComposition,
        @DrawableRes errorPlaceholder: Int,
    ) {
        displayImageInReusableTarget(
            imageView,
            CompositionImage(
                data.id,
                data.modifiedTime,
                data.coverModifyTime,
                data.size,
                data.isFileExists()
            ),
            null,
            errorPlaceholder
        )
    }

    fun displayImageInReusableTarget(
        imageView: ImageView,
        data: Composition,
        oldData: Composition?,
        @DrawableRes errorPlaceholder: Int,
    ) {
        var oldComposition: CompositionImage? = null
        if (oldData != null) {
            oldComposition = oldData.toImageRequest()
        }
        displayImageInReusableTarget(
            imageView,
            data.toImageRequest(),
            oldComposition,
            errorPlaceholder
        )
    }

    fun displayImage(
        imageView: ImageView,
        album: Album,
        @DrawableRes errorPlaceholder: Int,
    ) {
        if (!isValidContextForGlide(imageView)) {
            return
        }

        GlideApp.with(imageView)
            .asBitmap()
            .load(album)
            .override(getCoverSize())
            .placeholder(errorPlaceholder)
            .error(errorPlaceholder)
            .timeout(TIMEOUT_MILLIS)
            .into(imageView)
    }

    fun loadNotificationImage(
        data: Composition,
        onCompleted: (Bitmap?) -> Unit,
    ): Runnable {
        return loadNotificationImage(data.toImageRequest(), onCompleted)
    }

    fun loadNotificationImage(
        source: ExternalCompositionSource,
        onCompleted: (Bitmap?) -> Unit,
    ): Runnable {
        return loadNotificationImage(UriCompositionImage(source.uri), onCompleted)
    }

    fun getDefaultNotificationBitmap(): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (defaultNotificationBitmap == null) {
                defaultNotificationBitmap = createBitmap(10, 10, Bitmap.Config.RGB_565)
            }
            val color = themeController.getPrimaryThemeColor()
            defaultNotificationBitmap!!.eraseColor(color)
        } else {
            if (defaultNotificationBitmap == null) {
                val opt = BitmapFactory.Options()
                opt.inPreferredConfig = Bitmap.Config.RGB_565
                defaultNotificationBitmap = BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.ic_notification_icon,
                    opt
                )
            }
        }
        return defaultNotificationBitmap
    }

    fun loadImage(data: ExternalCompositionSource, onCompleted: (Bitmap?) -> Unit): Runnable {
        return loadImageCancellable(UriCompositionImage(data.uri), onCompleted)
    }

    fun loadImage(data: Composition, onCompleted: (Bitmap?) -> Unit): Runnable {
        return loadImageCancellable(data.toImageRequest(), onCompleted)
    }

    fun loadMediaSessionImage(data: Composition, onCompleted: (Bitmap?) -> Unit): Runnable {
        val target = simpleTarget(onCompleted)
        GlideApp.with(context)
            .asBitmap()
            .load(data.toImageRequest())
            .override(getCoverMediaSessionSize())
            .timeout(TIMEOUT_MILLIS)
            .into(target)
        return Runnable { GlideApp.with(context).clear(target) }
    }

    fun loadMediaSessionImage(
        data: ExternalCompositionSource,
        onCompleted: (Bitmap?) -> Unit,
    ): Runnable {
        val target = simpleTarget(onCompleted)
        GlideApp.with(context)
            .asBitmap()
            .load(UriCompositionImage(data.uri))
            .override(getCoverMediaSessionSize())
            .timeout(TIMEOUT_MILLIS)
            .into(target)
        return Runnable { GlideApp.with(context).clear(target) }
    }

    fun loadImageUri(data: Composition): Single<Opt<Uri>> {
        return Single.create { emitter ->
            loadImageUri(data) { uri -> emitter.onSuccess(Opt(uri)) }
        }
    }

    fun loadImageUri(data: Composition, onCompleted: (Uri?) -> Unit): Runnable {
        val imageData = data.toImageRequest()
        return loadImageUriInternal(imageData, onCompleted)
    }

    fun loadImageUri(data: ExternalCompositionSource, onCompleted: (Uri?) -> Unit): Runnable {
        val imageData = UriCompositionImage(data.uri)
        return loadImageUriInternal(imageData, onCompleted)
    }

    fun displayImage(
        widgetView: RemoteViews,
        @IdRes viewId: Int,
        componentName: ComponentName?,
        compositionId: Long,
        compositionUpdateTime: Long,
        lastCoverModifyTime: Long,
        compositionSize: Long,
        isFileExists: Boolean,
        isRoundCoverEnabled: Boolean,
        @DrawableRes placeholder: Int,
    ) {
        val widgetTarget = CustomAppWidgetTarget(
            context,
            viewId,
            widgetView,
            placeholder,
            componentName
        )

        val transformation = if (isRoundCoverEnabled) CircleCrop() else RoundedCorners(500)

        GlideApp.with(context)
            .asBitmap()
            .load(
                CompositionImage(
                    compositionId,
                    compositionUpdateTime,
                    lastCoverModifyTime,
                    compositionSize,
                    isFileExists
                )
            ).override(getCoverWidgetSize())
            .downsample(DownsampleStrategy.AT_LEAST)
            .transform(transformation)
            .timeout(TIMEOUT_MILLIS)
            .into(widgetTarget)
    }

    private fun displayImageInReusableTarget(
        imageView: ImageView,
        data: Any,
        oldData: Any?,
        @DrawableRes errorPlaceholder: Int,
    ) {
        if (!isValidContextForGlide(imageView)) {
            return
        }

        //here replacement with error placeholder flickers, don't know how to solve it
        GlideApp.with(imageView)
            .asBitmap()
            .load(data)
            .override(getCoverSize())
            .thumbnail(
                GlideApp.with(imageView)
                    .asBitmap()
                    .load(oldData)
                    .override(getCoverSize())
                    .timeout(TIMEOUT_MILLIS)
            )
            .listener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Bitmap>,
                    isFirstResource: Boolean,
                ): Boolean {
                    imageView.setImageResource(errorPlaceholder)
                    return true
                }

                override fun onResourceReady(
                    resource: Bitmap,
                    model: Any,
                    target: Target<Bitmap>?,
                    dataSource: DataSource,
                    isFirstResource: Boolean,
                ): Boolean {
                    return false
                }
            })
            .error(errorPlaceholder)
            .timeout(TIMEOUT_MILLIS)
            .into(imageView)
    }

    private fun loadImageUriInternal(
        imageData: Any,
        onCompleted: (Uri?) -> Unit,
    ): Runnable {
        val outerTarget = simpleTarget { file: File? ->
            if (file == null) {
                onCompleted(null)
                return@simpleTarget
            }
            val uri = Uri.Builder()
                .scheme(ContentResolver.SCHEME_CONTENT)
                .authority(ProviderAuthorities.of(context, CoversFileProvider::class.java))
                .path(file.path)
                .build()
            onCompleted(uri)
        }
        val innerTarget = simpleTarget<Bitmap> { _ ->
            GlideApp.with(context)
                .download(imageData)
                .onlyRetrieveFromCache(true)
                .timeout(TIMEOUT_MILLIS)
                .into(outerTarget)
        }
        GlideApp.with(context)
            .asBitmap()
            .load(imageData)
            .override(getCoverSize())
            .timeout(TIMEOUT_MILLIS)
            .into(innerTarget)
        return Runnable {
            GlideApp.with(context).clear(innerTarget)
            GlideApp.with(context).clear(outerTarget)
        }
    }

    private fun loadImageCancellable(data: Any, onCompleted: (Bitmap?) -> Unit): Runnable {
        val target = simpleTarget(onCompleted)
        GlideApp.with(context)
            .asBitmap()
            .load(data)
            .override(getCoverSize())
            .timeout(TIMEOUT_MILLIS)
            .into(target)
        return Runnable { GlideApp.with(context).clear(target) }
    }

    private fun loadNotificationImage(
        compositionImage: Any?,
        onCompleted: (Bitmap?) -> Unit,
    ): Runnable {
        val target = simpleTarget { bitmap: Bitmap? ->
            var resultBitmap = bitmap
            if (resultBitmap != null) {
                //possible fix for RemoteServiceException crash
                //https://stackoverflow.com/questions/54948936/bad-notification-the-given-region-must-intersect-with-the-bitmaps-dimensions
                resultBitmap = resultBitmap.copy(Bitmap.Config.RGB_565, false)
            }
            onCompleted(resultBitmap)
        }

        GlideApp.with(context)
            .asBitmap()
            .load(compositionImage)
            .override(getCoverMediaSessionSize())
            .timeout(NOTIFICATION_IMAGE_TIMEOUT_MILLIS)
            .into(target)

        return Runnable { GlideApp.with(context).clear(target) }
    }

    private fun loadImage(data: Any, onCompleted: (Bitmap?) -> Unit) {
        GlideApp.with(context)
            .asBitmap()
            .load(data)
            .override(getCoverSize())
            .timeout(TIMEOUT_MILLIS)
            .into(simpleTarget(onCompleted))
    }

    private fun <T : Any> simpleTarget(callback: (T?) -> Unit): CustomTarget<T> {
        return object : CustomTarget<T>() {
            override fun onResourceReady(resource: T, transition: Transition<in T>?) {
                callback(resource)
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                callback(null)
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                callback(null)
            }
        }
    }

    private fun getCoverSize(): Int = context.resources.getInteger(R.integer.icon_image_size)

    private fun getCoverWidgetSize(): Int = context.resources.getInteger(R.integer.icon_widget_image_size)

    private fun getCoverMediaSessionSize(): Int = context.resources.getInteger(R.integer.icon_media_session_image_size)

    companion object {
        private const val NOTIFICATION_IMAGE_TIMEOUT_MILLIS = 500

        private fun isValidContextForGlide(imageView: ImageView): Boolean {
            return isValidContextForGlide(imageView.context)
        }

        private fun isValidContextForGlide(context: Context?): Boolean {
            if (context == null) {
                return false
            }
            if (context is Activity) {
                return !context.isDestroyed && !context.isFinishing
            }
            return true
        }
    }
}
