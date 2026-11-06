package com.example.railswitch.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.railswitch.audio.HapticsManager
import com.example.railswitch.audio.SoundManager
import com.example.railswitch.data.PreferencesManager
import com.example.railswitch.ui.screens.about.AboutScreen
import com.example.railswitch.ui.screens.game.GameScreen
import com.example.railswitch.ui.screens.game.GameViewModel
import com.example.railswitch.ui.screens.home.HomeScreen
import com.example.railswitch.ui.screens.result.ResultScreen
import com.example.railswitch.ui.screens.settings.SettingsScreen
import com.example.railswitch.ui.screens.splash.SplashScreen

object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val GAME = "game"
    const val RESULT = "result/{score}/{avoided}/{streak}/{isNewBest}/{bestScore}"
    const val SETTINGS = "settings"
    const val ABOUT = "about"

    fun resultRoute(
        score: Int,
        avoided: Int,
        streak: Int,
        isNewBest: Boolean,
        bestScore: Int
    ): String = "result/$score/$avoided/$streak/$isNewBest/$bestScore"
}

@Composable
fun RailSwitchNavHost(
    preferencesManager: PreferencesManager,
    soundManager: SoundManager,
    hapticsManager: HapticsManager
) {
    val navController = rememberNavController()
    val settings by preferencesManager.settings.collectAsState()
    val stats by preferencesManager.stats.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() }
    ) {
        // 1. Splash Screen
        composable(Routes.SPLASH) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // 2. Home Screen
        composable(Routes.HOME) {
            HomeScreen(
                settings = settings,
                stats = stats,
                onDifficultyChanged = { preferencesManager.updateDifficulty(it) },
                onPlayClicked = { navController.navigate(Routes.GAME) },
                onSettingsClicked = { navController.navigate(Routes.SETTINGS) },
                onAboutClicked = { navController.navigate(Routes.ABOUT) }
            )
        }

        // 3. Game Screen
        composable(Routes.GAME) {
            val gameViewModel = remember {
                GameViewModel(
                    preferencesManager = preferencesManager,
                    soundManager = soundManager,
                    hapticsManager = hapticsManager,
                    initialDifficulty = settings.difficulty
                )
            }

            GameScreen(
                viewModel = gameViewModel,
                onNavigateToResult = { score, avoided, streak, isNewBest, bestScore ->
                    navController.navigate(
                        Routes.resultRoute(score, avoided, streak, isNewBest, bestScore)
                    ) {
                        popUpTo(Routes.GAME) { inclusive = true }
                    }
                },
                onQuitToMenu = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = false }
                    }
                }
            )
        }

        // 4. Result Screen
        composable(
            route = Routes.RESULT,
            arguments = listOf(
                navArgument("score") { type = NavType.IntType },
                navArgument("avoided") { type = NavType.IntType },
                navArgument("streak") { type = NavType.IntType },
                navArgument("isNewBest") { type = NavType.BoolType },
                navArgument("bestScore") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val score = backStackEntry.arguments?.getInt("score") ?: 0
            val avoided = backStackEntry.arguments?.getInt("avoided") ?: 0
            val streak = backStackEntry.arguments?.getInt("streak") ?: 0
            val isNewBest = backStackEntry.arguments?.getBoolean("isNewBest") ?: false
            val bestScore = backStackEntry.arguments?.getInt("bestScore") ?: 0

            ResultScreen(
                finalScore = score,
                blocksAvoided = avoided,
                highestStreak = streak,
                isNewBest = isNewBest,
                bestScore = bestScore,
                onPlayAgain = {
                    navController.navigate(Routes.GAME) {
                        popUpTo(Routes.RESULT) { inclusive = true }
                    }
                },
                onHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // 5. Settings Screen
        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = settings,
                onSoundChanged = { preferencesManager.updateSound(it) },
                onHapticsChanged = { preferencesManager.updateHaptics(it) },
                onDifficultyChanged = { preferencesManager.updateDifficulty(it) },
                onThemeChanged = { preferencesManager.updateTheme(it) },
                onResetStats = { preferencesManager.resetStatistics() },
                onBackClicked = { navController.popBackStack() }
            )
        }

        // 6. About Screen
        composable(Routes.ABOUT) {
            AboutScreen(
                onBackClicked = { navController.popBackStack() }
            )
        }
    }
}
