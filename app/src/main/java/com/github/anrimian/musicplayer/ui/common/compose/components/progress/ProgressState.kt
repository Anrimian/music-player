package com.github.anrimian.musicplayer.ui.common.compose.components.progress

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.anrimian.musicplayer.R
import com.github.anrimian.musicplayer.ui.common.compose.Dimens
import com.github.anrimian.musicplayer.ui.common.compose.PreviewAppTheme
import com.github.anrimian.musicplayer.ui.common.compose.components.buttons.AppTextButtonLarge
import com.github.anrimian.musicplayer.ui.common.compose.mediumLarge
import com.github.anrimian.musicplayer.ui.common.mvvm.progress.StatedData
import kotlinx.coroutines.delay

@Composable
fun <T> ProgressState(
    state: StatedData<T>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    errorAction: (() -> Unit)? = null,
    errorActionText: String = stringResource(R.string.try_again),
    emptyAction: (() -> Unit)? = null,
    emptyActionText: String = "",
    emptyContent: @Composable (String?, String, (() -> Unit)?, PaddingValues) -> Unit =
        { msg, actionText, action, padd -> EmptyContentState(msg ?: "", actionText, action, padd) },
    errorContent: @Composable (String, String, (() -> Unit)?, PaddingValues) -> Unit =
        { msg, actionText, action, padd -> EmptyContentState(msg, actionText, action, padd) },
    content: @Composable (T) -> Unit
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "ProgressStateAnimation",
        contentKey = { targetState ->
            when (targetState) {
                is StatedData.Content -> "Content" // do not recompose if content was updated
                is StatedData.Error -> targetState.message
                is StatedData.Empty -> targetState.message
                else -> targetState::class
            }
        },
        modifier = modifier
    ) { targetState ->
        when (targetState) {
            is StatedData.Content -> {
                content(targetState.data)
            }
            is StatedData.Loading -> {
                DelayedLoader(contentPadding)
            }
            is StatedData.Empty -> {
                emptyContent(targetState.message?.asString(), emptyActionText, emptyAction, contentPadding)
            }
            is StatedData.Error -> {
                errorContent(targetState.message, errorActionText, errorAction, contentPadding)
            }
        }
    }
}

@Composable
private fun DelayedLoader(contentPadding: PaddingValues, modifier: Modifier = Modifier) {
    var showLoader by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(750)
        showLoader = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        if (showLoader) {
            CircularProgressIndicator()
        }
    }
}

@Composable
private fun EmptyContentState(
    message: String,
    actionText: String,
    action: (() -> Unit)?,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(
                vertical = Dimens.contentVerticalMargin,
                horizontal = Dimens.contentHorizontalMargin
            )
            .padding(contentPadding)
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                style = MaterialTheme.typography.mediumLarge,
                textAlign = TextAlign.Center,
            )
            if (action != null) {
                Spacer(modifier = Modifier.height(Dimens.contentSpacingVerticalMarginLarge))

                AppTextButtonLarge(actionText, action)
            }
        }
    }
}

@Preview
@Composable
private fun EmptyContentStateWithActionPreview() {
    PreviewAppTheme {
        EmptyContentState(
            message = "Empty message",
            actionText = "Action",
            action = {},
            contentPadding = PaddingValues()
        )
    }
}

@Preview
@Composable
private fun EmptyContentStatePreview() {
    PreviewAppTheme {
        EmptyContentState(
            message = "Empty message",
            actionText = "Action",
            action = null,
            contentPadding = PaddingValues()
        )
    }
}