package com.github.anrimian.musicplayer.ui.common.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/*
 * =================================================================
 * MAPPING: Android View (Material Components) -> Compose Material 3
 * =================================================================
 *
 * DISPLAY (Large headings, promo screens)
 * TextAppearance.MaterialComponents.Headline1 -> displayLarge  (57sp)
 * TextAppearance.MaterialComponents.Headline2 -> displayMedium (45sp)
 * TextAppearance.MaterialComponents.Headline3 -> displaySmall  (36sp)
 *
 * HEADLINE (Screen titles/headers)
 * TextAppearance.MaterialComponents.Headline4 -> headlineLarge (32sp)
 * TextAppearance.MaterialComponents.Headline5 -> headlineMedium(28sp)
 * TextAppearance.MaterialComponents.Headline6 -> headlineSmall (24sp)
 *
 * TITLE (Section subheaders, AppBar)
 * TextAppearance.MaterialComponents.Subtitle1 -> titleLarge    (22sp) - Commonly used!
 * TextAppearance.AppCompat.Subhead            -> titleMedium   (16sp)
 * (no direct analog)                          -> titleSmall    (14sp)
 *
 * BODY (Body text)
 * TextAppearance.AppCompat.Body1              -> bodyLarge     (16sp) - Primary content
 * TextAppearance.AppCompat.Body2              -> bodyMedium    (14sp) - Secondary content
 * TextAppearance.AppCompat.Caption            -> bodySmall     (12sp)
 *
 * LABEL (Buttons, field labels)
 * TextAppearance.MaterialComponents.Button    -> labelLarge    (14sp) - Button text
 * TextAppearance.AppCompat.Overline           -> labelMedium   (12sp)
 * (very small captions)                       -> labelSmall    (11sp)
 *
 * =================================================================
 * TIP:
 * If in doubt, use:
 * - bodyLarge for regular text.
 * - titleMedium for list headers.
 * - labelMedium for captions or metadata.
 * =================================================================
 */

private val defaultTypography = Typography()

val xmlCompatStyle = PlatformTextStyle(
    includeFontPadding = true
)
val OldSchoolTypography = Typography(
    bodyLarge = defaultTypography.bodyLarge.copy(
        fontFamily = FontFamily.Default,
        letterSpacing = 0.sp,
        lineHeight = TextUnit.Unspecified,
        platformStyle = xmlCompatStyle
    ),
    bodyMedium = defaultTypography.bodyMedium.copy(
        fontFamily = FontFamily.Default,
        letterSpacing = 0.sp,
        lineHeight = TextUnit.Unspecified,
        platformStyle = xmlCompatStyle
    ),
    bodySmall = defaultTypography.bodySmall.copy(
        fontFamily = FontFamily.Default,
        letterSpacing = 0.sp,
        lineHeight = TextUnit.Unspecified,
        platformStyle = xmlCompatStyle
    ),
    titleMedium = defaultTypography.titleMedium.copy(
        fontFamily = FontFamily.Default,
        letterSpacing = 0.sp,
        lineHeight = TextUnit.Unspecified,
        platformStyle = xmlCompatStyle
    ),
    labelLarge = defaultTypography.labelLarge.copy(
        fontFamily = FontFamily.Default,
        letterSpacing = 0.sp,
        lineHeight = TextUnit.Unspecified,
        platformStyle = xmlCompatStyle
    ),
    labelSmall = defaultTypography.labelSmall.copy(
        fontFamily = FontFamily.Default,
        letterSpacing = 0.sp,
        lineHeight = TextUnit.Unspecified,
        platformStyle = xmlCompatStyle
    )
)

val Typography.itemPrimary: TextStyle
    @Composable
    get() = bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.Bold
    )

val Typography.itemPrimaryMedium: TextStyle
    @Composable
    get() = itemPrimary.copy(
        fontSize = 15.sp,
    )

val Typography.contentSubtitle: TextStyle
    @Composable
    get() = bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 16.sp,
    )

val Typography.contentSubtitleMedium: TextStyle
    @Composable
    get() = contentSubtitle.copy(
        fontSize = 13.sp,
    )

val Typography.medium: TextStyle
    @Composable
    get() = bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 15.sp
    )

val Typography.mediumLarge: TextStyle
    @Composable
    get() = bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontSize = 18.sp
    )

val Typography.labelExtraLarge: TextStyle
    @Composable
    get() = labelLarge.copy(
        fontSize = 18.sp
    )

val Typography.subtitle: TextStyle
    @Composable
    get() = bodyMedium.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

val Typography.contentTitle: TextStyle
    @Composable
    get() = titleSmall.copy(
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
    )

val Typography.dialogTitle: TextStyle
    @Composable
    get() = titleLarge.copy(
        fontSize = 19.sp,
        fontWeight = FontWeight.SemiBold,
    )

val Typography.dialogButton: TextStyle
    @Composable
    get() = labelLarge.copy(
        fontWeight = FontWeight.SemiBold,
    )

val Typography.editText: TextStyle
    @Composable
    get() = bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface
    )

val Typography.editTextHint: TextStyle
    @Composable
    get() = bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

val Typography.caption: TextStyle
    @Composable
    get() = bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

val Typography.captionError: TextStyle
    @Composable
    get() = bodySmall.copy(
        color = MaterialTheme.colorScheme.error,
    )

val Typography.labelSmallDense: TextStyle
    @Composable
    get() = labelSmall.copy(
        lineHeight = labelSmall.fontSize * 1.2f
    )

val Typography.settingsItemTitle: TextStyle
    @Composable
    get() = bodyMedium.copy(
        fontSize = 17.sp
    )