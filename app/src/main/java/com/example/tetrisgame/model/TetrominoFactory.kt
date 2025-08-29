package com.example.tetrisgame.model

/**
 * Factory object for creating Tetromino instances.
 * Provides methods to create tetrominos at the standard spawn position.
 */
object TetrominoFactory {
    
    /** Standard spawn position for new tetrominos (top-center of a 10-wide board) */
    private val SPAWN_POSITION = Position(x = 3, y = 0)
    
    /**
     * Creates a new tetromino of the specified type at the spawn position.
     *
     * @param type The type of tetromino to create
     * @param position Optional custom position (defaults to spawn position)
     * @param rotationState Optional initial rotation state (defaults to 0)
     * @return A new Tetromino instance
     */
    fun create(
        type: TetrominoType, 
        position: Position = SPAWN_POSITION,
        rotationState: Int = 0
    ): Tetromino {
        return Tetromino(
            type = type,
            position = position,
            rotationState = rotationState
        )
    }
    
    /**
     * Creates a random tetromino at the spawn position.
     *
     * @param position Optional custom position (defaults to spawn position)
     * @param rotationState Optional initial rotation state (defaults to 0)
     * @return A new random Tetromino instance
     */
    fun createRandom(
        position: Position = SPAWN_POSITION,
        rotationState: Int = 0
    ): Tetromino {
        return create(
            type = TetrominoType.random(),
            position = position,
            rotationState = rotationState
        )
    }
    
    /**
     * Creates a sequence of tetrominos using the standard 7-bag randomization system.
     * This ensures that all 7 pieces appear once before any piece can appear again.
     *
     * @return An infinite sequence of tetrominos with proper randomization
     */
    fun createRandomBag(): Sequence<Tetromino> = sequence {
        while (true) {
            val bag = TetrominoType.values().toMutableList()
            bag.shuffle()
            bag.forEach { type ->
                yield(create(type))
            }
        }
    }
    
    /**
     * Creates a tetromino at a specific position with custom rotation.
     * Useful for testing or special game modes.
     */
    fun createAt(
        type: TetrominoType,
        x: Int,
        y: Int,
        rotationState: Int = 0
    ): Tetromino {
        return create(
            type = type,
            position = Position(x, y),
            rotationState = rotationState
        )
    }
}