package com.example.tetrisgame.model

/**
 * Example usage demonstrating the Tetromino classes functionality.
 * This file shows how to use the tetromino system in a real game.
 */
class TetrominoExample {
    
    fun demonstrateUsage() {
        println("=== Tetromino System Demonstration ===\n")
        
        // 1. Create different types of tetrominos
        println("1. Creating tetrominos:")
        val iPiece = TetrominoFactory.create(TetrominoType.I)
        val tPiece = TetrominoFactory.create(TetrominoType.T)
        val randomPiece = TetrominoFactory.createRandom()
        
        println("   I-piece at ${iPiece.position}")
        println("   T-piece at ${tPiece.position}")
        println("   Random piece (${randomPiece.type}) at ${randomPiece.position}")
        
        // 2. Demonstrate movement
        println("\n2. Moving pieces:")
        val movedT = tPiece.moveDown().moveRight().moveRight()
        println("   T-piece moved to ${movedT.position}")
        
        // 3. Demonstrate rotation
        println("\n3. Rotating pieces:")
        val rotatedI = iPiece.rotate()
        println("   I-piece rotated from state ${iPiece.rotationState} to ${rotatedI.rotationState}")
        
        // 4. Get block positions
        println("\n4. Block positions:")
        val blocks = tPiece.getBlocks()
        println("   T-piece blocks at: ${blocks.joinToString(", ") { "(${it.x},${it.y})" }}")
        
        // 5. Demonstrate bag randomization
        println("\n5. Bag randomization (ensuring all 7 pieces appear):")
        val bag = TetrominoFactory.createRandomBag().take(7).toList()
        val types = bag.map { it.type }.distinct()
        println("   Generated ${types.size} unique types: ${types.joinToString(", ")}")
        
        // 6. Custom positioning
        println("\n6. Custom positioning:")
        val customPiece = TetrominoFactory.createAt(TetrominoType.L, 5, 10, 2)
        println("   L-piece at (${customPiece.position.x}, ${customPiece.position.y}) with rotation ${customPiece.rotationState}")
        
        println("\n=== Demonstration Complete ===")
    }
    
    /**
     * Display the shape of a tetromino in ASCII art
     */
    fun displayShape(tetromino: Tetromino) {
        println("${tetromino.type}-piece (rotation ${tetromino.rotationState}):")
        val shape = tetromino.getShape()
        
        for (row in shape) {
            val line = row.joinToString("") { if (it) "█" else "·" }
            println("  $line")
        }
        println()
    }
    
    /**
     * Show all rotation states for a tetromino type
     */
    fun showAllRotations(type: TetrominoType) {
        println("All rotations for $type-piece:")
        for (rotation in 0..3) {
            val tetromino = TetrominoFactory.create(type, Position(0, 0), rotation)
            print("  ${rotation * 90}°: ")
            val shape = tetromino.getShape()
            val compactView = shape.joinToString(" ") { row ->
                row.joinToString("") { if (it) "█" else "·" }
            }
            println(compactView)
        }
        println()
    }
}

/**
 * Run a complete demonstration of the tetromino system
 */
fun main() {
    val demo = TetrominoExample()
    demo.demonstrateUsage()
    
    println("\n=== Shape Visualization ===")
    TetrominoType.values().forEach { type ->
        val tetromino = TetrominoFactory.create(type, Position(0, 0))
        demo.displayShape(tetromino)
    }
    
    println("=== Rotation Examples ===")
    demo.showAllRotations(TetrominoType.T)
    demo.showAllRotations(TetrominoType.I)
}