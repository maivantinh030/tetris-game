// TetrisGameScreen.kt
package com.example.tetrisgame

import android.R.attr.x
import android.R.attr.y
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlin.math.abs

@Composable
fun TetrisGameScreen(
    navController: NavController? = null
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
            isGameOver = true
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
        val newGrid = grid.map { it.clone() }.toTypedArray()
        var linesCleared = 0
        // Lặp qua lưới từ dưới lên trên
        var writeIndex = grid.size - 1
        for (readIndex in grid.size - 1 downTo 0) {
            if (grid[readIndex].all { it != 0 }) {
                // Bỏ qua dòng đầy (không sao chép vào newGrid)
                linesCleared++
            } else {
                // Sao chép dòng không đầy lên vị trí writeIndex
                newGrid[writeIndex] = grid[readIndex].clone()
                writeIndex--
            }
        }
        // Điền các dòng trống ở đầu lưới
        for (i in 0 until linesCleared) {
            newGrid[i] = Array(10) { 0 }
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
            gameUpdateTrigger++
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

    // THÊM GAME LOOP - ĐIỀU QUAN TRỌNG NHẤT
    LaunchedEffect(isGameRunning, isPaused, isGameOver) {
        if (isGameRunning && !isPaused && !isGameOver) {
            // Spawn piece đầu tiên nếu chưa có
            if (currentPiece == null) {
                spawnNewPiece()
            }

            // Game loop chính
            while (isGameRunning && !isPaused && !isGameOver) {
                delay(dropSpeed)
                movePiece(0, 1) // Di chuyển piece xuống
            }
        }
    }

    // UI PHẦN - THÊM KEY ĐỂ FORCE RECOMPOSITION
    key(gameUpdateTrigger) {
        Box(modifier = Modifier.fillMaxSize()){
            Image(
                painter = painterResource(id = R.drawable.testbackground), // thay bg_image bằng tên ảnh của bạn
                contentDescription = null,
                contentScale = ContentScale.FillBounds, // Crop cho vừa màn hình
                modifier = Modifier.matchParentSize()
            )
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
                    onExit = { navController?.navigate("menu") }
                )
            }
            if(isGameOver){
                GameOverMenu(
                    onRestart = {
                        isGameRunning = false
                        isGameOver = false
                        resetGame()
                    },
                    onExit = { navController?.navigate("menu") },
                    score,
                    level,
                    line
                )
            }
        }
    }
}

@Composable
fun InforBox(text:String, value: String){
    val orbitronFont = FontFamily(
        Font(R.font.orbitron_extrabold, FontWeight.Bold) // Tham chiếu đến res/font/orbitron_bold.ttf
    )
    Column(modifier = Modifier
        .background(Color(0xFF1E4E5A), RoundedCornerShape(8.dp))
        .border(2.dp, Color(0xFF00D4FF), RoundedCornerShape(8.dp))
        .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = text, fontSize = 20.sp, fontWeight = FontWeight.Bold,fontFamily = orbitronFont,color = Color(0xFF00FFFF))
        Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold,fontFamily = orbitronFont,color = Color(0xFF00FFFF))
    }
}

@Composable
fun NextBox(nextPiece: Tetromino? = null){
    val orbitronFont = FontFamily(
        Font(R.font.orbitron_extrabold, FontWeight.Bold) // Tham chiếu đến res/font/orbitron_bold.ttf
    )
    Column(
        modifier = Modifier
            .background(Color(0xFF1E4E5A), RoundedCornerShape(8.dp))
            .border(2.dp, Color(0xFF00D4FF), RoundedCornerShape(8.dp))
            .width(150.dp)
            .height(130.dp)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        Text(text = "Next", fontSize = 20.sp, fontWeight = FontWeight.Bold,fontFamily = orbitronFont,color = Color(0xFF00FFFF))
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
fun GameOverMenu(
    onRestart:()-> Unit,
    onExit:()-> Unit,
    finalScore: Int ,
    finalLevel: Int ,
    linesCleared: Int
){
    val pixelFont = FontFamily(
        Font(R.font.pixel_emulator, FontWeight.ExtraBold) // Tham chiếu đến res/font/orbitron_bold.ttf
    )
    val titleStyle = androidx.compose.ui.text.TextStyle(
        fontSize = 80.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = pixelFont,
        lineHeight = 70.sp,
        platformStyle = androidx.compose.ui.text.PlatformTextStyle(
            includeFontPadding = false
        ),
        lineHeightStyle = androidx.compose.ui.text.style.LineHeightStyle(
            alignment = androidx.compose.ui.text.style.LineHeightStyle.Alignment.Center,
            trim = androidx.compose.ui.text.style.LineHeightStyle.Trim.Both
        )
    )

    val buttonTextStyle = androidx.compose.ui.text.TextStyle(
        fontSize = 25.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = pixelFont
    )

    val statsTextStyle = androidx.compose.ui.text.TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        fontFamily = pixelFont
    )
    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.8f))
    ){
        Card(modifier = Modifier
            .align(Alignment.Center))
        {
            Box{
                Image(
                    painter = painterResource(id = R.drawable.backgroundcard),
                    contentDescription = null,
                    modifier = Modifier.matchParentSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(3.dp, Color(0xFF00FFFF), RoundedCornerShape(12.dp))

                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Text(
                        text = " GAME\n OVER!",
                        style = titleStyle,
                        color = Color(0xFF00FFFF),

                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)){
                        Button(
                            onClick = onRestart,
                            modifier = Modifier
                                .width(140.dp)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00FFFF),
                                contentColor = Color.Black
                            )
                        ){
                            Text(text = "Retry",style = buttonTextStyle)
                        }
                        Spacer(modifier = Modifier.width(20.dp))
                        Button(
                            onClick = onExit,
                            modifier = Modifier
                                .width(140.dp)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00FFFF),
                                contentColor = Color.Black
                            )
                        )
                        {
                            Text(text = "Exit", style = buttonTextStyle)
                        }

                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Final Score: $finalScore no Level: $finalLevel\nLD: 40x250",
                        style = statsTextStyle,
                        color = Color(0xFF00FFFF),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
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
                .size(310.dp, 610.dp)
                .background(Color(0xFF000F1B),RoundedCornerShape(8.dp))
                .border(5.dp, Color(0xFF00D4FF), RoundedCornerShape(8.dp))
                .padding(4.dp)
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
                // Hàm helper để vẽ block với neon effect
                fun DrawScope.drawNeonBlock(x: Float, y: Float, blockColor: Color, isActive: Boolean = false) {
                    val glowIntensity = if (isActive) 0.9f else 0.6f
                    val borderWidth = 2.dp.toPx()

                    // Layer 1: Outer glow (xa nhất)
                    drawRect(
                        color = Color(0xFF00FFFF).copy(alpha = 0.2f * glowIntensity),
                        topLeft = Offset(x - 4, y - 4),
                        size = Size(cellSize + 8, cellSize + 8)
                    )

                    // Layer 2: Inner glow
                    drawRect(
                        color = Color(0xFF00FFFF).copy(alpha = 0.4f * glowIntensity),
                        topLeft = Offset(x - 2, y - 2),
                        size = Size(cellSize + 4, cellSize + 4)
                    )

                    // Layer 3: Main block
                    drawRect(
                        color = blockColor,
                        topLeft = Offset(x, y),
                        size = Size(cellSize, cellSize)
                    )

                    // Layer 4: Bright border
                    drawRect(
                        color = Color(0xFF00FFFF).copy(alpha = glowIntensity),
                        topLeft = Offset(x, y),
                        size = Size(cellSize, cellSize),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth)
                    )

                    // Layer 5: Inner highlight (3D effect)
                    drawRect(
                        color = Color.White.copy(alpha = 0.3f),
                        topLeft = Offset(x + borderWidth, y + borderWidth),
                        size = Size(cellSize - 2 * borderWidth, 1.dp.toPx())
                    )
                    drawRect(
                        color = Color.White.copy(alpha = 0.3f),
                        topLeft = Offset(x + borderWidth, y + borderWidth),
                        size = Size(1.dp.toPx(), cellSize - 2 * borderWidth)
                    )

                    // Layer 6: Bottom shadow
                    drawRect(
                        color = Color.Black.copy(alpha = 0.3f),
                        topLeft = Offset(x + borderWidth, y + cellSize - borderWidth - 1.dp.toPx()),
                        size = Size(cellSize - 2 * borderWidth, 1.dp.toPx())
                    )
                    drawRect(
                        color = Color.Black.copy(alpha = 0.3f),
                        topLeft = Offset(x + cellSize - borderWidth - 1.dp.toPx(), y + borderWidth),
                        size = Size(1.dp.toPx(), cellSize - 2 * borderWidth)
                    )
                }
                // Vẽ lưới nền
                drawIntoCanvas { canvas ->
                    for (i in 0..gridWidth) {
                        canvas.nativeCanvas.drawLine(
                            i * cellSize, 0f, i * cellSize, gridHeight * cellSize,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.argb(100, 0, 212, 255)
                                strokeWidth = 1f
                            }
                        )
                    }
                    for (i in 0..gridHeight) {
                        canvas.nativeCanvas.drawLine(
                            0f, i * cellSize, gridWidth * cellSize, i * cellSize,
                            android.graphics.Paint().apply {
                                color = android.graphics.Color.argb(100, 0, 212, 255)
                                strokeWidth = 1f
                            }
                        )
                    }
                }

                // Vẽ các khối đã đặt
                for (i in grid.indices) {
                    for (j in grid[i].indices) {
                        if (grid[i][j] != 0) {
                            val blockColor = TetrominoColors.getColorByCode(grid[i][j])
                            drawNeonBlock(j * cellSize, i * cellSize, blockColor, false)
                        }
                    }
                }

                // Vẽ currentPiece
                currentPiece?.let { piece ->
                    val shape = piece.getRotatedShape()
                    val posX = piece.position.x
                    val posY = piece.position.y
                    val pieceColor = piece.getColor()
                    for (i in shape.indices) {
                        for (j in shape[i].indices) {
                            if (shape[i][j] == 1 && posY + i >= 0 &&
                                posY + i < gridHeight && posX + j >= 0 && posX + j < gridWidth) {
                                drawNeonBlock((posX + j) * cellSize, (posY + i) * cellSize, pieceColor, true)
                            }
                        }
                    }
                }
            }
        }
    }
}

//@Preview
//@Composable
//fun PauseMenuPreview(showBackground: Boolean = true){
//    MaterialTheme {
//        PauseMenu(onResume = {}, onRestart = {}, onExit = {})
//    }
//}

@Preview
@Composable
fun GameOverPreview(showBackground: Boolean = true){
    MaterialTheme {
        GameOverMenu( onRestart = {}, onExit = {},54,127,20)
    }
}
@Preview
@Composable
fun TetrisGameScreenPreview(showBackground: Boolean = true) {
    MaterialTheme {
        TetrisGameScreen()
    }
}