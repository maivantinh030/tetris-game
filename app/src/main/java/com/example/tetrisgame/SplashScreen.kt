package com.example.tetrisgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    // Auto-navigate to menu after 3 seconds
    LaunchedEffect(Unit) {
        delay(3000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A1A2E),
                        Color(0xFF16213E),
                        Color(0xFF0F3460)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TETRIS",
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FFFF)
            )
            Text(
                text = "GAME",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FFFF)
            )
            
            CircularProgressIndicator(
                modifier = Modifier
                    .size(40.dp)
                    .padding(top = 32.dp),
                color = Color(0xFF00FFFF),
                strokeWidth = 4.dp
            )
        }
        
        Text(
            text = "nhóm F game android",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}