package com.antigravity.sudoku

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.antigravity.sudoku.data.feedback.HapticAndSoundManager
import com.antigravity.sudoku.data.local.GameStorage
import com.antigravity.sudoku.ui.daily.DailyChallengeScreen
import com.antigravity.sudoku.ui.game.GameScreen
import com.antigravity.sudoku.ui.game.GameViewModel
import com.antigravity.sudoku.ui.home.HomeScreen
import com.antigravity.sudoku.ui.settings.SettingsScreen
import com.antigravity.sudoku.ui.stats.StatsScreen
import com.antigravity.sudoku.ui.theme.SudokuTheme

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val storage = GameStorage(applicationContext)
        val feedbackManager = gameViewModel.feedbackManager

        setContent {
            val userSettings = storage.getSettings()
            val isDark = when (userSettings.darkMode) {
                true -> true
                false -> false
                null -> isSystemInDarkTheme()
            }

            SudokuTheme(darkTheme = isDark) {
                SudokuNavGraph(
                    gameViewModel = gameViewModel,
                    storage = storage,
                    feedbackManager = feedbackManager
                )
            }
        }
    }
}

@Composable
fun SudokuNavGraph(
    gameViewModel: GameViewModel,
    storage: GameStorage,
    feedbackManager: HapticAndSoundManager
) {
    val navController = rememberNavController()

    // 监听应用进入后台自动暂停对局计时
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) {
                gameViewModel.stopTimer()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            val savedGame = remember { storage.loadActiveGame() }
            HomeScreen(
                hasSavedGame = savedGame != null,
                savedGameDifficulty = savedGame?.difficulty,
                savedGameTime = savedGame?.elapsedTimeSeconds ?: 0L,
                onResumeGameClick = {
                    if (gameViewModel.resumeActiveGame()) {
                        navController.navigate("game")
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

        composable("daily") {
            val completedDates = remember { storage.getCompletedDailyDates() }
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
                storage = storage,
                feedbackManager = feedbackManager,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
