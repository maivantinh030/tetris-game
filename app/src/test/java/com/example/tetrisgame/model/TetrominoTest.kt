package com.example.tetrisgame.model

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for the Tetromino model classes.
 */
class TetrominoTest {

    @Test
    fun position_creation_and_operations() {
        val pos1 = Position(5, 10)
        assertEquals(5, pos1.x)
        assertEquals(10, pos1.y)
        
        val pos2 = pos1.plus(2, 3)
        assertEquals(7, pos2.x)
        assertEquals(13, pos2.y)
        
        val pos3 = pos1 + Position(1, -1)
        assertEquals(6, pos3.x)
        assertEquals(9, pos3.y)
    }

    @Test
    fun tetromino_type_random() {
        val types = mutableSetOf<TetrominoType>()
        repeat(100) {
            types.add(TetrominoType.random())
        }
        // After 100 iterations, we should have seen all 7 types
        assertTrue("Should have all 7 tetromino types", types.size >= 7)
    }

    @Test
    fun tetromino_creation_and_movement() {
        val tetromino = TetrominoFactory.create(TetrominoType.T)
        assertEquals(TetrominoType.T, tetromino.type)
        assertEquals(Position(3, 0), tetromino.position)
        assertEquals(0, tetromino.rotationState)
        
        val moved = tetromino.moveDown().moveRight()
        assertEquals(Position(4, 1), moved.position)
        
        val rotated = tetromino.rotate()
        assertEquals(1, rotated.rotationState)
    }

    @Test
    fun tetromino_rotation() {
        val tetromino = TetrominoFactory.create(TetrominoType.I)
        
        // Test clockwise rotation
        val rotated1 = tetromino.rotate()
        assertEquals(1, rotated1.rotationState)
        
        val rotated2 = rotated1.rotate()
        assertEquals(2, rotated2.rotationState)
        
        val rotated3 = rotated2.rotate()
        assertEquals(3, rotated3.rotationState)
        
        val rotated4 = rotated3.rotate()
        assertEquals(0, rotated4.rotationState)
        
        // Test counter-clockwise rotation
        val ccw = tetromino.rotateCounterClockwise()
        assertEquals(3, ccw.rotationState)
    }

    @Test
    fun tetromino_blocks_calculation() {
        val tetromino = TetrominoFactory.create(TetrominoType.O, Position(0, 0))
        val blocks = tetromino.getBlocks()
        
        // O-piece should have 4 blocks
        assertEquals(4, blocks.size)
        
        // Verify the positions for O-piece at (0,0)
        assertTrue(blocks.contains(Position(1, 1)))
        assertTrue(blocks.contains(Position(2, 1)))
        assertTrue(blocks.contains(Position(1, 2)))
        assertTrue(blocks.contains(Position(2, 2)))
    }

    @Test
    fun tetromino_shape_consistency() {
        // Test that all tetromino types have shapes defined
        TetrominoType.values().forEach { type ->
            val tetromino = TetrominoFactory.create(type)
            val shape = tetromino.getShape()
            
            // Each shape should be 4x4
            assertEquals(4, shape.size)
            shape.forEach { row ->
                assertEquals(4, row.size)
            }
            
            // Each tetromino should have exactly 4 blocks (except for testing)
            val blockCount = shape.sumOf { row -> row.count { it } }
            assertEquals("Tetromino $type should have 4 blocks", 4, blockCount)
        }
    }

    @Test
    fun tetromino_factory_random_creation() {
        val randomTetromino = TetrominoFactory.createRandom()
        assertNotNull(randomTetromino)
        assertEquals(Position(3, 0), randomTetromino.position)
        assertEquals(0, randomTetromino.rotationState)
    }

    @Test
    fun tetromino_factory_custom_creation() {
        val tetromino = TetrominoFactory.createAt(TetrominoType.L, 5, 10, 2)
        assertEquals(TetrominoType.L, tetromino.type)
        assertEquals(Position(5, 10), tetromino.position)
        assertEquals(2, tetromino.rotationState)
    }

    @Test
    fun tetromino_bag_randomization() {
        val bag = TetrominoFactory.createRandomBag().take(14).toList()
        
        // First 7 should contain all types
        val firstSeven = bag.take(7).map { it.type }.toSet()
        assertEquals(7, firstSeven.size)
        assertEquals(TetrominoType.values().toSet(), firstSeven)
        
        // Second 7 should also contain all types
        val secondSeven = bag.drop(7).take(7).map { it.type }.toSet()
        assertEquals(7, secondSeven.size)
        assertEquals(TetrominoType.values().toSet(), secondSeven)
    }
}