package com.github.anrimian.musicplayer.ui.common.compose.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.github.anrimian.musicplayer.ui.common.compose.contentSubtitle

@Composable
fun TextWithSeparators(
    items: List<AnnotatedString>,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.contentSubtitle,
    separatorColor: Color = style.color,
    separatorSize: Dp = 5.4.dp
) {
    val text = remember(items) {
        buildAnnotatedString {
            items.forEachIndexed { index, item ->
                append(item)
                if (index < items.lastIndex) {
                    val nextItem = items[index + 1]
                    if (!nextItem.startsWith('\n')) {
                        append(" ")
                        appendInlineContent(id = "dot", alternateText = "•")
                        append("\u00A0") // symbol for not breaking space after delimiter
                    }
                }
            }
        }
    }

    val inlineContent = remember(separatorSize, separatorColor, style) {
        mapOf(
            "dot" to InlineTextContent(
                Placeholder(
                    width = 0.5.em,
                    height = 1.em,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawCircle(
                                color = separatorColor,
                                radius = separatorSize.toPx() / 2f,
                                center = center.copy(y = center.y + 0.5.dp.toPx())
                            )
                        }
                )
            }
        )
    }

    Text(
        text = text,
        inlineContent = inlineContent,
        style = style,
        modifier = modifier,
    )
}