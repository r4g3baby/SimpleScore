package com.r4g3baby.simplescore.scoreboard.models.conditions

import com.r4g3baby.simplescore.scoreboard.models.Condition
import org.bukkit.entity.Player

data class HasPermission(
    val perm: String
) : Condition {
    override fun check(player: Player): Boolean {
        return player.hasPermission(perm)
    }
}
