package io.github.edwinchang24.shengjidisplay.pages

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.view.WindowManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationStyle
import io.github.edwinchang24.shengjidisplay.MainActivityViewModel
import io.github.edwinchang24.shengjidisplay.MainNavGraph
import io.github.edwinchang24.shengjidisplay.R
import io.github.edwinchang24.shengjidisplay.appDestination
import io.github.edwinchang24.shengjidisplay.components.CallsDisplay
import io.github.edwinchang24.shengjidisplay.components.PlayingCard
import io.github.edwinchang24.shengjidisplay.destinations.HomePageDestination
import io.github.edwinchang24.shengjidisplay.destinations.SettingsPageDestination
import io.github.edwinchang24.shengjidisplay.model.HorizontalOrientation
import io.github.edwinchang24.shengjidisplay.model.VerticalOrder
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

sealed interface DisplayContent {
    sealed interface Direction {
        data object Center : Direction
        data object Left : Direction
        data object Right : Direction

        fun opposite() = when (this) {
            Center -> Center
            Left -> Right
            Right -> Left
        }
    }

    data class Trump(val direction: Direction) : DisplayContent
    data class Calls(val direction: Direction) : DisplayContent
    data object None : DisplayContent
}

data class DisplaySettingsState(
    val autoHideCalls: Boolean,
    val verticalOrder: VerticalOrder,
    val perpendicularMode: Boolean,
    val horizontalOrientation: HorizontalOrientation,
    val autoSwitchSeconds: Int,
    val showCalls: Boolean
)

@SuppressLint("SourceLockedOrientationActivity")
@Destination(style = DisplayPageTransitions::class)
@MainNavGraph
@Composable
fun DisplayPage(
    navigator: DestinationsNavigator,
    mainActivityViewModel: MainActivityViewModel,
    displayViewModel: DisplayViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by mainActivityViewModel.state.collectAsStateWithLifecycle()
    val showCalls = !(state.settings.autoHideCalls && state.calls.all { it.found })
    val topContent by displayViewModel.topContent.collectAsStateWithLifecycle()
    val bottomContent by displayViewModel.bottomContent.collectAsStateWithLifecycle()
    var currentTimeMs by rememberSaveable { mutableLongStateOf(Clock.System.now().toEpochMilliseconds()) }
    LaunchedEffect(state.settings, showCalls) {
        displayViewModel.onPotentialUpdate(
            DisplaySettingsState(
                state.settings.autoHideCalls, state.settings.verticalOrder, state.settings.perpendicularMode,
                state.settings.horizontalOrientation, state.settings.autoSwitchSeconds, showCalls
            )
        )
    }
    LaunchedEffect(true) {
        currentTimeMs = Clock.System.now().toEpochMilliseconds()
        while (true) {
            delay(60_000 - (currentTimeMs % 60_000))
            currentTimeMs = Clock.System.now().toEpochMilliseconds()
        }
    }
    fun Context.activity(): Activity? = this as? Activity ?: (this as? ContextWrapper)?.baseContext?.activity()
    DisposableEffect(state.settings.keepScreenOn) {
        context.activity()?.window.let { window ->
            if (state.settings.keepScreenOn) window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            onDispose { window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) }
        }
    }
    DisposableEffect(state.settings.lockScreenOrientation) {
        context.activity().let { activity ->
            if (state.settings.lockScreenOrientation) {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
        }
    }
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
            ) {
                AnimatedContent(
                    topContent, transitionSpec = {
                    (fadeIn(tween(durationMillis = 1000)) + slideIntoContainer(towards = when (val ts = targetState) {
                        is DisplayContent.Trump -> when (ts.direction) {
                            DisplayContent.Direction.Center -> SlideDirection.Down
                            DisplayContent.Direction.Left -> SlideDirection.Right
                            DisplayContent.Direction.Right -> SlideDirection.Left
                        }

                        is DisplayContent.Calls -> when (ts.direction) {
                            DisplayContent.Direction.Center -> SlideDirection.Down
                            DisplayContent.Direction.Left -> SlideDirection.Right
                            DisplayContent.Direction.Right -> SlideDirection.Left
                        }

                        DisplayContent.None -> SlideDirection.Down
                    }, animationSpec = tween(durationMillis = 1000), initialOffset = { it / 3 }) togetherWith fadeOut(
                        tween(durationMillis = 1000)
                    ) + slideOutOfContainer(towards = when (val ins = initialState) {
                        is DisplayContent.Trump -> when (ins.direction) {
                            DisplayContent.Direction.Center -> SlideDirection.Up
                            DisplayContent.Direction.Left -> SlideDirection.Left
                            DisplayContent.Direction.Right -> SlideDirection.Right
                        }

                        is DisplayContent.Calls -> when (ins.direction) {
                            DisplayContent.Direction.Center -> SlideDirection.Up
                            DisplayContent.Direction.Left -> SlideDirection.Left
                            DisplayContent.Direction.Right -> SlideDirection.Right
                        }

                        DisplayContent.None -> SlideDirection.Up
                    }, animationSpec = tween(durationMillis = 1000), targetOffset = { it / 3 })).using(
                        SizeTransform(clip = false)
                    )
                }, label = "", modifier = Modifier.fillMaxSize()
                ) { tc ->
                    when (tc) {
                        is DisplayContent.Trump -> Box(
                            contentAlignment = Alignment.Center, modifier = Modifier
                            .fillMaxSize()
                            .rotate(
                                when (tc.direction) {
                                    DisplayContent.Direction.Center -> 180f
                                    DisplayContent.Direction.Left -> 90f
                                    DisplayContent.Direction.Right -> -90f
                                }
                            )
                        ) {
                            state.trump?.let {
                                PlayingCard(card = it, textStyle = LocalTextStyle.current.copy(fontSize = 84.sp))
                            } ?: Text("No trump selected")
                        }

                        is DisplayContent.Calls -> Box(
                            contentAlignment = Alignment.Center, modifier = Modifier
                            .fillMaxSize()
                            .rotate(
                                when (tc.direction) {
                                    DisplayContent.Direction.Center -> 180f
                                    DisplayContent.Direction.Left -> 90f
                                    DisplayContent.Direction.Right -> -90f
                                }
                            )
                        ) {
                            state.calls.takeIf { it.isNotEmpty() }?.let {
                                CallsDisplay(calls = state.calls, setFound = { index, found ->
                                    mainActivityViewModel.state.value = state.copy(calls = state.calls.toMutableList()
                                        .also { it[index] = it[index].copy(found = found) })
                                })
                            } ?: Text("No calls added")
                        }

                        DisplayContent.None -> Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
            AnimatedContent(
                when (topContent) {
                    is DisplayContent.Trump -> "Trump card"
                    is DisplayContent.Calls -> "Calls"
                    DisplayContent.None -> ""
                }, label = ""
            ) { label ->
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .rotate(180f)
                ) {
                    Text(label, style = MaterialTheme.typography.labelLarge)
                }
            }
            Row(
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(16.dp)
            ) {
                val timeFormat = LocalDateTime.Format {
                    amPmHour()
                    char(':')
                    minute()
                }
                val clockText =
                    Instant.fromEpochMilliseconds(currentTimeMs).toLocalDateTime(TimeZone.currentSystemDefault())
                        .format(timeFormat)
                if (state.settings.showClock) AnimatedContent(
                    // @formatter:off
                    targetState = state.settings.clockOrientation,
                    label = "",
                    transitionSpec = { fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut() },
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            mainActivityViewModel.state.value = state.copy(
                                settings = state.settings.copy(clockOrientation = !state.settings.clockOrientation)
                            )
                        }
                        .padding(8.dp)
                    // @formatter:on
                ) { targetOrientation ->
                    Text(
                        clockText, style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.rotate(if (targetOrientation) 0f else 180f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val autoPlay by displayViewModel.autoPlay.collectAsStateWithLifecycle()
                    AnimatedVisibility(
                        visible = state.settings.verticalOrder == VerticalOrder.Auto || (state.settings.perpendicularMode && state.settings.horizontalOrientation == HorizontalOrientation.Auto),
                        enter = fadeIn() + scaleIn(), exit = fadeOut() + scaleOut()
                    ) {
                        IconButton(onClick = { displayViewModel.autoPlay.value = !autoPlay }) {
                            if (autoPlay) Icon(painterResource(R.drawable.ic_pause), null)
                            else Icon(painterResource(R.drawable.ic_play_arrow), null)
                        }
                    }
                    IconButton(onClick = { navigator.navigate(SettingsPageDestination) }) {
                        Icon(painterResource(R.drawable.ic_settings), null)
                    }
                    IconButton(onClick = { navigator.navigateUp() }) {
                        Icon(painterResource(R.drawable.ic_close), null)
                    }
                }
                if (state.settings.showClock) AnimatedContent(
                    // @formatter:off
                    targetState = state.settings.clockOrientation,
                    label = "",
                    transitionSpec = { fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut() },
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable {
                            mainActivityViewModel.state.value = state.copy(
                                settings = state.settings.copy(clockOrientation = !state.settings.clockOrientation)
                            )
                        }
                        .padding(8.dp)
                    // @formatter:on
                ) { targetOrientation ->
                    Text(
                        clockText, style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.rotate(if (targetOrientation) 180f else 0f)
                    )
                }
            }
            AnimatedContent(
                when (bottomContent) {
                    is DisplayContent.Trump -> "Trump card"
                    is DisplayContent.Calls -> "Calls"
                    DisplayContent.None -> ""
                }, label = ""
            ) { label ->
                Box(
                    contentAlignment = Alignment.Center, modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                ) {
                    Text(label, style = MaterialTheme.typography.labelLarge)
                }
            }
            Box(
                contentAlignment = Alignment.Center, modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
            ) {
                AnimatedContent(
                    bottomContent, transitionSpec = {
                    (fadeIn(tween(durationMillis = 1000)) + slideIntoContainer(towards = when (val ts = targetState) {
                        is DisplayContent.Trump -> when (ts.direction) {
                            DisplayContent.Direction.Center -> SlideDirection.Up
                            DisplayContent.Direction.Left -> SlideDirection.Right
                            DisplayContent.Direction.Right -> SlideDirection.Left
                        }

                        is DisplayContent.Calls -> when (ts.direction) {
                            DisplayContent.Direction.Center -> SlideDirection.Up
                            DisplayContent.Direction.Left -> SlideDirection.Right
                            DisplayContent.Direction.Right -> SlideDirection.Left
                        }

                        DisplayContent.None -> SlideDirection.Up
                    }, animationSpec = tween(durationMillis = 1000), initialOffset = { it / 3 }) togetherWith fadeOut(
                        tween(durationMillis = 1000)
                    ) + slideOutOfContainer(towards = when (val ins = initialState) {
                        is DisplayContent.Trump -> when (ins.direction) {
                            DisplayContent.Direction.Center -> SlideDirection.Down
                            DisplayContent.Direction.Left -> SlideDirection.Left
                            DisplayContent.Direction.Right -> SlideDirection.Right
                        }

                        is DisplayContent.Calls -> when (ins.direction) {
                            DisplayContent.Direction.Center -> SlideDirection.Down
                            DisplayContent.Direction.Left -> SlideDirection.Left
                            DisplayContent.Direction.Right -> SlideDirection.Right
                        }

                        DisplayContent.None -> SlideDirection.Down
                    }, animationSpec = tween(durationMillis = 1000), targetOffset = { it / 3 })).using(
                        SizeTransform(clip = false)
                    )
                }, label = "", modifier = Modifier.fillMaxSize()
                ) { tc ->
                    when (tc) {
                        is DisplayContent.Trump -> Box(
                            contentAlignment = Alignment.Center, modifier = Modifier
                            .fillMaxSize()
                            .rotate(
                                when (tc.direction) {
                                    DisplayContent.Direction.Center -> 0f
                                    DisplayContent.Direction.Left -> 90f
                                    DisplayContent.Direction.Right -> -90f
                                }
                            )
                        ) {
                            state.trump?.let {
                                PlayingCard(card = it, textStyle = LocalTextStyle.current.copy(fontSize = 84.sp))
                            } ?: Text("No trump selected")
                        }

                        is DisplayContent.Calls -> Box(
                            contentAlignment = Alignment.Center, modifier = Modifier
                            .fillMaxSize()
                            .rotate(
                                when (tc.direction) {
                                    DisplayContent.Direction.Center -> 0f
                                    DisplayContent.Direction.Left -> 90f
                                    DisplayContent.Direction.Right -> -90f
                                }
                            )
                        ) {
                            state.calls.takeIf { it.isNotEmpty() }?.let {
                                CallsDisplay(calls = state.calls, setFound = { index, found ->
                                    mainActivityViewModel.state.value = state.copy(calls = state.calls.toMutableList()
                                        .also { it[index] = it[index].copy(found = found) })
                                })
                            } ?: Text("No calls added")
                        }

                        DisplayContent.None -> Box(modifier = Modifier.fillMaxSize())
                    }
                }
            }
        }
    }
}

object DisplayPageTransitions : DestinationStyle.Animated {
    override fun AnimatedContentTransitionScope<NavBackStackEntry>.enterTransition() =
        when (initialState.appDestination()) {
            HomePageDestination -> fadeIn(tween(delayMillis = 250))
            else -> fadeIn()
        }

    override fun AnimatedContentTransitionScope<NavBackStackEntry>.exitTransition() =
        when (targetState.appDestination()) {
            HomePageDestination -> fadeOut(tween(durationMillis = 250))
            else -> fadeOut()
        }

    override fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnterTransition() =
        when (initialState.appDestination()) {
            HomePageDestination -> fadeIn(tween(delayMillis = 250))
            else -> fadeIn()
        }

    override fun AnimatedContentTransitionScope<NavBackStackEntry>.popExitTransition() =
        when (targetState.appDestination()) {
            HomePageDestination -> fadeOut(tween(durationMillis = 250))
            else -> fadeOut()
        }
}
