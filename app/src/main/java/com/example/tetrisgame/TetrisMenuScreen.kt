package com.example.tetrisgame

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController

@Composable
fun TetrisMenuScreen(
    navController: NavController? = null
) {
    var showModeSelection by remember { mutableStateOf(false) }
    var showChallengeSelection by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val gameManager = remember { TetrisManager() }

    var showHighScoreDialog by remember { mutableStateOf(false) }
    var showModeSelectionDialog by remember { mutableStateOf(false) }
    var selectedHighScoreMode by remember { mutableStateOf(GameMode.CLASSIC) }
    // Kiểm tra có trạng thái lưu hay không
    val hasSavedGame = remember { TetrisManager().hasSavedState(context) }

    // Animation for title
    val infiniteTransition = rememberInfiniteTransition(label = "title")
    val titleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.testbackground),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
        )

        if (!showModeSelection && !showChallengeSelection) {
            // Main Menu
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Title with animation
                Text(
                    text = "TETRIS",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF00FFFF),
                    modifier = Modifier
                        .scale(titleScale)
                        .padding(bottom = 8.dp),
                    style = MaterialTheme.typography.displayLarge.copy(
                        letterSpacing = 8.sp
                    )
                )

                Text(
                    text = "GAME",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00D4FF),
                    modifier = Modifier.padding(bottom = 60.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Continue Button nếu có trạng thái đã lưu
                if (hasSavedGame) {
                    MenuButton(
                        text = "CONTINUE",
                        onClick = { navController?.navigate("continue") }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Play Button
                MenuButton(
                    text = "PLAY",
                    onClick = { showModeSelection = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Settings Button
                MenuButton(
                    text = "SETTINGS",
                    onClick = { /* TODO */ },
                    enabled = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                MenuButton(
                    text = "HIGH SCORE",
                    onClick = { showModeSelectionDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))
                // Exit Button
                MenuButton(
                    text = "EXIT",
                    onClick = { /* Exit app */ }
                )
            }
        } else if (showModeSelection && !showChallengeSelection) {
            ModeSelectionScreen(
                onBack = { showModeSelection = false },
                onModeSelect = { mode ->
                    when (mode) {
                        "classic" -> navController?.navigate("classic")
                        "invisible" -> navController?.navigate("invisible")
                        "challenge" -> showChallengeSelection = true
                    }
                }
            )
        } else if (showChallengeSelection) {
            // Challenge Level Selection - Sử dụng shared levels từ TetrisManager
            ChallengeLevelSelection(
                onBack = { showChallengeSelection = false },
                onLevelSelect = { level ->
                    navController?.navigate("challenge/${level}")
                },
                challengeLevels = TetrisManager.sharedChallengeLevels
            )
        }

        // Credits
        Text(
            text = "nhóm F game android",
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )

        if (showModeSelectionDialog) {
            HighScoreModeSelectionDialog(
                onModeSelected = {
                    selectedHighScoreMode = it
                    showHighScoreDialog = true
                    showModeSelectionDialog = false
                },
                onClose = { showModeSelectionDialog = false }
            )
        }
        if (showHighScoreDialog) {
            HighScoreDialog(
                mode = selectedHighScoreMode,
                onClose = { showHighScoreDialog = false }
            )
        }
    }
}

@Composable
fun MenuButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(280.dp)
            .height(60.dp)
            .scale(if (isPressed) 0.95f else 1f)
            .background(
                if (enabled) Color(0xFF1E4E5A) else Color(0xFF2A2A2A),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 3.dp,
                color = if (enabled) Color(0xFF00D4FF) else Color.Gray,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(enabled = enabled) {
                isPressed = true
                onClick()
            }
            .alpha(if (enabled) 1f else 0.5f),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color(0xFF00FFFF) else Color.Gray
        )
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun ModeSelectionScreen(
    onBack: () -> Unit,
    onModeSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SELECT MODE",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FFFF),
            modifier = Modifier.padding(bottom = 50.dp)
        )

        // Classic Mode
        ModeCard(
            title = "CLASSIC",
            description = "Traditional Tetris gameplay\nClear lines and increase your score",
            onClick = { onModeSelect("classic") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Classic Invisible Mode
        ModeCard(
            title = "CLASSIC INVISIBLE",
            description = "Classic mode where blocks\nbecome invisible after placement",
            onClick = { onModeSelect("invisible") }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Challenge Mode
        ModeCard(
            title = "CHALLENGE",
            description = "Complete specific objectives\nwith limited pieces",
            onClick = { onModeSelect("challenge") }
        )

        Spacer(modifier = Modifier.height(40.dp))


        // Back Button
        MenuButton(
            text = "BACK",
            onClick = onBack
        )
    }
}

@Composable
fun ModeCard(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .width(320.dp)
            .height(120.dp)
            .scale(if (isPressed) 0.97f else 1f)
            .background(
                Color(0xFF1E4E5A).copy(alpha = 0.9f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 3.dp,
                color = Color(0xFF00D4FF),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable {
                isPressed = true
                onClick()
            }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FFFF),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color(0xFFCCCCCC),
                lineHeight = 18.sp
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun ChallengeLevelSelection(
    onBack: () -> Unit,
    onLevelSelect: (Int) -> Unit,
    challengeLevels: List<ChallengeLevelConfig>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "CHALLENGE LEVELS",
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF00FFFF),
            modifier = Modifier.padding(top = 40.dp, bottom = 30.dp)
        )

        // Scrollable grid of levels
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Levels 1-5
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 1..5) {
                    val isLocked = !(challengeLevels.getOrNull(i - 1)?.isOpen ?: false)
                    LevelButton(
                        level = i,
                        onClick = { onLevelSelect(i) },
                        isLocked = isLocked
                    )
                }
            }

            // Levels 6-10
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 6..10) {
                    val isLocked = !(challengeLevels.getOrNull(i - 1)?.isOpen ?: false)
                    LevelButton(
                        level = i,
                        onClick = { onLevelSelect(i) },
                        isLocked = isLocked
                    )
                }
            }

            // Levels 11-15
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (i in 11..15) {
                    val isLocked = !(challengeLevels.getOrNull(i - 1)?.isOpen ?: false)
                    LevelButton(
                        level = i,
                        onClick = { onLevelSelect(i) },
                        isLocked = isLocked
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Back Button
        MenuButton(
            text = "BACK",
            onClick = onBack
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun LevelButton(
    level: Int,
    onClick: () -> Unit,
    isLocked: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(55.dp)
            .scale(if (isPressed && !isLocked) 0.9f else 1f)
            .background(
                if (isLocked) Color(0xFF3A3A3A) else Color(0xFF1E4E5A),
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 2.dp,
                color = if (isLocked) Color(0xFFFFD700).copy(alpha = 0.3f) else Color(0xFF00D4FF),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(enabled = !isLocked) {
                isPressed = true
                onClick()
            }
            .alpha(if (isLocked) 0.5f else 1f),
        contentAlignment = Alignment.Center
    ) {
        if (isLocked) {
            Text(
                text = level.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD700)
            )
        } else {
            Text(
                text = level.toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00FFFF)
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed && !isLocked) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TetrisMenuScreenPreview() {
    MaterialTheme {
        TetrisMenuScreen()
    }
}