package io.github.edwinchang24.shengjidisplay.interaction

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PressableWithEmphasis(content: @Composable PressableWithEmphasisScope.() -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val scale = remember { Animatable(1f) }
    LaunchedEffect(true) {
        interactionSource.interactions.collectLatest {
            if (it is PressInteraction.Press) {
                scale.animateTo(
                    1f,
                    SpringSpec(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                )
                scale.animateTo(
                    0.85f,
                    SpringSpec(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                )
            } else if (it is PressInteraction.Cancel || it is PressInteraction.Release) {
                scale.animateTo(
                    0.85f,
                    SpringSpec(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessHigh
                    )
                )
                scale.animateTo(
                    1f,
                    SpringSpec(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = 1000f)
                )
            }
        }
    }
    PressableWithEmphasisScope(interactionSource, scale.value).content()
}

class PressableWithEmphasisScope(
    val interactionSource: MutableInteractionSource,
    private val scale: Float
) {
    fun Modifier.pressEmphasis() = scale(scale)

    fun Modifier.clickableForEmphasis(onClick: () -> Unit) = composed {
        this@composed.clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            onClick = onClick
        )
    }
}
