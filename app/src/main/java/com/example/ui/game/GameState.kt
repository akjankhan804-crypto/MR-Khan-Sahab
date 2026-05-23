package com.example.ui.game

import androidx.compose.ui.geometry.Offset

data class Snake(
    val id: String,
    val name: String,
    val isBot: Boolean,
    val segments: List<Offset>, // segments[0] is the head
    val angle: Float, // in radians
    val skinId: Int,
    val isDead: Boolean = false,
    val score: Int = 0,
    val shieldDurationMs: Long = 0,
    val magnetDurationMs: Long = 0,
    val multiplierDurationMs: Long = 0,
    val speedUpActive: Boolean = false
)

data class GamePellet(
    val id: Int,
    val position: Offset,
    val value: Int,
    val colorHex: Int,
    val isBonus: Boolean = false,
    val isSpecialRemains: Boolean = false // food resulting from dead snakes
)

data class GamePowerUp(
    val id: Int,
    val position: Offset,
    val type: String, // "SPEED", "MAGNET", "SHIELD", "MULTIPLIER"
    val durationMs: Long = 10000 // duration given when collected
)

enum class BattleState {
    IDLE,
    MATCHING,
    PLAYING,
    WON,
    LOST
}
