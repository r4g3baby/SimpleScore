package com.r4g3baby.simplescore.scoreboard.models

import org.bukkit.entity.Player

abstract class Conditional(
    private val conditions: List<Condition>
) {
    fun canSee(player: Player): Boolean {
        return conditions.isEmpty() || !conditions.any { !it.check(player) }
    }
}