package com.example.tetrisgame

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = "menu"
) {
    val gameManager = remember { TetrisManager() }
    // Navigation graph implementation goes here
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
                gameMode = GameMode.CLASSIC
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
    }
}