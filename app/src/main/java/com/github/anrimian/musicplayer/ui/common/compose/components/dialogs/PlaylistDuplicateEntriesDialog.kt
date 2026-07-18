package com.github.anrimian.musicplayer.ui.common.compose.components.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.components.DialogLabelledCheckbox
import com.github.anrimian.musicplayer.ui.library.common.library.PlaylistDuplicateEntryDialog
import com.github.anrimian.musicplayer.ui.utils.compose.components.dialogs.BaseDialog

@Composable
fun PlaylistDuplicateEntriesDialog(
    data: PlaylistDuplicateEntryDialog,
    onConfirm: (ignoreDuplicates: Boolean) -> Unit,
    onCheckChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val message = stringResource(R.string.playlist_duplicates_description, data.playlistName)

    val headerText = stringResource(R.string.compositions)
    val remainingCount = data.totalDuplicatesCount - data.topDuplicateTitles.size
    val footerText = if (remainingCount > 0) {
        stringResource(R.string.more_template, remainingCount)
    } else {
        null
    }
    val duplicatesText = remember(data.topDuplicateTitles, headerText, footerText) {
        buildString {
            append(headerText)
            append(":\n  ")
            data.topDuplicateTitles.forEachIndexed { index, title ->
                if (index > 0) {
                    append("\n  ")
                }
                append(title)
            }
            if (footerText != null) {
                append("\n")
                append(footerText)
            }
        }
    }

    val neutralText = if (data.hasNonDuplicates) {
        stringResource(R.string.add_without_duplicates)
    } else {
        null
    }

    BaseDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.duplicates_detected),
        positiveText = stringResource(R.string.add),
        positiveAction = { onConfirm(false) },
        negativeText = stringResource(R.string.cancel),
        negativeAction = onDismiss,
        neutralText = neutralText,
        neutralAction = { onConfirm(true) },
        contentPadding = PaddingValues(0.dp)
    ) {
        Column {
            Text(
                modifier = Modifier.padding(horizontal = Dimens.dialogContentHorizontalPadding),
                text = message,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                modifier = Modifier.padding(horizontal = Dimens.dialogContentHorizontalPadding),
                text = duplicatesText,
                style = MaterialTheme.typography.bodyMedium
            )

            DialogLabelledCheckbox(
                label = stringResource(R.string.check_for_duplicates),
                isChecked = data.isDuplicateCheckEnabled,
                onCheckedChange = onCheckChange
            )
        }
    }
}