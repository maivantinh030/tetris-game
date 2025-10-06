package com.example.tetrisgame

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavGraph(
    navController : NavHostController = rememberNavController(),
    startDestination: String = "menu",
    onMusicToggle: () -> Unit = {},
    onSoundToggle: () -> Unit = {}
) { NavHost(navController = navController, startDestination = startDestination) {
        composable("menu") {
            TetrisMenuScreen(navController = navController)
        }
        composable ("game") {
            TetrisGameScreen(navController = navController)
        }
        composable("shooter"){
            val context = LocalContext.current
            val gridCols = 10
            val gridRows = 20
            val cellWidth = 30f
            val cellHeight = 30f
            val manager = remember { ShooterManager(gridCols, gridRows,context) }
            ShooterScreen(
                manager = manager,
                context = context,
                cellWidth = cellWidth,
                cellHeight = cellHeight,
                onMusicToggle = onMusicToggle,
                onSoundToggle = onSoundToggle,
                onExit = { navController.navigate("menu") },
                navController = navController

            )
        }
        composable("highscore"){
            HighscoreScreen(navController = navController, onExit = { navController.navigate("menu") })
        }

    }
}