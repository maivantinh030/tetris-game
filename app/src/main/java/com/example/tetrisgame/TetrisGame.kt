// TetrisGameScreen.kt
package com.example.tetrisgame

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun TetrisGameScreen(
    navController: NavController?=null
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
    // 1) Tìm các hàng đầy (KHÔNG xóa ngay)
    fun findFullRows(): List<Int> {
        val rows = mutableListOf<Int>()
        for (r in grid.indices) {
            if (grid[r].all { it != 0 }) rows += r
        }
        return rows
    }
    // 2) Áp dụng xóa các hàng sau khi animation kết thúc (LOGIC GIỮ NGUYÊN như clearLines cũ)
    fun applyClearedRows(rowsToClear: List<Int>) {
        if (rowsToClear.isEmpty()) return
        val newGrid = grid.map { it.clone() }.toTypedArray()


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
                1 -> 100 * level
                2 -> 300 * level
                3 -> 500 * level
                4 -> 800 * level
                else -> 0
            }
            score += baseScore
            showScoreEffect = true
            level = 1 + line / 10
            dropSpeed = (1000L - (level - 1) * 100L).coerceAtLeast(100L)
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

    fun movePiece(dx: Int, dy: Int) {
        currentPiece?.let { piece ->
            if (!checkCollision(dx, dy)) {
                currentPiece = piece.copy(
                    position = Position(piece.position.x + dx, piece.position.y + dy)
                )
                gameUpdateTrigger++ // Force recomposition
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
            gameUpdateTrigger++ // Force recomposition
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
        isPaused = false
        score = 0
        level = 1
        line = 0
        dropSpeed = 1000L
        currentPiece = null
        nextPiece = null
        showComboEffect = false
        gameUpdateTrigger++ // Force recomposition
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

    // UI PHẦN - THÊM KEY ĐỂ FORCE RECOMPOSITION

        Box(modifier = Modifier.fillMaxSize()){
            Image(
                painter = painterResource(id = R.drawable.testbackground),
                contentDescription = null,
                contentScale = ContentScale.FillBounds, // Crop cho vừa màn hình
                modifier = Modifier.matchParentSize()
            )

//            MovingBackground()

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
                        }
                    )
                }
            }

            // Combo Effect
            if (showComboEffect ) {
                ComboEffect(
                    comboCount = line,
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
            if(showScoreEffect){
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
fun RowClearOverlay(
    rowsToClear: List<Int>,
    cols: Int,
    rows: Int,
    cellSize: Dp,
    color: Color = Color.Cyan,
    durationMs: Int = 380,
    onAllFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (rowsToClear.isEmpty()) return

    val density = LocalDensity.current
    val cellSizePx = with(density) { cellSize.toPx() }
    var finishedCount by remember(rowsToClear) { mutableStateOf(0) }

    // Dùng tọa độ LOCAL khớp với Canvas trong gameGrid (không dùng boundsInWindow)
    Box(modifier = modifier) {
        rowsToClear.forEach { r ->
            val top = r * cellSizePx
            val bottom = top + cellSizePx
            val left = 0f
            val right = cols * cellSizePx
            val rowRect = Rect(left, top, right, bottom)
            val cellRects = (0 until cols).map { c ->
                val l = c * cellSizePx
                Rect(l, top, l + cellSizePx, bottom)
            }
            NeonBeamClearEffect(
                rowRect = rowRect,
                cellRects = cellRects,
                color = color,
                durationMs = durationMs,
                onFinished = {
                    finishedCount += 1
                    if (finishedCount >= rowsToClear.size) onAllFinished()
                }
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

@Composable
fun InforBox(text:String, value: String){
    val orbitronFont = FontFamily(
        Font(R.font.orbitron_extrabold, FontWeight.Bold)
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
        Font(R.font.orbitron_extrabold, FontWeight.Bold)
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
fun GameOverMenu(
    onRestart:()-> Unit,
    onExit:()-> Unit,
    finalScore: Int ,
    finalLevel: Int ,
    linesCleared: Int
){
    val pixelFont = FontFamily(
        Font(R.font.pixel_emulator, FontWeight.ExtraBold)
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
                        Spacer(modifier = Modifier.width(10.dp))
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
fun NeonBeamClearEffect(
    rowRect: Rect,           // Rect của cả hàng trên màn hình (px)
    cellRects: List<Rect>,   // Rect từng ô trong hàng (px)
    color: Color = Color.Cyan,
    durationMs: Int = 5000,
    onFinished: () -> Unit = {}
) {
    val anim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        anim.animateTo(1f, animationSpec = tween(durationMs, easing = FastOutSlowInEasing))
        onFinished()
    }

    val beamWidth = rowRect.height * 1.2f
    val beamX = rowRect.left + (rowRect.width * anim.value)

    Canvas(modifier = Modifier.fillMaxSize()) {
        // Beam chính
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, color.copy(alpha = 0.85f), Color.Transparent),
                startX = beamX - beamWidth / 2,
                endX = beamX + beamWidth / 2
            ),
            topLeft = Offset(rowRect.left, rowRect.top),
            size = rowRect.size
        )
        // Glow trail
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    color.copy(alpha = 0.28f),
                    Color.Transparent
                ),
                startX = beamX - beamWidth,
                endX = beamX + beamWidth
            ),
            topLeft = Offset(rowRect.left, rowRect.top),
            size = rowRect.size
        )
        // Fade cell theo tiến trình quét
        cellRects.forEach { cell ->
            val centerX = cell.left + cell.width / 2f
            val progressAtCell = ((centerX - rowRect.left) / rowRect.width).coerceIn(0f, 1f)
            val fadeAlpha = if (progressAtCell <= anim.value) 0.08f
            else 1f - ((progressAtCell - anim.value) * 1.5f).coerceIn(0f, 1f)
            drawRect(
                color = color.copy(alpha = fadeAlpha),
                topLeft = Offset(cell.left, cell.top),
                size = cell.size,
                blendMode = BlendMode.Plus
            )
        }
    }
}
@Composable
fun PauseMenu(
    onRestart:()-> Unit,
    onResume:()-> Unit,
    onExit:()-> Unit,
    currentScore: Int ,
    currentLevel: Int ,
    linesCleared: Int
){
    val pixelFont = FontFamily(
        Font(R.font.pixel_emulator, FontWeight.ExtraBold)
    )
    val titleStyle = androidx.compose.ui.text.TextStyle(
        fontSize = 70.sp,
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
                        text = "PAUSED",
                        style = titleStyle,
                        color = Color(0xFF00FFFF),

                        )
                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onResume,
                        modifier = Modifier
                            .width(180.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00FFFF),
                            contentColor = Color.Black
                        )
                    ){
                        Text(text = "Resume",style = buttonTextStyle)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onRestart,
                        modifier = Modifier
                            .width(180.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00FFFF),
                            contentColor = Color.Black
                        )
                    ){
                        Text(text = "Restart",style = buttonTextStyle)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onExit,
                        modifier = Modifier
                            .width(180.dp)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00FFFF),
                            contentColor = Color.Black
                        )
                    ){
                        Text(text = "Exit",style = buttonTextStyle)
                    }


                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Current Score: $currentScore no Level: $currentLevel\nLD: 40x250",
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
    onTap: () -> Unit,
    isClearing: Boolean,
    rowsToClear: List<Int>,
    onClearAnimationDone: () -> Unit
) {
    key(gameUpdateTrigger, currentPiece?.position, currentPiece?.rotation) {
        val cellSizeDp = 30.dp
        val gridWidth = 10
        val gridHeight = 20
        Box(
            modifier = Modifier
                .size(310.dp, 610.dp)
                .background(Color(0xFF000F1B),RoundedCornerShape(8.dp))
                .border(5.dp, Color(0xFF00D4FF), RoundedCornerShape(8.dp))
                .padding(4.dp)
                .pointerInput(isClearing) {
                    if (isClearing) return@pointerInput
                    var totalDrag = Offset.Zero
                    var hasTriggeredAction = false

                    detectDragGestures(
                        onDragStart = {
                            totalDrag = Offset.Zero
                            hasTriggeredAction = false
                        },
                        onDragEnd = {
                            totalDrag = Offset.Zero
                            hasTriggeredAction = false
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        if (isClearing) return@detectDragGestures
                        totalDrag += dragAmount
                        // Chỉ trigger action một lần cho mỗi gesture
                        if (!hasTriggeredAction) {
                            val threshold = 80f
                            val fastDropThreshold = 110f
                            Log.d("Gesture", "Swiped  ${totalDrag.x} , ${totalDrag.y})")
                            when {
                                totalDrag.x < -threshold && abs(totalDrag.x) > abs(totalDrag.y) -> {
                                    onSwipe("LEFT")
                                    hasTriggeredAction = true
                                    Log.d("Gesture", "Swiped LEFT ${totalDrag.x} , ${totalDrag.y})")
                                }
                                totalDrag.x > threshold && abs(totalDrag.x) > abs(totalDrag.y) -> {
                                    onSwipe("RIGHT")
                                    hasTriggeredAction = true
                                    Log.d("Gesture", "Swiped RIGHT ${totalDrag.x} , ${totalDrag.y})")
                                }
                                totalDrag.y > fastDropThreshold && abs(totalDrag.y) > abs(totalDrag.x) -> {
                                    onSwipe("FASTDROP")
                                    hasTriggeredAction = true
                                    Log.d("Gesture", "Swiped FASTDROP ${totalDrag.x} , ${totalDrag.y})")
                                }
                                totalDrag.y > threshold && abs(totalDrag.y) > abs(totalDrag.x) -> {
                                    onSwipe("DOWN")
                                    hasTriggeredAction = true
                                    Log.d("Gesture", "Swiped DOWN ${totalDrag.x} , ${totalDrag.y})")

                                }
                            }
                        }
                    }
                }
                .pointerInput(isClearing) {
                    if (isClearing) return@pointerInput
                    detectTapGestures(onTap = { onTap() })
                }
        ) {

            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = cellSizeDp.toPx()
                fun DrawScope.drawNeonBlock(x: Float, y: Float, blockColor: Color, isActive: Boolean = false) {
                    val glowIntensity = if (isActive) 0.8f else 0.6f // Tăng cường độ sáng khi hoạt động
                    val borderWidth = 2.dp.toPx()
                    val neonColor = Color.White // Màu cyan neon làm viền và glow


                    // Lớp 2: Glow giữa (sáng hơn)
                    drawRect(
                        color = neonColor.copy(alpha = 0.4f * glowIntensity),
                        topLeft = Offset(x - 6, y - 6),
                        size = Size(cellSize + 12, cellSize + 12)
                    )

                    // Lớp 3: Glow gần (rất sáng)
                    drawRect(
                        color = neonColor.copy(alpha = 0.6f * glowIntensity),
                        topLeft = Offset(x - 3, y - 3),
                        size = Size(cellSize + 6, cellSize + 6)
                    )

                    // Lớp 4: Khối chính
                    drawRect(
                        color = blockColor,
                        topLeft = Offset(x, y),
                        size = Size(cellSize, cellSize)
                    )

                    // Lớp 5: Viền neon rực rỡ
                    drawRect(
                        color = neonColor.copy(alpha = 1.0f * glowIntensity),
                        topLeft = Offset(x, y),
                        size = Size(cellSize, cellSize),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = borderWidth)
                    )



                    // Lớp 7: Bóng dưới nhẹ
                    drawRect(
                        color = Color.Black.copy(alpha = 0.2f),
                        topLeft = Offset(x + borderWidth, y + cellSize - borderWidth - 1.dp.toPx()),
                        size = Size(cellSize - 2 * borderWidth, 1.dp.toPx())
                    )
                    drawRect(
                        color = Color.Black.copy(alpha = 0.2f),
                        topLeft = Offset(x + cellSize - borderWidth - 1.dp.toPx(), y + borderWidth),
                        size = Size(1.dp.toPx(), cellSize - 2 * borderWidth)
                    )
                }               // Vẽ lưới nền
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
            // Overlay hiệu ứng (trùng kích thước Canvas do matchParentSize + padding ở Box)
            if (isClearing && rowsToClear.isNotEmpty()) {
                RowClearOverlay(
                    rowsToClear = rowsToClear,
                    cols = gridWidth,
                    rows = gridHeight,
                    cellSize = cellSizeDp,
                    color = Color.Cyan,
                    durationMs = 1200,
                    onAllFinished = onClearAnimationDone,
                    modifier = Modifier.matchParentSize()
                )
            }
        }
    }
}

@Composable
fun ScoreEffect(
    linesCleared : Int,
    level:Int,
    onAnimationComplete: () -> Unit
){
    val baseScore = when (linesCleared) {
        1 -> 100 * level
        2 -> 300 * level
        3 -> 500 * level
        4 -> 800 * level
        else -> 0
    }
    val textScale = remember { Animatable(0.5f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(50f) }

    // Screen shake nhẹ cho combo cao
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(linesCleared) {
        // Text animation: xuất hiện từ dưới lên
        launch {
            textAlpha.animateTo(1f, tween(300))
        }
        launch {
            textOffsetY.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
        }
        launch {
            // Scale up rồi về bình thường
            textScale.animateTo(1.2f, tween(200))
            textScale.animateTo(1.0f, tween(200))
        }
        // Screen shake cho combo >= 3
        if (linesCleared >= 3) {
            repeat(6) {
                launch {
                    shakeOffset.animateTo((-5..5).random().toFloat(), tween(50))
                }
                delay(50)
            }
            shakeOffset.animateTo(0f, tween(100))
        }
        // Giữ hiển thị 1.2 giây
        delay(1500)
        // Fade out
//        launch {
//            textAlpha.animateTo(0f, tween(500))
//        }
//        delay(500)
        onAnimationComplete()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(x = shakeOffset.value.dp,y = (150).dp)
            .scale(textScale.value)
        ,
        contentAlignment = Alignment.Center
    ) {
        // Text combo
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            modifier = Modifier
//                .offset(y = textOffsetY.value.dp)
//                .scale(textScale.value)
//        ) {
//            Text(
//                text = "+$baseScore",
//                fontSize = 40.sp,
//                fontWeight = FontWeight.Bold,
//                color = Color.White.copy(alpha = textAlpha.value),
//                textAlign = TextAlign.Center
//            )
//        }
        Text(
            text = "+$baseScore",
            fontSize = 43.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FFFF), // màu viền
            style = TextStyle(
                shadow = Shadow( // hack: tạo cảm giác viền
                    color = Color(0xFF00FFFF),
                    offset = Offset(0f, 0f),
                    blurRadius = 5f
                )
            ),
            textAlign = TextAlign.Center

        )
        Text(
            text = "+$baseScore",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = textAlpha.value),
            textAlign = TextAlign.Center
        )
    }

}
@Composable
fun ComboEffect(
    comboCount: Int,
    level: Int,
    onAnimationComplete: () -> Unit
) {

    val baseScore = when (comboCount) {
        1 -> 100 * level
        2 -> 300 * level
        3 -> 500 * level
        4 -> 800 * level
        else -> 0
    }
    Log.d("ComboEffect", "Combo: $comboCount, Base Score: $baseScore, Level: $level")
    // Animation states đơn giản
    val textScale = remember { Animatable(0.5f) }
    val textAlpha = remember { Animatable(0f) }
    val textOffsetY = remember { Animatable(50f) }

    // Screen shake nhẹ cho combo cao
    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(comboCount) {
        // Text animation: xuất hiện từ dưới lên
        launch {
            textAlpha.animateTo(1f, tween(300))
        }
        launch {
            textOffsetY.animateTo(0f, tween(400, easing = FastOutSlowInEasing))
        }
        launch {
            // Scale up rồi về bình thường
            textScale.animateTo(1.2f, tween(200))
            textScale.animateTo(1.0f, tween(200))
        }

        // Screen shake cho combo >= 3
        if (comboCount >= 3) {
            repeat(6) {
                launch {
                    shakeOffset.animateTo((-5..5).random().toFloat(), tween(50))
                }
                delay(50)
            }
            shakeOffset.animateTo(0f, tween(100))
        }

        // Giữ hiển thị 1.2 giây
        delay(1200)

        // Fade out
        launch {
            textAlpha.animateTo(0f, tween(500))
        }

        delay(500)
        onAnimationComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .offset(x = shakeOffset.value.dp),
        contentAlignment = Alignment.Center
    ) {
        // Particles đơn giản
        if (comboCount >= 2) {
            repeat(6) { index ->
                val angle = (60f * index)
                val distance = 60f
                Box(
                    modifier = Modifier
                        .offset(
                            x = (cos(Math.toRadians(angle.toDouble())) * distance).dp,
                            y = (sin(Math.toRadians(angle.toDouble())) * distance).dp
                        )
                        .size(8.dp)
                        .background(
                            color = when (index % 3) {
                                0 -> Color(0xFFFF6B6B).copy(alpha = textAlpha.value)
                                1 -> Color(0xFF4ECDC4).copy(alpha = textAlpha.value)
                                else -> Color(0xFFFFE66D).copy(alpha = textAlpha.value)
                            },
                            shape = CircleShape
                        )
                )
            }
        }
        // Text combo
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = textOffsetY.value.dp)
                .scale(textScale.value)
        ) {
            Text(
                text = "COMBO!",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FFFF).copy(alpha = textAlpha.value),
                textAlign = TextAlign.Center
            )

            Text(
                text = "x${comboCount}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFFF00).copy(alpha = textAlpha.value),
                textAlign = TextAlign.Center
            )

            Text(
                text = "Score + $baseScore",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FF00).copy(alpha = textAlpha.value),
                textAlign = TextAlign.Center
            )
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
