package com.antigravity.sudoku

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.antigravity.sudoku.data.local.GameStorage
import com.antigravity.sudoku.ui.daily.DailyChallengeScreen
import com.antigravity.sudoku.ui.game.GameScreen
import com.antigravity.sudoku.ui.game.GameViewModel
import com.antigravity.sudoku.ui.home.HomeScreen
import com.antigravity.sudoku.ui.settings.SettingsScreen
import com.antigravity.sudoku.ui.stats.StatsScreen
import com.antigravity.sudoku.ui.theme.SudokuTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val storage = GameStorage(applicationContext)

        setContent {
            val uiState by gameViewModel.uiState.collectAsState()
            val userSettings = uiState.settings

            LaunchedEffect(userSettings.portraitLocked) {
                requestedOrientation = if (userSettings.portraitLocked) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                } else {
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
                }
            }

            val isDark = userSettings.darkMode ?: isSystemInDarkTheme()

            SudokuTheme(darkTheme = isDark) {
                SudokuNavGraph(
                    gameViewModel = gameViewModel,
                    storage = storage
                )
            }
        }
    }
}

@Composable
fun SudokuNavGraph(
    gameViewModel: GameViewModel,
    storage: GameStorage
) {
    val navController = rememberNavController()
    val uiState by gameViewModel.uiState.collectAsState()

    // 监听应用进入前后台，自动暂停对局/恢复计时
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> gameViewModel.onAppPaused()
                Lifecycle.Event.ON_RESUME -> gameViewModel.onAppResumed()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") { backStackEntry ->
            var savedGame by remember { mutableStateOf(storage.loadActiveGame()) }
            var completedDates by remember { mutableStateOf(storage.getCompletedDailyDates()) }

            DisposableEffect(backStackEntry) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        savedGame = storage.loadActiveGame()
                        completedDates = storage.getCompletedDailyDates()
                    }
                }
                backStackEntry.lifecycle.addObserver(observer)
                onDispose {
                    backStackEntry.lifecycle.removeObserver(observer)
                }
            }

            val todayDateStr = remember {
                SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
            }

            HomeScreen(
                hasSavedGame = savedGame != null,
                savedGameDifficulty = savedGame?.difficulty,
                savedGameTime = savedGame?.elapsedTimeSeconds ?: 0L,
                isTodayDailyCompleted = todayDateStr in completedDates,
                onResumeGameClick = {
                    if (gameViewModel.resumeActiveGame()) {
                        navController.navigate("game")
                    } else {
                        savedGame = null
                    }
                },
                onStartNewGameClick = { difficulty ->
                    gameViewModel.startNewGame(difficulty)
                    navController.navigate("game")
                },
                onDailyChallengeClick = {
                    navController.navigate("daily")
                },
                onStatsClick = {
                    navController.navigate("stats")
                },
                onSettingsClick = {
                    navController.navigate("settings")
                }
            )
        }

        composable("game") {
            GameScreen(
                viewModel = gameViewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("daily") { backStackEntry ->
            var completedDates by remember { mutableStateOf(storage.getCompletedDailyDates()) }

            DisposableEffect(backStackEntry) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) {
                        completedDates = storage.getCompletedDailyDates()
                    }
                }
                backStackEntry.lifecycle.addObserver(observer)
                onDispose {
                    backStackEntry.lifecycle.removeObserver(observer)
                }
            }

            DailyChallengeScreen(
                completedDates = completedDates,
                onSelectDate = { dateString ->
                    gameViewModel.startDailyChallenge(dateString)
                    navController.navigate("game")
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("stats") {
            StatsScreen(
                storage = storage,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("settings") {
            SettingsScreen(
                settings = uiState.settings,
                onUpdateSettings = { gameViewModel.updateSettings(it) },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
