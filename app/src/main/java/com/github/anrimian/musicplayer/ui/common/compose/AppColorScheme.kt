package com.github.anrimian.musicplayer.ui.common.compose

import android.content.Context
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.utils.attrColor

fun createColorScheme(context: Context, isDark: Boolean): ColorScheme {
    return if (isDark) createDarkColorScheme(context) else createLightColorScheme(context)
}

private fun createDarkColorScheme(ctx: Context) = darkColorScheme(
    // [Key Action Colors]
    // High-emphasis buttons (Button), active states (Switch, Checkbox), and active indicators.
    // Standard M3: The strongest brand color.
    primary = Color(ctx.attrColor(R.attr.colorAccent)),
    onPrimary = Color(ctx.attrColor(android.R.attr.textColorPrimary)), // Text on top of primary buttons

    // [Container Colors]
    // Default background for FloatingActionButton, filled tonal buttons, and selected states (e.g., NavigationBar items).
    // Standard M3: Usually a lighter/pastel tone of Primary (to provide contrast without being too loud).
    primaryContainer = Color(ctx.attrColor(R.attr.colorAccent)),
    onPrimaryContainer = Color(ctx.attrColor(android.R.attr.textColorPrimary)),

    // [Secondary Actions]
    // Less prominent components like FilterChips, extended FABs, or badged icons.
    secondary = Color(ctx.attrColor(R.attr.colorPrimaryVariant)),
    onSecondary = Color(ctx.attrColor(android.R.attr.textColorPrimary)),

    // [Screen Background]
    // The underlying color of the scrollable content area.
    background = Color(ctx.attrColor(android.R.attr.colorBackground)),
    onBackground = Color(ctx.attrColor(android.R.attr.textColorPrimary)),

    // [Component Surfaces]
    // Background for Cards, Sheets, Menus.
    // In M3, Surface and Background are often the same, separated by elevation (tonal surface).
    surface = Color(ctx.attrColor(android.R.attr.colorBackground)),
    onSurface = Color(ctx.attrColor(android.R.attr.textColorPrimary)), // High Emphasis Text (Title, Body)

    // [Modal Containers]
    // Specifically for Dialogs and ModalBottomSheets.
    // Standard M3: Slightly lighter than Surface to show hierarchy.
    surfaceContainerHigh = Color(ctx.attrColor(R.attr.dialogBackground)),
    surfaceContainerHighest = Color(ctx.attrColor(R.attr.popupMenuBackgroundColor)),

    // [Medium Emphasis Content]
    // Critical color for Icons, Secondary Text (subtitles), and TextField labels/icons.
    // Standard M3: A grey-ish color, distinct from onSurface (black/white).
    onSurfaceVariant = Color(ctx.attrColor(android.R.attr.textColorSecondary)),

    // [Borders & Dividers]
    // Used for TextField borders (OutlinedTextField) and Switch borders (unchecked).
    outline = Color(ctx.attrColor(R.attr.colorControlThird)),

    // Used for decorative dividers (Divider/HorizontalDivider) and disabled borders.
    outlineVariant = Color(ctx.attrColor(android.R.attr.dividerHorizontal)),

    // [Error States]
    // Error text, error icons, and error state components.
    error = Color(ctx.attrColor(R.attr.colorError)),
    onError = Color.White
)

private fun createLightColorScheme(ctx: Context) = lightColorScheme(
    // [Key Action Colors]
    primary = Color(ctx.attrColor(R.attr.colorAccent)),
    onPrimary = Color(ctx.attrColor(android.R.attr.textColorPrimaryInverse)),

    // [Container Colors]
    // FABs, Segmented Buttons, Selected Navigation Items.
    primaryContainer = Color(ctx.attrColor(R.attr.colorAccent)),
    onPrimaryContainer = Color(ctx.attrColor(android.R.attr.textColorPrimaryInverse)),

    // [Secondary Actions]
    secondary = Color(ctx.attrColor(R.attr.colorPrimaryVariant)),
    onSecondary = Color(ctx.attrColor(android.R.attr.textColorPrimaryInverse)),

    // [Screen Background]
    background = Color(ctx.attrColor(android.R.attr.colorBackground)),
    onBackground = Color(ctx.attrColor(android.R.attr.textColorPrimary)),

    // [Component Surfaces]
    surface = Color(ctx.attrColor(android.R.attr.colorBackground)),
    onSurface = Color(ctx.attrColor(android.R.attr.textColorPrimary)),

    // [Modal Containers]
    surfaceContainerHigh = Color(ctx.attrColor(R.attr.dialogBackground)),
    surfaceContainerHighest = Color(ctx.attrColor(R.attr.popupMenuBackgroundColor)),

    // [Medium Emphasis Content]
    // Icons and Secondary Text.
    onSurfaceVariant = Color(ctx.attrColor(android.R.attr.textColorSecondary)),

    // [Borders & Dividers]
    outline = Color(ctx.attrColor(R.attr.colorControlThird)),
    outlineVariant = Color(ctx.attrColor(android.R.attr.dividerHorizontal)),

    // [Error States]
    error = Color(ctx.attrColor(R.attr.colorError)),
    onError = Color.White,
)


val ColorScheme.onSurfaceIcon: Color
    get() = this.onSurface.copy(alpha = 0.71f)

/**
 * Background for the initial swipe action.
 */
val ColorScheme.swipeContainer: Color
    get() = primary.copy(alpha = 0.7f)
/**
 * Background for the triggered swipe-remove action.
 */
val ColorScheme.swipeContainerActivated: Color
    get() = primary
/**
 * Color for content (icons/text) displayed on top of a [swipeContainer].
 * Usually matches [onPrimary], but extracted for semantic consistency.
 */
val ColorScheme.onSwipeContainer: Color
    get() = onPrimary


val ColorScheme.selectionContainer: Color
    get() = primary.copy(alpha = 0.12f)

/**
 * Color used for the overlay when an item is being dragged.
 */
val ColorScheme.dragContainer: Color
    get() = onSurface.copy(alpha = 0.06f)

/**
 * Color used for the background of the currently playing composition.
 */
val ColorScheme.playingContainer: Color
    get() = secondary.copy(alpha = 0.1f)

/**
 * Overlay color for loaded cover images to improve contrast.
 */
val ColorScheme.coverOverlay: Color
    get() = Color.Black.copy(alpha = 0.2f)

fun Color.applyDisabledAlpha(enabled: Boolean): Color {
    return if (enabled) this else this.copy(alpha = 0.38f)
}


@Immutable
class AppAdditionalColors

val LocalAdditionalColors = staticCompositionLocalOf { AppAdditionalColors() }

val MaterialTheme.additionalColors: AppAdditionalColors
    @Composable
    get() = LocalAdditionalColors.current