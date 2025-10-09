package com.example.tetrisgame

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "menu"
) {
    NavHost(navController = navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn(animationSpec = tween(1200)) +
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(1200))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(1200)) +
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(1200))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(1200)) +
                    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(1200))
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(1200)) +
                    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(1200))
        }

    ) {
        composable("menu") {
            TetrisMenuScreen(navController = navController)
        }
        composable("classic") {
            TetrisGameScreen(
                navController = navController,
                isInvisibleMode = false,
                gameMode = GameMode.CLASSIC
            )
        }
        composable("invisible") {
            TetrisGameScreen(
                navController = navController,
                isInvisibleMode = true,
                gameMode = GameMode.INVISIBLE // Sửa lại cho đúng mode INVISIBLE
            )
        }
        composable(
            "challenge/{level}"
        ) { backStackEntry ->
            val challengeLevel = backStackEntry.arguments?.getString("level")?.toIntOrNull() ?: 1
            TetrisGameScreen(
                navController = navController,
                isInvisibleMode = false,
                gameMode = GameMode.CHALLENGE,
                challengeLevel = challengeLevel
            )
        }
        // Màn hình continue, dùng isContinue = true
        composable("continue") {
            TetrisGameScreen(
                navController = navController,
                isContinue = true
            )
        }
        composable("settings") {
            SettingsScreen(navController = navController)
        }
    }
}