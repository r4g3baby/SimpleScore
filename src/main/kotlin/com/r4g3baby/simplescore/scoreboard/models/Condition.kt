package com.r4g3baby.simplescore.scoreboard.models

import org.bukkit.entity.Player

interface Condition {
    fun check(player: Player): Boolean

    enum class Type {
        HAS_PERMISSION,
        EQUALS,
        CONTAINS,
        ENDS_WITH,
        STARTS_WITH;
    }
}