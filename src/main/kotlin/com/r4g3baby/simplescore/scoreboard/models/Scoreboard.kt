package com.r4g3baby.simplescore.scoreboard.models

import org.bukkit.entity.Player

data class Scoreboard(
    val name: String,
    val titles: ScoreLine,
    val scores: Map<Int, ScoreLine>,
    val restricted: Boolean
) {
    fun canSee(player: Player): Boolean {
        return !restricted || player.hasPermission("simplescore.$name")
    }
}