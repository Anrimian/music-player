package com.github.anrimian.musicplayer.wear.infrastructure.remote.tiles

import androidx.annotation.DrawableRes
import androidx.wear.protolayout.ActionBuilders.AndroidActivity
import androidx.wear.protolayout.ActionBuilders.LaunchAction
import androidx.wear.protolayout.ActionBuilders.LoadAction
import androidx.wear.protolayout.ColorBuilders
import androidx.wear.protolayout.DimensionBuilders
import androidx.wear.protolayout.LayoutElementBuilders.Box
import androidx.wear.protolayout.LayoutElementBuilders.ColorFilter
import androidx.wear.protolayout.LayoutElementBuilders.Column
import androidx.wear.protolayout.LayoutElementBuilders.FONT_WEIGHT_BOLD
import androidx.wear.protolayout.LayoutElementBuilders.FontStyle
import androidx.wear.protolayout.LayoutElementBuilders.Image
import androidx.wear.protolayout.LayoutElementBuilders.Row
import androidx.wear.protolayout.LayoutElementBuilders.Text
import androidx.wear.protolayout.ModifiersBuilders
import androidx.wear.protolayout.ResourceBuilders
import androidx.wear.protolayout.TimelineBuilders
import androidx.wear.tiles.RequestBuilders
import androidx.wear.tiles.TileBuilders
import androidx.wear.tiles.TileService
import com.github.anrimian.musicplayer.wear.R
import com.github.anrimian.musicplayer.wear.di.Components
import com.github.anrimian.musicplayer.wear.ui.MainActivity
import com.github.anrimian.musicplayer.wear.ui.common.FormatUtils
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

class AppTileService: TileService() {

    private companion object {
        const val RESOURCES_VERSION = "1"

        const val REFRESH = "refresh"
        const val SKIP_TO_NEXT = "skip_to_next"
        const val SKIP_TO_PREVIOUS = "skip_to_previous"
        const val PLAY_PAUSE = "play_pause"
        const val VOLUME_DOWN = "volume_down"
        const val VOLUME_UP = "volume_up"
        const val OPEN_APP = "open_app"

        const val IMG_REFRESH = "img_refresh"
        const val IMG_SKIP_PREVIOUS = "img_skip_previous"
        const val IMG_SKIP_NEXT = "img_skip_next"
        const val IMG_PAUSE = "img_pause"
        const val IMG_PLAY = "img_play"
        const val IMG_VOLUME = "img_volume"
        const val IMG_MINUS = "img_minus"
        const val IMG_PLUS = "img_plus"
    }

    private val wearStateInteractor = Components.getAppComponent().wearStateInteractor()

    override fun onTileRequest(
        requestParams: RequestBuilders.TileRequest,
    ): ListenableFuture<TileBuilders.Tile> {
        processAction(requestParams)

        val currentComposition = wearStateInteractor.getCurrentComposition()
        val volume = wearStateInteractor.getVolumeState()
        val volumePercent = 100 * volume.getVolume() / volume.getMaxVolume()

        val layout = Column.Builder()
            .addContent(imageButton(IMG_REFRESH, REFRESH))
            .addContent(
                Column.Builder()
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setClickable(
                                ModifiersBuilders.Clickable.Builder()
                                    .setId(OPEN_APP)
                                    .setOnClick(LaunchAction.Builder()
                                        .setAndroidActivity(
                                            AndroidActivity.Builder()
                                                .setPackageName(packageName)
                                                .setClassName(MainActivity::class.qualifiedName!!)
                                                .build()
                                        )
                                        .build())
                                    .build()
                            )
                            .build()
                    )
                    .addContent(
                        Text.Builder()
                            .setFontStyle(
                                FontStyle.Builder()
                                    .setWeight(FONT_WEIGHT_BOLD)
                                    .setSize(DimensionBuilders.sp(13f))
                                    .build()
                            )
                            .setText(FormatUtils.formatCompositionTitle(this, currentComposition))
                            .build()
                    )
                    .addContent(
                        Text.Builder()
                            .setFontStyle(
                                FontStyle.Builder()
                                    .setSize(DimensionBuilders.sp(11f))
                                    .build()
                            )
                            .setText(FormatUtils.formatCompositionArtist(this, currentComposition))
                            .build()
                    )
                    .build()
            )
            .addContent(
                Row.Builder()
                    .addContent(imageButton(IMG_SKIP_PREVIOUS, SKIP_TO_PREVIOUS))
                    .addContent(
                        imageButton(
                            if (wearStateInteractor.isPlaying()) IMG_PAUSE else IMG_PLAY,
                            PLAY_PAUSE
                        )
                    )
                    .addContent(imageButton(IMG_SKIP_NEXT, SKIP_TO_NEXT))
                    .build()
            )
            .addContent(
                Box.Builder()
                    .setModifiers(
                        ModifiersBuilders.Modifiers.Builder()
                            .setBackground(
                                ModifiersBuilders.Background.Builder()
                                    .setColor(ColorBuilders.argb(getColor(R.color.color_accent)))
                                    .setCorner(
                                        ModifiersBuilders.Corner.Builder()
                                            .setRadius(DimensionBuilders.dp(24f))
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )

                    .addContent(
                        Row.Builder()
                            .addContent(image(IMG_MINUS))
                            .addContent(
                                Column.Builder()
                                    .addContent(image(IMG_VOLUME))
                                    .addContent(
                                        Text.Builder()
                                            .setText("$volumePercent%")//getString(R.string.percentage_template, volumePercent)
                                            .setFontStyle(
                                                FontStyle.Builder()
                                                    .setSize(DimensionBuilders.sp(11f))
                                                    .build()
                                            )
                                            .build()
                                    )
                                    .build()
                            )
                            .addContent(image(IMG_PLUS))
                            .build()
                    )
                    .addContent(
                        Row.Builder()
                            .addContent(
                                Box.Builder()
                                    .setWidth(DimensionBuilders.dp(48f))
                                    .setHeight(DimensionBuilders.dp(48f))
                                    .setModifiers(
                                        ModifiersBuilders.Modifiers.Builder()
                                            .clickId(VOLUME_DOWN)
                                            .build()
                                    )
                                    .build()
                            )
                            .addContent(
                                Box.Builder()
                                    .setWidth(DimensionBuilders.dp(48f))
                                    .setHeight(DimensionBuilders.dp(48f))
                                    .setModifiers(
                                        ModifiersBuilders.Modifiers.Builder()
                                            .clickId(VOLUME_UP)
                                            .build()
                                    )
                                    .build()
                            )
                            .build()
                    )
                    .build()
            )
            .build()

        val timeline = TimelineBuilders.Timeline.fromLayoutElement(layout)
        val tileBuilder = TileBuilders.Tile.Builder()
            .setTileTimeline(timeline)
            .setResourcesVersion(RESOURCES_VERSION)
            .build()
        return Futures.immediateFuture(tileBuilder)
    }

    override fun onTileResourcesRequest(
        requestParams: RequestBuilders.ResourcesRequest,
    ): ListenableFuture<ResourceBuilders.Resources> {
        val res = ResourceBuilders.Resources.Builder()
            .setVersion(RESOURCES_VERSION)
            .addId(IMG_REFRESH, R.drawable.ic_refresh)
            .addId(IMG_SKIP_PREVIOUS, R.drawable.ic_skip_previous)
            .addId(IMG_SKIP_NEXT, R.drawable.ic_skip_next)
            .addId(IMG_PAUSE, R.drawable.ic_pause)
            .addId(IMG_PLAY, R.drawable.ic_play)
            .addId(IMG_VOLUME, R.drawable.ic_volume)
            .addId(IMG_MINUS, R.drawable.ic_minus)
            .addId(IMG_PLUS, R.drawable.ic_plus)
            .build()
        return Futures.immediateFuture(res)
    }

    private fun processAction(requestParams: RequestBuilders.TileRequest) {
        when(requestParams.currentState.lastClickableId) {
            PLAY_PAUSE -> wearStateInteractor.playPause()
            SKIP_TO_PREVIOUS -> wearStateInteractor.skipToPrevious()
            SKIP_TO_NEXT -> wearStateInteractor.skipToNext()
            VOLUME_UP -> wearStateInteractor.changeVolume(true)
            VOLUME_DOWN -> wearStateInteractor.changeVolume(false)
        }
    }

    private fun imageButton(
        resourceId: String,
        clickId: String
    ): Image {
        return Image.Builder()
            .setWidth(DimensionBuilders.dp(48f))
            .setHeight(DimensionBuilders.dp(48f))
            .setColorFilter(
                ColorFilter.Builder()
                    .setTint(ColorBuilders.argb(getColor(R.color.icon_color)))
                    .build()
            )
            .clickId(clickId)
            .setResourceId(resourceId)
            .build()
    }

    private fun image(
        resourceId: String
    ): Image {
        return Image.Builder()
            .setWidth(DimensionBuilders.dp(24f))
            .setHeight(DimensionBuilders.dp(24f))
            .setColorFilter(
                ColorFilter.Builder()
                    .setTint(ColorBuilders.argb(getColor(R.color.icon_color)))
                    .build()
            )
            .setResourceId(resourceId)
            .build()
    }

    private fun Image.Builder.clickId(
        id: String,
    ): Image.Builder {
        setModifiers(ModifiersBuilders.Modifiers.Builder().clickId(id).build())
        return this
    }

    private fun ModifiersBuilders.Modifiers.Builder.clickId(
        id: String,
    ): ModifiersBuilders.Modifiers.Builder {
        setClickable(
            ModifiersBuilders.Clickable.Builder()
                .setId(id)
                .setOnClick(LoadAction.Builder().build())
                .build()
        )
        return this
    }

    private fun ResourceBuilders.Resources.Builder.addId(
        id: String,
        @DrawableRes resId: Int,
    ): ResourceBuilders.Resources.Builder {
        addIdToImageMapping(
            id,
            ResourceBuilders.ImageResource.Builder()
                .setAndroidResourceByResId(
                    ResourceBuilders.AndroidImageResourceByResId.Builder()
                        .setResourceId(resId)
                        .build()
                ).build()
        )
        return this
    }

}