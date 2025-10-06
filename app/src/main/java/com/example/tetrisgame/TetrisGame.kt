// TetrisGameScreen.kt
package com.example.tetrisgame

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.compareTo
import kotlin.inc


@Composable
fun TetrisGameScreen(
    navController: NavController?=null,
    isInvisibleMode: Boolean = false
) {
    var grid by remember { mutableStateOf(Array(20) { Array(10) { 0 } }) }
    var currentPiece by remember { mutableStateOf<Tetromino?>(null) }
    var nextPiece by remember { mutableStateOf<Tetromino?>(null) }
    var score by remember { mutableStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var level by remember { mutableStateOf(1) }
    var isGameRunning by remember { mutableStateOf(true) }
    var line by remember { mutableStateOf(0) }
    var dropSpeed by remember { mutableStateOf(1000L) }
    var gameUpdateTrigger by remember { mutableStateOf(0) }
    // Trạng thái clearing để UI biết đang chạy animation
    var isClearing by remember { mutableStateOf(false) }
    var pendingClearRows by remember { mutableStateOf<List<Int>>(emptyList()) }
    var showComboEffect by remember { mutableStateOf(false) }
    var linesCleared  by remember { mutableStateOf(0) }
    var showScoreEffect by remember{mutableStateOf(false)}
    var comboCount by remember { mutableStateOf(0) }
    var lastClearWasCombo by remember { mutableStateOf(false) }

    var blockOpacity by remember { mutableStateOf(Array(20) { Array(10) { 1f } }) }
    var opacityTrigger by remember { mutableStateOf(0) }

    var flashVisible by remember { mutableStateOf(false) }


    // 1) Tìm các hàng đầy (KHÔNG xóa ngay)
    fun findFullRows(): List<Int> {
        val rows = mutableListOf<Int>()
        for (r in grid.indices) {
            if (grid[r].all { it != 0 }) rows += r
        }

        if(rows.isNotEmpty()){
            if (lastClearWasCombo) {
                comboCount++
            } else {
                comboCount = 1
            }
            lastClearWasCombo = true

            if (comboCount >= 2) {
                showComboEffect = true
            }
        }
        else{
            lastClearWasCombo = false
            comboCount = 0
        }
        return rows
    }
    // 2) Áp dụng xóa các hàng sau khi animation kết thúc (LOGIC GIỮ NGUYÊN như clearLines cũ)
    fun applyClearedRows(rowsToClear: List<Int>) {
        if (rowsToClear.isEmpty()) return
        val newGrid = grid.map { it.clone() }.toTypedArray()
        if (isInvisibleMode) {
            blockOpacity = Array(20) { Array(10) { 1f } }
        }
        var writeIndex = grid.size - 1
        for (readIndex in grid.size - 1 downTo 0) {
            if (readIndex in rowsToClear) {
                linesCleared++
            } else {
                newGrid[writeIndex] = grid[readIndex].clone()
                writeIndex--
            }
        }
        // Điền các dòng trống ở đầu lưới (giữ nguyên số cột 10 như bạn đang dùng)
        for (i in 0 until linesCleared) {
            newGrid[i] = Array(10) { 0 }
        }
        // Cập nhật trạng thái/điểm với combo system
        if (linesCleared > 0) {
            grid = newGrid
            line += linesCleared
            // Calculate base score
            val baseScore = when (linesCleared) {
                1 -> 100 * level * comboCount
                2 -> 300 * level * comboCount
                3 -> 500 * level * comboCount
                4 -> 800 * level * comboCount
                else -> 0
            }
            showScoreEffect = true
            score += baseScore
            level = 1 + line / 10
            dropSpeed = (1000L - (level - 1) * 100L).coerceAtLeast(100L)
            if (isInvisibleMode) opacityTrigger++
            gameUpdateTrigger++
        }


    }

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
        nextPiece = TetrominoFactory.createStandardTetromino(lastType = currentPiece?.type)
        if (checkCollision()) {
            isGameRunning = false
            isGameOver = true
        }
    }

    fun placePiece() {
        currentPiece?.let { piece ->
            val shape = piece.getRotatedShape()
            val posX = piece.position.x
            val posY = piece.position.y
            val newGrid = grid.map { it.clone() }.toTypedArray()
            val newOpacity = blockOpacity.map { it.clone() }.toTypedArray()

            for (i in shape.indices) {
                for (j in shape[i].indices) {
                    if (shape[i][j] == 1 && posY + i >= 0) {
                        newGrid[posY + i][posX + j] = piece.color
                        newOpacity[posY + i][posX + j] = 1f
                    }
                }
            }
            grid = newGrid
            blockOpacity = newOpacity
            currentPiece = null
            if (isInvisibleMode) opacityTrigger++ // CHỈ FADE KHI BẬT MODE
        }
    }

    fun movePiece(dx: Int, dy: Int) {
        currentPiece?.let { piece ->
            if (!checkCollision(dx, dy)) {
                currentPiece = piece.copy(
                    position = Position(piece.position.x + dx, piece.position.y + dy)
                )
            } else if (dy > 0) {
                placePiece()
                val rows = findFullRows()
                if (rows.isNotEmpty()) {
                    pendingClearRows = rows
                    isClearing = true // UI sẽ render hiệu ứng
                } else {
                    // Reset combo when no lines cleared
                    spawnNewPiece()
                }
            }
        }
    }

    fun fastDropPiece(){
        currentPiece?.let{piece ->
            var newY = piece.position.y
            val maxY = grid.size
            while(newY < maxY &&!checkCollision(dx=0,dy = 1,
                    rotatedShape = piece.getRotatedShape())){
                newY+=1;
                currentPiece = piece.copy(position = Position(piece.position.x, newY))
            }
            placePiece()
            val rows = findFullRows()
            if (rows.isNotEmpty()) {
                pendingClearRows = rows
                isClearing = true // UI sẽ render hiệu ứng
            } else {

                spawnNewPiece()
            }

        }
    }

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
        blockOpacity = Array(20) { Array(10) { 1f } } // THÊM DÒNG NÀY
        isPaused = false
        score = 0
        level = 1
        line = 0
        dropSpeed = 1000L
        currentPiece = null
        nextPiece = null
        showComboEffect = false
        opacityTrigger = 0 // THÊM DÒNG NÀY
        gameUpdateTrigger++
        comboCount = 0
        nextPiece = TetrominoFactory.createStandardTetromino()
    }
    // Game Loop
    LaunchedEffect(isGameRunning, isPaused, isGameOver, isClearing) {
        if (isGameRunning && !isPaused && !isGameOver) {

            if (currentPiece == null&& !isClearing) {
                spawnNewPiece()
            }

            // Game loop chính
            while (isGameRunning && !isPaused && !isGameOver && !isClearing) {
                delay(dropSpeed)
                movePiece(0, 1) // Di chuyển piece xuống
            }
        }
    }
    // INVISIBLE MODE - Ẩn hoàn toàn sau 3 giây
    LaunchedEffect(opacityTrigger) {
        if (opacityTrigger > 0 && isInvisibleMode) {
            delay(3000) // Chờ 3 giây

            // Set opacity = 0 cho tất cả blocks (ẩn hoàn toàn, không fade)
            val newOpacity = Array(20) { Array(10) { 0f } }
            for (i in grid.indices) {
                for (j in grid[i].indices) {
                    if (grid[i][j] != 0) {
                        newOpacity[i][j] = 0f
                    }
                }
            }
            blockOpacity = newOpacity
            gameUpdateTrigger++
            launch {
                while (isGameRunning && isInvisibleMode) {
                    delay(10000L)  // Flash mỗi 10 giây
                    flashVisible = true
                    delay(500L)  // Visible 0.5 giây
                    flashVisible = false
                }
            }
        }
    }

    // UI PHẦN - THÊM KEY ĐỂ FORCE RECOMPOSITION
        Box(modifier = Modifier.fillMaxSize()){
            Image(
                painter = painterResource(id = R.drawable.testbackground),
                contentDescription = null,
                contentScale = ContentScale.FillBounds, // Crop cho vừa màn hình
                modifier = Modifier.matchParentSize()
            )
            key(gameUpdateTrigger){
                Column(
                    modifier = Modifier
                        .fillMaxSize()

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
                                .background(Color(0xFF1E4E5A), shape = CircleShape)
                                .border(4.dp, Color(0xFF00D4FF), shape = CircleShape)
                        ){
                            Icon(
                                painter = painterResource(id = R.drawable.pause),
                                contentDescription = "Pause",
                                tint = Color(0xFF00FFFF),
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
                                "FASTDROP" -> fastDropPiece()
                            }
                        },
                        onTap = { if (!isClearing) rotatePiece() },
                        isClearing = isClearing,
                        rowsToClear = pendingClearRows,
                        onClearAnimationDone = {
                            applyClearedRows(pendingClearRows)
                            pendingClearRows = emptyList()
                            isClearing = false
                            spawnNewPiece()
                        },
                        blockOpacity = blockOpacity
                    )
                }
            }

            // Combo Effect
            if (showComboEffect ) {
                ComboEffect(
                    lineCleared = linesCleared,
                    comboCount = comboCount,
                    level,
                    onAnimationComplete = {
                        showComboEffect = false
                    }
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
                        isGameRunning = true
                    },
                    onExit = { navController?.navigate("menu") },
                    currentScore =score,
                    currentLevel =level,
                    linesCleared =line
                )
            }
            if(showScoreEffect&&comboCount<2){
                ScoreEffect(
                    linesCleared = linesCleared,
                    level = level,
                    onAnimationComplete = {
                        showScoreEffect = false
                        linesCleared = 0
                    }
                )

            }
            if(isGameOver){
                GameOverMenu(
                    onRestart = {
                        isGameRunning = false
                        isGameOver = false
                        resetGame()
                        isGameRunning = true
                    },
                    onExit = { navController?.navigate("menu") },
                    score,
                    level,
                    line
                )
            }
        }

}


@Composable
fun MovingBackground() {
    val context = LocalContext.current
    val imageBitmap = remember {
        ContextCompat.getDrawable(context, R.drawable.testbackground)
            ?.toBitmap()
            ?.asImageBitmap()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidthPx = constraints.maxWidth

        // Tạo animation vô hạn
        val infiniteTransition = rememberInfiniteTransition()
        val offsetX by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -screenWidthPx.toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 10000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        Box(modifier = Modifier.fillMaxSize()) {
            imageBitmap?.let { img ->
                // Ảnh đầu tiên
                Image(
                    bitmap = img,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds, // Crop để vừa màn hình
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(offsetX.toInt(), 0) }
                )
                // Ảnh thứ hai để lặp
                Image(
                    bitmap = img,
                    contentDescription = null,
                    contentScale = ContentScale.FillBounds, // Crop để vừa màn hình
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset((offsetX + screenWidthPx).toInt(), 0) }
                )
            }
        }
    }
}



@Preview
@Composable
fun PauseMenuPreview(showBackground: Boolean = true){
    MaterialTheme {
        PauseMenu(onResume = {}, onRestart = {},
            onExit = {},
            currentScore = 54,
            currentLevel = 12,
            linesCleared = 20)
    }
}

@Preview
@Composable
fun GameOverPreview(showBackground: Boolean = true){
    MaterialTheme {
        GameOverMenu(onRestart = {}, onExit = {},
            finalScore = 2500,
            finalLevel = 15,
            linesCleared = 40)
    }
}
@Preview
@Composable
fun TetrisGameScreenPreview(showBackground: Boolean = true) {
    MaterialTheme {
        TetrisGameScreen()
    }
}
