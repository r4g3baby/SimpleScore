package com.r4g3baby.simplescore.scoreboard.models.conditions

import com.r4g3baby.simplescore.scoreboard.models.Condition
import org.bukkit.entity.Player

data class HasPermission(
    val permission: String,
    override val negate: Boolean = false
) : Condition() {
    override fun can(player: Player): Boolean {
        return player.hasPermission(permission)
    }

    override fun negate(negate: Boolean): Condition {
        return copy(negate = negate)
    }
}
