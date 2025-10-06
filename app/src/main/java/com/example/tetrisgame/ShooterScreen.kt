package com.example.tetrisgame

import android.content.Context
import android.media.SoundPool
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlin.math.min

@Composable
fun ShooterScreen(
    manager: ShooterManager,
    context: Context,
    cellWidth: Float,
    cellHeight: Float,
    onMusicToggle: () -> Unit,
    onSoundToggle: () -> Unit,
    onExit: () -> Unit,
    navController: NavController
) {
    val gridCols = 10
    val gridRows = 20
    val emptyGrid = remember { Array(gridRows) { Array(gridCols) { 0 } } }

    val shooterManager = remember { ShooterManager(gridCols, gridRows, context) }
    ShooterEffects(manager = shooterManager)
    val soundPool = remember { SoundPool.Builder().setMaxStreams(2).build() }
    val edgeSoundId = remember { soundPool.load(context, R.raw.beeping, 1) }
    val hitSoundId = remember { soundPool.load(context, R.raw.punch4, 1) }
    var hitStreamId by remember { mutableIntStateOf(0) }

    var soundOn by remember { mutableStateOf(SoundSettings.isSoundOn) }
    var musicOn by remember { mutableStateOf(SoundSettings.isMusicOn) }

    LaunchedEffect(Unit) {
        soundOn = SoundSettings.isSoundOn
        musicOn = SoundSettings.isMusicOn
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.testbackground),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        // Hiển thị máu và vàng
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(
                text = "Health: ${shooterManager.playerHealth}",
                color = Color.Red,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(
                text = "Kills: ${shooterManager.enemyKillCount}",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 20.sp
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            Text(
                text = "Gold: ${shooterManager.gold}",
                color = Color.Yellow,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }


        when {
            shooterManager.isWin -> {
                WinMenu(
                    onRestart = { shooterManager.resetGame() },
                    onHighscore = { navController.navigate("highscore") },
                    onExit = onExit,
                    finalScore = shooterManager.gold
                )
            }
            shooterManager.isGameOver -> {
                GameOverMenu(
                    onRestart = { shooterManager.resetGame() },
                    onExit = onExit,
                    finalScore = shooterManager.gold,
                    finalLevel = 1,
                    linesCleared = 0
                )
            }
            else ->{
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    gameGrid(
                        grid = emptyGrid,
                        currentPiece = null,
                        gameUpdateTrigger = 0,
                        onSwipe = { direction ->
                            when (direction) {
                                "LEFT" -> shooterManager.movePlayer(-1f, 0f)
                                "RIGHT" -> shooterManager.movePlayer(1f, 0f)
                                "UP" -> shooterManager.movePlayer(0f, -1f)
                                "DOWN" -> shooterManager.movePlayer(0f, 1f)
                                "FASTDROP" -> shooterManager.placeTrap()
                            }
                            if (shooterManager.playerX <= 0.5f || shooterManager.playerX >= gridCols - 0.5f ||
                                shooterManager.playerY <= 0.5f || shooterManager.playerY >= gridRows - 0.5f) {
                                if (soundOn) {
                                    soundPool.play(edgeSoundId, 1f, 1f, 0, 0, 1f)
                                }
                            }
                        },
                        onTap = {
                            shooterManager.fire()
                            if (soundOn) {
                                if (hitStreamId != 0) {
                                    soundPool.stop(hitStreamId)
                                }
                                hitStreamId = soundPool.play(hitSoundId, 1f, 1f, 0, 0, 1f)
                            }
                        },
                        isClearing = false,
                        rowsToClear = emptyList(),
                        onClearAnimationDone = {},
                        shooterMode = shooterManager.shooterMode,
                        playerX = shooterManager.playerX,
                        playerY = shooterManager.playerY,
                        playerShield = shooterManager.playerShield,
                        projectiles = shooterManager.projectiles,
                        enemies = shooterManager.enemies,
                        powerUps = shooterManager.powerUps,
                        traps = shooterManager.traps,
                        walls = shooterManager.walls,
                        onFire = { shooterManager.fire() },
                        onMissile = { shooterManager.fireMissile() },
                        onArrow = { shooterManager.fireArrow("right") },
                        onShield = { shooterManager.activateShield() }
                    )
                    HitEffectsOverlay(
                        cols = gridCols,
                        rows = gridRows,
                        effects = shooterManager.hitEffects
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 2.dp)
        ) {
            IconButton(onClick = {
                onSoundToggle()
                onMusicToggle()
                soundOn = SoundSettings.isSoundOn
            }) {
                Icon(
                    painter = painterResource(id = if (soundOn) R.drawable.musicon else R.drawable.musicoff),
                    modifier = Modifier.size(30.dp),
                    contentDescription = "Toggle All Sound",
                    tint = Color.Unspecified
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 2.dp)
        ) {
            IconButton(onClick = {
                onMusicToggle()
                musicOn = SoundSettings.isMusicOn
            }) {
                Icon(
                    painter = painterResource(id = if (musicOn) R.drawable.soundon else R.drawable.soundoff),
                    modifier = Modifier.size(24.dp),
                    contentDescription = "Toggle Music",
                    tint = Color.Unspecified
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 80.dp)
                    .size(60.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp))
                    .border(2.dp, Color.Cyan, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                IconButton(onClick = { shooterManager.activateShield()
                    shooterManager.gold-=20 }) {
                    Icon(
                        painter = painterResource(id = R.drawable.shield),
                        contentDescription = "Activate Shield",
                        tint = Color.Unspecified
                    )
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 160.dp)
                    .size(60.dp)
                    .background(Color.Gray, RoundedCornerShape(8.dp))
                    .border(2.dp, Color.Cyan, RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                IconButton(onClick = { shooterManager.createWall() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_brick_24),
                        contentDescription = "Create Wall",
                        tint = Color.Unspecified
                    )
                }
            }
        }
    }
}

@Composable
private fun HitEffectsOverlay(
    cols: Int,
    rows: Int,
    effects: List<HitEffect>
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val cellW = size.width / cols
        val cellH = size.height / rows
        val rUnit = min(cellW, cellH)

        for (e in effects) {
            val t = e.age.toFloat() / e.maxAge
            val radius = rUnit * (0.3f + 0.9f * t)
            drawCircle(
                color = Color.Yellow.copy(alpha = 1f - t),
                radius = radius,
                center = Offset(e.x * cellW, e.y * cellH)
            )
        }
    }
}

// WinMenu composable
@Composable
fun WinMenu(
    onRestart: () -> Unit,
    onHighscore: () -> Unit, // Callback để mở HighscoreScreen
    onExit: () -> Unit,
    finalScore: Int
) {
    val context = LocalContext.current
    val pixelFont = FontFamily(
        Font(R.font.pixel_emulator, FontWeight.ExtraBold)
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
    ) {
        Card(modifier = Modifier
            .align(Alignment.Center)
        ) {
            Box {

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .border(3.dp, Color(0xFF00FFFF), RoundedCornerShape(12.dp))
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {


                    Spacer(modifier = Modifier.height(4.dp))

                    Image(
                        painter = painterResource(id = R.drawable.congratulations), // Giả định có ảnh congratulations
                        contentDescription = null,
                        modifier = Modifier.height(200.dp).fillMaxWidth(),
                        contentScale = ContentScale.FillBounds
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                            Text(text = "Restart", style = buttonTextStyle, fontSize = 30.sp)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onHighscore,
                            modifier = Modifier
                                .width(140.dp)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF00FFFF),
                                contentColor = Color.Black
                            )
                        ) {
                            Text(text = "Highscore", style = buttonTextStyle)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
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
                        text = "Final Score: $finalScore",
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

// HighscoreScreen composable
@Composable
fun HighscoreScreen(
    navController: NavController,
    onExit: () -> Unit // Callback để quay lại WinMenu
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("highscores", Context.MODE_PRIVATE) }
    val pixelFont = FontFamily(
        Font(R.font.pixel_emulator, FontWeight.ExtraBold)
    )
    val titleStyle = androidx.compose.ui.text.TextStyle(
        fontSize = 40.sp,
        fontWeight = FontWeight.ExtraBold,
        fontFamily = pixelFont,
        lineHeight = 35.sp
    )

    val statsTextStyle = androidx.compose.ui.text.TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        fontFamily = pixelFont
    )

    // Lấy top 6 highscore từ SharedPreferences
    val highscores = remember {
        val scores = mutableListOf<Int>()
        for (i in 1..6) {
            val score = sharedPrefs.getInt("highscore_$i", 0)
            if (score > 0) scores.add(score)
        }
        scores.take(6)
    }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black.copy(alpha = 0.8f))
    ) {
        Card(modifier = Modifier
            .align(Alignment.Center)
            .width(300.dp)
            .height(400.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "HIGHSCORES",
                    style = titleStyle,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontSize = 30.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    highscores.forEachIndexed { index, score ->
                        Text(
                            text = "#${index + 1}: $score Gold",
                            style = statsTextStyle,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }
                    // Điền khoảng trống nếu ít hơn 6
                    repeat(6 - highscores.size) {
                        Text(
                            text = "#${highscores.size + it + 1}: -",
                            style = statsTextStyle,
                            color = Color(0xFF00FFFF),
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Button(
                    onClick = onExit,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00FFFF),
                        contentColor = Color.Black
                    )
                ) {
                    Text("Back", style = statsTextStyle)
                }
            }
        }
    }
}