package com.github.anrimian.musicplayer.ui.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.R

class AppDimens(
    val contentHorizontalMargin: Dp,
    val contentVerticalMargin: Dp,
    val contentVerticalMarginSmall: Dp,
    val contentSpacingVerticalMargin: Dp,
    val contentSpacingHorizontalMargin: Dp,
    val contentSpacingVerticalMarginLarge: Dp = 16.dp,
    val contentInternalVerticalMargin: Dp,
    val listVerticalMargin: Dp,
    val dividerThickness: Dp = 0.5.dp,
    val dialogContentVerticalPadding: Dp = 20.dp,
    val dialogContentHorizontalPadding: Dp = 24.dp,
    val dialogContentSpacingPadding: Dp = 8.dp,
    val fabSize: Dp = 56.dp,
    val bottomPaddingWithFab: Dp = fabSize + (contentVerticalMargin * 2),
    val toolbarContentStart: Dp,
    val checkboxVerticalPadding: Dp,
    val buttonExpandSize: Dp,
    val settingsIconHorizontalPadding: Dp,
    val coverImageListItemSize: Dp = 40.dp,
    val snackbarPadding: Dp = 12.dp,
)

@Composable
fun calculateAppDimens(): AppDimens {
    val context = LocalContext.current
    val density = LocalDensity.current.density
    val configuration = LocalConfiguration.current
    return remember(configuration) {
        val res = context.resources

        fun dim(id: Int) = (res.getDimension(id) / density).dp

        AppDimens(
            contentHorizontalMargin = dim(R.dimen.content_horizontal_margin),
            contentVerticalMargin = dim(R.dimen.content_vertical_margin),
            contentVerticalMarginSmall = dim(R.dimen.editor_content_margin_vertical),
            contentSpacingVerticalMargin = dim(R.dimen.content_spacing_vertical_margin),
            contentSpacingHorizontalMargin = dim(R.dimen.content_spacing_margin),
            contentInternalVerticalMargin = dim(R.dimen.content_internal_margin),
            listVerticalMargin = dim(R.dimen.list_vertical_margin),
            toolbarContentStart = dim(R.dimen.toolbar_content_start),
            checkboxVerticalPadding = dim(R.dimen.checkbox_text_vertical_padding),
            buttonExpandSize = dim(R.dimen.button_expand_size),
            settingsIconHorizontalPadding = dim(R.dimen.settings_icon_horizontal_margin),
            snackbarPadding = if (configuration.smallestScreenWidthDp >= 600) 16.dp else 12.dp
        )
    }
}

val LocalAppDimens = staticCompositionLocalOf {
    AppDimens(
        contentHorizontalMargin = 16.dp,
        contentVerticalMargin = 16.dp,
        contentVerticalMarginSmall = 12.dp,
        contentSpacingVerticalMargin = 16.dp,
        contentSpacingHorizontalMargin = 16.dp,
        contentInternalVerticalMargin = 8.dp,
        listVerticalMargin = 12.dp,
        contentSpacingVerticalMarginLarge = 16.dp,
        toolbarContentStart = 72.dp,
        checkboxVerticalPadding = 16.dp,
        buttonExpandSize = 48.dp,
        settingsIconHorizontalPadding = 24.dp,
        snackbarPadding = 12.dp
    )
}

object Dimens {
    val contentHorizontalMargin: Dp
        @Composable get() = current.contentHorizontalMargin
    val contentVerticalMargin: Dp
        @Composable get() = current.contentVerticalMargin
    val contentVerticalMarginSmall: Dp
        @Composable get() = current.contentVerticalMarginSmall
    val contentSpacingVerticalMargin: Dp
        @Composable get() = current.contentSpacingVerticalMargin
    val contentSpacingHorizontalMargin: Dp
        @Composable get() = current.contentSpacingHorizontalMargin
    val contentSpacingVerticalMarginLarge: Dp
        @Composable get() = current.contentSpacingVerticalMarginLarge
    val contentInternalVerticalMargin: Dp
        @Composable get() = current.contentInternalVerticalMargin
    val listVerticalMargin: Dp
        @Composable get() = current.listVerticalMargin
    val dividerThickness: Dp
        @Composable get() = current.dividerThickness
    val dialogContentVerticalPadding: Dp
        @Composable get() = current.dialogContentVerticalPadding
    val dialogContentHorizontalPadding: Dp
        @Composable get() = current.dialogContentHorizontalPadding
    val dialogContentSpacingPadding: Dp
        @Composable get() = current.dialogContentSpacingPadding
    val fabSize: Dp
        @Composable get() = current.fabSize
    val bottomPaddingWithFab: Dp
        @Composable get() = current.bottomPaddingWithFab

    val toolbarContentStart: Dp
        @Composable get() = current.toolbarContentStart
    val checkboxVerticalPadding: Dp
        @Composable get() = current.checkboxVerticalPadding
    val buttonExpandSize: Dp
        @Composable get() = current.buttonExpandSize
    val settingsIconHorizontalPadding: Dp
        @Composable get() = current.settingsIconHorizontalPadding
    val coverImageListItemSize: Dp
        @Composable get() = current.coverImageListItemSize
    val snackbarPadding: Dp
        @Composable get() = current.snackbarPadding


    private val current: AppDimens
        @Composable get() = LocalAppDimens.current
}