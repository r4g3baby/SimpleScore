package com.r4g3baby.simplescore.scoreboard.models

import org.bukkit.entity.Player

abstract class Condition {
    abstract val negate: Boolean

    fun check(player: Player) = if (negate) !can(player) else can(player)
    protected abstract fun can(player: Player): Boolean

    abstract fun negate(negate: Boolean): Condition

    enum class Type {
        HAS_PERMISSION,
        GREATER_THAN,
        LESS_THAN,
        EQUALS,
        CONTAINS,
        ENDS_WITH,
        STARTS_WITH;

        companion object {
            @JvmStatic
            fun fromValue(value: String): Type? {
                for (type in Type.values()) {
                    if (type.name.equals(value, ignoreCase = true)) {
                        return type
                    }
                }
                return null
            }
        }
    }
}