package com.example.tetrisgame

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

enum class GameMode {
    CLASSIC, CHALLENGE, INVISIBLE
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
    val isInvisibleMode: Boolean = false,
    val gameMode: GameMode = GameMode.CLASSIC,
    val challengeLevel: Int? = null
) {
    companion object {
        val sharedChallengeLevels = listOf(
            // LEVEL 1: Rất dễ - Học cách chơi
            ChallengeLevelConfig(
                level = 1,
                targetType = TargetType.LINES,
                targetValue = 2,
                piecesLimit = 15,
                isOpen = true,
                presetGrid = Array(18) { Array(10) { 0 } } + arrayOf(
                    arrayOf(1,1,1,1,0,0,1,1,1,1),
                    arrayOf(2,2,2,0,0,0,0,2,2,2)
                )
            ),

            // LEVEL 2: Dễ - Học xếp cơ bản
            ChallengeLevelConfig(
                level = 2,
                targetType = TargetType.SCORE,
                targetValue = 300,
                piecesLimit = 18,
                isOpen = false,
                presetGrid = Array(18) { Array(10) { 0 } } + arrayOf(
                    arrayOf(3,3,0,0,4,4,0,0,5,5),
                    arrayOf(0,6,6,6,0,0,7,7,7,0)
                )
            ),

            // LEVEL 3: Dễ - Nhiều lỗ hổng
            ChallengeLevelConfig(
                level = 3,
                targetType = TargetType.LINES,
                targetValue = 3,
                piecesLimit = 20,
                isOpen = false,
                presetGrid = Array(17) { Array(10) { 0 } } + arrayOf(
                    arrayOf(1,1,0,2,2,0,3,3,0,4),
                    arrayOf(0,5,5,0,6,6,0,7,7,0),
                    arrayOf(8,0,0,1,0,0,2,0,0,3)
                )
            ),

            // LEVEL 4: Dễ - Pattern đơn giản
            ChallengeLevelConfig(
                level = 4,
                targetType = TargetType.SCORE,
                targetValue = 400,
                piecesLimit = 22,
                isOpen = false,
                presetGrid = Array(17) { Array(10) { 0 } } + arrayOf(
                    arrayOf(0,0,4,4,4,4,4,4,0,0),
                    arrayOf(5,5,0,0,0,0,0,0,6,6),
                    arrayOf(0,7,7,7,0,0,8,8,8,0)
                )
            ),

            // LEVEL 5: Dễ - Chuẩn bị nâng cấp
            ChallengeLevelConfig(
                level = 5,
                targetType = TargetType.LINES,
                targetValue = 3,
                piecesLimit = 24,
                isOpen = false,
                presetGrid = Array(17) { Array(10) { 0 } } + arrayOf(
                    arrayOf(1,0,2,0,3,0,4,0,5,0),
                    arrayOf(0,6,0,7,0,8,0,1,0,2),
                    arrayOf(3,3,3,0,0,0,0,4,4,4)
                )
            ),

            // LEVEL 6: Trung bình - Tăng độ khó
            ChallengeLevelConfig(
                level = 6,
                targetType = TargetType.SCORE,
                targetValue = 600,
                piecesLimit = 26,
                isOpen = false,
                presetGrid = Array(17) { Array(10) { 0 } } + arrayOf(
                    arrayOf(5,5,5,0,0,0,0,6,6,6),
                    arrayOf(0,0,7,7,0,0,8,8,0,0),
                    arrayOf(1,1,0,0,2,2,0,0,3,3)
                )
            ),

            // LEVEL 7: Trung bình - Cần suy nghĩ
            ChallengeLevelConfig(
                level = 7,
                targetType = TargetType.LINES,
                targetValue = 4,
                piecesLimit = 28,
                isOpen = false,
                presetGrid = Array(16) { Array(10) { 0 } } + arrayOf(
                    arrayOf(4,4,4,4,0,0,5,5,5,5),
                    arrayOf(0,0,0,6,6,6,6,0,0,0),
                    arrayOf(7,7,0,0,0,0,0,0,8,8),
                    arrayOf(0,1,1,1,0,0,2,2,2,0)
                )
            ),

            // LEVEL 8: Trung bình - Pattern phức tạp hơn
            ChallengeLevelConfig(
                level = 8,
                targetType = TargetType.SCORE,
                targetValue = 800,
                piecesLimit = 30,
                isOpen = false,
                presetGrid = Array(17) { Array(10) { 0 } } + arrayOf(
                    arrayOf(3,0,0,4,4,4,4,0,0,5),
                    arrayOf(0,6,6,0,0,0,0,7,7,0),
                    arrayOf(8,0,1,1,0,0,2,2,0,3)
                )
            ),

            // LEVEL 9: Trung bình - Thử thách tư duy
            ChallengeLevelConfig(
                level = 9,
                targetType = TargetType.LINES,
                targetValue = 4,
                piecesLimit = 32,
                isOpen = false,
                presetGrid = Array(16) { Array(10) { 0 } } + arrayOf(
                    arrayOf(0,0,4,4,4,4,4,4,0,0),
                    arrayOf(5,5,0,0,0,0,0,0,6,6),
                    arrayOf(0,7,7,7,0,0,8,8,8,0),
                    arrayOf(1,0,0,2,2,2,2,0,0,3)
                )
            ),

            // LEVEL 10: Trung bình - Checkpoint
            ChallengeLevelConfig(
                level = 10,
                targetType = TargetType.SCORE,
                targetValue = 1000,
                piecesLimit = 34,
                isOpen = false,
                presetGrid = Array(17) { Array(10) { 0 } } + arrayOf(
                    arrayOf(4,4,0,0,0,0,0,0,5,5),
                    arrayOf(0,6,6,6,0,0,7,7,7,0),
                    arrayOf(8,0,0,1,1,1,1,0,0,2)
                )
            ),

            // LEVEL 11: Khó vừa - Cần kỹ năng
            ChallengeLevelConfig(
                level = 11,
                targetType = TargetType.LINES,
                targetValue = 5,
                piecesLimit = 36,
                isOpen = false,
                presetGrid = Array(16) { Array(10) { 0 } } + arrayOf(
                    arrayOf(3,3,3,0,0,0,0,4,4,4),
                    arrayOf(0,0,5,5,0,0,6,6,0,0),
                    arrayOf(7,7,0,0,0,0,0,0,8,8),
                    arrayOf(0,1,1,1,1,1,1,1,1,0)
                )
            ),

            // LEVEL 12: Khó vừa - Pattern phức tạp
            ChallengeLevelConfig(
                level = 12,
                targetType = TargetType.SCORE,
                targetValue = 1200,
                piecesLimit = 38,
                isOpen = false,
                presetGrid = Array(17) { Array(10) { 0 } } + arrayOf(
                    arrayOf(2,2,2,2,0,0,3,3,3,3),
                    arrayOf(0,0,0,4,4,4,4,0,0,0),
                    arrayOf(5,5,0,0,0,0,0,0,6,6)
                )
            ),

            // LEVEL 13: Khó vừa - Chiến thuật cao
            ChallengeLevelConfig(
                level = 13,
                targetType = TargetType.LINES,
                targetValue = 5,
                piecesLimit = 40,
                isOpen = false,
                presetGrid = Array(16) { Array(10) { 0 } } + arrayOf(
                    arrayOf(0,0,7,7,7,7,7,7,0,0),
                    arrayOf(8,8,0,0,0,0,0,0,1,1),
                    arrayOf(0,2,2,2,0,0,3,3,3,0),
                    arrayOf(4,0,0,5,5,5,5,0,0,6)
                )
            ),

            // LEVEL 14: Khó - Thử thách lớn
            ChallengeLevelConfig(
                level = 14,
                targetType = TargetType.SCORE,
                targetValue = 1500,
                piecesLimit = 42,
                isOpen = false,
                presetGrid = Array(17) { Array(10) { 0 } } + arrayOf(
                    arrayOf(7,7,7,0,0,0,0,8,8,8),
                    arrayOf(0,0,1,1,1,1,1,1,0,0),
                    arrayOf(2,2,0,0,0,0,0,0,3,3)
                )
            ),

            // LEVEL 15: Khó - Boss cuối
            ChallengeLevelConfig(
                level = 15,
                targetType = TargetType.LINES,
                targetValue = 6,
                piecesLimit = 45,
                isOpen = false,
                presetGrid = Array(16) { Array(10) { 0 } } + arrayOf(
                    arrayOf(4,4,4,4,0,0,5,5,5,5),
                    arrayOf(0,0,0,6,6,6,6,0,0,0),
                    arrayOf(7,7,7,0,0,0,0,8,8,8),
                    arrayOf(0,1,1,1,1,1,1,1,1,0)
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
    var opacityTrigger by mutableStateOf(0)
        private set
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
        when (gameMode) {
            GameMode.CHALLENGE -> {
                if (challengeLevel != null) {
                    loadChallengeLevel(challengeLevel)
                }
            }
            GameMode.INVISIBLE -> {
                // Invisible mode dùng grid mặc định, chỉ set các biến liên quan
                grid = Array(20) { Array(10) { 0 } }
                blockOpacity = Array(20) { Array(10) { 1f } }
                piecesUsed = 0
                score = 0
                line = 0
                level = 1
                dropSpeed = 800L
                isWin = false
                isGameOver = false
                isGameRunning = true
                currentPiece = null
                nextPiece = TetrominoFactory.createStandardTetromino()
                comboCount = 0
                linesCleared = 0
            }
            else -> {
                // Classic mode
                grid = Array(20) { Array(10) { 0 } }
                blockOpacity = Array(20) { Array(10) { 1f } }
                piecesUsed = 0
                score = 0
                line = 0
                level = 1
                dropSpeed = 1000L
                isWin = false
                isGameOver = false
                isGameRunning = true
                currentPiece = null
                nextPiece = TetrominoFactory.createStandardTetromino()
                comboCount = 0
                linesCleared = 0
            }
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

            val newLevel = (line / 5) + 1
            if (newLevel > level) {
                level = newLevel
                // Tăng tốc độ rơi theo level
                dropSpeed = maxOf(100L, 1000L - (level - 1) * 100L)
            }

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
        when (gameMode) {
            GameMode.CHALLENGE -> {
                if (challengeLevel != null) {
                    loadChallengeLevel(currentChallengeLevel)
                }
            }
            GameMode.INVISIBLE -> {
                grid = Array(20) { Array(10) { 0 } }
                blockOpacity = Array(20) { Array(10) { 1f } }
                isPaused = false
                score = 0
                level = 1
                line = 0
                dropSpeed = 800L
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
            else -> {
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


    fun nextChallengeLevel() {
        if (gameMode == GameMode.CHALLENGE && currentChallengeLevel < challengeLevels.size) {
            val nextLevelIndex = currentChallengeLevel
            if (nextLevelIndex < challengeLevels.size) {
                challengeLevels[nextLevelIndex].isOpen = true
            }
            loadChallengeLevel(currentChallengeLevel + 1)
        }
    }
    data class TetrisGameState(
        val grid: Array<Array<Int>>,
        val currentPiece: Tetromino?,
        val nextPiece: Tetromino?,
        val score: Int,
        val level: Int,
        val line: Int,
        val isPaused: Boolean,
        val gameMode: GameMode,
        val challengeLevel: Int?,
        val isInvisibleMode: Boolean,

    )

    fun exportState(): TetrisGameState {
        return TetrisGameState(
            grid = grid.map { it.clone() }.toTypedArray(),
            currentPiece = currentPiece,
            nextPiece = nextPiece,
            score = score,
            level = level,
            line = line,
            isPaused = isPaused,
            gameMode = gameMode,
            challengeLevel = challengeLevel,
            isInvisibleMode = isInvisibleMode
        )
    }

    fun importState(state: TetrisGameState) {
        grid = state.grid.map { it.clone() }.toTypedArray()
        currentPiece = state.currentPiece
        nextPiece = state.nextPiece
        score = state.score
        level = state.level
        line = state.line
        isPaused = state.isPaused

    }

    fun saveState(context: Context) {
        val prefs = context.getSharedPreferences("tetris_game", Context.MODE_PRIVATE)
        val gson = Gson()
        val stateJson = gson.toJson(this.exportState())
        prefs.edit().putString("game_state", stateJson).apply()
    }

    fun clearState(context: Context) {
        val prefs = context.getSharedPreferences("tetris_game", Context.MODE_PRIVATE)
        prefs.edit().remove("game_state").apply()
    }

    fun hasSavedState(context: Context): Boolean {
        val prefs = context.getSharedPreferences("tetris_game", Context.MODE_PRIVATE)
        return prefs.contains("game_state")
    }

    fun loadState(context: Context): Boolean {
        val prefs = context.getSharedPreferences("tetris_game", Context.MODE_PRIVATE)
        val gson = Gson()
        val stateJson = prefs.getString("game_state", null) ?: return false
        val state = gson.fromJson(stateJson, TetrisGameState::class.java)
        this.importState(state)
        return true
    }
}

object HighScoreManager {
    // key cho từng chế độ
    private fun getKey(mode: GameMode): String {
        return when (mode) {
            GameMode.CLASSIC -> "highscore_classic"
            GameMode.INVISIBLE -> "highscore_invisible"
            else -> "highscore_other"
        }
    }

    // Lấy list điểm top 5
    fun getHighScores(context: Context, mode: GameMode): List<Int> {
        val prefs = context.getSharedPreferences("tetris_highscore", Context.MODE_PRIVATE)
        val json = prefs.getString(getKey(mode), null) ?: return emptyList()
        val type = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(json, type)
    }

    // Thêm điểm mới, tự động giữ top 5
    fun addScore(context: Context, mode: GameMode, score: Int) {
        val scores = getHighScores(context, mode).toMutableList()
        scores.add(score)
        val topScores = scores.sortedDescending().take(5)
        val prefs = context.getSharedPreferences("tetris_highscore", Context.MODE_PRIVATE)
        val json = Gson().toJson(topScores)
        prefs.edit().putString(getKey(mode), json).apply()
    }
}