// TetrisGameScreen.kt
package com.example.tetrisgame

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun TetrisGameScreen() {
    var grid by remember { mutableStateOf(Array(20) { Array(10) { 0 } }) }
    var currentPiece by remember { mutableStateOf<Tetromino?>(null) }
    var nextPiece by remember { mutableStateOf<Tetromino?>(null) }
    var score by remember { mutableStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var level by remember { mutableStateOf(1) }
    var isGameRunning by remember { mutableStateOf(false) }
    var line by remember { mutableStateOf(0) }
    var dropSpeed by remember { mutableStateOf(1000L) }

    // THÊM STATE ĐỂ FORCE RECOMPOSITION
    var gameUpdateTrigger by remember { mutableStateOf(0) }

    fun checkCollision(dx: Int = 0, dy: Int = 0, rotatedShape: Array<Array<Int>>? = null): Boolean {
        currentPiece?.let { piece ->
            val shape = rotatedShape ?: piece.getRotatedShape()
            val newX = piece.position.x + dx
            val newY = piece.position.y + dy
            for (i in shape.indices) {
                for (j in shape[i].indices) {
                    if (shape[i][j] == 1) {
                        val gridX = newX + j
                        val gridY = newY + i
                        if (gridX < 0 || gridX >= grid[0].size || gridY >= grid.size ||
                            (gridY >= 0 && grid[gridY][gridX] != 0)) {
                            return true
                        }
                    }
                }
            }
        }
        return false
    }

    fun spawnNewPiece() {
        if (nextPiece == null) {
            nextPiece = TetrominoFactory.createStandardTetromino()
        }
        currentPiece = nextPiece
        nextPiece = TetrominoFactory.createStandardTetromino()
        gameUpdateTrigger++ // Force recomposition

        if (checkCollision()) {
            isGameRunning = false
            isPaused = true
        }
    }

    fun placePiece() {
        currentPiece?.let { piece ->
            val shape = piece.getRotatedShape()
            val posX = piece.position.x
            val posY = piece.position.y
            val newGrid = grid.map { it.clone() }.toTypedArray()

            for (i in shape.indices) {
                for (j in shape[i].indices) {
                    if (shape[i][j] == 1 && posY + i >= 0) {
                        newGrid[posY + i][posX + j] = piece.color
                    }
                }
            }
            grid = newGrid // Tạo grid mới
            currentPiece = null
            gameUpdateTrigger++ // Force recomposition
        }
    }

    fun clearLines() {
        var linesCleared = 0
        val newGrid = grid.map { row -> row.clone() }.toTypedArray()

        for (i in grid.indices.reversed()) {
            if (grid[i].all { it != 0 }) {
                linesCleared++
                for (j in i downTo 1) {
                    newGrid[j] = newGrid[j - 1].clone()
                }
                newGrid[0] = Array(10) { 0 }
            }
        }

        if (linesCleared > 0) {
            grid = newGrid
            line += linesCleared
            score += when (linesCleared) {
                1 -> 100 * level
                2 -> 300 * level
                3 -> 500 * level
                4 -> 800 * level
                else -> 0
            }
            level = 1 + line / 10
            dropSpeed = (1000L - (level - 1) * 100L).coerceAtLeast(100L)
            gameUpdateTrigger++ // Force recomposition
        }
    }

    // SỬA LỖI CHÍNH - MOVPIECE
    fun movePiece(dx: Int, dy: Int) {
        currentPiece?.let { piece ->
            if (!checkCollision(dx, dy)) {
                // TẠO COPY MỚI THAY VÌ MUTATE
                currentPiece = piece.copy(
                    position = Position(piece.position.x + dx, piece.position.y + dy)
                )
                gameUpdateTrigger++ // Force recomposition
            } else if (dy > 0) {
                placePiece()
                clearLines()
                spawnNewPiece()
            }
        }
    }

    // SỬA LỖI CHÍNH - ROTATEPIECE
    fun rotatePiece() {
        currentPiece?.let { piece ->
            val newRotation = (piece.rotation + 1) % 4
            // TẠO PIECE TẠM ĐỂ CHECK COLLISION
            val tempPiece = piece.copy(rotation = newRotation)
            val rotatedShape = tempPiece.getRotatedShape()

            if (!checkCollision(0, 0, rotatedShape)) {
                currentPiece = tempPiece
                gameUpdateTrigger++ // Force recomposition
            }
        }
    }

    fun resetGame() {
        grid = Array(20) { Array(10) { 0 } }
        isPaused = false
        score = 0
        level = 1
        line = 0
        dropSpeed = 1000L
        currentPiece = null
        nextPiece = null
        gameUpdateTrigger++ // Force recomposition
    }

    // Game Loop
    LaunchedEffect(isGameRunning, isPaused) {
        if (!isGameRunning && !isPaused) {
            resetGame()
            spawnNewPiece()
            isGameRunning = true
        }
        while (isGameRunning && !isPaused) {
            delay(dropSpeed)
            if (isGameRunning && !isPaused) {
                movePiece(0, 1)
            }
        }
    }

    // UI PHẦN - THÊM KEY ĐỂ FORCE RECOMPOSITION
    key(gameUpdateTrigger) {
        Box(modifier = Modifier.fillMaxSize()){
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
                        onClick = { isPaused = true },
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
                    InforBox("Score", score.toString())
                    InforBox("Level", level.toString())
                    NextBox(nextPiece)
                }

                gameGrid(
                    grid = grid,
                    currentPiece = currentPiece,
                    gameUpdateTrigger = gameUpdateTrigger,
                    onSwipe = { direction ->
                        when (direction) {
                            "LEFT" -> movePiece(-1, 0)
                            "RIGHT" -> movePiece(1, 0)
                            "DOWN" -> movePiece(0, 1)
                        }
                    },
                    onTap = { rotatePiece() }
                )
            }

            if(isPaused){
                PauseMenu(
                    onResume = {
                        isPaused = false
                        if (!isGameRunning) {
                            isGameRunning = true
                        }
                    },
                    onRestart = {
                        isGameRunning = false
                        isPaused = false
                        resetGame()
                    },
                    onExit = { /* Xử lý thoát khỏi trò chơi */ }
                )
            }
        }
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
fun NextBox(nextPiece: Tetromino? = null){
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

        // Hiển thị next piece thực tế
        nextPiece?.let { piece ->
            val shape = piece.getRotatedShape()
            val color = piece.getColor()

            Column {
                for (i in shape.indices) {
                    Row {
                        for (j in shape[i].indices) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(
                                        if (shape[i][j] == 1) color else Color.Transparent
                                    )
                            )
                            if (j < shape[i].size - 1) {
                                Spacer(modifier = Modifier.width(2.dp))
                            }
                        }
                    }
                    if (i < shape.size - 1) {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        } ?: run {
            // Fallback display khi không có nextPiece
            Row {
                Box(
                    modifier = Modifier
                        .size(25.dp)
                        .background(Color(0xFF87CEEB))
                )
                Spacer(modifier = Modifier.width(2.dp))
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
                        .background(Color(0xFF87CEEB))
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
}

@Composable
fun PauseMenu(
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onExit: () -> Unit
){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
    ){
        Card(modifier= Modifier
            .align(Alignment.Center))
        {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ){
                Text(
                    text = "Game Paused",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = onResume,
                    modifier = Modifier
                        .width(200.dp)
                        .height(40.dp)
                ){
                    Text(text = "Resume", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .width(200.dp)
                        .height(40.dp)
                ){
                    Text(text = "Restart", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onExit,
                    modifier = Modifier
                        .width(200.dp)
                        .height(40.dp)
                )
                {
                    Text(text = "Exit", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun gameGrid(
    grid: Array<Array<Int>>,
    currentPiece: Tetromino?,
    gameUpdateTrigger: Int,
    onSwipe: (String) -> Unit,
    onTap: () -> Unit
) {
    key(gameUpdateTrigger, currentPiece?.position, currentPiece?.rotation) {
        Box(
            modifier = Modifier
                .size(300.dp, 600.dp)
                .background(Color.White)
                .border(2.dp, Color.Black)
                .pointerInput(Unit) {
                    var totalDrag = Offset.Zero
                    var hasTriggeredAction = false

                    detectDragGestures(
                        onDragStart = {
                            totalDrag = Offset.Zero
                            hasTriggeredAction = false
                        },
                        onDragEnd = {
                            // Reset khi kết thúc drag
                            totalDrag = Offset.Zero
                            hasTriggeredAction = false
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        totalDrag += dragAmount

                        // Chỉ trigger action một lần cho mỗi gesture
                        if (!hasTriggeredAction) {
                            val threshold = 80f // Tăng threshold để tránh trigger nhầm

                            when {
                                totalDrag.x < -threshold && abs(totalDrag.x) > abs(totalDrag.y) -> {
                                    onSwipe("LEFT")
                                    hasTriggeredAction = true
                                }
                                totalDrag.x > threshold && abs(totalDrag.x) > abs(totalDrag.y) -> {
                                    onSwipe("RIGHT")
                                    hasTriggeredAction = true
                                }
                                totalDrag.y > threshold && abs(totalDrag.y) > abs(totalDrag.x) -> {
                                    onSwipe("DOWN")
                                    hasTriggeredAction = true
                                }
                            }
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { onTap() }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = 30.dp.toPx()
                val gridWidth = 10
                val gridHeight = 20

                // Vẽ lưới nền
                drawIntoCanvas { canvas ->
                    for (i in 0..gridWidth) {
                        canvas.nativeCanvas.drawLine(
                            i * cellSize, 0f, i * cellSize, gridHeight * cellSize,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.LTGRAY
                                strokeWidth = 1f
                            }
                        )
                    }
                    for (i in 0..gridHeight) {
                        canvas.nativeCanvas.drawLine(
                            0f, i * cellSize, gridWidth * cellSize, i * cellSize,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.LTGRAY
                                strokeWidth = 1f
                            }
                        )
                    }
                }

                // Vẽ các khối đã đặt
                for (i in grid.indices) {
                    for (j in grid[i].indices) {
                        if (grid[i][j] != 0) {
                            drawRect(
                                color = TetrominoColors.getColorByCode(grid[i][j]),
                                topLeft = Offset(j * cellSize, i * cellSize),
                                size = Size(cellSize, cellSize)
                            )
                        }
                    }
                }

                // Vẽ currentPiece
                currentPiece?.let { piece ->
                    val shape = piece.getRotatedShape()
                    val posX = piece.position.x
                    val posY = piece.position.y
                    for (i in shape.indices) {
                        for (j in shape[i].indices) {
                            if (shape[i][j] == 1 && posY + i >= 0 &&
                                posY + i < gridHeight && posX + j >= 0 && posX + j < gridWidth) {
                                drawRect(
                                    color = piece.getColor(),
                                    topLeft = Offset((posX + j) * cellSize, (posY + i) * cellSize),
                                    size = Size(cellSize, cellSize)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PauseMenuPreview(showBackground: Boolean = true){
    MaterialTheme {
        PauseMenu(onResume = {}, onRestart = {}, onExit = {})
    }
}

@Preview
@Composable
fun TetrisGameScreenPreview(showBackground: Boolean = true) {
    MaterialTheme {
        TetrisGameScreen()
    }
}