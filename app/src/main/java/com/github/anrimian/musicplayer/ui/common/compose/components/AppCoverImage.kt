package com.github.anrimian.musicplayer.ui.common.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.LocalAppShapes
import com.github.anrimian.musicplayer.ui.common.images.CoverImage

@Composable
fun AppCoverImage(
    composition: CompositionModel,
    isCoversEnabled: Boolean,
    modifier: Modifier = Modifier,
    clipShape: Shape = LocalAppShapes.current.coverShape
) {
    val commonModifier = modifier
        .size(Dimens.coverImageListItemSize)
        .clip(clipShape)

    if (isCoversEnabled) {
        CoverImage(
            model = composition,
            modifier = commonModifier
        )
    } else {
        Box(
            modifier = commonModifier
                .background(MaterialTheme.colorScheme.secondary)
        )
    }
}