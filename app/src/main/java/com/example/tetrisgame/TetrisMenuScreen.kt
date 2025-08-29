package com.example.tetrisgame

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun TetrisMenuScreen(
    onPlayClick: () -> Unit = {},
    onExitClick: () -> Unit = {}
) {
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
        // Title positioned higher
        Text(
            text = "TETRIS GAME",
            fontSize = 48.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FFFF),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onPlayClick,
                modifier = Modifier
                    .width(200.dp)
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FFFF),
                    contentColor = Color(0xFF1A1A2E)
                )
            ) {
                Text("Play", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {},
                modifier = Modifier
                    .width(200.dp)
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FFFF),
                    contentColor = Color(0xFF1A1A2E)
                )
            ) {
                Text("Settings", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onExitClick,
                modifier = Modifier
                    .width(200.dp)
                    .height(45.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00FFFF),
                    contentColor = Color(0xFF1A1A2E)
                )
            ) {
                Text("Exit", fontWeight = FontWeight.Bold)
            }
        }
        Text(
            text = "nhóm F game android",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TetrisMenuScreenPreview() {
    MaterialTheme {
        TetrisMenuScreen()
    }
}