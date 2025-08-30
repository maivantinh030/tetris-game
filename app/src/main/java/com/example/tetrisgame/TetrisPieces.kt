package com.example.tetrisgame

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class Position(val x: Int, val y: Int)

data class Tetromino(
    val shape: Array<Array<Int>>,
    val color: Int,
    var position: Position,
    var rotation: Int = 0
){
    fun getRotatedShape(): Array<Array<Int>> {
        var rotatedShape = shape
        repeat(rotation % 4) {
            rotatedShape = rotateMatrix(rotatedShape)
        }
        return rotatedShape
    }

    private fun rotateMatrix(matrix: Array<Array<Int>>): Array<Array<Int>> {
        val rows = matrix.size
        val cols = matrix[0].size
        val rotated = Array(cols) { Array(rows) { 0 } }

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                rotated[j][rows - 1 - i] = matrix[i][j]
            }
        }
        return rotated
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Tetromino
        return shape.contentDeepEquals(other.shape) &&
                color == other.color &&
                position == other.position &&
                rotation == other.rotation
    }

    override fun hashCode(): Int {
        var result = shape.contentDeepHashCode()
        result = 31 * result + color
        result = 31 * result + position.hashCode()
        result = 31 * result + rotation
        return result
    }

}

object TetrominoShapes {
    val I = arrayOf(
        arrayOf(1, 1, 1, 1)
    )

    val O = arrayOf(
        arrayOf(1, 1),
        arrayOf(1, 1)
    )

    val T = arrayOf(
        arrayOf(0, 1, 0),
        arrayOf(1, 1, 1)
    )

    val S = arrayOf(
        arrayOf(0, 1, 1),
        arrayOf(1, 1, 0)
    )

    val Z = arrayOf(
        arrayOf(1, 1, 0)    ,
        arrayOf(0, 1, 1)
    )

    val J = arrayOf(
        arrayOf(1, 0, 0),
        arrayOf(1, 1, 1)
    )

    val L = arrayOf(
        arrayOf(0, 0, 1),
        arrayOf(1, 1, 1)
    )
}


object TetrominoColors {
    // Định nghĩa các màu cố định cho từng loại tetromino (theo chuẩn Tetris)
    val standardColors = mapOf(
        "I" to Color(0xFF00FFFF), // Cyan
        "O" to Color(0xFFFFFF00), // Yellow
        "T" to Color(0xFF800080), // Purple
        "S" to Color(0xFF00FF00), // Green
        "Z" to Color(0xFFFF0000), // Red
        "J" to Color(0xFF0000FF), // Blue
        "L" to Color(0xFFFFA500)  // Orange
    )

    // Danh sách màu ngẫu nhiên đẹp
    val randomColors = listOf(
        Color(0xFFFF6B6B), // Red
        Color(0xFF4ECDC4), // Teal
        Color(0xFF45B7D1), // Blue
        Color(0xFF96CEB4), // Green
        Color(0xFFFECA57), // Yellow
        Color(0xFFFF9FF3), // Pink
        Color(0xFFB8860B), // Dark Golden Rod
        Color(0xFF9B59B6), // Purple
        Color(0xFFE67E22), // Orange
        Color(0xFF1ABC9C), // Turquoise
        Color(0xFFE74C3C), // Alizarin
        Color(0xFF2897E3), // Peter River
        Color(0xFF2ECC71), // Emerald
        Color(0xFFF1C40F), // Sun Flower
        Color(0xFF9B59B6), // Amethyst
        Color(0xFFE67E22), // Carrot
        Color(0xFF34495E), // Wet Asphalt
        Color(0xFFECF0F1)  // Clouds
    )

    // Màu cho grid (0 = empty, 8 = placed pieces)
    val gridColors = mapOf(
        0 to Color.Transparent,
        8 to Color(0xFF808080) // Gray for placed pieces
    )

    fun getStandardColor(shapeType: String): Color {
        return standardColors[shapeType] ?: Color.Gray
    }

    fun getRandomColor(): Color {
        return randomColors[Random.nextInt(randomColors.size)]
    }

    fun getColorByCode(colorCode: Int): Color {
        return when {
            colorCode == 0 -> Color.Transparent
            colorCode == 8 -> Color(0xFF808080)
            colorCode in 1..18 -> randomColors.getOrElse(colorCode - 1) { Color.Gray }
            else -> Color.Gray
        }
    }
}

object TetrominoFactory {
    private val allShapes = listOf(
        "I" to TetrominoShapes.I,
        "O" to TetrominoShapes.O,
        "T" to TetrominoShapes.T,
        "S" to TetrominoShapes.S,
        "Z" to TetrominoShapes.Z,
        "J" to TetrominoShapes.J,
        "L" to TetrominoShapes.L
    )

    // Tạo tetromino với màu chuẩn
    fun createStandardTetromino(): Tetromino {
        val (shapeType, shapeArray) = allShapes[Random.nextInt(allShapes.size)]
        val shape = shapeArray.map { it.clone() }.toTypedArray()


        val colorIndex = when(shapeType) {
            "I" -> 1
            "O" -> 2
            "T" -> 3
            "S" -> 4
            "Z" -> 5
            "J" -> 6
            "L" -> 7
            else -> 1
        }

        return Tetromino(
            shape = shape,
            color = colorIndex,
            position = Position(4, 0)
        )
    }

    // Tạo tetromino với màu ngẫu nhiên
    fun createRandomColorTetromino(): Tetromino {
        val (_, shapeArray) = allShapes[Random.nextInt(allShapes.size)]
        val shape = shapeArray.map { it.clone() }.toTypedArray()
        val randomColorCode = Random.nextInt(1, TetrominoColors.randomColors.size + 1)

        return Tetromino(
            shape = shape,
            color = randomColorCode,
            position = Position(4, 0)
        )
    }

    // Tạo tetromino với màu tùy chỉnh
    fun createCustomColorTetromino(color: Color): Tetromino {
        val (_, shapeArray) = allShapes[Random.nextInt(allShapes.size)]
        val shape = shapeArray.map { it.clone() }.toTypedArray()

        return Tetromino(
            shape = shape,
            color = color.hashCode(),
            position = Position(4, 0)
        )
    }
}

// Extension function để lấy màu từ Tetromino
fun Tetromino.getColor(): Color {
    return TetrominoColors.getColorByCode(this.color)
}