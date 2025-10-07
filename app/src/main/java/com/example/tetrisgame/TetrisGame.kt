// TetrisGameScreen.kt
package com.example.tetrisgame

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TetrisGameScreen(
    navController: NavController? = null,
    isInvisibleMode: Boolean = false,
    gameMode: GameMode = GameMode.CHALLENGE,
    challengeLevel: Int? = null // Chỉ dùng nếu CHALLENGE
) {
    val gameManager = remember(gameMode, challengeLevel) {
        TetrisManager(isInvisibleMode, gameMode, challengeLevel)
    }

    // Game Loop (giữ nguyên, nhưng thêm check cho Challenge win/lose)
    LaunchedEffect(gameManager.isGameRunning, gameManager.isPaused, gameManager.isGameOver, gameManager.isClearing, gameManager.isWin) {
        if (gameManager.isGameRunning && !gameManager.isPaused && !gameManager.isGameOver && !gameManager.isWin) {
            if (gameManager.currentPiece == null && !gameManager.isClearing) {
                gameManager.spawnNewPiece()
            }

            // Game loop chính
            while (gameManager.isGameRunning && !gameManager.isPaused && !gameManager.isGameOver && !gameManager.isWin && !gameManager.isClearing) {
                delay(gameManager.dropSpeed)
                gameManager.movePiece(0, 1)
            }
        }
    }

    // INVISIBLE MODE (giữ nguyên)
    LaunchedEffect(gameManager.opacityTrigger) {
        if (gameManager.opacityTrigger > 0 && isInvisibleMode) {
            delay(3000)
            gameManager.hideBlocksAfterDelay()

            launch {
                while (gameManager.isGameRunning && isInvisibleMode) {
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
                        onClick = { gameManager.isPaused = true },
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
                    when (gameMode) {
                        GameMode.CLASSIC -> {
                            InforBox("Score", gameManager.score.toString())
                            InforBox("Level", gameManager.level.toString())
                            NextBox(gameManager.nextPiece)
                        }
                        GameMode.CHALLENGE -> {
                            // Luôn hiển thị Pieces Remaining
                            InforBox("Pieces", "${gameManager.piecesUsed}/${gameManager.piecesLimit}")
                            NextBox(gameManager.nextPiece)
                            InforBox("${gameManager.targetType}","${gameManager.targetRemaining}")

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
                    gameManager.isGameRunning = true
                },
                onExit = { navController?.navigate("menu") },
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
                    gameManager.isGameRunning = true
                },
                onNextLevel = {
                    gameManager.nextChallengeLevel()
                    gameManager.isWin = false
                    gameManager.isGameRunning = true
                },
                onExit = { navController?.navigate("menu") },
                score = gameManager.score,
                lines = gameManager.line,
                level = gameManager.currentChallengeLevel
            )
        }

        // Lose/GameOver (thống nhất cho cả Classic và Challenge lose)
        if (gameManager.isGameOver) {
            if (gameMode == GameMode.CHALLENGE) {
                LoseMenu(
                    onRestart = {
                        gameManager.isGameRunning = false
                        gameManager.isGameOver = false
                        gameManager.resetGame()
                        gameManager.isGameRunning = true
                    },
                    onExit = { navController?.navigate("menu") },
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
                        gameManager.isGameRunning = true
                    },
                    onExit = { navController?.navigate("menu") },
                    gameManager.score,
                    gameManager.level,
                    gameManager.line
                )
            }
        }
    }
}



@Preview
@Composable
fun TetrisGameScreenPreview() {
    MaterialTheme {
        TetrisGameScreen(gameMode = GameMode.CLASSIC)
    }
}

@Preview
@Composable
fun ChallengePreview() {
    MaterialTheme {
        TetrisGameScreen(gameMode = GameMode.CHALLENGE, challengeLevel = 1)
    }
}