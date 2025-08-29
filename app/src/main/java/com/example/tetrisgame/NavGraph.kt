package com.example.tetrisgame

import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavGraph(
    navController : NavHostController = rememberNavController(),
    startDestination: String = "menu"
) {
    // Navigation graph implementation goes here
    NavHost(navController = navController, startDestination = startDestination) {
        composable("menu") {
            TetrisMenuScreen(navController = navController)
        }
        composable ("game") {
            TetrisGameScreen()
        }


    }
}