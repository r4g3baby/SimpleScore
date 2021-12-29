package com.r4g3baby.simplescore.scoreboard.models

import org.bukkit.entity.Player

data class Scoreboard(
    val name: String,
    val titles: ScoreFrames,
    val scores: List<BoardScore>,
    val conditions: List<Condition> = emptyList()
) {
    fun canSee(player: Player): Boolean {
        return conditions.isEmpty() || !conditions.any { !it.check(player) }
    }
}