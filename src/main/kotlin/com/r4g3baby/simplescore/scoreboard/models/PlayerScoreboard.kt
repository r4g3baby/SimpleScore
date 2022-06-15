package com.r4g3baby.simplescore.scoreboard.models

import org.bukkit.entity.Player

data class PlayerScoreboard(
    val name: String,
    private val titles: List<PlayerLine>,
    private val scores: Map<Int, List<PlayerLine>>,
    private val conditions: List<Condition>
) : Conditional(conditions) {
    fun getTitle(player: Player): PlayerLine? {
        return titles.firstOrNull { it.canSee(player) }
    }

    fun getScores(player: Player): Map<Int, PlayerLine?> {
        return scores.mapValues { (_, lines) ->
            lines.firstOrNull { it.canSee(player) }
        }
    }

    fun tick() {
        titles.forEach { line -> line.tick() }
        scores.forEach { it.value.forEach { line -> line.tick() } }
    }
}