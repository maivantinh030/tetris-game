package com.example.tetrisgame

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Nếu là chế độ "continue", truyền vào biến isContinue=true để khôi phục trạng thái từ saveState.
 * Nếu là chơi mới thì truyền đúng chế độ/gameMode như thường.
 */
@Composable
fun TetrisGameScreen(
    navController: NavController? = null,
    isInvisibleMode: Boolean = false,
    gameMode: GameMode = GameMode.CLASSIC,
    challengeLevel: Int? = null,
    isContinue: Boolean = false // Thêm biến này để nhận biết vào từ menu "Continue"
) {
    val context = LocalContext.current

    // Nếu là chế độ continue, tạo gameManager từ trạng thái đã lưu
    val gameManager = remember(isContinue, gameMode, challengeLevel) {
        if (isContinue) {
            // Đọc trạng thái đã lưu để khởi tạo đúng mode/challenge/invisible
            val tempManager = TetrisManager()
            val loaded = tempManager.loadState(context)
            val state = tempManager.exportState()
            TetrisManager(
                isInvisibleMode = state.isInvisibleMode,
                gameMode = state.gameMode,
                challengeLevel = state.challengeLevel
            ).apply {
                this.importState(state)
            }
        } else {
            TetrisManager(isInvisibleMode, gameMode, challengeLevel)
        }
    }

    // Game loop
    LaunchedEffect(gameManager.isGameRunning, gameManager.isPaused, gameManager.isGameOver, gameManager.isClearing, gameManager.isWin) {
        if (gameManager.isGameRunning && !gameManager.isPaused && !gameManager.isGameOver && !gameManager.isWin) {
            if (gameManager.currentPiece == null && !gameManager.isClearing) {
                gameManager.spawnNewPiece()
            }
            while (gameManager.isGameRunning && !gameManager.isPaused && !gameManager.isGameOver && !gameManager.isWin && !gameManager.isClearing) {
                delay(gameManager.dropSpeed)
                gameManager.movePiece(0, 1)
            }
        }
    }

    // INVISIBLE MODE (giữ nguyên)
    LaunchedEffect(gameManager.opacityTrigger) {
        if (gameManager.opacityTrigger > 0 && gameManager.isInvisibleMode) {
            delay(3000)
            gameManager.hideBlocksAfterDelay()
            launch {
                while (gameManager.isGameRunning && gameManager.isInvisibleMode) {
                    delay(10000L)
                    gameManager.toggleFlash()
                    delay(500L)
                    gameManager.toggleFlash()
                }
            }
        }
    }

    // UI
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.testbackground),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        key(gameManager.gameUpdateTrigger) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    IconButton(
                        onClick = {
                            gameManager.isPaused = true
                            gameManager.saveState(context) // Lưu trạng thái khi pause
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .background(Color(0xFF1E4E5A), shape = CircleShape)
                            .border(4.dp, Color(0xFF00D4FF), shape = CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.pause),
                            contentDescription = "Pause",
                            tint = Color(0xFF00FFFF),
                            modifier = Modifier.size(30.dp)
                        )
                    }
                }

                // Info Row: Tùy mode
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    when (gameManager.gameMode) {
                        GameMode.CLASSIC -> {
                            InforBox("Score", gameManager.score.toString())
                            InforBox("Level", gameManager.level.toString())
                            NextBox(gameManager.nextPiece)
                        }
                        GameMode.CHALLENGE -> {
                            InforBox("Pieces", "${gameManager.piecesUsed}/${gameManager.piecesLimit}")
                            NextBox(gameManager.nextPiece)
                            InforBox("${gameManager.targetType}","${gameManager.targetRemaining}")
                        }
                        GameMode.INVISIBLE -> {
                            InforBox("Score", gameManager.score.toString())
                            InforBox("Level", gameManager.level.toString())
                            NextBox(gameManager.nextPiece)
                        }
                    }
                }

                gameGrid(
                    grid = gameManager.grid,
                    currentPiece = gameManager.currentPiece,
                    gameUpdateTrigger = gameManager.gameUpdateTrigger,
                    onSwipe = { direction ->
                        when (direction) {
                            "LEFT" -> gameManager.movePiece(-1, 0)
                            "RIGHT" -> gameManager.movePiece(1, 0)
                            "DOWN" -> gameManager.movePiece(0, 1)
                            "FASTDROP" -> gameManager.fastDropPiece()
                        }
                    },
                    onTap = { if (!gameManager.isClearing) gameManager.rotatePiece() },
                    isClearing = gameManager.isClearing,
                    rowsToClear = gameManager.pendingClearRows,
                    onClearAnimationDone = { gameManager.finishClearAnimation() },
                    blockOpacity = gameManager.blockOpacity
                )
            }
        }

        // Combo Effect (giữ nguyên)
        if (gameManager.showComboEffect) {
            ComboEffect(
                lineCleared = gameManager.linesCleared,
                comboCount = gameManager.comboCount,
                gameManager.level,
                onAnimationComplete = {
                    gameManager.showComboEffect = false
                }
            )
        }

        // Menu pause
        if (gameManager.isPaused) {
            PauseMenu(
                onResume = {
                    gameManager.isPaused = false
                    if (!gameManager.isGameRunning) {
                        gameManager.isGameRunning = true
                    }
                },
                onRestart = {
                    gameManager.isGameRunning = false
                    gameManager.isPaused = false
                    gameManager.resetGame()
                    gameManager.clearState(context) // Xoá trạng thái lưu khi chơi lại
                    gameManager.isGameRunning = true
                },
                onExit = {
                    gameManager.clearState(context)
                    navController?.navigate("menu")
                },
                currentScore = gameManager.score,
                currentLevel = gameManager.level,
                linesCleared = gameManager.line
            )
        }

        if (gameManager.showScoreEffect && gameManager.comboCount < 2) {
            ScoreEffect(
                linesCleared = gameManager.linesCleared,
                level = gameManager.level,
                onAnimationComplete = {
                    gameManager.showScoreEffect = false
                    gameManager.linesCleared = 0
                }
            )
        }

        // Challenge: Win Menu
        if (gameManager.isWin) {
            WinMenu(
                onRestart = {
                    gameManager.isWin = false
                    gameManager.isGameRunning = false
                    gameManager.resetGame()
                    gameManager.clearState(context)
                    gameManager.isGameRunning = true
                },
                onNextLevel = {
                    
                    gameManager.nextChallengeLevel()
                    gameManager.isWin = false
                    gameManager.clearState(context)
                    gameManager.isGameRunning = true
                },
                onExit = {
                    gameManager.clearState(context)
                    navController?.navigate("menu")
                },
                score = gameManager.score,
                lines = gameManager.line,
                level = gameManager.currentChallengeLevel
            )
        }

        // Lose/GameOver (classic/challenge)
        if (gameManager.isGameOver) {
            if (gameManager.gameMode == GameMode.CHALLENGE) {
                LoseMenu(
                    onRestart = {
                        gameManager.isGameRunning = false
                        gameManager.isGameOver = false
                        gameManager.resetGame()
                        gameManager.clearState(context)
                        gameManager.isGameRunning = true
                    },
                    onExit = {
                        gameManager.clearState(context)
                        navController?.navigate("menu")
                    },
                    score = gameManager.score,
                    lines = gameManager.line,
                    level = gameManager.currentChallengeLevel,
                    targetType = gameManager.targetType!!,
                    targetValue = gameManager.targetRemaining,
                    piecesUsed = gameManager.piecesUsed,
                    piecesLimit = gameManager.piecesLimit
                )
            } else {

                GameOverMenu(
                    onRestart = {
                        gameManager.isGameRunning = false
                        gameManager.isGameOver = false
                        gameManager.resetGame()
                        gameManager.clearState(context)
                        gameManager.isGameRunning = true
                        HighScoreManager.addScore(context, gameMode, gameManager.score)
                    },
                    onExit = {
                        gameManager.clearState(context)
                        navController?.navigate("menu")
                        HighScoreManager.addScore(context, gameMode, gameManager.score)
                    },
                    gameManager.score,
                    gameManager.level,
                    gameManager.line
                )
            }
        }
    }
}



// Preview cho từng chế độ
@Preview
@Composable
fun TetrisGameScreenPreview() {
    MaterialTheme {
        TetrisGameScreen(gameMode = GameMode.CLASSIC)
    }
}

@Preview
@Composable
fun InvisiblePreview() {
    MaterialTheme {
        TetrisGameScreen(gameMode = GameMode.INVISIBLE, isInvisibleMode = true)
    }
}

@Preview
@Composable
fun ChallengePreview() {
    MaterialTheme {
        TetrisGameScreen(gameMode = GameMode.CHALLENGE, challengeLevel = 1)
    }
}