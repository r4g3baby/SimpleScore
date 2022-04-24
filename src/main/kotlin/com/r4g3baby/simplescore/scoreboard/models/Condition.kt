package com.r4g3baby.simplescore.scoreboard.models

import org.bukkit.entity.Player

interface Condition {
    fun check(player: Player): Boolean

    enum class Type {
        HAS_PERMISSION,
        GREATER_THAN,
        LESS_THAN,
        EQUALS,
        CONTAINS,
        ENDS_WITH,
        STARTS_WITH;
    }
}