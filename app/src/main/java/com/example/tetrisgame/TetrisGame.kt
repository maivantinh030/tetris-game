package com.example.tetrisgame

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly
    ){

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = { /* Handle back action */ },
                modifier = Modifier
                .size(50.dp)
                    .background(Color.Black, shape = RoundedCornerShape(8.dp))
            ){
                Icon(
                    painter = painterResource(id = R.drawable.pause),
                    contentDescription = "Pause",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ){

            InforBox("Score", "100")
            InforBox("Level", "1")
            NextBox()
        }
        gameGrid()
    }

}
@Composable
fun InforBox(text:String, value: String){
    Column(modifier = Modifier
        .background(Color(0xFFADD8E6))
        .border(1.dp,Color.Black)
        .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun NextBox(){
    Column(
        modifier = Modifier
            .background(Color.White)
            .border(1.dp, Color.Black)
            .width(150.dp)
            .height(130.dp)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "Next", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .background(Color(0xFF87CEEB)) // Xanh nhạt hơn cho khối
            )
            Spacer(modifier = Modifier.width(4.dp))
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .background(Color(0xFF87CEEB))
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Row {
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .background(Color.Transparent)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .background(Color(0xFF87CEEB))
            )
        }
    }
}

@Composable
fun gameGrid(){
    Box(
        modifier = Modifier
            .size(300.dp, 600.dp)
            .background(Color.LightGray)
            .border(2.dp, Color.Black)
    ){
        Canvas(modifier = Modifier.fillMaxSize()){
            val cellSize = 30.dp.toPx()
            val gridWidth = 10
            val gridHeight = 20
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
    }
}


//@Preview
//@Composable
//fun gameGridPreview(showBackground: Boolean = true) {
//    MaterialTheme {
//        gameGrid()
//    }
//}
//@Preview
//@Composable
//fun InforBoxPreview(showBackground: Boolean = true) {
//    MaterialTheme {
//        InforBox("Score","100" )
//    }
//}

@Preview
@Composable
fun TetrisGameScreenPreview(showBackground: Boolean = true) {
    MaterialTheme {
        TetrisGameScreen()
    }
}