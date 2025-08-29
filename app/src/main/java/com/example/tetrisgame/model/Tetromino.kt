package com.example.tetrisgame.model

/**
 * Represents a Tetris piece (Tetromino) with its type, position, and rotation state.
 * Each tetromino is defined by a 4x4 matrix for each of its 4 rotation states.
 *
 * @param type The type of tetromino (I, O, T, S, Z, J, L)
 * @param position The current position on the game board
 * @param rotationState The current rotation state (0-3, representing 0°, 90°, 180°, 270°)
 */
data class Tetromino(
    val type: TetrominoType,
    val position: Position,
    val rotationState: Int = 0
) {
    
    /**
     * Returns the shape matrix for the current tetromino type and rotation state.
     * Each shape is represented as a 4x4 boolean matrix where true indicates a filled block.
     */
    fun getShape(): Array<Array<Boolean>> {
        return shapes[type]!![rotationState % 4]
    }
    
    /**
     * Returns the list of absolute positions of all blocks that make up this tetromino.
     */
    fun getBlocks(): List<Position> {
        val shape = getShape()
        val blocks = mutableListOf<Position>()
        
        for (row in shape.indices) {
            for (col in shape[row].indices) {
                if (shape[row][col]) {
                    blocks.add(Position(position.x + col, position.y + row))
                }
            }
        }
        
        return blocks
    }
    
    /**
     * Returns a new tetromino rotated 90 degrees clockwise.
     */
    fun rotate(): Tetromino {
        return copy(rotationState = (rotationState + 1) % 4)
    }
    
    /**
     * Returns a new tetromino rotated 90 degrees counter-clockwise.
     */
    fun rotateCounterClockwise(): Tetromino {
        return copy(rotationState = (rotationState + 3) % 4)
    }
    
    /**
     * Returns a new tetromino moved by the specified offset.
     */
    fun move(deltaX: Int, deltaY: Int): Tetromino {
        return copy(position = position.plus(deltaX, deltaY))
    }
    
    /**
     * Returns a new tetromino moved to the specified position.
     */
    fun moveTo(newPosition: Position): Tetromino {
        return copy(position = newPosition)
    }
    
    /**
     * Returns a new tetromino moved down by one row.
     */
    fun moveDown(): Tetromino = move(0, 1)
    
    /**
     * Returns a new tetromino moved left by one column.
     */
    fun moveLeft(): Tetromino = move(-1, 0)
    
    /**
     * Returns a new tetromino moved right by one column.
     */
    fun moveRight(): Tetromino = move(1, 0)
    
    companion object {
        /**
         * Shape definitions for all tetromino types and their rotation states.
         * Each tetromino has 4 rotation states, each defined as a 4x4 boolean matrix.
         */
        private val shapes = mapOf(
            // I-piece (straight line)
            TetrominoType.I to arrayOf(
                // 0° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(true,  true,  true,  true),
                    arrayOf(false, false, false, false),
                    arrayOf(false, false, false, false)
                ),
                // 90° rotation
                arrayOf(
                    arrayOf(false, false, true,  false),
                    arrayOf(false, false, true,  false),
                    arrayOf(false, false, true,  false),
                    arrayOf(false, false, true,  false)
                ),
                // 180° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, false, false, false),
                    arrayOf(true,  true,  true,  true),
                    arrayOf(false, false, false, false)
                ),
                // 270° rotation
                arrayOf(
                    arrayOf(false, true,  false, false),
                    arrayOf(false, true,  false, false),
                    arrayOf(false, true,  false, false),
                    arrayOf(false, true,  false, false)
                )
            ),
            
            // O-piece (square)
            TetrominoType.O to arrayOf(
                // All rotations are the same for O-piece
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, true,  true,  false),
                    arrayOf(false, true,  true,  false),
                    arrayOf(false, false, false, false)
                ),
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, true,  true,  false),
                    arrayOf(false, true,  true,  false),
                    arrayOf(false, false, false, false)
                ),
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, true,  true,  false),
                    arrayOf(false, true,  true,  false),
                    arrayOf(false, false, false, false)
                ),
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, true,  true,  false),
                    arrayOf(false, true,  true,  false),
                    arrayOf(false, false, false, false)
                )
            ),
            
            // T-piece
            TetrominoType.T to arrayOf(
                // 0° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, true,  false, false),
                    arrayOf(true,  true,  true,  false),
                    arrayOf(false, false, false, false)
                ),
                // 90° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, true,  false, false),
                    arrayOf(false, true,  true,  false),
                    arrayOf(false, true,  false, false)
                ),
                // 180° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, false, false, false),
                    arrayOf(true,  true,  true,  false),
                    arrayOf(false, true,  false, false)
                ),
                // 270° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, true,  false, false),
                    arrayOf(true,  true,  false, false),
                    arrayOf(false, true,  false, false)
                )
            ),
            
            // S-piece
            TetrominoType.S to arrayOf(
                // 0° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, true,  true,  false),
                    arrayOf(true,  true,  false, false),
                    arrayOf(false, false, false, false)
                ),
                // 90° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, true,  false, false),
                    arrayOf(false, true,  true,  false),
                    arrayOf(false, false, true,  false)
                ),
                // 180° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, false, false, false),
                    arrayOf(false, true,  true,  false),
                    arrayOf(true,  true,  false, false)
                ),
                // 270° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(true,  false, false, false),
                    arrayOf(true,  true,  false, false),
                    arrayOf(false, true,  false, false)
                )
            ),
            
            // Z-piece
            TetrominoType.Z to arrayOf(
                // 0° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(true,  true,  false, false),
                    arrayOf(false, true,  true,  false),
                    arrayOf(false, false, false, false)
                ),
                // 90° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, false, true,  false),
                    arrayOf(false, true,  true,  false),
                    arrayOf(false, true,  false, false)
                ),
                // 180° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, false, false, false),
                    arrayOf(true,  true,  false, false),
                    arrayOf(false, true,  true,  false)
                ),
                // 270° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, true,  false, false),
                    arrayOf(true,  true,  false, false),
                    arrayOf(true,  false, false, false)
                )
            ),
            
            // J-piece
            TetrominoType.J to arrayOf(
                // 0° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(true,  false, false, false),
                    arrayOf(true,  true,  true,  false),
                    arrayOf(false, false, false, false)
                ),
                // 90° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, true,  true,  false),
                    arrayOf(false, true,  false, false),
                    arrayOf(false, true,  false, false)
                ),
                // 180° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, false, false, false),
                    arrayOf(true,  true,  true,  false),
                    arrayOf(false, false, true,  false)
                ),
                // 270° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, true,  false, false),
                    arrayOf(false, true,  false, false),
                    arrayOf(true,  true,  false, false)
                )
            ),
            
            // L-piece
            TetrominoType.L to arrayOf(
                // 0° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, false, true,  false),
                    arrayOf(true,  true,  true,  false),
                    arrayOf(false, false, false, false)
                ),
                // 90° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, true,  false, false),
                    arrayOf(false, true,  false, false),
                    arrayOf(false, true,  true,  false)
                ),
                // 180° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(false, false, false, false),
                    arrayOf(true,  true,  true,  false),
                    arrayOf(true,  false, false, false)
                ),
                // 270° rotation
                arrayOf(
                    arrayOf(false, false, false, false),
                    arrayOf(true,  true,  false, false),
                    arrayOf(false, true,  false, false),
                    arrayOf(false, true,  false, false)
                )
            )
        )
    }
}