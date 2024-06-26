package io.github.edwinchang24.shengjidisplay.interaction

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale

@Composable
expect fun PressableWithEmphasis(content: @Composable PressableWithEmphasisScope.() -> Unit)

class PressableWithEmphasisScope(
    val interactionSource: MutableInteractionSource,
    private val scale: Float
) {
    fun Modifier.pressEmphasis() = scale(scale)

    @OptIn(ExperimentalFoundationApi::class)
    fun Modifier.clickableForEmphasis(onLongClick: (() -> Unit)? = null, onClick: () -> Unit) =
        composed {
            this@composed.combinedClickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick,
                onLongClick = onLongClick
            )
        }
}
