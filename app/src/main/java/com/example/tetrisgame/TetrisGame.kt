package com.example.tetrisgame

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.*
import kotlin.random.Random


@Composable
fun TetrisGameScreen() {
    var grid by remember { mutableStateOf(Array(20) { Array(10) { 0 } }) }
    fun addTetromino() {
        grid = grid.copyOf()
        grid[0][3] = 1
        grid[0][4] = 1
        grid[0][5] = 1
        grid[1][4] = 1
    }
    fun moveDown() {
        grid = grid.copyOf()
        for (i in grid.size - 1 downTo 1) {
            for (j in grid[0].indices) {
                if (grid[i][j] == 0 && grid[i - 1][j] == 1) {
                    grid[i][j] = 1
                    grid[i - 1][j] = 0
                }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()){
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ){
            Canvas(
                modifier = Modifier
                    .padding(16.dp)
                    .size(width = 240.dp, height = 667.dp) // Kích thước cố định cho canvas
            ){
                val cellSize = 29.dp.toPx()
                val gridWidth = 8
                val gridHeight = 23
                drawIntoCanvas {canvas->
                    for(i in 0..gridWidth){
                        canvas.nativeCanvas.drawLine(
                            i*cellSize,
                            0f,
                            i*cellSize,
                            gridHeight * cellSize,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.GRAY
                                strokeWidth = 3f
                            }
                        )
                        for(i in 0..gridHeight){
                            canvas.nativeCanvas.drawLine(
                                0f,
                                i*cellSize,
                                gridWidth * cellSize,
                                i*cellSize,
                                android.graphics.Paint().apply {
                                    color = android.graphics.Color.GRAY
                                    strokeWidth = 3f
                                }
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.Top
            ){
                Button(
                    onClick = {  },
                    modifier = Modifier
                        .padding(8.dp)
                        .width(120.dp)
                        .height(50.dp)
                ) {
                    Text(text = "X", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
                
            }
        }

    }

}

@Preview(showBackground = true)
@Composable
fun TetrisGameScreenPreview() {
    MaterialTheme {
        TetrisGameScreen()
    }
}