package com.github.anrimian.musicplayer.ui.library.common.order

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.di.utils.DialogViewModelContainer
import com.github.anrimian.musicplayer.domain.models.order.Order
import com.github.anrimian.musicplayer.domain.models.order.OrderType
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme
import com.github.anrimian.musicplayer.ui.common.compose.components.AppHorizontalDivider
import com.github.anrimian.musicplayer.ui.common.compose.components.DialogLabelledCheckbox
import com.github.anrimian.musicplayer.ui.common.effects.ObserveEffects
import com.github.anrimian.musicplayer.ui.common.format.FormatUtils
import com.github.anrimian.musicplayer.ui.utils.compose.components.dialogs.BaseDialog

@Composable
fun SelectOrderDialog(
    data: SelectOrderDialogData,
    onDismiss: () -> Unit,
    onOrderSelected: (Order) -> Unit
) {
    DialogViewModelContainer<SelectOrderViewModel>(data, onDismiss) { viewModel, dismiss ->
        val state by viewModel.state.collectAsStateWithLifecycle()

        ObserveEffects(viewModel.effects) { effect ->
            when (effect) {
                is SelectOrderEffect.CloseWithOrder -> {
                    onOrderSelected(effect.order)
                    dismiss()
                }
                SelectOrderEffect.Close -> dismiss()
            }
        }

        SelectOrderDialogContent(
            state = state,
            data = data,
            onDismiss = dismiss,
            onOrderTypeSelected = viewModel::onOrderTypeSelected,
            onReverseChanged = viewModel::onReverseChanged,
            onDisplayFileNameChanged = viewModel::onDisplayFileNameChanged,
            onComplete = viewModel::onCompleteButtonClicked
        )
    }
}

@Composable
private fun SelectOrderDialogContent(
    state: SelectOrderState,
    data: SelectOrderDialogData,
    onDismiss: () -> Unit,
    onOrderTypeSelected: (OrderType) -> Unit,
    onReverseChanged: (Boolean) -> Unit,
    onDisplayFileNameChanged: (Boolean) -> Unit,
    onComplete: () -> Unit
) {
    BaseDialog(
        onDismissRequest = onDismiss,
        title = stringResource(R.string.order),
        positiveText = stringResource(android.R.string.ok),
        positiveAction = onComplete,
        positiveEnabled = state.selectedOrderType != null,
        negativeText = stringResource(R.string.cancel),
        negativeAction = onDismiss,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column {
            LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                items(data.allowedOrderTypes) { orderType ->
                    OrderTypeItem(
                        text = stringResource(FormatUtils.getOrderTitle(orderType)),
                        isSelected = state.selectedOrderType == orderType,
                        onClick = { onOrderTypeSelected(orderType) }
                    )
                }
            }

            if (state.selectedOrderType != null || data.showFileNameSetting) {
                Spacer(Modifier.height(8.dp))
            }

            val selectedOrderType = state.selectedOrderType
            AnimatedVisibility(
                visible = selectedOrderType != null,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                if (selectedOrderType != null) {
                    DialogLabelledCheckbox(
                        label = stringResource(FormatUtils.getReversedOrderText(selectedOrderType)),
                        isChecked = state.isReverse,
                        onCheckedChange = onReverseChanged,
                        textPaddingWidth = 12.dp,
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = Dimens.checkboxVerticalPadding)
                    )
                }
            }

            if (data.showFileNameSetting) {
                AppHorizontalDivider()

                DialogLabelledCheckbox(
                    label = stringResource(R.string.display_composition_file_name),
                    isChecked = state.isDisplayFileNameEnabled,
                    onCheckedChange = onDisplayFileNameChanged,
                    textPaddingWidth = 12.dp,
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = Dimens.checkboxVerticalPadding)
                )
            }
        }
    }
}

@Composable
private fun OrderTypeItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview
@Composable
private fun SelectOrderDialogContentPreview() {
    PreviewAppTheme {
        SelectOrderDialogContent(
            state = SelectOrderState(
                OrderType.DURATION,
                isReverse = false,
                isDisplayFileNameEnabled = true
            ),
            data = SelectOrderDialogData(
                selectedOrder = Order(OrderType.NAME, false),
                showFileNameSetting = true,
                allowedOrderTypes = listOf(
                    OrderType.NAME,
                    OrderType.ADD_TIME,
                    OrderType.DURATION
                ),
            ),
            onDismiss = {},
            onOrderTypeSelected = {},
            onReverseChanged = {},
            onDisplayFileNameChanged = {},
            onComplete = {}
        )
    }
}

@Preview
@Composable
private fun SelectOrderDialogContentNoStatePreview() {
    PreviewAppTheme {
        SelectOrderDialogContent(
            state = SelectOrderState(
                null,
                isReverse = false,
                isDisplayFileNameEnabled = true
            ),
            data = SelectOrderDialogData(
                selectedOrder = null,
                showFileNameSetting = false,
                allowedOrderTypes = listOf(
                    OrderType.NAME,
                    OrderType.ADD_TIME,
                    OrderType.DURATION
                ),
            ),
            onDismiss = {},
            onOrderTypeSelected = {},
            onReverseChanged = {},
            onDisplayFileNameChanged = {},
            onComplete = {}
        )
    }
}

@Preview
@Composable
private fun SelectOrderDialogContentNoState2Preview() {
    PreviewAppTheme {
        SelectOrderDialogContent(
            state = SelectOrderState(
                null,
                isReverse = false,
                isDisplayFileNameEnabled = true
            ),
            data = SelectOrderDialogData(
                selectedOrder = null,
                showFileNameSetting = true,
                allowedOrderTypes = listOf(
                    OrderType.NAME,
                    OrderType.ADD_TIME,
                    OrderType.DURATION
                ),
            ),
            onDismiss = {},
            onOrderTypeSelected = {},
            onReverseChanged = {},
            onDisplayFileNameChanged = {},
            onComplete = {}
        )
    }
}