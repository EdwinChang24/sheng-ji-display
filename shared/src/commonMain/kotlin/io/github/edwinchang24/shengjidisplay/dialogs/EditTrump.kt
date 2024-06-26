package io.github.edwinchang24.shengjidisplay.dialogs

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.edwinchang24.shengjidisplay.components.ButtonWithEmphasis
import io.github.edwinchang24.shengjidisplay.components.OutlinedButtonWithEmphasis
import io.github.edwinchang24.shengjidisplay.components.RankPicker
import io.github.edwinchang24.shengjidisplay.components.SuitPicker
import io.github.edwinchang24.shengjidisplay.model.AppState
import io.github.edwinchang24.shengjidisplay.model.PlayingCard
import io.github.edwinchang24.shengjidisplay.navigation.Navigator
import io.github.edwinchang24.shengjidisplay.resources.Res
import io.github.edwinchang24.shengjidisplay.resources.ic_close
import io.github.edwinchang24.shengjidisplay.resources.ic_done
import org.jetbrains.compose.resources.painterResource

@Composable
fun EditTrumpDialog(navigator: Navigator, state: AppState, setState: (AppState) -> Unit) {
    var rank by rememberSaveable { mutableStateOf(state.trump?.rank) }
    var suit by rememberSaveable { mutableStateOf(state.trump?.suit) }
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier =
            Modifier.width(IntrinsicSize.Max)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 24.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Text("Edit trump card", style = MaterialTheme.typography.headlineMedium)
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Rank", style = MaterialTheme.typography.labelMedium)
                RankPicker(
                    rank,
                    { rank = it },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Suit", style = MaterialTheme.typography.labelMedium)
                SuitPicker(
                    suit,
                    { suit = it },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
            modifier =
                Modifier.fillMaxWidth()
                    .horizontalScroll(rememberScrollState(), reverseScrolling = true)
                    .padding(horizontal = 24.dp)
        ) {
            OutlinedButtonWithEmphasis(
                text = "Cancel",
                icon = painterResource(Res.drawable.ic_close),
                onClick = navigator::closeDialog
            )
            ButtonWithEmphasis(
                text = "Done",
                icon = painterResource(Res.drawable.ic_done),
                onClick = {
                    rank?.let { r ->
                        suit?.let { s ->
                            setState(state.copy(trump = PlayingCard(r, s)))
                            navigator.closeDialog()
                        }
                    }
                },
                enabled = rank != null && suit != null
            )
        }
    }
}
