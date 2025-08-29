package com.example.tetrisgame.model

/**
 * Enum representing the 7 standard Tetris pieces (Tetrominos).
 * Each piece has a unique shape and color.
 */
enum class TetrominoType {
    /** I-piece - straight line */
    I,
    
    /** O-piece - square/box */
    O,
    
    /** T-piece - T-shape */
    T,
    
    /** S-piece - S-shaped zigzag */
    S,
    
    /** Z-piece - Z-shaped zigzag */
    Z,
    
    /** J-piece - L-shape (mirrored) */
    J,
    
    /** L-piece - L-shape */
    L;
    
    companion object {
        /**
         * Returns a random tetromino type.
         */
        fun random(): TetrominoType = values().random()
    }
}