package com.github.anrimian.musicplayer.ui.common.compose.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.contentSubtitleMedium
import com.github.anrimian.musicplayer.ui.common.compose.medium
import com.github.anrimian.musicplayer.ui.utils.compose.components.ClassicMorphCheckbox

@Composable
fun DialogLabelledCheckbox(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    textPaddingWidth: Dp = 24.dp,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = Dimens.dialogContentHorizontalPadding,
        vertical = Dimens.checkboxVerticalPadding
    )
) {
    LabelledCheckbox(
        label = label,
        isChecked = isChecked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        description = description,
        textPaddingWidth = textPaddingWidth,
        contentPadding = contentPadding
    )
}

@Composable
fun LabelledCheckbox(
    label: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    textPaddingWidth: Dp = 24.dp,
    contentPadding: PaddingValues = PaddingValues(
        horizontal = Dimens.contentHorizontalMargin,
        vertical = Dimens.checkboxVerticalPadding
    )
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .toggleable(
                value = isChecked,
                onValueChange = onCheckedChange,
                role = Role.Checkbox
            )
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ClassicMorphCheckbox(
            checked = isChecked
        )
        Spacer(Modifier.width(textPaddingWidth))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.medium
            )
            if (description != null) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.contentSubtitleMedium,
                )
            }
        }
    }
}