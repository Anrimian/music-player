package com.github.anrimian.musicplayer.ui.common.compose.components.dialogs

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.utils.compose.components.WheelPicker
import com.github.anrimian.musicplayer.ui.utils.compose.components.dialogs.BaseDialog
import kotlin.math.abs

@Composable
fun NumberPickerDialog(
    minValue: Long,
    maxValue: Long,
    currentValue: Long,
    stepValue: Long = 1,
    onDismissRequest: () -> Unit,
    onValuePicked: (Long) -> Unit,
    valueFormatter: ((Long) -> String)? = null,
) {
    val values = remember(minValue, maxValue, stepValue) {
        (minValue..maxValue step stepValue).toList()
    }

    val initialSelection = remember(values, currentValue) {
        values.minByOrNull { abs(it - currentValue) } ?: minValue
    }

    var selectedValue by remember(initialSelection) { mutableStateOf(initialSelection) }

    BaseDialog(
        onDismissRequest = onDismissRequest,
        positiveText = stringResource(android.R.string.ok),
        positiveAction = { onValuePicked(selectedValue) },
        negativeText = stringResource(R.string.cancel)
    ) {
        WheelPicker(
            values = values,
            initialValue = initialSelection,
            onValueChanged = { selectedValue = it },
            valueFormatter = { item -> valueFormatter?.invoke(item) ?: item.toString() },
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}