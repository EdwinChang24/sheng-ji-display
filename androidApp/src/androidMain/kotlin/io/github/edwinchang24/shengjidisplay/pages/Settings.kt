package io.github.edwinchang24.shengjidisplay.pages

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle
import io.github.edwinchang24.shengjidisplay.BuildConfig
import io.github.edwinchang24.shengjidisplay.MainActivityViewModel
import io.github.edwinchang24.shengjidisplay.MainNavGraph
import io.github.edwinchang24.shengjidisplay.R
import io.github.edwinchang24.shengjidisplay.components.IconButtonWithEmphasis
import io.github.edwinchang24.shengjidisplay.interaction.PressableWithEmphasis
import io.github.edwinchang24.shengjidisplay.model.ContentRotationSetting
import io.github.edwinchang24.shengjidisplay.model.VerticalOrder

@OptIn(ExperimentalMaterial3Api::class)
@Destination(style = SettingsPageTransitions::class)
@MainNavGraph
@Composable
fun SettingsPage(navigator: DestinationsNavigator, viewModel: MainActivityViewModel) {
    val state by
        viewModel.state.collectAsStateWithLifecycle(
            lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
        )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Display settings") },
                navigationIcon = {
                    IconButtonWithEmphasis(onClick = { navigator.navigateUp() }) {
                        Icon(painterResource(R.drawable.ic_arrow_back), null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
        ) {
            SectionHeader("General")
            BooleanPicker(
                value = state.settings.keepScreenOn,
                setValue = {
                    viewModel.state.value =
                        state.copy(settings = state.settings.copy(keepScreenOn = it))
                }
            ) {
                Text("Keep screen on")
            }
            BooleanPicker(
                value = state.settings.lockScreenOrientation,
                setValue = {
                    viewModel.state.value =
                        state.copy(settings = state.settings.copy(lockScreenOrientation = it))
                }
            ) {
                Text("Lock screen orientation to portrait")
            }
            BooleanPicker(
                value = state.settings.fullScreen,
                setValue = {
                    viewModel.state.value =
                        state.copy(settings = state.settings.copy(fullScreen = it))
                }
            ) {
                Text("Use fullscreen")
            }
            ContentRotationPicker(
                contentRotationSetting = state.settings.contentRotation,
                setContentRotationSetting = {
                    viewModel.state.value =
                        state.copy(settings = state.settings.copy(contentRotation = it))
                }
            )
            AutoSwitchSecondsPicker(
                autoSwitchSeconds = state.settings.autoSwitchSeconds,
                setAutoSwitchSeconds = {
                    viewModel.state.value =
                        state.copy(settings = state.settings.copy(autoSwitchSeconds = it))
                },
                enabled =
                    state.settings.verticalOrder == VerticalOrder.Auto ||
                        state.settings.contentRotation == ContentRotationSetting.Auto
            )
            BooleanPicker(
                value = state.settings.showClock,
                setValue = {
                    viewModel.state.value =
                        state.copy(settings = state.settings.copy(showClock = it))
                }
            ) {
                Text("Show clock")
            }
            SectionHeader("Main display")
            VerticalOrderPicker(
                verticalOrder = state.settings.verticalOrder,
                setVerticalOrder = {
                    viewModel.state.value =
                        state.copy(settings = state.settings.copy(verticalOrder = it))
                }
            )
            BooleanPicker(
                value = state.settings.autoHideCalls,
                setValue = {
                    viewModel.state.value =
                        state.copy(settings = state.settings.copy(autoHideCalls = it))
                }
            ) {
                Text("Hide calls when all are found")
            }
            Text(
                "${stringResource(R.string.app_name)} ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally).padding(24.dp)
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp)
    )
}

@Composable
private fun BooleanPicker(
    value: Boolean,
    setValue: (Boolean) -> Unit,
    label: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier =
            Modifier.fillMaxWidth()
                .clickable { setValue(!value) }
                .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        label()
        Spacer(modifier = Modifier.weight(1f))
        Checkbox(checked = value, onCheckedChange = { setValue(it) })
    }
}

@Composable
private fun RowScope.PickerCard(
    onClick: () -> Unit,
    selected: Boolean,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit
) {
    OutlinedCard(
        onClick = onClick,
        border =
            BorderStroke(
                width =
                    animateDpAsState(
                            if (selected) 4.dp else CardDefaults.outlinedCardBorder().width,
                            label = ""
                        )
                        .value,
                color =
                    if (selected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
            ),
        interactionSource = interactionSource,
        modifier = Modifier.weight(1f).fillMaxHeight(),
        content = content
    )
}

@Composable
private fun VerticalOrderPicker(
    verticalOrder: VerticalOrder,
    setVerticalOrder: (VerticalOrder) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text("Vertical ordering")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
        ) {
            PressableWithEmphasis {
                PickerCard(
                    onClick = { setVerticalOrder(VerticalOrder.Auto) },
                    selected = verticalOrder == VerticalOrder.Auto,
                    interactionSource = interactionSource
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            "Auto switch",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp).pressEmphasis()
                        )
                    }
                }
            }
            PressableWithEmphasis {
                PickerCard(
                    onClick = { setVerticalOrder(VerticalOrder.TrumpOnTop) },
                    selected = verticalOrder == VerticalOrder.TrumpOnTop,
                    interactionSource = interactionSource
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize().padding(16.dp).pressEmphasis()
                    ) {
                        Text(
                            "Trump",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.rotate(180f)
                        )
                        HorizontalDivider()
                        Text("Calls", textAlign = TextAlign.Center)
                    }
                }
            }
            PressableWithEmphasis {
                PickerCard(
                    onClick = { setVerticalOrder(VerticalOrder.CallsOnTop) },
                    selected = verticalOrder == VerticalOrder.CallsOnTop,
                    interactionSource = interactionSource
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize().padding(16.dp).pressEmphasis()
                    ) {
                        Text(
                            "Calls",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.rotate(180f)
                        )
                        HorizontalDivider()
                        Text("Trump", textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun ContentRotationPicker(
    contentRotationSetting: ContentRotationSetting,
    setContentRotationSetting: (ContentRotationSetting) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Text("Content rotation")
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)
        ) {
            PressableWithEmphasis {
                PickerCard(
                    onClick = { setContentRotationSetting(ContentRotationSetting.Auto) },
                    selected = contentRotationSetting == ContentRotationSetting.Auto,
                    interactionSource = interactionSource
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Text(
                            "Auto switch",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(8.dp).pressEmphasis()
                        )
                    }
                }
            }
            PressableWithEmphasis {
                PickerCard(
                    onClick = { setContentRotationSetting(ContentRotationSetting.Center) },
                    selected = contentRotationSetting == ContentRotationSetting.Center,
                    interactionSource = interactionSource
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize().padding(16.dp).pressEmphasis()
                    ) {
                        Text("Aa", textAlign = TextAlign.Center, modifier = Modifier.rotate(180f))
                        HorizontalDivider()
                        Text("Aa", textAlign = TextAlign.Center)
                    }
                }
            }
            PressableWithEmphasis {
                PickerCard(
                    onClick = { setContentRotationSetting(ContentRotationSetting.TopTowardsRight) },
                    selected = contentRotationSetting == ContentRotationSetting.TopTowardsRight,
                    interactionSource = interactionSource
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize().padding(16.dp).pressEmphasis()
                    ) {
                        Text("Aa", textAlign = TextAlign.Center, modifier = Modifier.rotate(-90f))
                        HorizontalDivider()
                        Text("Aa", textAlign = TextAlign.Center, modifier = Modifier.rotate(90f))
                    }
                }
            }
            PressableWithEmphasis {
                PickerCard(
                    onClick = {
                        setContentRotationSetting(ContentRotationSetting.BottomTowardsRight)
                    },
                    selected = contentRotationSetting == ContentRotationSetting.BottomTowardsRight,
                    interactionSource = interactionSource
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize().padding(16.dp).pressEmphasis()
                    ) {
                        Text("Aa", textAlign = TextAlign.Center, modifier = Modifier.rotate(90f))
                        HorizontalDivider()
                        Text("Aa", textAlign = TextAlign.Center, modifier = Modifier.rotate(-90f))
                    }
                }
            }
        }
    }
}

private val autoSwitchIntervals =
    mapOf(
        3 to "3 seconds",
        5 to "5 seconds",
        10 to "10 seconds",
        20 to "20 seconds",
        60 to "1 minute",
        300 to "5 minutes",
        1_577_847_600 to "50 years"
    )

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AutoSwitchSecondsPicker(
    autoSwitchSeconds: Int,
    setAutoSwitchSeconds: (Int) -> Unit,
    enabled: Boolean
) {
    val focusManager = LocalFocusManager.current
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            "Auto switch interval",
            color = LocalContentColor.current.copy(alpha = if (enabled) 1f else 0.5f)
        )
        var expanded by rememberSaveable { mutableStateOf(false) }
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = autoSwitchIntervals[autoSwitchSeconds] ?: "$autoSwitchSeconds seconds",
                onValueChange = {},
                enabled = enabled,
                readOnly = true,
                leadingIcon = { Icon(painterResource(R.drawable.ic_timer), null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier.menuAnchor()
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = {
                    expanded = false
                    focusManager.clearFocus()
                }
            ) {
                autoSwitchIntervals.forEach { (seconds, name) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            setAutoSwitchSeconds(seconds)
                            expanded = false
                            focusManager.clearFocus()
                        }
                    )
                }
            }
        }
    }
}

object SettingsPageTransitions : DestinationStyle.Animated {
    override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition() =
        slideInHorizontally { fullWidth ->
            fullWidth
        }

    override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition() =
        slideOutHorizontally { fullWidth ->
            fullWidth
        }

    override fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition() =
        slideInHorizontally { fullWidth ->
            fullWidth
        }

    override fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition() =
        slideOutHorizontally { fullWidth ->
            fullWidth
        }
}
