package io.github.edwinchang24.shengjidisplay.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.edwinchang24.shengjidisplay.interaction.PressableWithEmphasis
import io.github.edwinchang24.shengjidisplay.model.Call
import io.github.edwinchang24.shengjidisplay.model.PlayingCard
import io.github.edwinchang24.shengjidisplay.model.Suit
import io.github.edwinchang24.shengjidisplay.theme.ShengJiDisplayTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CallsDisplay(
    calls: List<Call>,
    setFound: (index: Int, found: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        maxItemsInEachRow = 2,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        calls.forEachIndexed { index, call ->
            PressableWithEmphasis {
                OutlinedCard(modifier = Modifier.size(128.dp)) {
                    Box(
                        modifier =
                            Modifier.fillMaxSize()
                                .clickableForEmphasis(
                                    onLongClick = {
                                        setFound(
                                            index,
                                            (call.found + call.number) % (call.number + 1)
                                        )
                                    },
                                    onClick = {
                                        setFound(index, (call.found + 1) % (call.number + 1))
                                    }
                                )
                    ) {
                        Column(
                            verticalArrangement =
                                Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier =
                                Modifier.fillMaxSize()
                                    .alpha(
                                        animateFloatAsState(
                                                targetValue =
                                                    if (call.found == call.number) 0.4f else 1f,
                                                label = ""
                                            )
                                            .value
                                    )
                                    .padding(16.dp)
                                    .pressEmphasis()
                        ) {
                            PlayingCard(
                                call.card,
                                textStyle = LocalTextStyle.current.copy(fontSize = 48.sp)
                            )
                            CallFoundText(
                                call = call,
                                style = LocalTextStyle.current.copy(fontSize = 28.sp)
                            )
                        }
                        val color = MaterialTheme.colorScheme.outlineVariant
                        val lineScale by
                            animateFloatAsState(
                                if (call.found == call.number) 0.5f else 0f,
                                animationSpec = tween(durationMillis = 100),
                                label = ""
                            )
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawLine(
                                color,
                                start =
                                    Offset(
                                        size.width * (0.5f - lineScale),
                                        size.height * (0.5f + lineScale)
                                    ),
                                end =
                                    Offset(
                                        size.width * (0.5f + lineScale),
                                        size.height * (0.5f - lineScale)
                                    ),
                                strokeWidth = 12f
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun CallsDisplayPreview() {
    ShengJiDisplayTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            var calls by remember {
                mutableStateOf(
                    listOf(
                        Call(PlayingCard("A", Suit.SPADES), number = 1, found = 0),
                        Call(PlayingCard("K", Suit.HEARTS), number = 2, found = 2)
                    )
                )
            }
            CallsDisplay(
                calls,
                { index, found ->
                    calls = calls.toMutableList().also { it[index] = it[index].copy(found = found) }
                },
                modifier = Modifier.wrapContentSize()
            )
        }
    }
}
