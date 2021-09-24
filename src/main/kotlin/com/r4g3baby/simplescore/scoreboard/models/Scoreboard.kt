package com.r4g3baby.simplescore.scoreboard.models

import org.bukkit.entity.Player

data class Scoreboard(
    val name: String,
    val titles: ScoreLine,
    val scores: Map<Int, ScoreLine>,
    val permission: String? = null
) {
    fun canSee(player: Player): Boolean {
        return permission.isNullOrBlank() || player.hasPermission("simplescore.$permission")
    }
}