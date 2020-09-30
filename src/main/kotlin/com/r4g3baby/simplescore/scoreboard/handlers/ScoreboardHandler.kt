package com.r4g3baby.simplescore.scoreboard.handlers

import org.bukkit.entity.Player

abstract class ScoreboardHandler() {
    abstract fun createScoreboard(player: Player)
    abstract fun removeScoreboard(player: Player)
    abstract fun updateScoreboard(title: String, scores: Map<Int, String>, player: Player)

    fun getPlayerIdentifier(player: Player): String {
        return "sb${player.uniqueId.toString().replace("-", "").substring(0..13)}"
    }
}