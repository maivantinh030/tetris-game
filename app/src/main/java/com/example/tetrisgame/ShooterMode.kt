package com.example.tetrisgame

import android.content.Context
import android.provider.Settings.Global.putInt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import kotlinx.coroutines.delay
import kotlin.random.Random

data class Projectile(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float = 0.35f,
    val type: String = "basic"
)

data class Enemy(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val size: Float = 1.0f,
    val health: Int = 1,
    val shield: Boolean = false
)

data class HitEffect(
    val x: Float,
    val y: Float,
    val age: Int = 0,
    val maxAge: Int = 20
)

data class PowerUp(
    val x: Float,
    val y: Float,
    val type: String, // "health", "shield", "speed", "weapon", "single", "triple", "laser", "gold"
    val vy: Float = 0.1f
)

data class Trap(
    val x: Float,
    val y: Float,
    val duration: Int = 60
)

data class Wall(
    val x: Float,
    val y: Float,
    val health: Int = 1
)

class ShooterManager(
    val gridCols: Int,
    val gridRows: Int,
    private val context: Context // Thêm context để lưu SharedPreferences
) {
    var shooterMode by mutableStateOf(true)
    var isGameOver by mutableStateOf(false)
    var isWin by mutableStateOf(false)
    var projectiles by mutableStateOf(listOf<Projectile>())
    var enemies by mutableStateOf(listOf<Enemy>())
    var hitEffects by mutableStateOf(listOf<HitEffect>())
    var powerUps by mutableStateOf(listOf<PowerUp>())
    var traps by mutableStateOf(listOf<Trap>())
    var walls by mutableStateOf(listOf<Wall>())
    var playerX by mutableStateOf(gridCols / 2f)
    var playerY by mutableStateOf(gridRows - 1f)
    var playerHealth by mutableStateOf(100)
    var playerShield by mutableStateOf(false)
    var playerSpeedBoost by mutableStateOf(1f)
    var playerExtraWeapon by mutableStateOf(false)
    var fireMode by mutableStateOf("single")
    var gold by mutableStateOf(0)
    var enemyKillCount by mutableStateOf(0)

    fun movePlayer(dx: Float, dy: Float) {
        if (!shooterMode || isGameOver || isWin) return
        val speed = 1f * playerSpeedBoost
        playerX = (playerX + dx * speed).coerceIn(0.5f, gridCols - 0.5f)
        playerY = (playerY + dy * speed).coerceIn(0.5f, gridRows - 0.5f)
    }

    fun fire() {
        if (!shooterMode || isGameOver || isWin) return
        when (fireMode) {
            "single" -> {
                projectiles = projectiles + Projectile(
                    x = playerX,
                    y = playerY - 0.6f,
                    vx = 0f,
                    vy = -0.8f,
                    type = "basic"
                )
            }
            "triple" -> {
                projectiles = projectiles + listOf(
                    Projectile(x = playerX - 0.5f, y = playerY - 0.6f, vx = -0.2f, vy = -0.8f, type = "basic"),
                    Projectile(x = playerX, y = playerY - 0.6f, vx = 0f, vy = -0.8f, type = "basic"),
                    Projectile(x = playerX + 0.5f, y = playerY - 0.6f, vx = 0.2f, vy = -0.8f, type = "basic")
                )
            }
            "laser" -> {
                projectiles = projectiles + Projectile(
                    x = playerX,
                    y = playerY - 0.6f,
                    vx = 0f,
                    vy = -1.5f,
                    size = 0.5f,
                    type = "laser"
                )
            }
        }
    }

    fun fireMissile() {
        if (!shooterMode || !playerExtraWeapon || isGameOver || isWin) return
        projectiles = projectiles + Projectile(
            x = playerX,
            y = playerY - 0.6f,
            vx = 0f,
            vy = -1.2f,
            size = 0.5f,
            type = "missile"
        )
    }

    fun fireArrow(direction: String) {
        if (!shooterMode || isGameOver || isWin) return
        val vx = if (direction == "left") -0.8f else 0.8f
        projectiles = projectiles + Projectile(
            x = playerX,
            y = playerY,
            vx = vx,
            vy = 0f,
            type = "arrow"
        )
    }

    fun activateShield() {
        if (!shooterMode || isGameOver || isWin) return
        playerShield = true
    }

    fun placeTrap() {
        if (!shooterMode || isGameOver || isWin) return
        traps = traps + Trap(x = playerX, y = playerY - 1f)
    }

    fun boostArmor() {
        if (isGameOver || isWin) return
        playerHealth += 1
    }

    fun createWall() {
        if (!shooterMode || isGameOver || isWin) return
        walls = walls + Wall(x = playerX, y = playerY - 2f)
    }

    fun resetGame() {
        isGameOver = false
        isWin = false
        playerHealth = 3
        gold = 0
        enemyKillCount = 0
        playerShield = false
        playerSpeedBoost = 1f
        playerExtraWeapon = false
        fireMode = "single"
        projectiles = emptyList()
        enemies = emptyList()
        powerUps = emptyList()
        traps = emptyList()
        walls = emptyList()
        playerX = gridCols / 2f
        playerY = gridRows - 1f
    }

    fun saveHighscore(score: Int) {
        val sharedPrefs = context.getSharedPreferences("highscores", Context.MODE_PRIVATE)
        val scores = mutableListOf<Int>()
        for (i in 1..6) {
            val currentScore = sharedPrefs.getInt("highscore_$i", 0)
            if (currentScore > 0) scores.add(currentScore)
        }
        scores.add(score)
        scores.sortDescending()
        scores.take(6)
        sharedPrefs.edit {
            for (i in scores.indices) {
                putInt("highscore_${i + 1}", scores[i])
            }
        }
    }
}
private data class Aabb(val left: Float, val top: Float, val right: Float, val bottom: Float)
private fun rectFromCenter(cx: Float, cy: Float, half: Float): Aabb =
    Aabb(cx - half, cy - half, cx + half, cy + half)
private fun intersects(a: Aabb, b: Aabb): Boolean =
    a.left < b.right && a.right > b.left && a.top < b.bottom && a.bottom > b.top

@Composable
fun ShooterEffects(manager: ShooterManager) {
    LaunchedEffect(Unit) {
        if (manager.enemies.isEmpty()) {
            repeat(3) {
                manager.enemies = manager.enemies + spawnEnemyTop(manager.gridCols, manager.gridRows)
            }
        }
    }

    LaunchedEffect(manager.shooterMode) {
        var frameCount = 0
        while (manager.shooterMode) {
            delay(16)
            frameCount++

            // Spawn power-up ngẫu nhiên, bao gồm "gold"
            if (frameCount % Random.nextInt(100, 200) == 0) {
                val types = listOf("single", "triple", "laser", "shield", "gold","health")
                manager.powerUps = manager.powerUps + PowerUp(
                    x = Random.nextDouble(0.5, manager.gridCols - 0.5).toFloat(),
                    y = -0.5f,
                    type = types.random()
                )
            }

            manager.projectiles = manager.projectiles
                .map { p -> p.copy(x = p.x + p.vx, y = p.y + p.vy) }
                .filter { p ->
                    p.y + p.size > 0f && p.y - p.size < manager.gridRows &&
                            p.x + p.size > 0f && p.x - p.size < manager.gridCols
                }

            val cols = manager.gridCols
            val rows = manager.gridRows

            val proj = manager.projectiles.toMutableList()
            val nextEnemies = mutableListOf<Enemy>()
            val nextPowerUps = manager.powerUps.toMutableList()
            val nextTraps = mutableListOf<Trap>()
            val nextWalls = manager.walls.toMutableList()

            for (e in manager.enemies) {
                var moved = e.copy(y = e.y + e.vy, vx = 0f)
                val eRect = rectFromCenter(moved.x, moved.y, e.size * 0.5f)
                val trapped = manager.traps.any { t ->
                    val tRect = rectFromCenter(t.x, t.y, 0.5f)
                    intersects(eRect, tRect)
                }
                if (trapped) {
                    moved = moved.copy(vy = 0f)
                }

                val wallHitIdx = nextWalls.indexOfFirst { w ->
                    val wRect = rectFromCenter(w.x, w.y, 1.0f)
                    intersects(eRect, wRect)
                }
                var replace = false
                if (wallHitIdx >= 0) {
                    val wall = nextWalls[wallHitIdx]
                    nextWalls[wallHitIdx] = wall.copy(health = wall.health - 1)
                    if (nextWalls[wallHitIdx].health <= 0) {
                        nextWalls.removeAt(wallHitIdx)
                    }
                    replace = true
                    manager.enemyKillCount += 1
                }

                if (moved.y > rows + e.size) {
                    replace = true // Chỉ thay thế kẻ địch, không trừ máu
                } else {
                    val hitIdx = proj.indexOfFirst { p ->
                        val pRect = rectFromCenter(p.x, p.y, p.size)
                        intersects(eRect, pRect)
                    }
                    if (hitIdx >= 0) {
                        val p = proj[hitIdx]
                        proj.removeAt(hitIdx)
                        var damage = when (p.type) {
                            "missile" -> 2
                            "laser" -> 3
                            else -> 1
                        }
                        moved = moved.copy(health = moved.health - damage)
                        if (moved.health <= 0) {
                            manager.hitEffects = manager.hitEffects + HitEffect(x = moved.x, y = moved.y)
                            replace = true
                            manager.enemyKillCount += 1
                            if (manager.enemyKillCount >= 20) { // Kiểm tra điều kiện win
                                manager.isWin = true
                                manager.saveHighscore(manager.gold)
                            }
                            if (Random.nextBoolean()) {
                                nextPowerUps += PowerUp(x = moved.x, y = moved.y, type = "gold")
                            }
                        }
                    }

                    val playerRect = rectFromCenter(manager.playerX, manager.playerY, 0.5f)
                    if (intersects(eRect, playerRect)) {
                        replace = true
                        if (!manager.playerShield) {
                            manager.playerHealth -= 5
                            if (manager.playerHealth <= 0) {
                                manager.isGameOver = true // Kích hoạt game over
                            }// Chỉ trừ máu khi chạm player
                        } else {
                            manager.playerShield = false
                        }
                    }
                }

                if (replace) {
                    nextEnemies += spawnEnemyTop(cols, rows)
                } else {
                    nextEnemies += moved
                }
            }

            manager.powerUps = nextPowerUps
                .map { it.copy(y = it.y + it.vy) }
                .filter { pu ->
                    if (pu.y > rows) return@filter false
                    val puRect = rectFromCenter(pu.x, pu.y, 0.3f)
                    val playerRect = rectFromCenter(manager.playerX, manager.playerY, 0.5f)
                    if (intersects(puRect, playerRect)) {
                        when (pu.type) {
                            "health" -> manager.playerHealth += 20
                            "shield" -> manager.playerShield = true
                            "speed" -> manager.playerSpeedBoost = 1.5f
                            "weapon" -> manager.playerExtraWeapon = true
                            "single" -> manager.fireMode = "single"
                            "triple" -> manager.fireMode = "triple"
                            "laser" -> manager.fireMode = "laser"
                            "gold" -> manager.gold += 5
                        }
                        return@filter false
                    }
                    true
                }

            manager.traps = manager.traps
                .map { it.copy(duration = it.duration - 1) }
                .filter { it.duration > 0 }
            manager.walls = nextWalls.filter { it.health > 0 }

            manager.projectiles = proj
            manager.enemies = nextEnemies

            manager.hitEffects = manager.hitEffects
                .map { it.copy(age = it.age + 1) }
                .filter { it.age < it.maxAge }

            while (manager.enemies.size < 3) {
                manager.enemies = manager.enemies + spawnEnemyTop(cols, rows)
            }
        }
    }
}

private fun spawnEnemyTop(cols: Int, rows: Int): Enemy {
    val size = 1.0f
    val speedDown = Random.nextDouble(0.10, 0.18).toFloat()
    val x = Random.nextDouble(0.5, cols - 0.5).toFloat()
    val y = -size
    val hasShield = Random.nextBoolean()
    return Enemy(x = x, y = y, vx = 0f, vy = speedDown, size = size, shield = hasShield)
}
