// TetrisManager.kt
package com.example.tetrisgame

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class GameMode {
    CLASSIC, CHALLENGE
}

enum class TargetType {
    LINES, SCORE
}

data class ChallengeLevelConfig(
    val level: Int,
    val targetType: TargetType,
    val targetValue: Int,
    val piecesLimit: Int,
    var isOpen: Boolean = false,
    val presetGrid: Array<Array<Int>> = Array(20) { Array(10) { 0 } }
)

class TetrisManager(
    private val isInvisibleMode: Boolean = false,
    val gameMode: GameMode = GameMode.CLASSIC,
    private val challengeLevel: Int? = null
) {
    companion object {
        val sharedChallengeLevels = listOf(
            ChallengeLevelConfig(
                level = 1,
                targetType = TargetType.LINES,
                targetValue = 3,
                piecesLimit = 18,
                isOpen = true,
                presetGrid = Array(18) { Array(10) { 0 } } + arrayOf(
                    arrayOf(1,1,1,1,1,1,1,1,0,1),
                    arrayOf(0,0,0,0,0,0,0,0,1,0)
                )
            ),
            ChallengeLevelConfig(
                level = 2,
                targetType = TargetType.SCORE,
                targetValue = 500,
                piecesLimit = 15,
                isOpen = false,
                presetGrid = Array(19) { Array(10) { 0 } } + arrayOf(
                    arrayOf(2,2,2,2,0,2,2,2,2,2)
                )
            ),
            ChallengeLevelConfig(
                level = 3,
                targetType = TargetType.LINES,
                targetValue = 4,
                piecesLimit = 18,
                isOpen = false,
                presetGrid = Array(17) { Array(10) { 0 } } + arrayOf(
                    arrayOf(3,0,3,0,3,3,3,3,3,0),
                    arrayOf(1,1,1,1,1,1,1,1,0,1),
                    arrayOf(0,4,0,4,0,0,4,4,4,0)
                )
            ),
            ChallengeLevelConfig(
                level = 4,
                targetType = TargetType.SCORE,
                targetValue = 800,
                piecesLimit = 20,
                isOpen = false,
                presetGrid = Array(18) { Array(10) { 0 } } + arrayOf(
                    arrayOf(5,5,5,5,5,0,0,0,0,0),
                    arrayOf(0,0,5,5,5,5,5,5,5,0)
                )
            ),
            ChallengeLevelConfig(
                level = 5,
                targetType = TargetType.LINES,
                targetValue = 5,
                piecesLimit = 22,
                isOpen = false,
                presetGrid = Array(16) { Array(10) { 0 } } + arrayOf(
                    arrayOf(0,6,0,6,6,6,6,6,6,0),
                    arrayOf(2,2,2,2,2,2,0,2,2,2),
                    arrayOf(1,1,1,1,1,1,1,0,1,1),
                    arrayOf(3,3,3,3,3,0,3,3,3,3)
                )
            ),
            ChallengeLevelConfig(
                level = 6,
                targetType = TargetType.SCORE,
                targetValue = 1200,
                piecesLimit = 25,
                isOpen = false,
                presetGrid = Array(17) { Array(10) { 0 } } + arrayOf(
                    arrayOf(4,4,4,4,4,4,4,4,0,4),
                    arrayOf(7,7,7,7,7,7,7,7,0,7),
                    arrayOf(0,0,0,0,0,7,7,7,7,7)
                )
            ),
            ChallengeLevelConfig(
                level = 7,
                targetType = TargetType.LINES,
                targetValue = 6,
                piecesLimit = 28,
                isOpen = false,
                presetGrid = Array(15) { Array(10) { 0 } } + arrayOf(
                    arrayOf(8,0,8,8,8,8,8,8,8,0),
                    arrayOf(5,5,5,5,5,5,5,5,0,5),
                    arrayOf(1,1,1,0,1,1,1,1,1,1),
                    arrayOf(2,2,2,2,2,2,2,2,0,2),
                    arrayOf(3,3,3,3,3,3,3,3,0,3)
                )
            ),
            ChallengeLevelConfig(
                level = 8,
                targetType = TargetType.SCORE,
                targetValue = 1600,
                piecesLimit = 30,
                isOpen = false,
                presetGrid = Array(16) { Array(10) { 0 } } + arrayOf(
                    arrayOf(6,0,6,0,6,0,6,0,6,0),
                    arrayOf(4,4,4,4,4,4,4,4,4,4),
                    arrayOf(9,9,9,9,9,9,0,9,9,0),
                    arrayOf(0,9,9,9,9,9,9,9,9,9)
                )
            ),
            ChallengeLevelConfig(
                level = 9,
                targetType = TargetType.LINES,
                targetValue = 7,
                piecesLimit = 32,
                isOpen = false,
                presetGrid = Array(14) { Array(10) { 0 } } + arrayOf(
                    arrayOf(0,10,0,10,10,10,10,10,10,0),
                    arrayOf(7,7,7,7,7,7,0,7,7,7),
                    arrayOf(5,5,5,5,5,0,5,5,5,5),
                    arrayOf(3,3,3,3,3,3,3,0,3,3),
                    arrayOf(1,1,1,1,1,1,1,1,0,1),
                    arrayOf(2,2,2,2,2,2,2,2,0,2)
                )
            ),
            ChallengeLevelConfig(
                level = 10,
                targetType = TargetType.SCORE,
                targetValue = 2000,
                piecesLimit = 35,
                isOpen = false,
                presetGrid = Array(15) { Array(10) { 0 } } + arrayOf(
                    arrayOf(4,4,0,4,4,4,4,4,4,4),
                    arrayOf(0,6,0,6,6,6,6,6,6,0),
                    arrayOf(8,8,8,8,8,8,0,8,8,8),
                    arrayOf(1,1,1,1,1,1,1,1,0,1),
                    arrayOf(2,2,0,2,2,2,2,2,2,2)
                )
            ),
            ChallengeLevelConfig(
                level = 11,
                targetType = TargetType.LINES,
                targetValue = 8,
                piecesLimit = 38,
                isOpen = false,
                presetGrid = Array(13) { Array(10) { 0 } } + arrayOf(
                    arrayOf(9,0,9,0,9,9,9,9,9,0),
                    arrayOf(7,7,7,7,7,7,7,7,0,7),
                    arrayOf(5,5,0,5,5,5,5,5,5,5),
                    arrayOf(3,3,3,3,3,3,0,3,3,3),
                    arrayOf(1,1,1,1,1,1,1,0,1,1),
                    arrayOf(2,2,2,2,2,2,2,2,0,2),
                    arrayOf(4,4,4,4,4,4,4,4,0,4)
                )
            ),
            ChallengeLevelConfig(
                level = 12,
                targetType = TargetType.SCORE,
                targetValue = 2500,
                piecesLimit = 40,
                isOpen = false,
                presetGrid = Array(14) { Array(10) { 0 } } + arrayOf(
                    arrayOf(6,0,6,6,0,6,6,6,6,6),
                    arrayOf(0,8,0,8,8,8,8,8,8,0),
                    arrayOf(10,10,10,10,10,10,10,10,0,10),
                    arrayOf(1,1,1,1,1,1,1,1,1,0),
                    arrayOf(2,2,2,2,2,2,2,2,2,0),
                    arrayOf(3,3,3,3,3,3,3,3,3,0)
                )
            ),
            ChallengeLevelConfig(
                level = 13,
                targetType = TargetType.LINES,
                targetValue = 9,
                piecesLimit = 42,
                isOpen = false,
                presetGrid = Array(12) { Array(10) { 0 } } + arrayOf(
                    arrayOf(0,11,0,11,0,11,11,11,11,0),
                    arrayOf(9,9,9,9,9,9,0,9,9,9),
                    arrayOf(7,7,0,7,7,7,7,7,7,7),
                    arrayOf(5,5,5,5,5,5,0,5,5,5),
                    arrayOf(3,3,3,3,3,0,3,3,3,3),
                    arrayOf(1,1,1,1,1,1,1,1,0,1),
                    arrayOf(2,2,2,2,2,2,2,0,2,2),
                    arrayOf(4,4,4,4,0,4,4,4,4,4)
                )
            ),
            ChallengeLevelConfig(
                level = 14,
                targetType = TargetType.SCORE,
                targetValue = 3000,
                piecesLimit = 45,
                isOpen = false,
                presetGrid = Array(13) { Array(10) { 0 } } + arrayOf(
                    arrayOf(12,12,12,12,0,12,12,12,12,12),
                    arrayOf(0,10,0,10,10,0,10,10,10,0),
                    arrayOf(8,8,8,8,8,8,8,8,0,8),
                    arrayOf(6,6,6,6,6,6,6,6,6,0),
                    arrayOf(4,4,4,4,4,4,4,4,4,4),
                    arrayOf(2,2,2,2,2,2,2,2,0,2),
                    arrayOf(1,1,1,1,1,1,1,1,0,1)
                )
            ),
            ChallengeLevelConfig(
                level = 15,
                targetType = TargetType.LINES,
                targetValue = 10,
                piecesLimit = 48,
                isOpen = false,
                presetGrid = Array(11) { Array(10) { 0 } } + arrayOf(
                    arrayOf(0,13,0,13,0,13,0,13,13,0),
                    arrayOf(11,11,11,11,11,11,11,0,11,11),
                    arrayOf(9,9,0,9,9,9,9,9,9,9),
                    arrayOf(7,7,7,7,7,7,0,7,7,7),
                    arrayOf(5,5,5,5,5,0,5,5,5,5),
                    arrayOf(3,3,3,3,3,3,3,3,0,3),
                    arrayOf(1,1,1,1,1,1,1,1,0,1),
                    arrayOf(2,2,2,2,2,2,0,2,2,2),
                    arrayOf(4,4,0,4,4,4,4,4,4,4)
                )
            )
        )
    }

    var grid by mutableStateOf(Array(20) { Array(10) { 0 } })
        private set
    var currentPiece by mutableStateOf<Tetromino?>(null)
        private set
    var nextPiece by mutableStateOf<Tetromino?>(null)
        private set
    var score by mutableStateOf(0)
        private set
    var isPaused by mutableStateOf(false)
    var isGameOver by mutableStateOf(false)
    var level by mutableStateOf(1)
        private set
    var isGameRunning by mutableStateOf(true)
    var line by mutableStateOf(0)
        private set
    var dropSpeed by mutableStateOf(1000L)
        private set
    var gameUpdateTrigger by mutableStateOf(0)
        private set
    var isClearing by mutableStateOf(false)
        private set
    var pendingClearRows by mutableStateOf<List<Int>>(emptyList())
        private set
    var showComboEffect by mutableStateOf(false)
    var linesCleared by mutableStateOf(0)
    var showScoreEffect by mutableStateOf(false)
    var comboCount by mutableStateOf(0)
        private set
    var lastClearWasCombo by mutableStateOf(false)
        private set
    var blockOpacity by mutableStateOf(Array(20) { Array(10) { 1f } })
        private set
    var opacityTrigger by mutableStateOf(0)
        private set
    var flashVisible by mutableStateOf(false)
    var piecesUsed by mutableStateOf(0)
        private set
    var piecesLimit by mutableStateOf(0)
        private set
    var targetType by mutableStateOf<TargetType?>(null)
        private set
    var targetRemaining by mutableStateOf(0)
        private set
    private var initialTargetValue = 0
    var currentChallengeLevel by mutableStateOf(1)
        private set
    var isWin by mutableStateOf(false)
    var challengeLevels = sharedChallengeLevels

    init {
        if (gameMode == GameMode.CHALLENGE && challengeLevel != null) {
            loadChallengeLevel(challengeLevel)
        }
    }

    private fun loadChallengeLevel(level: Int) {
        val config = challengeLevels.find { it.level == level } ?: challengeLevels.first()
        currentChallengeLevel = config.level
        targetType = config.targetType
        initialTargetValue = config.targetValue
        targetRemaining = config.targetValue
        piecesLimit = config.piecesLimit
        grid = config.presetGrid.map { it.clone() }.toTypedArray()
        blockOpacity = Array(20) { Array(10) { 1f } }
        piecesUsed = 0
        score = 0
        line = 0
        this.level = 1
        dropSpeed = 800L
        isWin = false
        isGameOver = false
        isGameRunning = true
        currentPiece = null
        nextPiece = TetrominoFactory.createStandardTetromino()
        comboCount = 0
        linesCleared = 0
        for (r in grid.indices) {
            for (c in grid[r].indices) {
                if (grid[r][c] != 0) blockOpacity[r][c] = 1f
            }
        }
    }

    fun findFullRows(): List<Int> {
        val rows = mutableListOf<Int>()
        for (r in grid.indices) {
            if (grid[r].all { it != 0 }) rows += r
        }
        if (rows.isNotEmpty()) {
            if (lastClearWasCombo) {
                comboCount++
            } else {
                comboCount = 1
            }
            lastClearWasCombo = true
            if (comboCount >= 2) {
                showComboEffect = true
            }
        } else {
            lastClearWasCombo = false
            comboCount = 0
        }
        return rows
    }

    fun applyClearedRows(rowsToClear: List<Int>) {
        if (rowsToClear.isEmpty()) return
        val newGrid = grid.map { it.clone() }.toTypedArray()
        if (isInvisibleMode) {
            blockOpacity = Array(20) { Array(10) { 1f } }
        }
        var writeIndex = grid.size - 1
        var clearedThisTime = 0
        for (readIndex in grid.size - 1 downTo 0) {
            if (readIndex in rowsToClear) {
                clearedThisTime++
            } else {
                newGrid[writeIndex] = grid[readIndex].clone()
                writeIndex--
            }
        }
        for (i in 0 until clearedThisTime) {
            newGrid[i] = Array(10) { 0 }
        }

        if (clearedThisTime > 0) {
            grid = newGrid
            line += clearedThisTime
            linesCleared += clearedThisTime
            val baseScore = when (clearedThisTime) {
                1 -> 100 * level * comboCount
                2 -> 300 * level * comboCount
                3 -> 500 * level * comboCount
                4 -> 800 * level * comboCount
                else -> 0
            }
            showScoreEffect = true
            score += baseScore

            if (gameMode == GameMode.CHALLENGE) {
                when (targetType) {
                    TargetType.LINES -> {
                        targetRemaining = maxOf(0, targetRemaining - clearedThisTime)
                        if (targetRemaining <= 0) {
                            isWin = true
                            if (currentChallengeLevel < challengeLevels.size) {
                                challengeLevels[currentChallengeLevel].isOpen = true
                            }
                            isGameRunning = false
                        }
                    }
                    TargetType.SCORE -> {
                        targetRemaining = maxOf(0, targetRemaining - baseScore)
                        if (targetRemaining <= 0) {
                            isWin = true
                            if (currentChallengeLevel < challengeLevels.size) {
                                challengeLevels[currentChallengeLevel].isOpen = true
                            }
                            isGameRunning = false
                        }
                    }
                    else -> {}
                }
            }
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

        if (gameMode == GameMode.CHALLENGE) {
            piecesUsed++
            if (piecesUsed >= piecesLimit) {
                if (targetRemaining > 0) {
                    isGameRunning = false
                    isGameOver = true
                } else {
                    isWin = true
                }
            }
        }

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
            if (isInvisibleMode) opacityTrigger++
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
                    isClearing = true
                } else {
                    spawnNewPiece()
                }
            }
        }
    }

    fun fastDropPiece() {
        currentPiece?.let { piece ->
            var newY = piece.position.y
            val maxY = grid.size
            while (newY < maxY && !checkCollision(dx = 0, dy = 1, rotatedShape = piece.getRotatedShape())) {
                newY += 1
                currentPiece = piece.copy(position = Position(piece.position.x, newY))
            }
            placePiece()
            val rows = findFullRows()
            if (rows.isNotEmpty()) {
                pendingClearRows = rows
                isClearing = true
            } else {
                spawnNewPiece()
            }
        }
    }

    fun rotatePiece() {
        currentPiece?.let { piece ->
            val newRotation = (piece.rotation + 1) % 4
            val tempPiece = piece.copy(rotation = newRotation)
            val rotatedShape = tempPiece.getRotatedShape()
            if (!checkCollision(0, 0, rotatedShape)) {
                currentPiece = tempPiece
                gameUpdateTrigger++
            }
        }
    }

    fun resetGame() {
        if (gameMode == GameMode.CHALLENGE && challengeLevel != null) {
            loadChallengeLevel(currentChallengeLevel)
        } else {
            grid = Array(20) { Array(10) { 0 } }
            blockOpacity = Array(20) { Array(10) { 1f } }
            isPaused = false
            score = 0
            level = 1
            line = 0
            dropSpeed = 1000L
            currentPiece = null
            nextPiece = null
            showComboEffect = false
            opacityTrigger = 0
            gameUpdateTrigger++
            comboCount = 0
            linesCleared = 0
            showScoreEffect = false
            lastClearWasCombo = false
            isClearing = false
            pendingClearRows = emptyList()
            nextPiece = TetrominoFactory.createStandardTetromino()
            piecesUsed = 0
        }
    }

    fun finishClearAnimation() {
        applyClearedRows(pendingClearRows)
        pendingClearRows = emptyList()
        isClearing = false
        spawnNewPiece()
    }

    fun hideBlocksAfterDelay() {
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
    }

    fun toggleFlash() {
        flashVisible = !flashVisible
    }

    fun nextChallengeLevel() {
        if (gameMode == GameMode.CHALLENGE && currentChallengeLevel < challengeLevels.size) {
            val nextLevelIndex = currentChallengeLevel
            if (nextLevelIndex < challengeLevels.size) {
                challengeLevels[nextLevelIndex].isOpen = true
            }
            loadChallengeLevel(currentChallengeLevel + 1)
        }
    }
}