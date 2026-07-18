package com.github.anrimian.musicplayer.ui.common.compose.components.popup.menu.composition

import android.R.attr.duration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.domain.models.composition.Composition
import com.github.anrimian.musicplayer.domain.models.composition.CompositionModel
import com.github.anrimian.musicplayer.domain.models.composition.InitialSource
import com.github.anrimian.musicplayer.domain.models.composition.LocalFileStatus
import com.github.anrimian.musicplayer.domain.models.utils.CompositionHelper
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme
import com.github.anrimian.musicplayer.ui.common.compose.components.AppHorizontalDivider
import com.github.anrimian.musicplayer.ui.common.compose.components.TextWithSeparators
import com.github.anrimian.musicplayer.ui.common.compose.components.popup.menu.AppPopupMenu
import com.github.anrimian.musicplayer.ui.common.compose.contentSubtitle
import com.github.anrimian.musicplayer.ui.common.compose.itemPrimaryMedium
import com.github.anrimian.musicplayer.ui.common.format.AppFormatUtils
import com.github.anrimian.musicplayer.ui.common.format.AppTimeFormatUtils
import com.github.anrimian.musicplayer.ui.common.models.menu.AppMenuItem
import com.github.anrimian.musicplayer.ui.utils.compose.UiText
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun CompositionPopupMenu(
    composition: CompositionModel,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    menuItems: ImmutableList<AppMenuItem>,
    itemClickListener: (AppMenuItem) -> Unit,
    modifier: Modifier = Modifier
) {
    AppPopupMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        menuItems = menuItems,
        itemClickListener = itemClickListener,
        modifier = modifier,
        headerContent = { modifier -> CompositionMenuHeader(composition, modifier) }
    )
}

@Composable
private fun CompositionMenuHeader(
    composition: CompositionModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 242.dp)
    ) {
        Column(modifier = modifier) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = CompositionHelper.formatCompositionName(composition),
                style = MaterialTheme.typography.itemPrimaryMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(2.dp))

            val artist = AppFormatUtils.formatArtist(composition.artist)
            val durationText = remember(duration) { AppTimeFormatUtils.formatMilliseconds(composition.duration) }
            val sizeText = AppFormatUtils.formatSize(composition.size)

            val subtitleItems = remember(artist, duration) {
                val list = mutableListOf(
                    AnnotatedString(artist),
                    AnnotatedString(durationText),
                    AnnotatedString(sizeText),
                )
                val comment = composition.comment
                if (!comment.isNullOrBlank()) {
                    list.add(AnnotatedString("\n" + comment))
                }
                return@remember list.toImmutableList()
            }

            TextWithSeparators(
                items = subtitleItems,
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.contentSubtitle
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        AppHorizontalDivider()

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Preview
@Composable
private fun CompositionPopupMenuPreview() {
    PreviewAppTheme {
        CompositionPopupMenu(
            composition = Composition(
                id = 1L,
                title = "Composition Title",
                artist = "Artist Name",
                album = "Album Name",
                duration = 123456L,
                size = 12345678L,
                comment = "This is a comment",
                storageId = 1L,
                addedTime = 0L,
                modifiedTime = 0L,
                coverModifyTime = 0L,
                fileStatus = LocalFileStatus.AVAILABLE,
                corruptionType = null,
                isFileExists = true,
                initialSource = InitialSource.LOCAL
            ),
            expanded = true,
            onDismissRequest = {},
            menuItems = persistentListOf(
                AppMenuItem(id = 1, title = UiText.DynamicString("Menu Item 1")),
                AppMenuItem(id = 2, title = UiText.DynamicString("Menu Item 2")),
            ),
            itemClickListener = {}
        )
    }
}