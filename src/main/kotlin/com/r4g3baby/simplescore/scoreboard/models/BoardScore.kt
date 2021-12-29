package com.r4g3baby.simplescore.scoreboard.models

import org.bukkit.entity.Player

data class BoardScore(
    val score: Int,
    val frames: ScoreFrames,
    val conditions: List<Condition> = emptyList()
) {
    fun canSee(player: Player): Boolean {
        return conditions.isEmpty() || !conditions.any { !it.check(player) }
    }
}
