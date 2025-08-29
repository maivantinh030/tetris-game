package com.example.tetrisgame

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Navigation routes
object Routes {
    const val SPLASH = "splash"
    const val MENU = "menu"
    const val GAME = "game"
}

@Composable
fun TetrisNavigation(
    navController: NavHostController = rememberNavController(),
    onExitApp: () -> Unit = {}
) {
    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Routes.MENU) {
                        // Remove splash from back stack
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Routes.MENU) {
            TetrisMenuScreen(
                onPlayClick = {
                    navController.navigate(Routes.GAME)
                },
                onExitClick = {
                    onExitApp()
                }
            )
        }
        
        composable(Routes.GAME) {
            TetrisGameScreen(
                onPauseClick = {
                    navController.popBackStack()
                },
                onRestartClick = {
                    // Restart game by navigating to game again
                    navController.navigate(Routes.GAME) {
                        // Remove current game instance from back stack
                        popUpTo(Routes.GAME) { inclusive = true }
                    }
                }
            )
        }
    }
}