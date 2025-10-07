// Component.kt
package com.example.tetrisgame

import android.util.Log
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
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
import kotlin.math.abs

@Composable
fun InforBox(text: String, value: String) {
    val orbitronFont = FontFamily(
        Font(R.font.orbitron_extrabold, FontWeight.Bold)
    )
    Column(
        modifier = Modifier
            .background(Color(0xFF1E4E5A), RoundedCornerShape(8.dp))
            .border(2.dp, Color(0xFF00D4FF), RoundedCornerShape(8.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = orbitronFont,
            color = Color(0xFF00FFFF)
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = orbitronFont,
            color = Color(0xFF00FFFF)
        )
    }
}

@Composable
fun NextBox(nextPiece: Tetromino? = null) {
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
    ) {
        Text(
            text = "Next",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = orbitronFont,
            color = Color(0xFF00FFFF)
        )
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
    onRestart: () -> Unit,
    onExit: () -> Unit,
    finalScore: Int,
    finalLevel: Int,
    linesCleared: Int
) {
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Box {
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
                ) {
                    Text(
                        text = " GAME\n OVER!",
                        style = titleStyle,
                        color = Color(0xFF00FFFF),
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Button(
                            onClick = onRestart,
                            modifier = Modifier
                                .width(140.dp)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00FFFF),
                                contentColor = Color.Black
                            )
                        ) {
                            Text(text = "Retry", style = buttonTextStyle)
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
                        ) {
                            Text(text = "Exit", style = buttonTextStyle)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Final Score: $finalScore | Level: $finalLevel\nLines: $linesCleared",
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
fun PauseMenu(
    onRestart: () -> Unit,
    onResume: () -> Unit,
    onExit: () -> Unit,
    currentScore: Int,
    currentLevel: Int,
    linesCleared: Int
) {
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Box {
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
                ) {
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
                    ) {
                        Text(text = "Resume", style = buttonTextStyle)
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
                    ) {
                        Text(text = "Restart", style = buttonTextStyle)
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
                    ) {
                        Text(text = "Exit", style = buttonTextStyle)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Current Score: $currentScore | Level: $currentLevel\nLines: $linesCleared",
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
fun WinMenu(
    onRestart: () -> Unit,
    onNextLevel: () -> Unit,
    onExit: () -> Unit,
    score: Int,
    lines: Int,
    level: Int
) {
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Box {
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
                ) {

                    Text(
                        text = "YOU WIN!",
                        style = titleStyle,
                        color = Color(0xFF00FFFF), // Xanh cho thắng
                        fontSize = 60.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Score: $score | Level: $level\nLines: $lines",
                        style = statsTextStyle,
                        color = Color(0xFF00FFFF),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = onNextLevel,
                            modifier = Modifier
                                .width(150.dp)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00FFFF),
                                contentColor = Color.Black
                            )
                        ) {
                            Text(text = "Next", style = buttonTextStyle)
                        }
                        Button(
                            onClick = onExit,
                            modifier = Modifier
                                .width(150.dp)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00FFFF),
                                contentColor = Color.Black
                            )
                        ) {
                            Text(text = "Exit", style = buttonTextStyle)
                        }
                    }


                }
            }
        }
    }
}

@Composable
fun LoseMenu(
    onRestart: () -> Unit,
    onExit: () -> Unit,
    score: Int,
    lines: Int,
    level: Int,
    targetType: TargetType,
    targetValue: Int,
    piecesUsed: Int,
    piecesLimit: Int
) {
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
        ) {
            Box {
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
                ) {
                    Text(
                        text = "CHALLENGE\n FAILED!",
                        style = titleStyle,
                        color = Color(0xFFFF0000),
                        fontSize = 40.sp
                        // Đỏ cho thua
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                        Button(
                            onClick = onRestart,
                            modifier = Modifier
                                .width(140.dp)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00FFFF),
                                contentColor = Color.Black
                            )
                        ) {
                            Text(text = "Retry", style = buttonTextStyle)
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
                        ) {
                            Text(text = "Exit", style = buttonTextStyle)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    val targetText = when (targetType) {
                        TargetType.LINES -> "Lines: $lines / $targetValue"
                        TargetType.SCORE -> "Score: $score / $targetValue"
                    }
                    Text(
                        text = "Level: $level\n$targetText\nPieces: $piecesUsed / $piecesLimit",
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
    onClearAnimationDone: () -> Unit,
    blockOpacity: Array<Array<Float>> = Array(20) { Array(10) { 1f } }
) {
    key(gameUpdateTrigger) {
        val cellSizeDp = 30.dp
        val gridWidth = 10
        val gridHeight = 20
        Box(
            modifier = Modifier
                .size(310.dp, 610.dp)
                .background(Color(0xFF000F1B), RoundedCornerShape(8.dp))
                .border(5.dp, Color(0xFF00D4FF), RoundedCornerShape(8.dp))
                .padding(4.dp)
                .pointerInput(isClearing) {
                    if (isClearing) return@pointerInput
                    var dragDistance = Offset.Zero
                    var hasDropped = false

                    detectDragGestures(
                        onDragStart = {
                            dragDistance = Offset.Zero
                            hasDropped = false
                        },
                        onDragEnd = {
                            dragDistance = Offset.Zero
                            hasDropped = false
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        if (isClearing || hasDropped) return@detectDragGestures

                        dragDistance += dragAmount

                        // FASTDROP - ưu tiên cao nhất
                        if (dragDistance.y > 120f && abs(dragDistance.y) > abs(dragDistance.x)) {
                            onSwipe("FASTDROP")
                            hasDropped = true
                            return@detectDragGestures
                        }

                        // Di chuyển NGANG
                        if (abs(dragDistance.x) > abs(dragDistance.y) && abs(dragDistance.x) > 35f) {
                            if (dragDistance.x < 0) {
                                onSwipe("LEFT")
                            } else {
                                onSwipe("RIGHT")
                            }
                            dragDistance = Offset(0f, dragDistance.y)
                        }
                        // Di chuyển XUỐNG
                        else if (abs(dragDistance.y) > abs(dragDistance.x) && dragDistance.y > 35f) {
                            onSwipe("DOWN")
                            dragDistance = Offset(dragDistance.x, 0f)
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
                            val alpha = blockOpacity[i][j]

                            // CHỈ VẼ KHI OPACITY > 0 (tức là đang hiển thị)
                            if (alpha > 0f) {
                                drawNeonBlock(j * cellSize, i * cellSize, blockColor, false)
                            }
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

@Preview(showBackground = true)
@Composable
fun WinMenuPreview() {
    MaterialTheme {
        WinMenu(onRestart = {}, onNextLevel = {}, onExit = {},
            score = 1500, lines = 30, level = 2)
    }
}