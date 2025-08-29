package com.example.tetrisgame.model

/**
 * Data class representing a position with x and y coordinates.
 * Used to track the position of tetrominos on the game board.
 *
 * @param x The horizontal coordinate
 * @param y The vertical coordinate
 */
data class Position(
    val x: Int,
    val y: Int
) {
    /**
     * Creates a new position by adding the given offsets to the current position.
     */
    fun plus(deltaX: Int, deltaY: Int): Position = Position(x + deltaX, y + deltaY)
    
    /**
     * Creates a new position by adding another position to this one.
     */
    operator fun plus(other: Position): Position = Position(x + other.x, y + other.y)
}