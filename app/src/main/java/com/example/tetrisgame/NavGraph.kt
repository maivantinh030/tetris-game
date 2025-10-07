package com.example.tetrisgame

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
    NavHost(navController = navController, startDestination = startDestination) {
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