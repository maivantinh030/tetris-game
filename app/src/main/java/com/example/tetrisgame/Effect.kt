package com.example.tetrisgame

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

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
fun ComboEffect(
    lineCleared:Int,
    comboCount: Int,
    level: Int,
    onAnimationComplete: () -> Unit
) {

    val baseScore = when (lineCleared) {
        1 -> 100 * level *comboCount
        2 -> 300 * level * comboCount
        3 -> 500 * level * comboCount
        4 -> 800 * level * comboCount
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
